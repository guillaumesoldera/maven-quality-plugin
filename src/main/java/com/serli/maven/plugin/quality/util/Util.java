package com.serli.maven.plugin.quality.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.validator.UrlValidator;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import com.serli.maven.plugin.quality.model.DependencyLocation;
import com.serli.maven.plugin.quality.model.jaxb.FormattingConventions;
import com.serli.maven.plugin.quality.model.jaxb.Group;
import com.serli.maven.plugin.quality.model.jaxb.MavenConventions;
import com.serli.maven.plugin.quality.model.jaxb.Tag;

public final class Util {

  private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] { "http", "https" });
  
  private Util() {

  }

  public static List<Integer> getDefinitionsLine(List<DependencyLocation> dependencyLocationList, Dependency dependency) {
    List<Integer> definitionsLine = new ArrayList<Integer>();

    for (DependencyLocation dependencyLocation : dependencyLocationList) {
      int line = getLine(dependencyLocation, dependency);
      if (line != -1) {
        definitionsLine.add(dependencyLocation.getLine());
      }
    }

    return definitionsLine;
  }

  private static int getLine(DependencyLocation dependencyLocation, Dependency dependency) {
    int result = -1;
    Dependency dependencySaved = dependencyLocation.getDependency();
    String groupId = dependencySaved.getGroupId();
    String artifactId = dependencySaved.getArtifactId();
    if (dependency.getGroupId().equals(groupId) && dependency.getArtifactId().equals(artifactId)) {
      result = dependencyLocation.getLine();
    }
    return result;
  }

  public static int getLastDefinitionLine(List<DependencyLocation> dependencyLocationList, Dependency dependency) {
    int result = -1;
    for (int i = dependencyLocationList.size() - 1; i >= 0; i--) {
      int line = getLine(dependencyLocationList.get(i), dependency);
      if (line != -1) {
        result = line;
        break;
      }
    }
    return result;
  }

  public static int getLastDefinitionLine(List<DependencyLocation> dependencyLocationList, String groupId, String artifactId) {
    Dependency dependency = new Dependency();
    dependency.setArtifactId(artifactId);
    dependency.setGroupId(groupId);
    return getLastDefinitionLine(dependencyLocationList, dependency);
  }

  public static MavenConventions getMavenConventions(String conventionsMavenFileName) throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance(new Class[] { MavenConventions.class, FormattingConventions.class, Group.class, Tag.class });
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    InputStream mavenConventions = Thread.currentThread().getContextClassLoader().getResourceAsStream(conventionsMavenFileName);
    MavenConventions conventions = (MavenConventions) unmarshaller.unmarshal(mavenConventions);
    return conventions;
  }

  public static String buildOrderViolationMessage(int lineNumber, String tagName, String previousTagName) {

    return MessageFormat
        .format("The tag {0} sould be before tag {1} (Line {2,number, integer}).", tagName, previousTagName, lineNumber);

  }

  public static String buildTabSpacedViolationMessage(int lineNumber, String tagName, int tabSpaceReal, int tabSpacedWanted) {
    return MessageFormat
        .format(
            "There is {1,number, integer} space indents before tag {0}. The wanted value is {2,number, integer}. (Line {3,number, integer}).",
            tagName, tabSpaceReal, tabSpacedWanted, lineNumber);
  }

  public static String buildSkipLineViolationMessage(int lineNumber, String tagName, int skipLineReal, int skipLineWanted) {
    return MessageFormat.format(
        "There is {1,number, integer} line skipped after tag {0}. The wanted value is {2,number, integer}. (Line {3,number, integer}).",
        tagName, skipLineReal, skipLineWanted, lineNumber);
  }
  
  public static String buildNamingViolationMessage(int lineNumber, String tag, String pattern) {
    return MessageFormat.format(
        "{0} should match with pattern {1}. (Line {2,number, integer}).",
        tag, pattern, lineNumber);
  }
  
  public static String buildPrefixNamingViolationMessage(int lineNumber, String tag) {
    return MessageFormat.format(
        "{0} should start with top level domain names (com, edu, gov, mil, net, org) or one of the English two-letter codes identifying countries as specified in ISO Standard 3166. (Line {1,number, integer}).",
        tag, lineNumber);
  }
  
  
  
  /**
   * Write a string in outputFile. If outputFile is null, write in log.
   * 
   * @param pString
   *          String to write.
   * @param outputFile File to write.
   * @throws IOException
   */
  public static void writeFile(String pString, File outputFile, Log log) throws IOException {
    if (outputFile != null) {
      FileWriter fw = new FileWriter(outputFile, true);
      BufferedWriter output = new BufferedWriter(fw);
      output.write(pString);
      output.flush();
      output.close();
    } else {
      log.info(pString);
    }
  }
  
  /**
   * @param url
   *          not null
   * @return <code>true</code> if the url is valid, <code>false</code>
   *         otherwise.
   */
  public static boolean isArtifactUrlValid(String url) {
    if (StringUtils.isEmpty(url)) {
      return false;
    }

    return URL_VALIDATOR.isValid(url);
  }
  
  public static boolean isMatching(String pattern, String string) {
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(string);
    return m.matches();
  }
  
  public static List<String> getAvailablesGroupIdPrefix() {
    //com, edu, gov, mil, net, org
    List<String> result = new ArrayList<String>();
    result.add("com");
    result.add("edu");
    result.add("gov");
    result.add("mil");
    result.add("net");
    result.add("org");
    String[] isoCountries = Locale.getISOCountries();
    for (String isoCountry : isoCountries) {
      result.add(isoCountry.toLowerCase());
    }
    return result;
  }
}
