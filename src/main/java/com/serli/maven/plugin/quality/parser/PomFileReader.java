package com.serli.maven.plugin.quality.parser;

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.serli.maven.plugin.quality.model.DependencyLocation;
import com.serli.maven.plugin.quality.model.jaxb.GroupId;
import com.serli.maven.plugin.quality.model.jaxb.MavenConventions;
import com.serli.maven.plugin.quality.model.violations.MavenConventionsViolation;
import com.serli.maven.plugin.quality.model.violations.NamingConventionsViolation;
import com.serli.maven.plugin.quality.model.violations.StructureConventionsViolation;
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
   * Method checkMavenConventions.
   * 
   * @param tagName
   * @param encoding
   * @param strict
   * @param parser
   */
  private List<MavenConventionsViolation> checkMavenConventions(String tagName, XmlPullParser parser, boolean strict, String encoding,
      boolean checkSkipLine, boolean checkTabSpaced, MavenConventions mavenConventions) throws IOException, XmlPullParserException {
    List<MavenConventionsViolation> listMavenConventionsViolations = new ArrayList<MavenConventionsViolation>();
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
            listMavenConventionsViolations.add(structureConventionsViolation);
          }

        } else if (parser.getDepth() == 2) {
          
          int positionPrevious = mavenConventions.getPosition(previousTag);
          int positionTag = mavenConventions.getPosition(parser.getName());
          if (positionTag != -1 && positionPrevious != -1) {
            if (positionTag < positionPrevious) {
              String message = Util.buildOrderViolationMessage(parser.getLineNumber(), parser.getName(), previousTag);
              structureConventionsViolation = new StructureConventionsViolation(parser.getName(), parser.getLineNumber(), message);
              listMavenConventionsViolations.add(structureConventionsViolation);
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
              listMavenConventionsViolations.add(structureConventionsViolation);
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
              listMavenConventionsViolations.add(structureConventionsViolation);
            }
          }

          previousTagLineNumber = parser.getLineNumber();
          previousTag = parser.getName();
          
          

          if ("groupId".equals(parser.getName())) {
            String tag = "groupId";
            // we get line number of start tag because 'nextText' method will position parser on end tag
            int groupIdLineNumber = parser.getLineNumber();
            String groupId = parser.nextText();
            // we set linePreviousTagEnd because we are already in end tag
            linePreviousTagEnd = parser.getLineNumber();
            GroupId groupIdConvention = mavenConventions.getNamingConventions().getGroupId();
            boolean groupIdOK = groupIdConvention.isMatching(groupId);
            if (!groupIdOK) {
              String message = Util.buildNamingViolationMessage(groupIdLineNumber, tag, mavenConventions.getNamingConventions().getGroupId().getPattern());
              NamingConventionsViolation namingConventionsViolation = new NamingConventionsViolation(tag, groupIdLineNumber, message);
              listMavenConventionsViolations.add(namingConventionsViolation);
            }
            boolean prefixGroupIdOK = groupIdConvention.isPrefixOk(groupId);
            if (!prefixGroupIdOK) {
              String message = Util.buildPrefixNamingViolationMessage(groupIdLineNumber, tag);
              NamingConventionsViolation namingConventionsViolation = new NamingConventionsViolation(tag, groupIdLineNumber, message);
              listMavenConventionsViolations.add(namingConventionsViolation);
            }
          }
          if ("artifactId".equals(parser.getName())) {
            String tag = "artifactId";
            // we set linePreviousTagEnd because we are already in end tag
            int artifactIdLineNumber = parser.getLineNumber();
            String artifactId = parser.nextText();
            // we set linePreviousTagEnd because we are already in end tag
            linePreviousTagEnd = parser.getLineNumber();
            boolean artifactIdOK = mavenConventions.getNamingConventions().getArtifactId().isMatching(artifactId);
            if (!artifactIdOK) {
              String message = Util.buildNamingViolationMessage(artifactIdLineNumber, tag, mavenConventions.getNamingConventions().getArtifactId().getPattern());
              NamingConventionsViolation namingConventionsViolation = new NamingConventionsViolation(tag, artifactIdLineNumber, message);
              listMavenConventionsViolations.add(namingConventionsViolation);
            }
          }
        }
      } else if (eventType == XmlPullParser.END_TAG) {
        if (parser.getDepth() <= 2) {
          linePreviousTagEnd = parser.getLineNumber();
        }
      }
      eventType = parser.next();
    }
    return listMavenConventionsViolations;
  } // -- List<StructureConventionsViolation> parseModel(String, XmlPullParser, boolean, String, boolean, boolean, MavenConventions)

  /**
   * Method checkMavenConventions
   * 
   * @param reader
   * @param strict
   */
  public List<MavenConventionsViolation> checkMavenConventions(Reader reader, boolean strict, boolean checkSkipLine, boolean checkTabSpaced,
      MavenConventions mavenConventions) throws IOException, XmlPullParserException {
    XmlPullParser parser = initParser(reader);
    String encoding = parser.getInputEncoding();

    return checkMavenConventions("project", parser, strict, encoding, checkSkipLine, checkTabSpaced, mavenConventions);
  } 

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
   * Method checkMavenConventions. Parse pom file and loos if Maven conventions (tags order, skip
   * lines) are OK.
   * 
   * @param reader
   * @param checkSkipLine
   * @param mavenConventions
   */
  public List<MavenConventionsViolation> checkMavenConventions(Reader reader, boolean checkSkipLine, boolean checkTabSpaced,
      MavenConventions mavenConventions) throws IOException, XmlPullParserException {
    return checkMavenConventions(reader, true, checkSkipLine, checkTabSpaced, mavenConventions);
  } 

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
    int currentDependencyLine = 0;
    Dependency currentDependency = null;
    while (eventType != XmlPullParser.END_DOCUMENT) {
      if (eventType == XmlPullParser.START_TAG) {
        if (inRoot) {
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
