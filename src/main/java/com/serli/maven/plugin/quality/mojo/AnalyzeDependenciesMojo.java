package com.serli.maven.plugin.quality.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalysis;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzer;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.serli.maven.plugin.quality.model.DependencyLocation;
import com.serli.maven.plugin.quality.model.MismatchDepMgtModel;
import com.serli.maven.plugin.quality.parser.PomFileReader;
import com.serli.maven.plugin.quality.util.Util;

/**
 * Goal which analyzes pom dependencies.
 * 
 * @goal analyze-dependencies
 * @requiresDependencyResolution test
 * @execute phase="test-compile"
 */
public class AnalyzeDependenciesMojo extends AbstractMavenQualityMojo {

  /**
   * The Maven project dependency analyzer to use.
   * 
   * @component
   * @required
   * @readonly
   */
  private ProjectDependencyAnalyzer analyzer;

  /**
   * Location of the file.
   * 
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File outputDirectory;

  /**
   * Output file.
   * 
   * @parameter expression="${outputFile}"
   *            default-value="target/reports/dependencies-analysis.xml"
   */
  private File outputFile;

  /**
   * Whether to fail the build if a dependency warning is found.
   * 
   * @parameter expression="${failOnWarning}" default-value="false"
   */
  private boolean failOnWarning;

  /**
   * Ignore Runtime,Provide,Test,System scopes for unused dependency analysis
   * 
   * @parameter expression="${ignoreNonCompile}" default-value="false"
   */
  private boolean ignoreNonCompile;

  /**
   * Check for mismatches in your dependencyManagement section.
   * 
   * @parameter expression="${analyzeDepMgt}" default-value="true"
   */
  private boolean analyzeDepMgt;

  /**
   * Check for unique dependency declaration.
   * 
   * @parameter expression="${uniqueDeclaration}" default-value="true"
   */
  private boolean uniqueDeclaration;

  /**
   * Ignore Direct Dependency Overrides of dependencyManagement section.
   * 
   * @parameter expression="${ignoreDirect}" default-value="false"
   */
  private boolean ignoreDirect;

  /**
   * Output the xml for the dependencies analyze report in log console.
   * 
   * @parameter expression="${outputXML}" default-value="false"
   */
  private boolean outputXML;

  /**
   * Print results in log console.
   * 
   * @parameter expression="${logConsole}" default-value="true"
   */
  private boolean logConsole;

  /**
   * Structure which contains dependencies and line number where the dependency
   * is declared in pom file.
   */
  private List<DependencyLocation> dependencyLocationList;

  private StringBuffer dependenciesResult;

  private Set<Artifact> usedDeclared;

  private Set<Artifact> usedUndeclared;

  private Set<Artifact> unusedDeclared;

  private Map<Dependency, List<Integer>> checkUniqueDeclaration;

  private MismatchDepMgtModel mismatchDepMgtModel;

