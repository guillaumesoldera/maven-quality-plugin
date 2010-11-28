package com.serli.maven.plugin.quality;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
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
public class AnalyzeDependenciesMojo extends AbstractMojo {

  /**
   * The Maven project to analyze.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

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
  private boolean ignoreDirect = false;

  /**
   * Output the xml for the dependencies analyze report.
   * 
   * @parameter expression="${outputXML}" default-value="true"
   */
  private boolean outputXML;

  /**
   * Structure which contains dependencies and line number where the dependency is declared in pom file.
   */
  private List<DependencyLocation> dependencyLocationList;

  public void execute() throws MojoExecutionException {

    // TODO regarder si les dépendances sont triées. (bonne pratique : groupée
    // par groupId ou par scope)

    File f = outputDirectory;

    if (!f.exists()) {
      f.mkdirs();
    }

    if ("pom".equals(project.getPackaging())) {
      getLog().info("Skipping pom project");
      return;
    }

    if (outputDirectory == null || !outputDirectory.exists()) {
      getLog().info("Skipping project with no build directory");
      return;
    }

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

  }

  // private methods --------------------------------------------------------

  private List<DependencyLocation> buildDependenciesLineStructure() throws XmlPullParserException, IOException {
    PomFileReader pomFileReader = new PomFileReader(getLog());
    File pom = new File("pom.xml");
    FileReader reader;

    reader = new FileReader(pom);
    List<DependencyLocation> buildDependencyLineStructure = pomFileReader.buildDependencyLineStructure(reader);
    getLog().info("taille de la structure : " + buildDependencyLineStructure.size());
    return buildDependencyLineStructure;
  }

  /**
   * Check if a dependency is declared many times in looking for in List<DependencyLocation> structure built before.
   * @see #buildDependenciesLineStructure()
   * @return Map contains dependency and lines where this dependency is declared many times.
   */
  private Map<Dependency, List<Integer>> checkUniqueDeclaration() {

    Map<Dependency, List<Integer>> multipleDefinitions = new HashMap<Dependency, List<Integer>>();
    for (DependencyLocation dependencyLocation : dependencyLocationList) {
      Dependency depDefined = dependencyLocation.getDependency();
      boolean alreadyCheck = false;
      
      // we looks if this dependency is not already checked and put in result map.
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
      analysis = analyzer.analyze(project);
    } catch (ProjectDependencyAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }

    Set<Artifact> usedDeclared = analysis.getUsedDeclaredArtifacts();
    Set<Artifact> usedUndeclared = analysis.getUsedUndeclaredArtifacts();
    Set<Artifact> unusedDeclared = analysis.getUnusedDeclaredArtifacts();

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
      getLog().info("No dependency problems found");
      return false;
    }

    Map<Dependency, List<Integer>> checkUniqueDeclaration = null;
    if (uniqueDeclaration) {
      checkUniqueDeclaration = checkUniqueDeclaration();
    }

    MismatchDepMgtModel mismatchDepMgtModel = new MismatchDepMgtModel();
    if (analyzeDepMgt) {
      mismatchDepMgtModel = checkDependencyManagement();
    }

    if (outputXML) {
      StringBuffer dependenciesResult = writeDependenciesResult(usedDeclared, unusedDeclared, usedUndeclared, mismatchDepMgtModel,
          checkUniqueDeclaration);
      writeFile(dependenciesResult.toString());
    } else {
      if (!usedDeclared.isEmpty()) {
        getLog().info("Used declared dependencies found:");
        logArtifacts(analysis.getUsedDeclaredArtifacts(), false);
      }
      if (!usedUndeclared.isEmpty()) {
        getLog().warn("Used undeclared dependencies found:");
        logArtifacts(usedUndeclared, true);
      }
      if (!unusedDeclared.isEmpty()) {
        getLog().warn("Unused declared dependencies found:");
        logArtifacts(unusedDeclared, true);
      }

    }

