package com.serli.maven.plugin.quality.model;

import org.apache.maven.project.MavenProject;

public class MavenProjectComparable extends org.apache.maven.project.MavenProject implements Comparable<MavenProjectComparable> {

  public MavenProjectComparable(MavenProject project) {
    super(project.getModel());
  }
  
  public int compareTo(MavenProjectComparable o) {
    return getId().compareTo(o.getId());
  }

}