  public void execution() throws MojoExecutionException {

    if ("pom".equals(getProject().getPackaging())) {
      getLog().info("Skipping pom project");
      return;
    }

    if (outputDirectory == null || !outputDirectory.exists()) {
      getLog().info("Skipping project with no build directory");
      return;
    }

    // TODO regarder si les dépendances sont triées. (bonne pratique : groupée
    // par groupId ou par scope)

    File f = outputDirectory;

    if (!f.exists()) {
      f.mkdirs();
    }

    Util.buildOutputFile(outputFile);

    boolean warningDep;
    try {
      // build structure which associates dependency and line number in pom
      // file.
      dependencyLocationList = buildDependenciesLineStructure();

      warningDep = checkDependencies();

      if (warningDep && failOnWarning) {
        throw new MojoExecutionException("Dependency problems found");
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Analyze problem", e);
    } catch (XmlPullParserException e) {
      throw new MojoExecutionException("Analyze problem", e);
    }

    getLog().info("results are available in " + outputFile.getPath());
  }

  

  // private methods --------------------------------------------------------

  private List<DependencyLocation> buildDependenciesLineStructure() throws XmlPullParserException, IOException {
    PomFileReader pomFileReader = new PomFileReader(getLog());
    File pom = new File("pom.xml");
    FileReader reader;

    reader = new FileReader(pom);
    List<DependencyLocation> buildDependencyLineStructure = pomFileReader.buildDependencyLineStructure(reader);
    return buildDependencyLineStructure;
  }

  /**
   * Check if a dependency is declared many times in looking for in
   * List<DependencyLocation> structure built before.
   * 
   * @see #buildDependenciesLineStructure()
   * @return Map contains dependency and lines where this dependency is declared
   *         many times.
   */
  private Map<Dependency, List<Integer>> checkUniqueDeclaration() {

    Map<Dependency, List<Integer>> multipleDefinitions = new HashMap<Dependency, List<Integer>>();
    for (DependencyLocation dependencyLocation : dependencyLocationList) {
      Dependency depDefined = dependencyLocation.getDependency();
      boolean alreadyCheck = false;

      // we looks if this dependency is not already checked and put in result
      // map.
      for (Dependency dep : multipleDefinitions.keySet()) {
        if (dep.getGroupId().equals(depDefined.getGroupId()) && dep.getArtifactId().equals(depDefined.getArtifactId())) {
          alreadyCheck = true;
          break;
        }
      }

      if (!alreadyCheck) {
        List<Integer> definitionsLine = Util.getDefinitionsLine(dependencyLocationList, dependencyLocation.getDependency());
        if (definitionsLine.size() > 1) {
          multipleDefinitions.put(dependencyLocation.getDependency(), definitionsLine);
        }
      }
    }

    return multipleDefinitions;
  }

  @SuppressWarnings("unchecked")
  private boolean checkDependencies() throws MojoExecutionException, IOException {
    ProjectDependencyAnalysis analysis;
    try {
      analysis = analyzer.analyze(getProject());
    } catch (ProjectDependencyAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }

    usedDeclared = analysis.getUsedDeclaredArtifacts();
    usedUndeclared = analysis.getUsedUndeclaredArtifacts();
    unusedDeclared = analysis.getUnusedDeclaredArtifacts();

    // TODO voir si on garde tous les scopes, même runtime....
    if (ignoreNonCompile) {
      Set<Artifact> filteredUnusedDeclared = new HashSet<Artifact>(unusedDeclared);
      Iterator<Artifact> iter = filteredUnusedDeclared.iterator();
      while (iter.hasNext()) {
        Artifact artifact = iter.next();
        if (!artifact.getScope().equals(Artifact.SCOPE_COMPILE)) {
          iter.remove();
        }
      }
      unusedDeclared = filteredUnusedDeclared;
    }

    if ((usedDeclared.isEmpty()) && usedUndeclared.isEmpty() && unusedDeclared.isEmpty()) {
      getLog().info("No dependency found");
      return false;
    }

    checkUniqueDeclaration = null;
    if (uniqueDeclaration) {
      checkUniqueDeclaration = checkUniqueDeclaration();
    }

    mismatchDepMgtModel = new MismatchDepMgtModel();
    if (analyzeDepMgt) {
      mismatchDepMgtModel = checkDependencyManagement();
    }

    dependenciesResult = writeDependenciesResult(usedDeclared, unusedDeclared, usedUndeclared, mismatchDepMgtModel, checkUniqueDeclaration);
    Util.writeFile(dependenciesResult.toString(), outputFile, getLog());
    if (logConsole) {
      if (outputXML) {
        getLog().info(dependenciesResult.toString());
      } else {
        if (!usedDeclared.isEmpty()) {
          getLog().info("Used declared dependencies found:");
          logArtifacts(analysis.getUsedDeclaredArtifacts(), false);
        }
        if (!usedUndeclared.isEmpty()) {
          getLog().info("Used undeclared dependencies found:");
          logArtifacts(usedUndeclared, true);
        }
        if (!unusedDeclared.isEmpty()) {
          getLog().info("Unused declared dependencies found:");
          logArtifacts(unusedDeclared, true);
        }
        if (checkUniqueDeclaration != null && !checkUniqueDeclaration.isEmpty()) {
          getLog().info("multiple declaration found");
          logMultipleDeclaration(checkUniqueDeclaration);
        }

        // TODO afficher aussi les exclusion error, multiple declaration et
        // overriden version
      }
    }

    boolean hasMismatch = (mismatchDepMgtModel != null && (mismatchDepMgtModel.hasMismatches() || mismatchDepMgtModel.hasExclusionErrors()));
    boolean multipleDeclaration = checkUniqueDeclaration != null && !checkUniqueDeclaration.isEmpty();

    boolean result = !usedUndeclared.isEmpty() || !unusedDeclared.isEmpty();
    if (analyzeDepMgt) {
      result = result || hasMismatch;
    }
    if (uniqueDeclaration) {
      result = result || multipleDeclaration;
    }
    return result;
  }

  /**
   * Does the work of checking the DependencyManagement Section.
   * 
   * @return true if errors are found.
   * @throws MojoExecutionException
   */
  @SuppressWarnings("unchecked")
  private MismatchDepMgtModel checkDependencyManagement() throws MojoExecutionException {
    MismatchDepMgtModel mismatchDepMgtModel = new MismatchDepMgtModel();
    Map<Artifact, Dependency> mismatch = null;
    List<Artifact> exclusionErrors = null;
    getLog().info("Found Resolved Dependency / DependencyManagement mismatches:");

    List<Dependency> depMgtDependencies = null;

    DependencyManagement depMgt = getProject().getDependencyManagement();
    if (depMgt != null) {
      depMgtDependencies = depMgt.getDependencies();
    }

    if (depMgtDependencies != null && !depMgtDependencies.isEmpty()) {
      // put all the dependencies from depMgt into a map for quick lookup
      Map<String, Dependency> depMgtMap = new HashMap<String, Dependency>();
      Map<String, Exclusion> exclusions = new HashMap<String, Exclusion>();
      Iterator<Dependency> iter = depMgtDependencies.iterator();
      while (iter.hasNext()) {
        Dependency depMgtDependency = iter.next();
        depMgtMap.put(depMgtDependency.getManagementKey(), depMgtDependency);

        // now put all the exclusions into a map for quick lookup
        exclusions.putAll(addExclusions(depMgtDependency.getExclusions()));
      }

      // get dependencies for the project (including transitive)
      Set<Artifact> allDependencyArtifacts = new HashSet<Artifact>(getProject().getArtifacts());

      // don't warn if a dependency that is directly listed overrides
      // depMgt. That's ok.
      if (this.ignoreDirect) {
        getLog().info("\tIgnoring Direct Dependencies.");
        Set<Artifact> directDependencies = getProject().getDependencyArtifacts();
        allDependencyArtifacts.removeAll(directDependencies);
      }

      // log exclusion errors
      exclusionErrors = getExclusionErrors(exclusions, allDependencyArtifacts);

      if (logConsole && !outputXML) {
        for (Artifact exclusion : exclusionErrors) {
          getLog().info(
              StringUtils.stripEnd(getArtifactManagementKey(exclusion), ":") + " was excluded in DepMgt, but version "
                  + exclusion.getVersion() + " has been found in the dependency tree.");
        }
      }

      // find and log version mismatches
      mismatch = getMismatch(depMgtMap, allDependencyArtifacts);

      if (logConsole && !outputXML) {
        Iterator<Artifact> mismatchIter = mismatch.keySet().iterator();
        while (mismatchIter.hasNext()) {
          Artifact resolvedArtifact = mismatchIter.next();
          Dependency depMgtDependency = mismatch.get(resolvedArtifact);
          logMismatch(resolvedArtifact, depMgtDependency);
        }
      }
    } else {
      getLog().info("   Nothing in DepMgt.");
    }

    mismatchDepMgtModel.setMismatch(mismatch);
    mismatchDepMgtModel.setExclusionErrors(exclusionErrors);

    return mismatchDepMgtModel;
  }

  private void logArtifacts(Set<Artifact> artifacts, boolean warn) throws IOException {
    if (artifacts.isEmpty()) {
      getLog().info("   None");
    } else {
      for (Iterator<Artifact> iterator = artifacts.iterator(); iterator.hasNext();) {
        Artifact artifact = iterator.next();

        // called because artifact will set the version to -SNAPSHOT only if I
        // do this. MNG-2961
        artifact.isSnapshot();

        getLog().info("   " + artifact);

      }
    }
  }

  private void logMultipleDeclaration(Map<Dependency, List<Integer>> multipleDeclaration) {
    Iterator<Dependency> iterator = multipleDeclaration.keySet().iterator();
    while (iterator.hasNext()) {
      Dependency dependency = iterator.next();
      getLog().info("\tDependency : " + StringUtils.stripEnd(dependency.getManagementKey(), ":"));
      for (Integer line : multipleDeclaration.get(dependency)) {
        getLog().info("\t\t\tLine declaration : " + line);
      }
    }
  }

  /**
   * Returns a map of the exclusions using the Dependency ManagementKey as the
   * keyset.
   * 
   * @param exclusionList
   *          to be added to the map.
   * @return a map of the exclusions using the Dependency ManagementKey as the
   *         keyset.
   */
  public Map<String, Exclusion> addExclusions(List<Exclusion> exclusionList) {
    Map<String, Exclusion> exclusions = new HashMap<String, Exclusion>();
    if (exclusionList != null) {
      Iterator<Exclusion> exclusionIter = exclusionList.iterator();
      while (exclusionIter.hasNext()) {
        Exclusion exclusion = exclusionIter.next();
        exclusions.put(getExclusionKey(exclusion), exclusion);
      }
    }
    return exclusions;
  }

  /**
   * Returns a List of the artifacts that should have been excluded, but were
   * found in the dependency tree.
   * 
   * @param exclusions
   *          a map of the DependencyManagement exclusions, with the
   *          ManagementKey as the key and Dependency as the value.
   * @param allDependencyArtifacts
   *          resolved artifacts to be compared.
   * @return list of artifacts that should have been excluded.
   */
  public List<Artifact> getExclusionErrors(Map<String, Exclusion> exclusions, Set<Artifact> allDependencyArtifacts) {
    List<Artifact> list = new ArrayList<Artifact>();

    Iterator<Artifact> iter = allDependencyArtifacts.iterator();
    while (iter.hasNext()) {
      Artifact artifact = (Artifact) iter.next();
      if (exclusions.containsKey(getExclusionKey(artifact))) {
        list.add(artifact);
      }
    }

    return list;
  }

  public String getExclusionKey(Artifact artifact) {
    return artifact.getGroupId() + ":" + artifact.getArtifactId();
  }

  public String getExclusionKey(Exclusion ex) {
    return ex.getGroupId() + ":" + ex.getArtifactId();
  }

  /**
   * Calculate the mismatches between the DependencyManagement and resolved
   * artifacts
   * 
   * @param depMgtMap
   *          contains the Dependency.GetManagementKey as the keyset for quick
   *          lookup.
   * @param allDependencyArtifacts
   *          contains the set of all artifacts to compare.
   * @return a map containing the resolved artifact as the key and the listed
   *         dependency as the value.
   */
  public Map<Artifact, Dependency> getMismatch(Map<String, Dependency> depMgtMap, Set<Artifact> allDependencyArtifacts) {
    Map<Artifact, Dependency> mismatchMap = new HashMap<Artifact, Dependency>();

    Iterator<Artifact> iter = allDependencyArtifacts.iterator();
    while (iter.hasNext()) {
      Artifact dependencyArtifact = (Artifact) iter.next();
      Dependency depFromDepMgt = (Dependency) depMgtMap.get(getArtifactManagementKey(dependencyArtifact));
      if (depFromDepMgt != null) {

        // workaround for MNG-2961
        dependencyArtifact.isSnapshot();

        if (!depFromDepMgt.getVersion().equals(dependencyArtifact.getBaseVersion())) {
          mismatchMap.put(dependencyArtifact, depFromDepMgt);
        }
      }
    }
    return mismatchMap;
  }

  /**
   * This function displays the log to the screen showing the versions and
   * information about the artifacts that don't match.
   * 
   * @param dependencyArtifact
   *          the artifact that was resolved.
   * @param dependencyFromDepMgt
   *          the dependency listed in the DependencyManagement section.
   * 
   */
  public void logMismatch(Artifact dependencyArtifact, Dependency dependencyFromDepMgt) {
    getLog().info("\tDependency: " + StringUtils.stripEnd(dependencyFromDepMgt.getManagementKey(), ":"));
    getLog().info("\t\tDepMgt  : " + dependencyFromDepMgt.getVersion());
    getLog().info("\t\tResolved: " + dependencyArtifact.getBaseVersion());
  }

  /**
   * This function returns a string comparable with Dependency.GetManagementKey.
   * 
   * @param artifact
   *          to gen the key for
   * @return a string in the form: groupId:ArtifactId:Type[:Classifier]
   */
  public String getArtifactManagementKey(Artifact artifact) {
    return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType()
        + ((artifact.getClassifier() != null) ? ":" + artifact.getClassifier() : "");
  }

  private StringBuffer writeDependenciesResult(Set<Artifact> usedDeclared, Set<Artifact> unusedDeclared, Set<Artifact> usedUndeclared,
      MismatchDepMgtModel mismatchDepMgtModel, Map<Dependency, List<Integer>> checkUniqueDeclaration) {
    StringWriter out = new StringWriter();
    PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(out);
    writer.startElement("dependencies");
    writer.startElement("usedDeclared");
    writer = writeDependencyXML(usedDeclared, writer, true);
    writer.endElement();
    writer.startElement("unusedDeclared");
    writer = writeDependencyXML(unusedDeclared, writer, true);
    writer.endElement();
    writer.startElement("usedUndeclared");
    writer = writeDependencyXML(usedUndeclared, writer, false);
    writer.endElement();

    if (mismatchDepMgtModel != null && mismatchDepMgtModel.hasMismatches()) {
      writer = writeMismatch(mismatchDepMgtModel.getMismatch(), writer);
      writer = writeExclusionErrors(mismatchDepMgtModel.getExclusionErrors(), writer);
    }

    if (checkUniqueDeclaration != null && !checkUniqueDeclaration.isEmpty()) {
      writer = writeMultipleDeclaration(checkUniqueDeclaration, writer);
    }

    writer.endElement();
    return out.getBuffer();
  }

  private PrettyPrintXMLWriter writeMultipleDeclaration(Map<Dependency, List<Integer>> checkUniqueDeclaration, PrettyPrintXMLWriter writer) {
    if (checkUniqueDeclaration != null) {
      writer.startElement("multipleDeclaration");
      for (Dependency dep : checkUniqueDeclaration.keySet()) {
        writer = writeMultipleDeclaration(dep, checkUniqueDeclaration.get(dep), writer);
      }
      writer.endElement();
    }

    return writer;
  }

  private PrettyPrintXMLWriter writeMultipleDeclaration(Dependency dep, List<Integer> list, PrettyPrintXMLWriter writer) {
    writer.startElement("dependency");
    writer.startElement("groupId");
    writer.writeText(dep.getGroupId());
    writer.endElement();
    writer.startElement("artifactId");
    writer.writeText(dep.getArtifactId());
    writer.endElement();
    writer.startElement("declarations");
    for (Integer i : list) {
      writer.startElement("declarationLine");
      writer.writeText(i.toString());
      writer.endElement();
    }
    writer.endElement();
    writer.endElement();

    return writer;
  }

  private PrettyPrintXMLWriter writeExclusionErrors(List<Artifact> exclusionErrors, PrettyPrintXMLWriter writer) {
    if (exclusionErrors != null) {
      writer.startElement("exclusionErrors");
      Iterator<Artifact> exclusionIter = exclusionErrors.iterator();
      while (exclusionIter.hasNext()) {
        Artifact exclusion = (Artifact) exclusionIter.next();
        writer = writeExclusionError(writer, exclusion);
        getLog().info(
            StringUtils.stripEnd(getArtifactManagementKey(exclusion), ":") + " was excluded in DepMgt, but version "
                + exclusion.getVersion() + " has been found in the dependency tree.");

      }
      writer.endElement();
    }

    return writer;
  }

  private PrettyPrintXMLWriter writeExclusionError(PrettyPrintXMLWriter writer, Artifact exclusion) {
    if (exclusion != null) {
      writer.startElement("dependency");
      writer.startElement("groupId");
      writer.writeText(exclusion.getGroupId());
      writer.endElement();
      writer.startElement("artifactId");
      writer.writeText(exclusion.getArtifactId());
      writer.endElement();
      writer.startElement("versionFound");
      writer.writeText(exclusion.getVersion());
      writer.endElement();
      writer.endElement();
    }
    return writer;
  }

  private PrettyPrintXMLWriter writeMismatch(Map<Artifact, Dependency> mismatch, PrettyPrintXMLWriter writer) {
    if (mismatch != null) {
      writer.startElement("overridenVersions");
      Iterator<Artifact> mismatchIter = mismatch.keySet().iterator();
      while (mismatchIter.hasNext()) {
        Artifact resolvedArtifact = mismatchIter.next();
        Dependency depMgtDependency = mismatch.get(resolvedArtifact);
        writer = writeMismatch(resolvedArtifact, depMgtDependency, writer, true);
      }
      writer.endElement();
    }
    return writer;
  }

  private PrettyPrintXMLWriter writeMismatch(Artifact resolvedArtifact, Dependency depMgtDependency, PrettyPrintXMLWriter writer,
      boolean printLineNumber) {
    if (resolvedArtifact == null || depMgtDependency == null) {
      return writer;
    }
    writer.startElement("dependency");
    writer.startElement("groupId");
    writer.writeText(depMgtDependency.getGroupId());
    writer.endElement();
    writer.startElement("artifactId");
    writer.writeText(depMgtDependency.getArtifactId());
    writer.endElement();
    writer.startElement("versionDepMgt");
    writer.writeText(depMgtDependency.getVersion());
    writer.endElement();
    writer.startElement("versionResolved");
    writer.writeText(resolvedArtifact.getBaseVersion());
    writer.endElement();

    if (printLineNumber) {
      if (printLineNumber) {
        writer.startElement("lineNumber");
        int lastDefinitionLine = Util.getLastDefinitionLine(dependencyLocationList, resolvedArtifact.getGroupId(),
            resolvedArtifact.getArtifactId());
        writer.writeText(lastDefinitionLine + "");
        writer.endElement();
      }
    }

    writer.endElement();
    return writer;
  }

  private PrettyPrintXMLWriter writeDependencyXML(Set<Artifact> artifacts, PrettyPrintXMLWriter writer, boolean printLineNumber) {
    if (!artifacts.isEmpty()) {

      Iterator<Artifact> iter = artifacts.iterator();
      while (iter.hasNext()) {
        Artifact artifact = (Artifact) iter.next();

        // called because artifact will set the version to -SNAPSHOT only if I
        // do this. MNG-2961
        artifact.isSnapshot();

        writer.startElement("dependency");
        writer.startElement("groupId");
        writer.writeText(artifact.getGroupId());
        writer.endElement();
        writer.startElement("artifactId");
        writer.writeText(artifact.getArtifactId());
        writer.endElement();
        writer.startElement("version");
        writer.writeText(artifact.getBaseVersion());
        writer.endElement();

        if (!Artifact.SCOPE_COMPILE.equals(artifact.getScope())) {
          writer.startElement("scope");
          writer.writeText(artifact.getScope());
          writer.endElement();
        }
        if (printLineNumber) {
          writer.startElement("lineNumber");
          int lastDefinitionLine = Util.getLastDefinitionLine(dependencyLocationList, artifact.getGroupId(), artifact.getArtifactId());
          writer.writeText(lastDefinitionLine + "");
          writer.endElement();
        }
        writer.endElement();
      }

    }
    return writer;
  }

  @Override
  protected void executeReport(Locale locale) throws MavenReportException {
    try {
      outputReportDirectory.mkdirs();
      execution();

      Sink sink = getSink();
      sink.head();
      sink.title();
      sink.text(getOutputName());
      sink.title_();
      sink.head_();

      sink.body();
      sink.paragraph();
      sink.text(getDescription(locale));
      sink.paragraph_();
      sink.text(getI18nString(locale, "intro"));
      sink.list();
      sink.listItem();
      sink.link("useddeclared");
      sink.text(getI18nString(locale, "useddeclared.description"));
      sink.link_();
      sink.listItem_();
      sink.listItem();
      sink.link("usedundeclared");
      sink.text(getI18nString(locale, "usedundeclared.description"));
      sink.link_();
      sink.listItem_();
      sink.listItem();
      if (ignoreNonCompile) {
        sink.link("unuseddeclared.ignoreall");
      } else {
        sink.link("unuseddeclared");
      }
      sink.text(getI18nString(locale, "unuseddeclared.description"));
      sink.link_();
      sink.listItem_();
      sink.listItem();
      sink.link("overriden");
      sink.text(getI18nString(locale, "overriden.description"));
      sink.link_();
      sink.listItem_();
      sink.listItem();
      sink.link("multiple");
      sink.text(getI18nString(locale, "multiple.description"));
      sink.link_();
      sink.listItem_();
      sink.listItem();
      sink.link("exclusions");
      sink.text(getI18nString(locale, "exclusions.description"));
      sink.link_();
      sink.listItem_();
      sink.list_();

      writeSection(sink, usedDeclared, "useddeclared", locale, true);
      writeSection(sink, usedUndeclared, "usedundeclared", locale, false);
      if (ignoreNonCompile) {
        writeSection(sink, unusedDeclared, "unuseddeclared.ignoreall", locale, true);
      } else {
        writeSection(sink, unusedDeclared, "unuseddeclared", locale, true);
      }

      writeOverridenVersion(sink, locale);

      writeMultipleDeclaration(sink, locale);

      writeExclusionError(sink, locale);

      sink.body_();
      sink.flush();
      sink.close();

    } catch (MojoExecutionException e) {
      e.printStackTrace();
      throw new MavenReportException("MavenReportException", e);
    }

  }

  private void writeSection(Sink sink, Set<Artifact> artifacts, String key, Locale locale, boolean lineNumber) {
    writeBegin(sink, key, locale);
    if (artifacts != null && !artifacts.isEmpty()) {
      sink.table();
      writeHeaderCell(sink, getI18nString(locale, "groupid"));
      writeHeaderCell(sink, getI18nString(locale, "artifactid"));
      writeHeaderCell(sink, getI18nString(locale, "version"));
      writeHeaderCell(sink, getI18nString(locale, "scope"));
      if (lineNumber) {
        writeHeaderCell(sink, getI18nString(locale, "linenumber"));
      }
      for (Artifact artifact : artifacts) {
        sink.tableRow();
        writeCell(sink, artifact.getGroupId());
        writeCell(sink, artifact.getArtifactId());
        writeCell(sink, artifact.getBaseVersion());
        writeCell(sink, artifact.getScope());
        if (lineNumber) {
          writeCell(sink, "" + Util.getLastDefinitionLine(dependencyLocationList, artifact.getGroupId(), artifact.getArtifactId()));
        }
        sink.tableRow_();
      }

      sink.table_();
    } else {
      sink.text(getI18nString(locale, "noentry"));
    }

    writeEnd(sink);
  }

  private void writeMultipleDeclaration(Sink sink, Locale locale) {
    writeBegin(sink, "multiple", locale);
    if (checkUniqueDeclaration != null && !checkUniqueDeclaration.isEmpty()) {
      sink.table();
      writeHeaderCell(sink, getI18nString(locale, "artifact"));
      writeHeaderCell(sink, getI18nString(locale, "linesdeclaration"));
      for (Dependency dep : checkUniqueDeclaration.keySet()) {
        sink.tableRow();
        StringBuffer buffer = new StringBuffer("<groupId>");
        buffer = buffer.append(dep.getGroupId());
        buffer = buffer.append("</groupId>\n");
        sink.tableCell();
        sink.text(buffer.toString());
        sink.lineBreak();
        buffer = new StringBuffer("<artifactId>");
        buffer = buffer.append(dep.getArtifactId());
        buffer = buffer.append("</artifactId>");
        sink.text(buffer.toString());
        sink.tableCell_();
        sink.tableCell();
        sink.table();
        List<Integer> list = checkUniqueDeclaration.get(dep);
        for (Integer line : list) {
          sink.tableRow();
          writeCell(sink, line.toString());
          sink.tableRow_();
        }
        sink.table_();
        sink.tableCell_();
        sink.tableRow_();

      }
      sink.table_();
    } else {
      sink.text(getI18nString(locale, "noentry"));
    }
    writeEnd(sink);
  }

  private void writeExclusionError(Sink sink, Locale locale) {
    writeBegin(sink, "exclusions", locale);
    if (mismatchDepMgtModel != null && mismatchDepMgtModel.hasExclusionErrors()) {
      List<Artifact> exclusionErrors = mismatchDepMgtModel.getExclusionErrors();
      sink.table();
      writeHeaderCell(sink, getI18nString(locale, "groupid"));
      writeHeaderCell(sink, getI18nString(locale, "artifactid"));
      writeHeaderCell(sink, getI18nString(locale, "versionFound"));
      for (Artifact exclusion : exclusionErrors) {
        sink.tableRow();
        writeCell(sink, exclusion.getGroupId());
        writeCell(sink, exclusion.getArtifactId());
        writeCell(sink, exclusion.getVersion());
        sink.tableRow_();
      }
      sink.table_();
    } else {
      sink.text(getI18nString(locale, "noentry"));
    }
    writeEnd(sink);
  }

  private void writeOverridenVersion(Sink sink, Locale locale) {
    writeBegin(sink, "overriden", locale);
    if (ignoreDirect) {
      sink.paragraph();
      sink.text(getI18nString(locale, "overriden.ignoredirect"));
      sink.paragraph_();
    }
    if (mismatchDepMgtModel != null && mismatchDepMgtModel.hasMismatches()) {
      sink.table();
      writeHeaderCell(sink, getI18nString(locale, "groupid"));
      writeHeaderCell(sink, getI18nString(locale, "artifactid"));
      writeHeaderCell(sink, getI18nString(locale, "versionDepMgt"));
      writeHeaderCell(sink, getI18nString(locale, "versionResolved"));
      writeHeaderCell(sink, getI18nString(locale, "linenumber"));
      Map<Artifact, Dependency> mismatch = mismatchDepMgtModel.getMismatch();
      Iterator<Artifact> mismatchIter = mismatch.keySet().iterator();
      while (mismatchIter.hasNext()) {
        Artifact resolvedArtifact = mismatchIter.next();
        Dependency depMgtDependency = mismatch.get(resolvedArtifact);
        if (resolvedArtifact != null && depMgtDependency != null) {
          sink.tableRow();
          writeCell(sink, depMgtDependency.getGroupId());
          writeCell(sink, depMgtDependency.getArtifactId());
          writeCell(sink, depMgtDependency.getVersion());
          writeCell(sink, resolvedArtifact.getBaseVersion());
          writeCell(sink,
              "" + Util.getLastDefinitionLine(dependencyLocationList, resolvedArtifact.getGroupId(), resolvedArtifact.getArtifactId()));
          sink.tableRow_();
        }
      }

      sink.table_();
    } else {
      sink.text(getI18nString(locale, "noentry"));
    }
    writeEnd(sink);
  }

  
  /** {@inheritDoc} */
  public String getOutputName() {
    return "dependencies-analysis";
  }

  @Override
  protected String getI18Nsection() {
    return "dependencies";
  }

}
