package com.serli.maven.plugin.quality.model;

public class MavenProject extends org.apache.maven.project.MavenProject implements Comparable<MavenProject> {

  public MavenProject(org.apache.maven.project.MavenProject project) {
    super(project.getModel());
  }
  
  public int compareTo(MavenProject o) {
    return getId().compareTo(o.getId());
  }

}
