package com.serli.maven.plugin.quality.parser;

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.ActivationOS;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Extension;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Notifier;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Parent;
import org.apache.maven.model.PatternSet;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginConfiguration;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryBase;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.model.Site;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.serli.maven.plugin.quality.model.DependencyLocation;
import com.serli.maven.plugin.quality.model.StructureConventionsViolation;
import com.serli.maven.plugin.quality.model.jaxb.MavenConventions;
import com.serli.maven.plugin.quality.util.Util;

/**
 * Based on MavenXpp3Reader.class
 * 
 * @author Guillaume
 * 
 */
public class PomFileReader {

  /**
   * Logger.
   */
  private Log log;

  public Log getLog() {
    return log;
  }

  public PomFileReader(Log pLog) {
    this.log = pLog;
  }

  /**
   * Method getBooleanValue
   * 
   * @param s
   * @param parser
   * @param attribute
   */
  public boolean getBooleanValue(String s, String attribute, XmlPullParser parser) throws XmlPullParserException {
    if (s != null) {
      return Boolean.valueOf(s).booleanValue();
    }
    return false;
  } // -- boolean getBooleanValue(String, String, XmlPullParser)

  /**
   * Method getCharacterValue
   * 
   * @param s
   * @param parser
   * @param attribute
   */
  public char getCharacterValue(String s, String attribute, XmlPullParser parser) throws XmlPullParserException {
    if (s != null) {
      return s.charAt(0);
    }
    return 0;
  } // -- char getCharacterValue(String, String, XmlPullParser)

  /**
   * Method getDateValue
   * 
   * @param s
   * @param parser
   * @param attribute
   */
  public java.util.Date getDateValue(String s, String attribute, XmlPullParser parser) throws XmlPullParserException {
    if (s != null) {
      DateFormat dateParser = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
      return dateParser.parse(s, new ParsePosition(0));
    }
    return null;
  } // -- java.util.Date getDateValue(String, String, XmlPullParser)

  /**
   * Method getDoubleValue
   * 
   * @param s
   * @param strict
   * @param parser
   * @param attribute
   */
  public double getDoubleValue(String s, String attribute, XmlPullParser parser, boolean strict) throws XmlPullParserException {
    if (s != null) {
      try {
        return Double.valueOf(s).doubleValue();
      } catch (NumberFormatException e) {
        if (strict) {
          throw new XmlPullParserException("Unable to parse element '" + attribute + "', must be a floating point number", parser, null);
        }
      }
    }
    return 0;
  } // -- double getDoubleValue(String, String, XmlPullParser, boolean)

  /**
   * Method getFloatValue
   * 
   * @param s
   * @param strict
   * @param parser
   * @param attribute
   */
  public float getFloatValue(String s, String attribute, XmlPullParser parser, boolean strict) throws XmlPullParserException {
    if (s != null) {
      try {
        return Float.valueOf(s).floatValue();
      } catch (NumberFormatException e) {
        if (strict) {
          throw new XmlPullParserException("Unable to parse element '" + attribute + "', must be a floating point number", parser, null);
        }
      }
    }
    return 0;
  } // -- float getFloatValue(String, String, XmlPullParser, boolean)

  /**
   * Method getIntegerValue
   * 
   * @param s
   * @param strict
   * @param parser
   * @param attribute
   */
  public int getIntegerValue(String s, String attribute, XmlPullParser parser, boolean strict) throws XmlPullParserException {
    if (s != null) {
      try {
        return Integer.valueOf(s).intValue();
      } catch (NumberFormatException e) {
        if (strict) {
          throw new XmlPullParserException("Unable to parse element '" + attribute + "', must be an integer", parser, null);
        }
      }
    }
    return 0;
  } // -- int getIntegerValue(String, String, XmlPullParser, boolean)

  /**
   * Method getLongValue
   * 
   * @param s
   * @param strict
   * @param parser
   * @param attribute
   */
  public long getLongValue(String s, String attribute, XmlPullParser parser, boolean strict) throws XmlPullParserException {
    if (s != null) {
      try {
        return Long.valueOf(s).longValue();
      } catch (NumberFormatException e) {
        if (strict) {
          throw new XmlPullParserException("Unable to parse element '" + attribute + "', must be a long integer", parser, null);
        }
      }
    }
    return 0;
  } // -- long getLongValue(String, String, XmlPullParser, boolean)

  /**
   * Method getRequiredAttributeValue
   * 
   * @param s
   * @param strict
   * @param parser
   * @param attribute
   */
  public String getRequiredAttributeValue(String s, String attribute, XmlPullParser parser, boolean strict) throws XmlPullParserException {
    if (s == null) {
      if (strict) {
        throw new XmlPullParserException("Missing required value for attribute '" + attribute + "'", parser, null);
      }
    }
    return s;
  } // -- String getRequiredAttributeValue(String, String, XmlPullParser,
    // boolean)

  /**
   * Method getShortValue
   * 
   * @param s
   * @param strict
   * @param parser
   * @param attribute
   */
  public short getShortValue(String s, String attribute, XmlPullParser parser, boolean strict) throws XmlPullParserException {
    if (s != null) {
      try {
        return Short.valueOf(s).shortValue();
      } catch (NumberFormatException e) {
        if (strict) {
          throw new XmlPullParserException("Unable to parse element '" + attribute + "', must be a short integer", parser, null);
        }
      }
    }
    return 0;
  } // -- short getShortValue(String, String, XmlPullParser, boolean)

  /**
   * Method getTrimmedValue
   * 
   * @param s
   */
  public String getTrimmedValue(String s) {
    if (s != null) {
      s = s.trim();
    }
    return s;
  } // -- String getTrimmedValue(String)

