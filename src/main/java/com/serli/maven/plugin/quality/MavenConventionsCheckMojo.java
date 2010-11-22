package com.serli.maven.plugin.quality;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which checks maven conventions.
 * 
 * @goal check-maven-conventions
 * @requiresDependencyResolution test
 * @phase verify
 */
public class MavenConventionsCheckMojo extends AbstractMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {

  }

}
