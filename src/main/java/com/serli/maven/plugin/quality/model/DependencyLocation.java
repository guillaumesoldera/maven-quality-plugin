package com.serli.maven.plugin.quality.model;

import org.apache.maven.model.Dependency;

/**
 * Structure to associate a dependency with its location in pom file.
 * @author Guillaume
 *
 */
public class DependencyLocation {

  /**
   * Dependency.
   */
  private Dependency dependency;
  
  /**
   * Location in pom file.
   */
  private int line;

  public DependencyLocation() {
    
  }
  
  public Dependency getDependency() {
    return dependency;
  }

  public void setDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }
  
  
  
}