  /**
   * Method parseActivation
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Activation parseActivation(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Activation activation = new Activation();
    activation.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("activeByDefault")) {
        if (parsed.contains("activeByDefault")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("activeByDefault");
        activation.setActiveByDefault(getBooleanValue(getTrimmedValue(parser.nextText()), "activeByDefault", parser));
      } else if (parser.getName().equals("jdk")) {
        if (parsed.contains("jdk")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("jdk");
        activation.setJdk(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("os")) {
        if (parsed.contains("os")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("os");
        activation.setOs(parseActivationOS("os", parser, strict, encoding));
      } else if (parser.getName().equals("property")) {
        if (parsed.contains("property")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("property");
        activation.setProperty(parseActivationProperty("property", parser, strict, encoding));
      } else if (parser.getName().equals("file")) {
        if (parsed.contains("file")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("file");
        activation.setFile(parseActivationFile("file", parser, strict, encoding));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return activation;
  } // -- Activation parseActivation(String, XmlPullParser, boolean, String)

  /**
   * Method parseActivationFile
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private ActivationFile parseActivationFile(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    ActivationFile activationFile = new ActivationFile();
    activationFile.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("missing")) {
        if (parsed.contains("missing")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("missing");
        activationFile.setMissing(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("exists")) {
        if (parsed.contains("exists")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("exists");
        activationFile.setExists(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return activationFile;
  } // -- ActivationFile parseActivationFile(String, XmlPullParser, boolean,
    // String)

  /**
   * Method parseActivationOS
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private ActivationOS parseActivationOS(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    ActivationOS activationOS = new ActivationOS();
    activationOS.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        activationOS.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("family")) {
        if (parsed.contains("family")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("family");
        activationOS.setFamily(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("arch")) {
        if (parsed.contains("arch")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("arch");
        activationOS.setArch(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("version")) {
        if (parsed.contains("version")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("version");
        activationOS.setVersion(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return activationOS;
  } // -- ActivationOS parseActivationOS(String, XmlPullParser, boolean, String)

  /**
   * Method parseActivationProperty
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private ActivationProperty parseActivationProperty(String tagName, XmlPullParser parser, boolean strict, String encoding)
      throws IOException, XmlPullParserException {
    ActivationProperty activationProperty = new ActivationProperty();
    activationProperty.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        activationProperty.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("value")) {
        if (parsed.contains("value")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("value");
        activationProperty.setValue(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return activationProperty;
  } // -- ActivationProperty parseActivationProperty(String, XmlPullParser,
    // boolean, String)

  /**
   * Method parseBuild
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Build parseBuild(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Build build = new Build();
    build.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("sourceDirectory")) {
        if (parsed.contains("sourceDirectory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("sourceDirectory");
        build.setSourceDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("scriptSourceDirectory")) {
        if (parsed.contains("scriptSourceDirectory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("scriptSourceDirectory");
        build.setScriptSourceDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("testSourceDirectory")) {
        if (parsed.contains("testSourceDirectory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("testSourceDirectory");
        build.setTestSourceDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("outputDirectory")) {
        if (parsed.contains("outputDirectory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("outputDirectory");
        build.setOutputDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("testOutputDirectory")) {
        if (parsed.contains("testOutputDirectory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("testOutputDirectory");
        build.setTestOutputDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("extensions")) {
        if (parsed.contains("extensions")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("extensions");
        java.util.List<Extension> extensions = new java.util.ArrayList<Extension>();
        build.setExtensions(extensions);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("extension")) {
            extensions.add(parseExtension("extension", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("defaultGoal")) {
        if (parsed.contains("defaultGoal")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("defaultGoal");
        build.setDefaultGoal(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("resources")) {
        if (parsed.contains("resources")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("resources");
        java.util.List<Resource> resources = new java.util.ArrayList<Resource>();
        build.setResources(resources);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("resource")) {
            resources.add(parseResource("resource", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("testResources")) {
        if (parsed.contains("testResources")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("testResources");
        java.util.List<Resource> testResources = new java.util.ArrayList<Resource>();
        build.setTestResources(testResources);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("testResource")) {
            testResources.add(parseResource("testResource", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("directory")) {
        if (parsed.contains("directory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("directory");
        build.setDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("finalName")) {
        if (parsed.contains("finalName")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("finalName");
        build.setFinalName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("filters")) {
        if (parsed.contains("filters")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("filters");
        java.util.List<String> filters = new java.util.ArrayList<String>();
        build.setFilters(filters);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("filter")) {
            filters.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("pluginManagement")) {
        if (parsed.contains("pluginManagement")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("pluginManagement");
        build.setPluginManagement(parsePluginManagement("pluginManagement", parser, strict, encoding));
      } else if (parser.getName().equals("plugins")) {
        if (parsed.contains("plugins")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("plugins");
        java.util.List<Plugin> plugins = new java.util.ArrayList<Plugin>();
        build.setPlugins(plugins);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("plugin")) {
            plugins.add(parsePlugin("plugin", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return build;
  } // -- Build parseBuild(String, XmlPullParser, boolean, String)

  /**
   * Method parseBuildBase
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private BuildBase parseBuildBase(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    BuildBase buildBase = new BuildBase();
    buildBase.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("defaultGoal")) {
        if (parsed.contains("defaultGoal")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("defaultGoal");
        buildBase.setDefaultGoal(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("resources")) {
        if (parsed.contains("resources")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("resources");
        java.util.List<Resource> resources = new java.util.ArrayList<Resource>();
        buildBase.setResources(resources);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("resource")) {
            resources.add(parseResource("resource", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("testResources")) {
        if (parsed.contains("testResources")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("testResources");
        java.util.List<Resource> testResources = new java.util.ArrayList<Resource>();
        buildBase.setTestResources(testResources);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("testResource")) {
            testResources.add(parseResource("testResource", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("directory")) {
        if (parsed.contains("directory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("directory");
        buildBase.setDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("finalName")) {
        if (parsed.contains("finalName")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("finalName");
        buildBase.setFinalName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("filters")) {
        if (parsed.contains("filters")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("filters");
        java.util.List<String> filters = new java.util.ArrayList<String>();
        buildBase.setFilters(filters);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("filter")) {
            filters.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("pluginManagement")) {
        if (parsed.contains("pluginManagement")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("pluginManagement");
        buildBase.setPluginManagement(parsePluginManagement("pluginManagement", parser, strict, encoding));
      } else if (parser.getName().equals("plugins")) {
        if (parsed.contains("plugins")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("plugins");
        java.util.List<Plugin> plugins = new java.util.ArrayList<Plugin>();
        buildBase.setPlugins(plugins);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("plugin")) {
            plugins.add(parsePlugin("plugin", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return buildBase;
  } // -- BuildBase parseBuildBase(String, XmlPullParser, boolean, String)

  /**
   * Method parseCiManagement
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private CiManagement parseCiManagement(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    CiManagement ciManagement = new CiManagement();
    ciManagement.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("system")) {
        if (parsed.contains("system")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("system");
        ciManagement.setSystem(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        ciManagement.setUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("notifiers")) {
        if (parsed.contains("notifiers")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("notifiers");
        java.util.List<Notifier> notifiers = new java.util.ArrayList<Notifier>();
        ciManagement.setNotifiers(notifiers);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("notifier")) {
            notifiers.add(parseNotifier("notifier", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return ciManagement;
  } // -- CiManagement parseCiManagement(String, XmlPullParser, boolean, String)

  /**
   * Method parseConfigurationContainer
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private ConfigurationContainer parseConfigurationContainer(String tagName, XmlPullParser parser, boolean strict, String encoding)
      throws IOException, XmlPullParserException {
    ConfigurationContainer configurationContainer = new ConfigurationContainer();
    configurationContainer.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("inherited")) {
        if (parsed.contains("inherited")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("inherited");
        configurationContainer.setInherited(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("configuration")) {
        if (parsed.contains("configuration")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("configuration");
        configurationContainer.setConfiguration(Xpp3DomBuilder.build(parser));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return configurationContainer;
  } // -- ConfigurationContainer parseConfigurationContainer(String,
    // XmlPullParser, boolean, String)

  /**
   * Method parseContributor
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Contributor parseContributor(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Contributor contributor = new Contributor();
    contributor.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        contributor.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("email")) {
        if (parsed.contains("email")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("email");
        contributor.setEmail(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        contributor.setUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("organization") || parser.getName().equals("organisation")) {
        if (parsed.contains("organization")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("organization");
        contributor.setOrganization(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("organizationUrl") || parser.getName().equals("organisationUrl")) {
        if (parsed.contains("organizationUrl")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("organizationUrl");
        contributor.setOrganizationUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("roles")) {
        if (parsed.contains("roles")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("roles");
        java.util.List<String> roles = new java.util.ArrayList<String>();
        contributor.setRoles(roles);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("role")) {
            roles.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("timezone")) {
        if (parsed.contains("timezone")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("timezone");
        contributor.setTimezone(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("properties")) {
        if (parsed.contains("properties")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("properties");
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          String key = parser.getName();
          String value = parser.nextText().trim();
          contributor.addProperty(key, value);
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return contributor;
  } // -- Contributor parseContributor(String, XmlPullParser, boolean, String)

  /**
   * Method parseDependency
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Dependency parseDependency(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Dependency dependency = new Dependency();
    dependency.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("groupId")) {
        if (parsed.contains("groupId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("groupId");
        dependency.setGroupId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("artifactId")) {
        if (parsed.contains("artifactId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("artifactId");
        dependency.setArtifactId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("version")) {
        if (parsed.contains("version")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("version");
        dependency.setVersion(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("type")) {
        if (parsed.contains("type")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("type");
        dependency.setType(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("classifier")) {
        if (parsed.contains("classifier")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("classifier");
        dependency.setClassifier(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("scope")) {
        if (parsed.contains("scope")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("scope");
        dependency.setScope(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("systemPath")) {
        if (parsed.contains("systemPath")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("systemPath");
        dependency.setSystemPath(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("exclusions")) {
        if (parsed.contains("exclusions")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("exclusions");
        java.util.List<Exclusion> exclusions = new java.util.ArrayList<Exclusion>();
        dependency.setExclusions(exclusions);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("exclusion")) {
            exclusions.add(parseExclusion("exclusion", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("optional")) {
        if (parsed.contains("optional")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("optional");
        dependency.setOptional(getBooleanValue(getTrimmedValue(parser.nextText()), "optional", parser));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return dependency;
  } // -- Dependency parseDependency(String, XmlPullParser, boolean, String)

  /**
   * Method parseDependencyManagement
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private DependencyManagement parseDependencyManagement(String tagName, XmlPullParser parser, boolean strict, String encoding)
      throws IOException, XmlPullParserException {
    DependencyManagement dependencyManagement = new DependencyManagement();
    dependencyManagement.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("dependencies")) {
        if (parsed.contains("dependencies")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("dependencies");
        java.util.List<Dependency> dependencies = new java.util.ArrayList<Dependency>();
        dependencyManagement.setDependencies(dependencies);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("dependency")) {
            dependencies.add(parseDependency("dependency", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return dependencyManagement;
  } // -- DependencyManagement parseDependencyManagement(String, XmlPullParser,
    // boolean, String)

  /**
   * Method parseDeploymentRepository
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private DeploymentRepository parseDeploymentRepository(String tagName, XmlPullParser parser, boolean strict, String encoding)
      throws IOException, XmlPullParserException {
    DeploymentRepository deploymentRepository = new DeploymentRepository();
    deploymentRepository.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("uniqueVersion")) {
        if (parsed.contains("uniqueVersion")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("uniqueVersion");
        deploymentRepository.setUniqueVersion(getBooleanValue(getTrimmedValue(parser.nextText()), "uniqueVersion", parser));
      } else if (parser.getName().equals("id")) {
        if (parsed.contains("id")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("id");
        deploymentRepository.setId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        deploymentRepository.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        deploymentRepository.setUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("layout")) {
        if (parsed.contains("layout")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("layout");
        deploymentRepository.setLayout(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return deploymentRepository;
  } // -- DeploymentRepository parseDeploymentRepository(String, XmlPullParser,
    // boolean, String)

  /**
   * Method parseDeveloper
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Developer parseDeveloper(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Developer developer = new Developer();
    developer.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("id")) {
        if (parsed.contains("id")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("id");
        developer.setId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        developer.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("email")) {
        if (parsed.contains("email")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("email");
        developer.setEmail(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        developer.setUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("organization") || parser.getName().equals("organisation")) {
        if (parsed.contains("organization")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("organization");
        developer.setOrganization(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("organizationUrl") || parser.getName().equals("organisationUrl")) {
        if (parsed.contains("organizationUrl")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("organizationUrl");
        developer.setOrganizationUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("roles")) {
        if (parsed.contains("roles")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("roles");
        java.util.List<String> roles = new java.util.ArrayList<String>();
        developer.setRoles(roles);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("role")) {
            roles.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("timezone")) {
        if (parsed.contains("timezone")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("timezone");
        developer.setTimezone(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("properties")) {
        if (parsed.contains("properties")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("properties");
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          String key = parser.getName();
          String value = parser.nextText().trim();
          developer.addProperty(key, value);
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return developer;
  } // -- Developer parseDeveloper(String, XmlPullParser, boolean, String)

  /**
   * Method parseDistributionManagement
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private DistributionManagement parseDistributionManagement(String tagName, XmlPullParser parser, boolean strict, String encoding)
      throws IOException, XmlPullParserException {
    DistributionManagement distributionManagement = new DistributionManagement();
    distributionManagement.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("repository")) {
        if (parsed.contains("repository")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("repository");
        distributionManagement.setRepository(parseDeploymentRepository("repository", parser, strict, encoding));
      } else if (parser.getName().equals("snapshotRepository")) {
        if (parsed.contains("snapshotRepository")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("snapshotRepository");
        distributionManagement.setSnapshotRepository(parseDeploymentRepository("snapshotRepository", parser, strict, encoding));
      } else if (parser.getName().equals("site")) {
        if (parsed.contains("site")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("site");
        distributionManagement.setSite(parseSite("site", parser, strict, encoding));
      } else if (parser.getName().equals("downloadUrl")) {
        if (parsed.contains("downloadUrl")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("downloadUrl");
        distributionManagement.setDownloadUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("relocation")) {
        if (parsed.contains("relocation")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("relocation");
        distributionManagement.setRelocation(parseRelocation("relocation", parser, strict, encoding));
      } else if (parser.getName().equals("status")) {
        if (parsed.contains("status")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("status");
        distributionManagement.setStatus(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return distributionManagement;
  } // -- DistributionManagement parseDistributionManagement(String,
    // XmlPullParser, boolean, String)

  /**
   * Method parseExclusion
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Exclusion parseExclusion(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Exclusion exclusion = new Exclusion();
    exclusion.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("artifactId")) {
        if (parsed.contains("artifactId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("artifactId");
        exclusion.setArtifactId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("groupId")) {
        if (parsed.contains("groupId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("groupId");
        exclusion.setGroupId(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return exclusion;
  } // -- Exclusion parseExclusion(String, XmlPullParser, boolean, String)

  /**
   * Method parseExtension
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Extension parseExtension(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Extension extension = new Extension();
    extension.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("groupId")) {
        if (parsed.contains("groupId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("groupId");
        extension.setGroupId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("artifactId")) {
        if (parsed.contains("artifactId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("artifactId");
        extension.setArtifactId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("version")) {
        if (parsed.contains("version")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("version");
        extension.setVersion(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return extension;
  } // -- Extension parseExtension(String, XmlPullParser, boolean, String)

  /**
   * Method parseFileSet
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private FileSet parseFileSet(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    FileSet fileSet = new FileSet();
    fileSet.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("directory")) {
        if (parsed.contains("directory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("directory");
        fileSet.setDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("includes")) {
        if (parsed.contains("includes")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("includes");
        java.util.List<String> includes = new java.util.ArrayList<String>();
        fileSet.setIncludes(includes);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("include")) {
            includes.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("excludes")) {
        if (parsed.contains("excludes")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("excludes");
        java.util.List<String> excludes = new java.util.ArrayList<String>();
        fileSet.setExcludes(excludes);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("exclude")) {
            excludes.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return fileSet;
  } // -- FileSet parseFileSet(String, XmlPullParser, boolean, String)

  /**
   * Method parseIssueManagement
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private IssueManagement parseIssueManagement(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    IssueManagement issueManagement = new IssueManagement();
    issueManagement.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("system")) {
        if (parsed.contains("system")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("system");
        issueManagement.setSystem(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        issueManagement.setUrl(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return issueManagement;
  } // -- IssueManagement parseIssueManagement(String, XmlPullParser, boolean,
    // String)

  /**
   * Method parseLicense
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private License parseLicense(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    License license = new License();
    license.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        license.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        license.setUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("distribution")) {
        if (parsed.contains("distribution")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("distribution");
        license.setDistribution(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("comments")) {
        if (parsed.contains("comments")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("comments");
        license.setComments(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return license;
  } // -- License parseLicense(String, XmlPullParser, boolean, String)

  /**
   * Method parseMailingList
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private MailingList parseMailingList(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    MailingList mailingList = new MailingList();
    mailingList.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        mailingList.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("subscribe")) {
        if (parsed.contains("subscribe")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("subscribe");
        mailingList.setSubscribe(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("unsubscribe")) {
        if (parsed.contains("unsubscribe")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("unsubscribe");
        mailingList.setUnsubscribe(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("post")) {
        if (parsed.contains("post")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("post");
        mailingList.setPost(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("archive")) {
        if (parsed.contains("archive")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("archive");
        mailingList.setArchive(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("otherArchives")) {
        if (parsed.contains("otherArchives")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("otherArchives");
        java.util.List<String> otherArchives = new java.util.ArrayList<String>();
        mailingList.setOtherArchives(otherArchives);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("otherArchive")) {
            otherArchives.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return mailingList;
  } // -- MailingList parseMailingList(String, XmlPullParser, boolean, String)

  /**
   * Method parseModel
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private List<StructureConventionsViolation> parseModel(String tagName, XmlPullParser parser, boolean strict, String encoding,
      boolean checkSkipLine, boolean checkTabSpaced, MavenConventions mavenConventions) throws IOException, XmlPullParserException {
    List<StructureConventionsViolation> listStructureConventionsViolations = new ArrayList<StructureConventionsViolation>();
    Model model = new Model();
    model.setModelEncoding(encoding);
    int eventType = parser.getEventType();
    String previousTag = null;
    int previousTagLineNumber = -1;
    int linePreviousTagEnd = -1;
    while (eventType != XmlPullParser.END_DOCUMENT) {
      if (eventType == XmlPullParser.START_TAG) {
        StructureConventionsViolation structureConventionsViolation = null;
        if (parser.getName().equals(tagName)) {
          previousTag = tagName;
          previousTagLineNumber = parser.getLineNumber();
          if (parser.getLineNumber() != 1) {
            structureConventionsViolation = new StructureConventionsViolation();
            structureConventionsViolation.setLineNumber(parser.getLineNumber());
            structureConventionsViolation.setTagName(parser.getName());
            structureConventionsViolation.setMessage("The tag '" + parser.getName() + "' should be on first line");
            listStructureConventionsViolations.add(structureConventionsViolation);
          }

        } else if (parser.getDepth() == 2) {
          int positionPrevious = mavenConventions.getPosition(previousTag);
          int positionTag = mavenConventions.getPosition(parser.getName());
          if (positionTag != -1 && positionPrevious != -1) {
            if (positionTag < positionPrevious) {
              String message = Util.buildOrderViolationMessage(parser.getLineNumber(), parser.getName(), previousTag);
              structureConventionsViolation = new StructureConventionsViolation(parser.getName(), parser.getLineNumber(), message);
              listStructureConventionsViolations.add(structureConventionsViolation);
            }
          }
          int spaceIndentWanted = mavenConventions.getSpaceIndent(parser.getName());
          if (spaceIndentWanted != -1) {
            int columnNumber = parser.getColumnNumber();

            int spaceIndentReal = columnNumber - parser.getName().length() - 2;
            if (spaceIndentReal != spaceIndentWanted) {
              String message = Util.buildTabSpacedViolationMessage(parser.getLineNumber(), parser.getName(), spaceIndentReal,
                  spaceIndentWanted);
              structureConventionsViolation = new StructureConventionsViolation(parser.getName(), parser.getLineNumber(), message);
              listStructureConventionsViolations.add(structureConventionsViolation);
            }
          }
          int skipLineWanted = -1;
          if (mavenConventions.sameGroup(parser.getName(), previousTag)) {
            skipLineWanted = mavenConventions.getSkipLine(previousTag);
          } else {
            skipLineWanted = mavenConventions.getGroup(previousTag).getSkipLine();
          }
          if (skipLineWanted != -1) {
            int skipLineReal = parser.getLineNumber() - 1;
            if (linePreviousTagEnd != -1) {
              skipLineReal -= linePreviousTagEnd;
            } else {
              // we are after tag 'project' and in this case we check lines skipped with START_TAG
              skipLineReal -= previousTagLineNumber;
            }
            if (skipLineReal != skipLineWanted) {
              String message = Util.buildSkipLineViolationMessage(parser.getLineNumber(), previousTag, skipLineReal, skipLineWanted);
              structureConventionsViolation = new StructureConventionsViolation(parser.getName(), parser.getLineNumber(), message);
              listStructureConventionsViolations.add(structureConventionsViolation);
            }
          }

          previousTagLineNumber = parser.getLineNumber();
          previousTag = parser.getName();
        }
      } else if (eventType == XmlPullParser.END_TAG) {
        if (parser.getDepth() <= 2) {
          linePreviousTagEnd = parser.getLineNumber();
        }
      }
      eventType = parser.next();
    }
    return listStructureConventionsViolations;
  } // -- List<StructureConventionsViolation> parseModel(String, XmlPullParser, boolean, String, boolean, boolean, MavenConventions)

  /**
   * Method parseModelBase
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private ModelBase parseModelBase(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    ModelBase modelBase = new ModelBase();
    modelBase.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("modules")) {
        if (parsed.contains("modules")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("modules");
        java.util.List<String> modules = new java.util.ArrayList<String>();
        modelBase.setModules(modules);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("module")) {
            modules.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("repositories")) {
        if (parsed.contains("repositories")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("repositories");
        java.util.List<Repository> repositories = new java.util.ArrayList<Repository>();
        modelBase.setRepositories(repositories);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("repository")) {
            repositories.add(parseRepository("repository", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("pluginRepositories")) {
        if (parsed.contains("pluginRepositories")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("pluginRepositories");
        java.util.List<Repository> pluginRepositories = new java.util.ArrayList<Repository>();
        modelBase.setPluginRepositories(pluginRepositories);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("pluginRepository")) {
            pluginRepositories.add(parseRepository("pluginRepository", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("dependencies")) {
        if (parsed.contains("dependencies")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("dependencies");
        java.util.List<Dependency> dependencies = new java.util.ArrayList<Dependency>();
        modelBase.setDependencies(dependencies);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("dependency")) {
            dependencies.add(parseDependency("dependency", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("reports")) {
        if (parsed.contains("reports")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("reports");
        modelBase.setReports(Xpp3DomBuilder.build(parser));
      } else if (parser.getName().equals("reporting")) {
        if (parsed.contains("reporting")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("reporting");
        modelBase.setReporting(parseReporting("reporting", parser, strict, encoding));
      } else if (parser.getName().equals("dependencyManagement")) {
        if (parsed.contains("dependencyManagement")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("dependencyManagement");
        modelBase.setDependencyManagement(parseDependencyManagement("dependencyManagement", parser, strict, encoding));
      } else if (parser.getName().equals("distributionManagement")) {
        if (parsed.contains("distributionManagement")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("distributionManagement");
        modelBase.setDistributionManagement(parseDistributionManagement("distributionManagement", parser, strict, encoding));
      } else if (parser.getName().equals("properties")) {
        if (parsed.contains("properties")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("properties");
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          String key = parser.getName();
          String value = parser.nextText().trim();
          modelBase.addProperty(key, value);
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return modelBase;
  } // -- ModelBase parseModelBase(String, XmlPullParser, boolean, String)

  /**
   * Method parseNotifier
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Notifier parseNotifier(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Notifier notifier = new Notifier();
    notifier.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("type")) {
        if (parsed.contains("type")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("type");
        notifier.setType(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("sendOnError")) {
        if (parsed.contains("sendOnError")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("sendOnError");
        notifier.setSendOnError(getBooleanValue(getTrimmedValue(parser.nextText()), "sendOnError", parser));
      } else if (parser.getName().equals("sendOnFailure")) {
        if (parsed.contains("sendOnFailure")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("sendOnFailure");
        notifier.setSendOnFailure(getBooleanValue(getTrimmedValue(parser.nextText()), "sendOnFailure", parser));
      } else if (parser.getName().equals("sendOnSuccess")) {
        if (parsed.contains("sendOnSuccess")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("sendOnSuccess");
        notifier.setSendOnSuccess(getBooleanValue(getTrimmedValue(parser.nextText()), "sendOnSuccess", parser));
      } else if (parser.getName().equals("sendOnWarning")) {
        if (parsed.contains("sendOnWarning")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("sendOnWarning");
        notifier.setSendOnWarning(getBooleanValue(getTrimmedValue(parser.nextText()), "sendOnWarning", parser));
      } else if (parser.getName().equals("address")) {
        if (parsed.contains("address")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("address");
        notifier.setAddress(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("configuration")) {
        if (parsed.contains("configuration")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("configuration");
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          String key = parser.getName();
          String value = parser.nextText().trim();
          notifier.addConfiguration(key, value);
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return notifier;
  } // -- Notifier parseNotifier(String, XmlPullParser, boolean, String)

  /**
   * Method parseOrganization
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Organization parseOrganization(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Organization organization = new Organization();
    organization.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        organization.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        organization.setUrl(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return organization;
  } // -- Organization parseOrganization(String, XmlPullParser, boolean, String)

  /**
   * Method parseParent
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Parent parseParent(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Parent parent = new Parent();
    parent.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("artifactId")) {
        if (parsed.contains("artifactId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("artifactId");
        parent.setArtifactId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("groupId")) {
        if (parsed.contains("groupId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("groupId");
        parent.setGroupId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("version")) {
        if (parsed.contains("version")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("version");
        parent.setVersion(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("relativePath")) {
        if (parsed.contains("relativePath")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("relativePath");
        parent.setRelativePath(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return parent;
  } // -- Parent parseParent(String, XmlPullParser, boolean, String)

  /**
   * Method parsePatternSet
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private PatternSet parsePatternSet(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    PatternSet patternSet = new PatternSet();
    patternSet.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("includes")) {
        if (parsed.contains("includes")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("includes");
        java.util.List<String> includes = new java.util.ArrayList<String>();
        patternSet.setIncludes(includes);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("include")) {
            includes.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("excludes")) {
        if (parsed.contains("excludes")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("excludes");
        java.util.List<String> excludes = new java.util.ArrayList<String>();
        patternSet.setExcludes(excludes);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("exclude")) {
            excludes.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return patternSet;
  } // -- PatternSet parsePatternSet(String, XmlPullParser, boolean, String)

  /**
   * Method parsePlugin
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Plugin parsePlugin(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Plugin plugin = new Plugin();
    plugin.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("groupId")) {
        if (parsed.contains("groupId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("groupId");
        plugin.setGroupId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("artifactId")) {
        if (parsed.contains("artifactId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("artifactId");
        plugin.setArtifactId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("version")) {
        if (parsed.contains("version")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("version");
        plugin.setVersion(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("extensions")) {
        if (parsed.contains("extensions")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("extensions");
        plugin.setExtensions(getBooleanValue(getTrimmedValue(parser.nextText()), "extensions", parser));
      } else if (parser.getName().equals("executions")) {
        if (parsed.contains("executions")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("executions");
        java.util.List<PluginExecution> executions = new java.util.ArrayList<PluginExecution>();
        plugin.setExecutions(executions);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("execution")) {
            executions.add(parsePluginExecution("execution", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("dependencies")) {
        if (parsed.contains("dependencies")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("dependencies");
        java.util.List<Dependency> dependencies = new java.util.ArrayList<Dependency>();
        plugin.setDependencies(dependencies);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("dependency")) {
            dependencies.add(parseDependency("dependency", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("goals")) {
        if (parsed.contains("goals")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("goals");
        plugin.setGoals(Xpp3DomBuilder.build(parser));
      } else if (parser.getName().equals("inherited")) {
        if (parsed.contains("inherited")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("inherited");
        plugin.setInherited(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("configuration")) {
        if (parsed.contains("configuration")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("configuration");
        plugin.setConfiguration(Xpp3DomBuilder.build(parser));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return plugin;
  } // -- Plugin parsePlugin(String, XmlPullParser, boolean, String)

  /**
   * Method parsePluginConfiguration
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private PluginConfiguration parsePluginConfiguration(String tagName, XmlPullParser parser, boolean strict, String encoding)
      throws IOException, XmlPullParserException {
    PluginConfiguration pluginConfiguration = new PluginConfiguration();
    pluginConfiguration.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("pluginManagement")) {
        if (parsed.contains("pluginManagement")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("pluginManagement");
        pluginConfiguration.setPluginManagement(parsePluginManagement("pluginManagement", parser, strict, encoding));
      } else if (parser.getName().equals("plugins")) {
        if (parsed.contains("plugins")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("plugins");
        java.util.List<Plugin> plugins = new java.util.ArrayList<Plugin>();
        pluginConfiguration.setPlugins(plugins);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("plugin")) {
            plugins.add(parsePlugin("plugin", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return pluginConfiguration;
  } // -- PluginConfiguration parsePluginConfiguration(String, XmlPullParser,
    // boolean, String)

  /**
   * Method parsePluginContainer
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private PluginContainer parsePluginContainer(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    PluginContainer pluginContainer = new PluginContainer();
    pluginContainer.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("plugins")) {
        if (parsed.contains("plugins")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("plugins");
        java.util.List<Plugin> plugins = new java.util.ArrayList<Plugin>();
        pluginContainer.setPlugins(plugins);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("plugin")) {
            plugins.add(parsePlugin("plugin", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return pluginContainer;
  } // -- PluginContainer parsePluginContainer(String, XmlPullParser, boolean,
    // String)

  /**
   * Method parsePluginExecution
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private PluginExecution parsePluginExecution(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    PluginExecution pluginExecution = new PluginExecution();
    pluginExecution.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("id")) {
        if (parsed.contains("id")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("id");
        pluginExecution.setId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("phase")) {
        if (parsed.contains("phase")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("phase");
        pluginExecution.setPhase(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("goals")) {
        if (parsed.contains("goals")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("goals");
        java.util.List<String> goals = new java.util.ArrayList<String>();
        pluginExecution.setGoals(goals);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("goal")) {
            goals.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("inherited")) {
        if (parsed.contains("inherited")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("inherited");
        pluginExecution.setInherited(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("configuration")) {
        if (parsed.contains("configuration")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("configuration");
        pluginExecution.setConfiguration(Xpp3DomBuilder.build(parser));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return pluginExecution;
  } // -- PluginExecution parsePluginExecution(String, XmlPullParser, boolean,
    // String)

  /**
   * Method parsePluginManagement
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private PluginManagement parsePluginManagement(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    PluginManagement pluginManagement = new PluginManagement();
    pluginManagement.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("plugins")) {
        if (parsed.contains("plugins")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("plugins");
        java.util.List<Plugin> plugins = new java.util.ArrayList<Plugin>();
        pluginManagement.setPlugins(plugins);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("plugin")) {
            plugins.add(parsePlugin("plugin", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return pluginManagement;
  } // -- PluginManagement parsePluginManagement(String, XmlPullParser, boolean,
    // String)

  /**
   * Method parsePrerequisites
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Prerequisites parsePrerequisites(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Prerequisites prerequisites = new Prerequisites();
    prerequisites.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("maven")) {
        if (parsed.contains("maven")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("maven");
        prerequisites.setMaven(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return prerequisites;
  } // -- Prerequisites parsePrerequisites(String, XmlPullParser, boolean,
    // String)

  /**
   * Method parseProfile
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Profile parseProfile(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Profile profile = new Profile();
    profile.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("id")) {
        if (parsed.contains("id")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("id");
        profile.setId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("activation")) {
        if (parsed.contains("activation")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("activation");
        profile.setActivation(parseActivation("activation", parser, strict, encoding));
      } else if (parser.getName().equals("build")) {
        if (parsed.contains("build")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("build");
        profile.setBuild(parseBuildBase("build", parser, strict, encoding));
      } else if (parser.getName().equals("modules")) {
        if (parsed.contains("modules")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("modules");
        java.util.List<String> modules = new java.util.ArrayList<String>();
        profile.setModules(modules);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("module")) {
            modules.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("repositories")) {
        if (parsed.contains("repositories")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("repositories");
        java.util.List<Repository> repositories = new java.util.ArrayList<Repository>();
        profile.setRepositories(repositories);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("repository")) {
            repositories.add(parseRepository("repository", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("pluginRepositories")) {
        if (parsed.contains("pluginRepositories")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("pluginRepositories");
        java.util.List<Repository> pluginRepositories = new java.util.ArrayList<Repository>();
        profile.setPluginRepositories(pluginRepositories);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("pluginRepository")) {
            pluginRepositories.add(parseRepository("pluginRepository", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("dependencies")) {
        if (parsed.contains("dependencies")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("dependencies");
        java.util.List<Dependency> dependencies = new java.util.ArrayList<Dependency>();
        profile.setDependencies(dependencies);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("dependency")) {
            dependencies.add(parseDependency("dependency", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("reports")) {
        if (parsed.contains("reports")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("reports");
        profile.setReports(Xpp3DomBuilder.build(parser));
      } else if (parser.getName().equals("reporting")) {
        if (parsed.contains("reporting")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("reporting");
        profile.setReporting(parseReporting("reporting", parser, strict, encoding));
      } else if (parser.getName().equals("dependencyManagement")) {
        if (parsed.contains("dependencyManagement")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("dependencyManagement");
        profile.setDependencyManagement(parseDependencyManagement("dependencyManagement", parser, strict, encoding));
      } else if (parser.getName().equals("distributionManagement")) {
        if (parsed.contains("distributionManagement")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("distributionManagement");
        profile.setDistributionManagement(parseDistributionManagement("distributionManagement", parser, strict, encoding));
      } else if (parser.getName().equals("properties")) {
        if (parsed.contains("properties")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("properties");
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          String key = parser.getName();
          String value = parser.nextText().trim();
          profile.addProperty(key, value);
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return profile;
  } // -- Profile parseProfile(String, XmlPullParser, boolean, String)

  /**
   * Method parseRelocation
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Relocation parseRelocation(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Relocation relocation = new Relocation();
    relocation.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("groupId")) {
        if (parsed.contains("groupId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("groupId");
        relocation.setGroupId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("artifactId")) {
        if (parsed.contains("artifactId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("artifactId");
        relocation.setArtifactId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("version")) {
        if (parsed.contains("version")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("version");
        relocation.setVersion(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("message")) {
        if (parsed.contains("message")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("message");
        relocation.setMessage(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return relocation;
  } // -- Relocation parseRelocation(String, XmlPullParser, boolean, String)

  /**
   * Method parseReportPlugin
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private ReportPlugin parseReportPlugin(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    ReportPlugin reportPlugin = new ReportPlugin();
    reportPlugin.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("groupId")) {
        if (parsed.contains("groupId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("groupId");
        reportPlugin.setGroupId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("artifactId")) {
        if (parsed.contains("artifactId")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("artifactId");
        reportPlugin.setArtifactId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("version")) {
        if (parsed.contains("version")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("version");
        reportPlugin.setVersion(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("inherited")) {
        if (parsed.contains("inherited")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("inherited");
        reportPlugin.setInherited(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("configuration")) {
        if (parsed.contains("configuration")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("configuration");
        reportPlugin.setConfiguration(Xpp3DomBuilder.build(parser));
      } else if (parser.getName().equals("reportSets")) {
        if (parsed.contains("reportSets")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("reportSets");
        java.util.List<ReportSet> reportSets = new java.util.ArrayList<ReportSet>();
        reportPlugin.setReportSets(reportSets);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("reportSet")) {
            reportSets.add(parseReportSet("reportSet", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return reportPlugin;
  } // -- ReportPlugin parseReportPlugin(String, XmlPullParser, boolean, String)

  /**
   * Method parseReportSet
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private ReportSet parseReportSet(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    ReportSet reportSet = new ReportSet();
    reportSet.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("id")) {
        if (parsed.contains("id")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("id");
        reportSet.setId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("configuration")) {
        if (parsed.contains("configuration")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("configuration");
        reportSet.setConfiguration(Xpp3DomBuilder.build(parser));
      } else if (parser.getName().equals("inherited")) {
        if (parsed.contains("inherited")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("inherited");
        reportSet.setInherited(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("reports")) {
        if (parsed.contains("reports")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("reports");
        java.util.List<String> reports = new java.util.ArrayList<String>();
        reportSet.setReports(reports);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("report")) {
            reports.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return reportSet;
  } // -- ReportSet parseReportSet(String, XmlPullParser, boolean, String)

  /**
   * Method parseReporting
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Reporting parseReporting(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Reporting reporting = new Reporting();
    reporting.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("excludeDefaults")) {
        if (parsed.contains("excludeDefaults")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("excludeDefaults");
        reporting.setExcludeDefaults(getBooleanValue(getTrimmedValue(parser.nextText()), "excludeDefaults", parser));
      } else if (parser.getName().equals("outputDirectory")) {
        if (parsed.contains("outputDirectory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("outputDirectory");
        reporting.setOutputDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("plugins")) {
        if (parsed.contains("plugins")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("plugins");
        java.util.List<ReportPlugin> plugins = new java.util.ArrayList<ReportPlugin>();
        reporting.setPlugins(plugins);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("plugin")) {
            plugins.add(parseReportPlugin("plugin", parser, strict, encoding));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return reporting;
  } // -- Reporting parseReporting(String, XmlPullParser, boolean, String)

  /**
   * Method parseRepository
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Repository parseRepository(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Repository repository = new Repository();
    repository.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("releases")) {
        if (parsed.contains("releases")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("releases");
        repository.setReleases(parseRepositoryPolicy("releases", parser, strict, encoding));
      } else if (parser.getName().equals("snapshots")) {
        if (parsed.contains("snapshots")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("snapshots");
        repository.setSnapshots(parseRepositoryPolicy("snapshots", parser, strict, encoding));
      } else if (parser.getName().equals("id")) {
        if (parsed.contains("id")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("id");
        repository.setId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        repository.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        repository.setUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("layout")) {
        if (parsed.contains("layout")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("layout");
        repository.setLayout(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return repository;
  } // -- Repository parseRepository(String, XmlPullParser, boolean, String)

  /**
   * Method parseRepositoryBase
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private RepositoryBase parseRepositoryBase(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    RepositoryBase repositoryBase = new RepositoryBase();
    repositoryBase.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("id")) {
        if (parsed.contains("id")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("id");
        repositoryBase.setId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        repositoryBase.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        repositoryBase.setUrl(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("layout")) {
        if (parsed.contains("layout")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("layout");
        repositoryBase.setLayout(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return repositoryBase;
  } // -- RepositoryBase parseRepositoryBase(String, XmlPullParser, boolean,
    // String)

  /**
   * Method parseRepositoryPolicy
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private RepositoryPolicy parseRepositoryPolicy(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    RepositoryPolicy repositoryPolicy = new RepositoryPolicy();
    repositoryPolicy.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("enabled")) {
        if (parsed.contains("enabled")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("enabled");
        repositoryPolicy.setEnabled(getBooleanValue(getTrimmedValue(parser.nextText()), "enabled", parser));
      } else if (parser.getName().equals("updatePolicy")) {
        if (parsed.contains("updatePolicy")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("updatePolicy");
        repositoryPolicy.setUpdatePolicy(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("checksumPolicy")) {
        if (parsed.contains("checksumPolicy")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("checksumPolicy");
        repositoryPolicy.setChecksumPolicy(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return repositoryPolicy;
  } // -- RepositoryPolicy parseRepositoryPolicy(String, XmlPullParser, boolean,
    // String)

  /**
   * Method parseResource
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Resource parseResource(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException,
      XmlPullParserException {
    Resource resource = new Resource();
    resource.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("targetPath")) {
        if (parsed.contains("targetPath")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("targetPath");
        resource.setTargetPath(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("filtering")) {
        if (parsed.contains("filtering")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("filtering");
        resource.setFiltering(getBooleanValue(getTrimmedValue(parser.nextText()), "filtering", parser));
      } else if (parser.getName().equals("directory")) {
        if (parsed.contains("directory")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("directory");
        resource.setDirectory(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("includes")) {
        if (parsed.contains("includes")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("includes");
        java.util.List<String> includes = new java.util.ArrayList<String>();
        resource.setIncludes(includes);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("include")) {
            includes.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else if (parser.getName().equals("excludes")) {
        if (parsed.contains("excludes")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("excludes");
        java.util.List<String> excludes = new java.util.ArrayList<String>();
        resource.setExcludes(excludes);
        while (parser.nextTag() == XmlPullParser.START_TAG) {
          if (parser.getName().equals("exclude")) {
            excludes.add(getTrimmedValue(parser.nextText()));
          } else {
            parser.nextText();
          }
        }
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return resource;
  } // -- Resource parseResource(String, XmlPullParser, boolean, String)

  /**
   * Method parseScm
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Scm parseScm(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException, XmlPullParserException {
    Scm scm = new Scm();
    scm.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("connection")) {
        if (parsed.contains("connection")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("connection");
        scm.setConnection(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("developerConnection")) {
        if (parsed.contains("developerConnection")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("developerConnection");
        scm.setDeveloperConnection(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("tag")) {
        if (parsed.contains("tag")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("tag");
        scm.setTag(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        scm.setUrl(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return scm;
  } // -- Scm parseScm(String, XmlPullParser, boolean, String)

  /**
   * Method parseSite
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private Site parseSite(String tagName, XmlPullParser parser, boolean strict, String encoding) throws IOException, XmlPullParserException {
    Site site = new Site();
    site.setModelEncoding(encoding);
    java.util.Set<String> parsed = new java.util.HashSet<String>();
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (parser.getName().equals("id")) {
        if (parsed.contains("id")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("id");
        site.setId(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("name")) {
        if (parsed.contains("name")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("name");
        site.setName(getTrimmedValue(parser.nextText()));
      } else if (parser.getName().equals("url")) {
        if (parsed.contains("url")) {
          throw new XmlPullParserException("Duplicated tag: '" + parser.getName() + "'", parser, null);
        }
        parsed.add("url");
        site.setUrl(getTrimmedValue(parser.nextText()));
      } else {
        if (strict) {
          throw new XmlPullParserException("Unrecognised tag: '" + parser.getName() + "'", parser, null);
        }
      }
    }
    return site;
  } // -- Site parseSite(String, XmlPullParser, boolean, String)

  /**
   * Method read
   * 
   * @param reader
   * @param strict
   */
  public List<StructureConventionsViolation> read(Reader reader, boolean strict, boolean checkSkipLine, boolean checkTabSpaced,
      MavenConventions mavenConventions) throws IOException, XmlPullParserException {
    XmlPullParser parser = initParser(reader);
    String encoding = parser.getInputEncoding();

    return parseModel("project", parser, strict, encoding, checkSkipLine, checkTabSpaced, mavenConventions);
  } // -- List<StructureConventionsViolation> read(Reader, boolean)

