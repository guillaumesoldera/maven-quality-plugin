package com.serli.maven.plugin.quality.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.reporting.MavenReportException;
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
public class MavenConventionsCheckMojo extends AbstractMavenQualityMojo {

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
   * @parameter expression="${logConsole}" default-value="false"
   */
  private boolean logConsole;

  /**
   * Output file.
   * 
   */
  private File outputFile;

  private List<MavenConventionsViolation> listViolation;

  public void execution() throws MojoExecutionException {

    File f = outputDirectory;

    if (!f.exists()) {
      f.mkdirs();
    }

    if (outputDirectory == null || !outputDirectory.exists()) {
      getLog().info("Skipping project with no build directory");
      return;
    }

    outputFile = new File(outputDirectory, "reports/maven-conventions-check.xml");
    Util.buildOutputFile(outputFile);

    try {
      MavenConventions conventions = Util.getMavenConventions(mavenConventions);
      File pom = new File("pom.xml");
      FileReader reader;
      PomFileReader pomFileReader = new PomFileReader(getLog());

      reader = new FileReader(pom);
      listViolation = pomFileReader.checkMavenConventions(reader, true, true, conventions);

      if (listViolation != null) {
        StringBuffer writeViolation = writeConventionsViolation(listViolation);
        Util.writeFile(writeViolation.toString(), outputFile, getLog());
        if (logConsole) {
          for (MavenConventionsViolation violation : listViolation) {
            getLog().info(violation.getMessage());
          }
        }
        if (listViolation.size() == 0) {
          getLog().info("No violations found");
        }
      } else {
        getLog().info("No violations found");
      }

    } catch (JAXBException e1) {
      throw new MojoExecutionException(e1.getMessage());
    } catch (FileNotFoundException e) {
      throw new MojoExecutionException(e.getMessage());
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    } catch (XmlPullParserException e) {
      throw new MojoExecutionException(e.getMessage());
    }
    getLog().info("results are available in " + outputFile.getPath());
  }

  private StringBuffer writeConventionsViolation(List<MavenConventionsViolation> listViolation) {
    StringWriter out = new StringWriter();
    PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(out);
    writer.startElement("mavenConventionsViolation");
    List<MavenConventionsViolation> listStructureConventionsViolations = getStructureConventionsViolations(listViolation);
    List<MavenConventionsViolation> listNamingConventionsViolations = getNamingConventionsViolations(listViolation);
    writer = writeFormattingViolation(listStructureConventionsViolations, writer);
    writer = writeNamingViolation(listNamingConventionsViolations, writer);
    writer.endElement();
    return out.getBuffer();
  }

  private PrettyPrintXMLWriter writeNamingViolation(List<MavenConventionsViolation> listNamingConventionsViolations,
      PrettyPrintXMLWriter writer) {
    writer.startElement("namingViolations");
    for (MavenConventionsViolation violation : listNamingConventionsViolations) {
      writer = writeMavenConventionsViolation(writer, violation);
    }
    writer.endElement();
    return writer;
  }

  private PrettyPrintXMLWriter writeFormattingViolation(List<MavenConventionsViolation> listStructureConventionsViolations,
      PrettyPrintXMLWriter writer) {
    writer.startElement("formattingViolations");
    for (MavenConventionsViolation violation : listStructureConventionsViolations) {
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

  private List<MavenConventionsViolation> getStructureConventionsViolations(List<MavenConventionsViolation> mavenConventionsViolations) {
    List<MavenConventionsViolation> structureConventionsViolations = new ArrayList<MavenConventionsViolation>();

    for (MavenConventionsViolation violations : mavenConventionsViolations) {
      if (violations instanceof StructureConventionsViolation) {
        structureConventionsViolations.add((StructureConventionsViolation) violations);
      }
    }

    return structureConventionsViolations;
  }

  private List<MavenConventionsViolation> getNamingConventionsViolations(List<MavenConventionsViolation> mavenConventionsViolations) {
    List<MavenConventionsViolation> namingConventionsViolations = new ArrayList<MavenConventionsViolation>();

    for (MavenConventionsViolation violations : mavenConventionsViolations) {
      if (violations instanceof NamingConventionsViolation) {
        namingConventionsViolations.add((NamingConventionsViolation) violations);
      }
    }

    return namingConventionsViolations;
  }

  public String getOutputName() {
    return "maven-convention-check";
  }

  @Override
  protected String getI18Nsection() {
    return "mavenconventions";
  }

  @Override
  protected void executeReport(Locale locale) throws MavenReportException {
    outputReportDirectory.mkdirs();

    try {
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

      if (listViolation != null) {
        sink.list();
        sink.listItem();
        sink.link("formattingviolations");
        sink.text(getI18nString(locale, "formattingviolations.name"));
        sink.link_();
        sink.listItem_();
        sink.listItem();
        sink.link("namingviolations");
        sink.text(getI18nString(locale, "namingviolations.name"));
        sink.link_();
        sink.listItem_();
        sink.list_();
        List<MavenConventionsViolation> structureConventionsViolations = getStructureConventionsViolations(listViolation);
        List<MavenConventionsViolation> namingConventionsViolations = getNamingConventionsViolations(listViolation);
        writeSection(sink, "formattingviolations", locale, structureConventionsViolations);
        writeSection(sink, "namingviolations", locale, namingConventionsViolations);
      } else {
        sink.text(getI18nString(locale, "noviolation"));
      }

      sink.body_();
      sink.flush();
      sink.close();

    } catch (MojoExecutionException e) {
      throw new MavenReportException(e.getMessage(), e);
    }

  }

  private void writeSection(Sink sink, String key, Locale locale, List<MavenConventionsViolation> listConventionViolation) {
    writeBegin(sink, key, locale);

    if (listConventionViolation != null && !listConventionViolation.isEmpty()) {
      sink.table();
      writeHeaderCell(sink, getI18nString(locale, "tag"));
      writeHeaderCell(sink, getI18nString(locale, "message"));
      writeHeaderCell(sink, getI18nString(locale, "linenumber"));
      for (MavenConventionsViolation violation : listConventionViolation) {
        sink.tableRow();
        writeCell(sink, violation.getTagName());
        writeCell(sink, violation.getMessage());
        writeCell(sink, violation.getLineNumber() + "");
        sink.tableRow_();
      }
      sink.table_();
    } else {
      sink.text(getI18nString(locale, "noviolation"));
    }

    writeEnd(sink);
  }

}
