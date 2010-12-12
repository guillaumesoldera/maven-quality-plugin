package com.serli.maven.plugin.quality.model.jaxb;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.namespace.QName;

import com.serli.maven.plugin.quality.util.Util;

public class TagPatterned {

 private Map<QName,Object> any;
  
  @XmlAnyAttribute
  public Map<QName,Object> getAny(){
      if( any == null ){
          any = new HashMap<QName,Object>();
      }
      return any;
  }
  
  public String getPattern() {
    return (String) any.get(new QName("format"));
  }
  
  public boolean isMatching(String string) {
    return Util.isMatching(getPattern(), string);
  }
}