  private XmlPullParser initParser(Reader reader) throws XmlPullParserException, IOException {
    XmlPullParser parser = new MXParser();

    parser.setInput(reader);

    if (true) {
      // ----------------------------------------------------------------------
      // Latin 1 entities
      // ----------------------------------------------------------------------

      parser.defineEntityReplacementText("nbsp", "\u00a0");
      parser.defineEntityReplacementText("iexcl", "\u00a1");
      parser.defineEntityReplacementText("cent", "\u00a2");
      parser.defineEntityReplacementText("pound", "\u00a3");
      parser.defineEntityReplacementText("curren", "\u00a4");
      parser.defineEntityReplacementText("yen", "\u00a5");
      parser.defineEntityReplacementText("brvbar", "\u00a6");
      parser.defineEntityReplacementText("sect", "\u00a7");
      parser.defineEntityReplacementText("uml", "\u00a8");
      parser.defineEntityReplacementText("copy", "\u00a9");
      parser.defineEntityReplacementText("ordf", "\u00aa");
      parser.defineEntityReplacementText("laquo", "\u00ab");
      parser.defineEntityReplacementText("not", "\u00ac");
      parser.defineEntityReplacementText("shy", "\u00ad");
      parser.defineEntityReplacementText("reg", "\u00ae");
      parser.defineEntityReplacementText("macr", "\u00af");
      parser.defineEntityReplacementText("deg", "\u00b0");
      parser.defineEntityReplacementText("plusmn", "\u00b1");
      parser.defineEntityReplacementText("sup2", "\u00b2");
      parser.defineEntityReplacementText("sup3", "\u00b3");
      parser.defineEntityReplacementText("acute", "\u00b4");
      parser.defineEntityReplacementText("micro", "\u00b5");
      parser.defineEntityReplacementText("para", "\u00b6");
      parser.defineEntityReplacementText("middot", "\u00b7");
      parser.defineEntityReplacementText("cedil", "\u00b8");
      parser.defineEntityReplacementText("sup1", "\u00b9");
      parser.defineEntityReplacementText("ordm", "\u00ba");
      parser.defineEntityReplacementText("raquo", "\u00bb");
      parser.defineEntityReplacementText("frac14", "\u00bc");
      parser.defineEntityReplacementText("frac12", "\u00bd");
      parser.defineEntityReplacementText("frac34", "\u00be");
      parser.defineEntityReplacementText("iquest", "\u00bf");
      parser.defineEntityReplacementText("Agrave", "\u00c0");
      parser.defineEntityReplacementText("Aacute", "\u00c1");
      parser.defineEntityReplacementText("Acirc", "\u00c2");
      parser.defineEntityReplacementText("Atilde", "\u00c3");
      parser.defineEntityReplacementText("Auml", "\u00c4");
      parser.defineEntityReplacementText("Aring", "\u00c5");
      parser.defineEntityReplacementText("AElig", "\u00c6");
      parser.defineEntityReplacementText("Ccedil", "\u00c7");
      parser.defineEntityReplacementText("Egrave", "\u00c8");
      parser.defineEntityReplacementText("Eacute", "\u00c9");
      parser.defineEntityReplacementText("Ecirc", "\u00ca");
      parser.defineEntityReplacementText("Euml", "\u00cb");
      parser.defineEntityReplacementText("Igrave", "\u00cc");
      parser.defineEntityReplacementText("Iacute", "\u00cd");
      parser.defineEntityReplacementText("Icirc", "\u00ce");
      parser.defineEntityReplacementText("Iuml", "\u00cf");
      parser.defineEntityReplacementText("ETH", "\u00d0");
      parser.defineEntityReplacementText("Ntilde", "\u00d1");
      parser.defineEntityReplacementText("Ograve", "\u00d2");
      parser.defineEntityReplacementText("Oacute", "\u00d3");
      parser.defineEntityReplacementText("Ocirc", "\u00d4");
      parser.defineEntityReplacementText("Otilde", "\u00d5");
      parser.defineEntityReplacementText("Ouml", "\u00d6");
      parser.defineEntityReplacementText("times", "\u00d7");
      parser.defineEntityReplacementText("Oslash", "\u00d8");
      parser.defineEntityReplacementText("Ugrave", "\u00d9");
      parser.defineEntityReplacementText("Uacute", "\u00da");
      parser.defineEntityReplacementText("Ucirc", "\u00db");
      parser.defineEntityReplacementText("Uuml", "\u00dc");
      parser.defineEntityReplacementText("Yacute", "\u00dd");
      parser.defineEntityReplacementText("THORN", "\u00de");
      parser.defineEntityReplacementText("szlig", "\u00df");
      parser.defineEntityReplacementText("agrave", "\u00e0");
      parser.defineEntityReplacementText("aacute", "\u00e1");
      parser.defineEntityReplacementText("acirc", "\u00e2");
      parser.defineEntityReplacementText("atilde", "\u00e3");
      parser.defineEntityReplacementText("auml", "\u00e4");
      parser.defineEntityReplacementText("aring", "\u00e5");
      parser.defineEntityReplacementText("aelig", "\u00e6");
      parser.defineEntityReplacementText("ccedil", "\u00e7");
      parser.defineEntityReplacementText("egrave", "\u00e8");
      parser.defineEntityReplacementText("eacute", "\u00e9");
      parser.defineEntityReplacementText("ecirc", "\u00ea");
      parser.defineEntityReplacementText("euml", "\u00eb");
      parser.defineEntityReplacementText("igrave", "\u00ec");
      parser.defineEntityReplacementText("iacute", "\u00ed");
      parser.defineEntityReplacementText("icirc", "\u00ee");
      parser.defineEntityReplacementText("iuml", "\u00ef");
      parser.defineEntityReplacementText("eth", "\u00f0");
      parser.defineEntityReplacementText("ntilde", "\u00f1");
      parser.defineEntityReplacementText("ograve", "\u00f2");
      parser.defineEntityReplacementText("oacute", "\u00f3");
      parser.defineEntityReplacementText("ocirc", "\u00f4");
      parser.defineEntityReplacementText("otilde", "\u00f5");
      parser.defineEntityReplacementText("ouml", "\u00f6");
      parser.defineEntityReplacementText("divide", "\u00f7");
      parser.defineEntityReplacementText("oslash", "\u00f8");
      parser.defineEntityReplacementText("ugrave", "\u00f9");
      parser.defineEntityReplacementText("uacute", "\u00fa");
      parser.defineEntityReplacementText("ucirc", "\u00fb");
      parser.defineEntityReplacementText("uuml", "\u00fc");
      parser.defineEntityReplacementText("yacute", "\u00fd");
      parser.defineEntityReplacementText("thorn", "\u00fe");
      parser.defineEntityReplacementText("yuml", "\u00ff");

      // ----------------------------------------------------------------------
      // Special entities
      // ----------------------------------------------------------------------

      parser.defineEntityReplacementText("OElig", "\u0152");
      parser.defineEntityReplacementText("oelig", "\u0153");
      parser.defineEntityReplacementText("Scaron", "\u0160");
      parser.defineEntityReplacementText("scaron", "\u0161");
      parser.defineEntityReplacementText("Yuml", "\u0178");
      parser.defineEntityReplacementText("circ", "\u02c6");
      parser.defineEntityReplacementText("tilde", "\u02dc");
      parser.defineEntityReplacementText("ensp", "\u2002");
      parser.defineEntityReplacementText("emsp", "\u2003");
      parser.defineEntityReplacementText("thinsp", "\u2009");
      parser.defineEntityReplacementText("zwnj", "\u200c");
      parser.defineEntityReplacementText("zwj", "\u200d");
      parser.defineEntityReplacementText("lrm", "\u200e");
      parser.defineEntityReplacementText("rlm", "\u200f");
      parser.defineEntityReplacementText("ndash", "\u2013");
      parser.defineEntityReplacementText("mdash", "\u2014");
      parser.defineEntityReplacementText("lsquo", "\u2018");
      parser.defineEntityReplacementText("rsquo", "\u2019");
      parser.defineEntityReplacementText("sbquo", "\u201a");
      parser.defineEntityReplacementText("ldquo", "\u201c");
      parser.defineEntityReplacementText("rdquo", "\u201d");
      parser.defineEntityReplacementText("bdquo", "\u201e");
      parser.defineEntityReplacementText("dagger", "\u2020");
      parser.defineEntityReplacementText("Dagger", "\u2021");
      parser.defineEntityReplacementText("permil", "\u2030");
      parser.defineEntityReplacementText("lsaquo", "\u2039");
      parser.defineEntityReplacementText("rsaquo", "\u203a");
      parser.defineEntityReplacementText("euro", "\u20ac");

      // ----------------------------------------------------------------------
      // Symbol entities
      // ----------------------------------------------------------------------

      parser.defineEntityReplacementText("fnof", "\u0192");
      parser.defineEntityReplacementText("Alpha", "\u0391");
      parser.defineEntityReplacementText("Beta", "\u0392");
      parser.defineEntityReplacementText("Gamma", "\u0393");
      parser.defineEntityReplacementText("Delta", "\u0394");
      parser.defineEntityReplacementText("Epsilon", "\u0395");
      parser.defineEntityReplacementText("Zeta", "\u0396");
      parser.defineEntityReplacementText("Eta", "\u0397");
      parser.defineEntityReplacementText("Theta", "\u0398");
      parser.defineEntityReplacementText("Iota", "\u0399");
      parser.defineEntityReplacementText("Kappa", "\u039a");
      parser.defineEntityReplacementText("Lambda", "\u039b");
      parser.defineEntityReplacementText("Mu", "\u039c");
      parser.defineEntityReplacementText("Nu", "\u039d");
      parser.defineEntityReplacementText("Xi", "\u039e");
      parser.defineEntityReplacementText("Omicron", "\u039f");
      parser.defineEntityReplacementText("Pi", "\u03a0");
      parser.defineEntityReplacementText("Rho", "\u03a1");
      parser.defineEntityReplacementText("Sigma", "\u03a3");
      parser.defineEntityReplacementText("Tau", "\u03a4");
      parser.defineEntityReplacementText("Upsilon", "\u03a5");
      parser.defineEntityReplacementText("Phi", "\u03a6");
      parser.defineEntityReplacementText("Chi", "\u03a7");
      parser.defineEntityReplacementText("Psi", "\u03a8");
      parser.defineEntityReplacementText("Omega", "\u03a9");
      parser.defineEntityReplacementText("alpha", "\u03b1");
      parser.defineEntityReplacementText("beta", "\u03b2");
      parser.defineEntityReplacementText("gamma", "\u03b3");
      parser.defineEntityReplacementText("delta", "\u03b4");
      parser.defineEntityReplacementText("epsilon", "\u03b5");
      parser.defineEntityReplacementText("zeta", "\u03b6");
      parser.defineEntityReplacementText("eta", "\u03b7");
      parser.defineEntityReplacementText("theta", "\u03b8");
      parser.defineEntityReplacementText("iota", "\u03b9");
      parser.defineEntityReplacementText("kappa", "\u03ba");
      parser.defineEntityReplacementText("lambda", "\u03bb");
      parser.defineEntityReplacementText("mu", "\u03bc");
      parser.defineEntityReplacementText("nu", "\u03bd");
      parser.defineEntityReplacementText("xi", "\u03be");
      parser.defineEntityReplacementText("omicron", "\u03bf");
      parser.defineEntityReplacementText("pi", "\u03c0");
      parser.defineEntityReplacementText("rho", "\u03c1");
      parser.defineEntityReplacementText("sigmaf", "\u03c2");
      parser.defineEntityReplacementText("sigma", "\u03c3");
      parser.defineEntityReplacementText("tau", "\u03c4");
      parser.defineEntityReplacementText("upsilon", "\u03c5");
      parser.defineEntityReplacementText("phi", "\u03c6");
      parser.defineEntityReplacementText("chi", "\u03c7");
      parser.defineEntityReplacementText("psi", "\u03c8");
      parser.defineEntityReplacementText("omega", "\u03c9");
      parser.defineEntityReplacementText("thetasym", "\u03d1");
      parser.defineEntityReplacementText("upsih", "\u03d2");
      parser.defineEntityReplacementText("piv", "\u03d6");
      parser.defineEntityReplacementText("bull", "\u2022");
      parser.defineEntityReplacementText("hellip", "\u2026");
      parser.defineEntityReplacementText("prime", "\u2032");
      parser.defineEntityReplacementText("Prime", "\u2033");
      parser.defineEntityReplacementText("oline", "\u203e");
      parser.defineEntityReplacementText("frasl", "\u2044");
      parser.defineEntityReplacementText("weierp", "\u2118");
      parser.defineEntityReplacementText("image", "\u2111");
      parser.defineEntityReplacementText("real", "\u211c");
      parser.defineEntityReplacementText("trade", "\u2122");
      parser.defineEntityReplacementText("alefsym", "\u2135");
      parser.defineEntityReplacementText("larr", "\u2190");
      parser.defineEntityReplacementText("uarr", "\u2191");
      parser.defineEntityReplacementText("rarr", "\u2192");
      parser.defineEntityReplacementText("darr", "\u2193");
      parser.defineEntityReplacementText("harr", "\u2194");
      parser.defineEntityReplacementText("crarr", "\u21b5");
      parser.defineEntityReplacementText("lArr", "\u21d0");
      parser.defineEntityReplacementText("uArr", "\u21d1");
      parser.defineEntityReplacementText("rArr", "\u21d2");
      parser.defineEntityReplacementText("dArr", "\u21d3");
      parser.defineEntityReplacementText("hArr", "\u21d4");
      parser.defineEntityReplacementText("forall", "\u2200");
      parser.defineEntityReplacementText("part", "\u2202");
      parser.defineEntityReplacementText("exist", "\u2203");
      parser.defineEntityReplacementText("empty", "\u2205");
      parser.defineEntityReplacementText("nabla", "\u2207");
      parser.defineEntityReplacementText("isin", "\u2208");
      parser.defineEntityReplacementText("notin", "\u2209");
      parser.defineEntityReplacementText("ni", "\u220b");
      parser.defineEntityReplacementText("prod", "\u220f");
      parser.defineEntityReplacementText("sum", "\u2211");
      parser.defineEntityReplacementText("minus", "\u2212");
      parser.defineEntityReplacementText("lowast", "\u2217");
      parser.defineEntityReplacementText("radic", "\u221a");
      parser.defineEntityReplacementText("prop", "\u221d");
      parser.defineEntityReplacementText("infin", "\u221e");
      parser.defineEntityReplacementText("ang", "\u2220");
      parser.defineEntityReplacementText("and", "\u2227");
      parser.defineEntityReplacementText("or", "\u2228");
      parser.defineEntityReplacementText("cap", "\u2229");
      parser.defineEntityReplacementText("cup", "\u222a");
      parser.defineEntityReplacementText("int", "\u222b");
      parser.defineEntityReplacementText("there4", "\u2234");
      parser.defineEntityReplacementText("sim", "\u223c");
      parser.defineEntityReplacementText("cong", "\u2245");
      parser.defineEntityReplacementText("asymp", "\u2248");
      parser.defineEntityReplacementText("ne", "\u2260");
      parser.defineEntityReplacementText("equiv", "\u2261");
      parser.defineEntityReplacementText("le", "\u2264");
      parser.defineEntityReplacementText("ge", "\u2265");
      parser.defineEntityReplacementText("sub", "\u2282");
      parser.defineEntityReplacementText("sup", "\u2283");
      parser.defineEntityReplacementText("nsub", "\u2284");
      parser.defineEntityReplacementText("sube", "\u2286");
      parser.defineEntityReplacementText("supe", "\u2287");
      parser.defineEntityReplacementText("oplus", "\u2295");
      parser.defineEntityReplacementText("otimes", "\u2297");
      parser.defineEntityReplacementText("perp", "\u22a5");
      parser.defineEntityReplacementText("sdot", "\u22c5");
      parser.defineEntityReplacementText("lceil", "\u2308");
      parser.defineEntityReplacementText("rceil", "\u2309");
      parser.defineEntityReplacementText("lfloor", "\u230a");
      parser.defineEntityReplacementText("rfloor", "\u230b");
      parser.defineEntityReplacementText("lang", "\u2329");
      parser.defineEntityReplacementText("rang", "\u232a");
      parser.defineEntityReplacementText("loz", "\u25ca");
      parser.defineEntityReplacementText("spades", "\u2660");
      parser.defineEntityReplacementText("clubs", "\u2663");
      parser.defineEntityReplacementText("hearts", "\u2665");
      parser.defineEntityReplacementText("diams", "\u2666");

    }

    parser.next();
    return parser;
  }

