package com.serli.maven.plugin.quality.model.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class wich represents a tag in maven-conventions.xml file.
 * Contains tag name, spaced tab and lines number to skip.
 * @author Guillaume
 *
 */
@XmlRootElement
public class Tag {

  private String tagName;
  
  private int spaceIndent;
  
  private int skipLine;

  public Tag() {
    
  }
  
  public Tag(String tagName, int spaceIndent, int skipLine) {
    super();
    this.tagName = tagName;
    this.spaceIndent = spaceIndent;
    this.skipLine = skipLine;
  }




  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public int getSpaceIndent() {
    return spaceIndent;
  }

  public void setSpaceIndent(int spaceIndent) {
    this.spaceIndent = spaceIndent;
  }

  public int getSkipLine() {
    return skipLine;
  }

  public void setSkipLine(int skipLine) {
    this.skipLine = skipLine;
  }

  @Override
  public boolean equals(Object obj) {
    return tagName.equals(((Tag) obj).tagName);
  }
  
}
