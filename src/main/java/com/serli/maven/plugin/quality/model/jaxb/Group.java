package com.serli.maven.plugin.quality.model.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Group {

  private String name;
  
  private List<Tag> listTags;

  public Group() {
  }
  
  
  public Group(String name, List<Tag> listTags) {
    super();
    this.name = name;
    this.listTags = listTags;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlElementWrapper(name = "tags")
  @XmlElement(name = "tag")
  public List<Tag> getListTags() {
    return listTags;
  }

  public void setListTags(List<Tag> listTags) {
    this.listTags = listTags;
  }
  
  public int getSkipLine() {
    return listTags.get(listTags.size() - 1).getSkipLine();
  }
  
  public boolean contains(String tagName) {
    boolean contained = false;
    for (Tag tag : listTags) {
      if (tag.getTagName().equals(tagName)) {
        contained = true;
        break;
      }
    }
    return contained;
  }
}
