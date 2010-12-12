package com.serli.maven.plugin.quality.model.violations;

public class MavenConventionsViolation {

  private String tagName;
  
  private int lineNumber;
  
  private String message;
  
  public MavenConventionsViolation() {
    
  }

  
  
  public MavenConventionsViolation(String tagName, int lineNumber, String message) {
    super();
    this.tagName = tagName;
    this.lineNumber = lineNumber;
    this.message = message;
  }



  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
  
}
