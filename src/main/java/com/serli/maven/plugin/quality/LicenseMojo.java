package com.serli.maven.plugin.quality;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which checks licenses and headers.
 * 
 * @goal license
 * @requiresDependencyResolution test
 * @phase verify
 */
public class LicenseMojo extends AbstractMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {
    // TODO s'inspirer de http://maven-license-plugin.googlecode.com/svn/branches/maven-license-plugin-1.x.x/src/main/java/com/google/code/mojo/license/LicenseCheckMojo.java
    // TODO extends de AbstractLicenseMojo, mais ecrire dans un fichier les 'missingHeader'

    // TODO voir aussi le project-info pour avoir les licenses des dependances (determiner la compatibilite des licenses ?)

  }

}
