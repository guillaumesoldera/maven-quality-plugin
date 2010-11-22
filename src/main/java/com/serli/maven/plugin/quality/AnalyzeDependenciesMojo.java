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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalysis;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzer;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal touch
 * 
 * @phase process-sources
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
   * Output the xml for the dependencies analyze report.
   * 
   * @parameter expression="${outputXML}" default-value="true"
   */
  private boolean outputXML;

  public void execute() throws MojoExecutionException {
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

    boolean warning;
    try {
      warning = checkDependencies();
      if (warning && failOnWarning) {
        throw new MojoExecutionException("Dependency problems found");
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Analyze problem", e);
    }

  }

  // private methods --------------------------------------------------------

  private boolean checkDependencies() throws MojoExecutionException, IOException {
    ProjectDependencyAnalysis analysis;
    try {
      analysis = analyzer.analyze(project);
    } catch (ProjectDependencyAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }

    Set usedDeclared = analysis.getUsedDeclaredArtifacts();
    Set usedUndeclared = analysis.getUsedUndeclaredArtifacts();
    Set unusedDeclared = analysis.getUnusedDeclaredArtifacts();

    if (ignoreNonCompile) {
      Set filteredUnusedDeclared = new HashSet(unusedDeclared);
      Iterator iter = filteredUnusedDeclared.iterator();
      while (iter.hasNext()) {
        Artifact artifact = (Artifact) iter.next();
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

    if (outputXML) {
      writeDependenciesResult(usedDeclared, unusedDeclared, usedUndeclared);
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

    return !usedUndeclared.isEmpty() || !unusedDeclared.isEmpty();
  }

  private void logArtifacts(Set artifacts, boolean warn) throws IOException {
    if (artifacts.isEmpty()) {
      writeFile("   None");
    } else {
      for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
        Artifact artifact = (Artifact) iterator.next();

        // called because artifact will set the version to -SNAPSHOT only if I
        // do this. MNG-2961
        artifact.isSnapshot();

        writeFile("   " + artifact);

      }
    }
  }

  private StringBuffer writeDependenciesResult(Set usedDeclared, Set unusedDeclared, Set usedUndeclared) {
    StringWriter out = new StringWriter();
    PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(out);
    writer.startElement("dependencies");
    writer.startElement("usedDeclared");
    writer = writeDependencyXML(usedDeclared, writer);
    writer.endElement();
    writer.startElement("unusedDeclared");
    writer = writeDependencyXML(unusedDeclared, writer);
    writer.endElement();
    writer.startElement("usedUndeclared");
    writer = writeDependencyXML(usedUndeclared, writer);
    writer.endElement();
    writer.endElement();
    return out.getBuffer();
  }

  private PrettyPrintXMLWriter writeDependencyXML(Set artifacts, PrettyPrintXMLWriter writer) {
    if (!artifacts.isEmpty()) {
      getLog().info("Add the following to your pom to correct the missing dependencies: ");

      // StringWriter out = new StringWriter();
      // PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(out);

      Iterator iter = artifacts.iterator();
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
        writer.endElement();
      }

      // getLog().info("\n" + out.getBuffer());
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
