package com.serli.maven.plugin.quality.model.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

import com.serli.maven.plugin.quality.util.Util;

@XmlRootElement(name ="groupId")
public class GroupId extends TagPatterned {

  public boolean isPrefixOk(String groupId) {
    boolean result = false;
    
    for (String prefix : Util.getAvailablesGroupIdPrefix()) {
      if (groupId.startsWith(prefix + ".")) {
        result = true;
        break;
      }
    }
    
    return result;
  }
  

  
  
}
