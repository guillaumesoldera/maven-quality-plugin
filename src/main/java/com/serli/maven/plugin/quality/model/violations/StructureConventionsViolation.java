package com.serli.maven.plugin.quality.model.violations;


public class StructureConventionsViolation extends MavenConventionsViolation {

  public StructureConventionsViolation() {
    super();
  }

  public StructureConventionsViolation(String tagName, int lineNumber, String message) {
    super(tagName, lineNumber, message);
  }

}