  /**
   * Method read. Parse pom file and loos if Maven conventions (tags order, skip
   * lines) are OK.
   * 
   * @param reader
   * @param checkSkipLine
   * @param mavenConventions
   */
  public List<StructureConventionsViolation> read(Reader reader, boolean checkSkipLine, boolean checkTabSpaced,
      MavenConventions mavenConventions) throws IOException, XmlPullParserException {
    return read(reader, true, checkSkipLine, checkTabSpaced, mavenConventions);
  } // -- List<StructureConventionsViolation> read(Reader)

  /**
   * Build a list which contains dependency object and line number where this
   * dependency is declared in pom file.
   * 
   * @param reader
   *          Reader.
   * @return List which contains dependency object and line number where this
   *         dependency is declared in pom file.
   * @throws XmlPullParserException
   *           Exception if problem occured during parsing.
   * @throws IOException
   */
  public List<DependencyLocation> buildDependencyLineStructure(Reader reader) throws XmlPullParserException, IOException {
    XmlPullParser parser = initParser(reader);
    String encoding = parser.getInputEncoding();
    return buildDependencyLineStructure("project", parser, encoding);
  }

  /**
   * Build a list which contains dependency object and line number where this
   * dependency is declared in pom file.
   * 
   * @param tagName
   *          Root tag.
   * @param parser
   *          Parser used.
   * @param encoding
   *          File encoding.
   * @return List which contains dependency object and line number where this
   *         dependency is declared in pom file.
   * @throws XmlPullParserException
   *           Exception if problem occured during parsing.
   * @throws IOException
   */
  private List<DependencyLocation> buildDependencyLineStructure(String tagName, XmlPullParser parser, String encoding) throws IOException,
      XmlPullParserException {
    List<DependencyLocation> dependencyLine = new ArrayList<DependencyLocation>();
    boolean inRoot = true;
    boolean inDependencies = false;
    boolean inDependency = true;
    boolean inExclusions = false;
    int eventType = parser.getEventType();
    boolean foundRoot = false;
    int currentDependencyLine = 0;
    Dependency currentDependency = null;
    while (eventType != XmlPullParser.END_DOCUMENT) {
      if (eventType == XmlPullParser.START_TAG) {
        if (parser.getName().equals(tagName)) {
          foundRoot = true;
        } else if (inRoot) {
          if (parser.getName().equals("dependencies")) {
            inRoot = false;
            inDependencies = true;
          } else {
            inDependencies = false;
          }
        } else if (inDependencies) {
          if (parser.getName().equals("dependency")) {
            inDependencies = false;
            inDependency = true;
            currentDependency = new Dependency();
            currentDependencyLine = parser.getLineNumber();
          }
        } else if (inDependency) {
          // currentDependency = parseDependency(parser.getName(), parser,
          // false, encoding);
          if (parser.getName().equals("exclusions")) {
            inExclusions = true;
          } else {
            if (!inExclusions) {
              if (parser.getName().equals("groupId")) {
                currentDependency.setGroupId(getTrimmedValue(parser.nextText()));
              } else if (parser.getName().equals("artifactId")) {
                currentDependency.setArtifactId(getTrimmedValue(parser.nextText()));
              } else if (parser.getName().equals("version")) {
                currentDependency.setVersion(getTrimmedValue(parser.nextText()));
              } else if (parser.getName().equals("exclusions")) {
                inExclusions = true;
              }
            }
          }
        }
      } else if (eventType == XmlPullParser.END_TAG) {
        if (inDependency) {
          if (parser.getName().equals("dependency")) {
            DependencyLocation dependencyLocation = new DependencyLocation();
            dependencyLocation.setDependency(currentDependency);
            dependencyLocation.setLine(currentDependencyLine);
            dependencyLine.add(dependencyLocation);
            inDependency = false;
            inDependencies = true;
          } else if (parser.getName().equals("exclusions")) {
            inExclusions = false;
          }
        } else if (inDependencies) {
          if (parser.getName().equals("dependencies")) {
            inDependencies = false;
            inRoot = true;
          }
        }

      }
      eventType = parser.next();
    }
    return dependencyLine;
  } // -- List<DependencyLocation> buildDependencyLineStructure(String,
    // XmlPullParser, String)

}