    boolean hasMismatch = (mismatchDepMgtModel != null && mismatchDepMgtModel.hasMismatched());
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
  private MismatchDepMgtModel checkDependencyManagement() throws MojoExecutionException {
    boolean foundError = false;
    MismatchDepMgtModel mismatchDepMgtModel = new MismatchDepMgtModel();
    Map mismatch = null;
    List exclusionErrors = null;
    getLog().info("Found Resolved Dependency / DependencyManagement mismatches:");

    List<Dependency> depMgtDependencies = null;

    DependencyManagement depMgt = project.getDependencyManagement();
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
      Set<Artifact> allDependencyArtifacts = new HashSet<Artifact>(project.getArtifacts());

      // don't warn if a dependency that is directly listed overrides
      // depMgt. That's ok.
      if (this.ignoreDirect) {
        getLog().info("\tIgnoring Direct Dependencies.");
        Set directDependencies = project.getDependencyArtifacts();
        allDependencyArtifacts.removeAll(directDependencies);
      }

      // log exclusion errors
      exclusionErrors = getExclusionErrors(exclusions, allDependencyArtifacts);
      if (exclusionErrors != null && exclusionErrors.size() > 0) {
        foundError = true;
      }
      // while (exclusionIter.hasNext()) {
      // Artifact exclusion = (Artifact) exclusionIter.next();
      // getLog().info(
      // StringUtils.stripEnd(getArtifactManagementKey(exclusion), ":") +
      // " was excluded in DepMgt, but version "
      // + exclusion.getVersion() + " has been found in the dependency tree.");
      // }

      // find and log version mismatches
      mismatch = getMismatch(depMgtMap, allDependencyArtifacts);
      // Iterator mismatchIter = mismatch.keySet().iterator();
      // while ( mismatchIter.hasNext() )
      // {
      // Artifact resolvedArtifact = (Artifact) mismatchIter.next();
      // Dependency depMgtDependency = (Dependency) mismatch.get(
      // resolvedArtifact );
      // logMismatch( resolvedArtifact, depMgtDependency );
      // }
      if (!foundError) {
        getLog().info("   None");
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
      writeFile("   None");
    } else {
      for (Iterator<Artifact> iterator = artifacts.iterator(); iterator.hasNext();) {
        Artifact artifact = iterator.next();

        // called because artifact will set the version to -SNAPSHOT only if I
        // do this. MNG-2961
        artifact.isSnapshot();

        writeFile("   " + artifact);

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
  public List<Artifact> getExclusionErrors(Map exclusions, Set<Artifact> allDependencyArtifacts) {
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
  public Map<Artifact, Dependency> getMismatch(Map depMgtMap, Set<Artifact> allDependencyArtifacts) {
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

    if (mismatchDepMgtModel != null && mismatchDepMgtModel.hasMismatched()) {
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

  private PrettyPrintXMLWriter writeMismatch(Map mismatch, PrettyPrintXMLWriter writer) {
    if (mismatch != null) {
      writer.startElement("overridenVersions");
      Iterator<Artifact> mismatchIter = mismatch.keySet().iterator();
      while (mismatchIter.hasNext()) {
        Artifact resolvedArtifact = (Artifact) mismatchIter.next();
        Dependency depMgtDependency = (Dependency) mismatch.get(resolvedArtifact);
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
    logMismatch(resolvedArtifact, depMgtDependency);
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

  /**
   * Write a string in outputFile. If outputFile is null, write in log.
   * 
   * @param pString
   *          String to write.
   * @throws IOException
   */
  private void writeFile(String pString) throws IOException {
    if (outputFile != null) {
      FileWriter fw = new FileWriter(outputFile, true);
      BufferedWriter output = new BufferedWriter(fw);
      output.write(pString);
      output.flush();
      output.close();
    } else {
      getLog().warn(pString);
    }
  }

}
