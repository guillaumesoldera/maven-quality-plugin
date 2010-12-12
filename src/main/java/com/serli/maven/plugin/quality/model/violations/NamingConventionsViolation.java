package com.serli.maven.plugin.quality.model.violations;

public class NamingConventionsViolation extends MavenConventionsViolation {

  public NamingConventionsViolation() {
    super();
  }
  
  public NamingConventionsViolation(String tagName, int lineNumber, String message) {
    super(tagName, lineNumber, message);
  }
  
}
