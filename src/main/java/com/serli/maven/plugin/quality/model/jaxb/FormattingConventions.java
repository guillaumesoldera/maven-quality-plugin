package com.serli.maven.plugin.quality.model.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name ="formatting")
public class FormattingConventions {

private List<Group> listGroups;
  
  public FormattingConventions() {
    
  }
  
  public FormattingConventions(List<Group> groups) {
    this.listGroups = groups;
  }
  
  @XmlElementWrapper(name = "groups")
  @XmlElement(name = "group")
  public List<Group> getListGroups() {
    return listGroups;
  }
  
  public void setListGroups(List<Group> pListGroups) {
    this.listGroups = pListGroups;
  }
}
