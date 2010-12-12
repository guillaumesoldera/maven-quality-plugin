package com.serli.maven.plugin.quality.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class MavenConventions {

  private NamingConventions namingConventions;
  
  private FormattingConventions formattingConventions;
  
  @XmlTransient
  private List<Tag> listTags;
  
  public MavenConventions() {
    
  }
  
  public MavenConventions(FormattingConventions formatting) {
    this.formattingConventions = formatting;
    
    
    
  }
  
  public List<Tag> getListTags() {
    if (listTags == null) {
      listTags = new ArrayList<Tag>();
      for (Group group : formattingConventions.getListGroups()) {
        listTags.addAll(group.getListTags());
      }
    }
    return listTags;
  }
  
  @XmlElement(name = "formatting")
  public FormattingConventions getFormattingConventions() {
    return formattingConventions;
  }
  
  public void setFormattingConventions(FormattingConventions formattingConventions) {
    this.formattingConventions = formattingConventions;
  }
  
  @XmlElement(name = "naming")
  public NamingConventions getNamingConventions() {
    return namingConventions;
  }
  
  public void setNamingConventions(NamingConventions namingConventions) {
    this.namingConventions = namingConventions;
  }
  
  public int getPosition(String tagName) {
    int result = -1;
    if (getListTags() != null && getListTags().size() > 0) {
      for (int i = 0; i< getListTags().size(); i++) {
        if (getListTags().get(i).getTagName().equals(tagName)) {
          result = i;
          break;
        }
      }
    } 
    
    return result;
  }
  
  public int getSpaceIndent(String tagName) {
    int result = -1;
    if (getListTags() != null && getListTags().size() > 0) {
      for (int i = 0; i< getListTags().size(); i++) {
        if (getListTags().get(i).getTagName().equals(tagName)) {
          result = getListTags().get(i).getSpaceIndent();
        }
      }
    }
    return result;
  }
  
  public int getSkipLine(String tagName) {
    int result = -1;
    if (listTags != null && listTags.size() > 0) {
      for (int i = 0; i< listTags.size(); i++) {
        if (listTags.get(i).getTagName().equals(tagName)) {
          result = listTags.get(i).getSkipLine();
        }
      }
    }
    return result;
  }
  
  public boolean sameGroup(String tagName1, String tagName2) {
    boolean result = false;
    for (Group group : formattingConventions.getListGroups()) {
      if (group.contains(tagName1) && group.contains(tagName2)) {
        result = true;
        break;
      }
    }
    return result;
  }
  
  public Group getGroup(String tagName) {
    Group result = null;
    for (Group group : formattingConventions.getListGroups()) {
      if (group.contains(tagName)) {
        result = group;
        break;
      }
    }
    return result;
  }
}
