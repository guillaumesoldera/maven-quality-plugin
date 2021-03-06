package com.serli.maven.plugin.quality.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.serli.maven.plugin.quality.model.jaxb.MavenConventions;
import com.serli.maven.plugin.quality.model.violations.MavenConventionsViolation;
import com.serli.maven.plugin.quality.model.violations.NamingConventionsViolation;
import com.serli.maven.plugin.quality.model.violations.StructureConventionsViolation;
import com.serli.maven.plugin.quality.parser.PomFileReader;
import com.serli.maven.plugin.quality.util.Util;

/**
 * Goal which checks maven conventions.
 * 
 * @goal check-maven-conventions
 * @requiresDependencyResolution test
 * @phase verify
 */
public class MavenConventionsCheckMojo extends AbstractMojo {

  /**
   * Location of the file.
   * 
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File outputDirectory;

  /**
   * Maven conventions file name.
   * 
   * @parameter expression="${mavenConventions}"
   *            default-value="maven-conventions.xml"
   * @required
   */
  private String mavenConventions;

  /**
   * Output the xml for the dependencies analyze report.
   * 
   * @parameter expression="${outputXML}" default-value="true"
   */
  private boolean outputXML;

  /**
   * Output file.
   * 
   * @parameter expression="${outputFile}"
   */
  private File outputFile;

  /**
   * The Maven project to analyze.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
//  private MavenProject project;

  public void execute() throws MojoExecutionException, MojoFailureException {

    File f = outputDirectory;

    if (!f.exists()) {
      f.mkdirs();
    }

    if (outputDirectory == null || !outputDirectory.exists()) {
      getLog().info("Skipping project with no build directory");
      return;
    }
    try {
      MavenConventions conventions = Util.getMavenConventions(mavenConventions);
      File pom = new File("pom.xml");
      FileReader reader;
      PomFileReader pomFileReader = new PomFileReader(getLog());

      reader = new FileReader(pom);
      List<MavenConventionsViolation> listViolation = pomFileReader.checkMavenConventions(reader, true, true, conventions);
      
      if (listViolation != null) {
        if (outputXML) {
          StringBuffer writeViolation = writeConventionsViolation(listViolation);
          Util.writeFile(writeViolation.toString(), outputFile, getLog());
        } else {

          for (MavenConventionsViolation violation : listViolation) {
            getLog().info(violation.getMessage());
          }
        }
      } else {
        getLog().info("No violations found");
      }

    } catch (JAXBException e1) {
      throw new MojoExecutionException(e1.getMessage());
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (XmlPullParserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  private StringBuffer writeConventionsViolation(List<MavenConventionsViolation> listViolation) {
    StringWriter out = new StringWriter();
    PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(out);
    writer.startElement("mavenConventionsViolation");
    List<StructureConventionsViolation> listStructureConventionsViolations = getStructureConventionsViolations(listViolation);
    List<NamingConventionsViolation> listNamingConventionsViolations = getNamingConventionsViolations(listViolation);
    writer = writeFormattingViolation(listStructureConventionsViolations, writer);
    writer = writeNamingViolation(listNamingConventionsViolations, writer);
    writer.endElement();
    return out.getBuffer();
  }

  private PrettyPrintXMLWriter writeNamingViolation(List<NamingConventionsViolation> listNamingConventionsViolations, PrettyPrintXMLWriter writer) {
    writer.startElement("namingViolations");
    for (NamingConventionsViolation violation : listNamingConventionsViolations) {
      writer = writeMavenConventionsViolation(writer, violation);
    }
    writer.endElement();
    return writer;
  }
  
  private PrettyPrintXMLWriter writeFormattingViolation(List<StructureConventionsViolation> listStructureConventionsViolations, PrettyPrintXMLWriter writer) {
    writer.startElement("formattingViolations");
    for (StructureConventionsViolation violation : listStructureConventionsViolations) {
      writer = writeMavenConventionsViolation(writer, violation);
    }
    writer.endElement();
    return writer;
  }

  private PrettyPrintXMLWriter writeMavenConventionsViolation(PrettyPrintXMLWriter writer, MavenConventionsViolation violation) {
    if (violation != null) {
      writer.startElement("violation");
      writer.startElement("tag");
      writer.writeText(violation.getTagName());
      writer.endElement();
      writer.startElement("line");
      writer.writeText(violation.getLineNumber() + "");
      writer.endElement();
      writer.startElement("message");
      writer.writeText(violation.getMessage());
      writer.endElement();
      writer.endElement();
    }
    return writer;
  }
  
  private List<StructureConventionsViolation> getStructureConventionsViolations(List<MavenConventionsViolation> mavenConventionsViolations) {
    List<StructureConventionsViolation> structureConventionsViolations = new ArrayList<StructureConventionsViolation>();
    
    for (MavenConventionsViolation violations : mavenConventionsViolations) {
      if (violations instanceof StructureConventionsViolation) {
        structureConventionsViolations.add((StructureConventionsViolation) violations);
      }
    }
    
    return structureConventionsViolations;
  }
  
  private List<NamingConventionsViolation> getNamingConventionsViolations(List<MavenConventionsViolation> mavenConventionsViolations) {
    List<NamingConventionsViolation> namingConventionsViolations = new ArrayList<NamingConventionsViolation>();
    
    for (MavenConventionsViolation violations : mavenConventionsViolations) {
      if (violations instanceof NamingConventionsViolation) {
        namingConventionsViolations.add((NamingConventionsViolation) violations);
      }
    }
    
    return namingConventionsViolations;
  }
  
}
