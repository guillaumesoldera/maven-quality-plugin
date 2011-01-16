package com.serli.maven.plugin.quality.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File;
import java.util.Locale;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.codehaus.plexus.i18n.I18N;

/**
 * Abstract class used by mojos which generate a report.
 *
 */
public abstract class AbstractMavenQualityMojo extends AbstractMavenReport {

  /**
   * The Maven project to analyze.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * The output directory for the report. Note that this parameter is only evaluated if the goal is run directly from
   * the command line. If the goal is run indirectly as part of a site generation, the output directory configured in
   * the Maven Site Plugin is used instead.
   *
   * @parameter expression="${project.reporting.outputDirectory}"
   * @required
   */
  protected File outputReportDirectory;

  /**
   * Internationalization component.
   *
   * @component
   */
  protected I18N i18n;

  /**
   * Doxia Site Renderer component.
   *
   * @component
   */
  protected Renderer siteRenderer;
  
  protected abstract void execution() throws MojoExecutionException;
  
  protected String getI18nString( Locale locale, String key )
  {
      return i18n.getString( "maven-quality-plugin", locale, "report." + getI18Nsection() + '.' + key );
  }

  protected abstract String getI18Nsection();

  /** {@inheritDoc} */
  public String getName( Locale locale )
  {
      return getI18nString( locale, "name" );
  }

  /** {@inheritDoc} */
  public String getDescription( Locale locale )
  {
      return getI18nString( locale, "description" );
  }

  @Override
  protected Renderer getSiteRenderer() {
    return siteRenderer;
  }
  
  @Override
  protected MavenProject getProject() {
    return project;
  }

  @Override
  protected String getOutputDirectory() {
    return outputReportDirectory.getAbsolutePath();
  }
  
  @Override
  public String getCategoryName() {
    return super.getCategoryName();
  }

  protected void writeCell(Sink sink, String contents) {
    sink.tableCell();
    sink.text(contents);
    sink.tableCell_();
  }

  protected void writeHeaderCell(Sink sink, String contents) {
    sink.tableHeaderCell();
    sink.text(contents);
    sink.tableHeaderCell_();
  }

  protected void writeEnd(Sink sink) {
    sink.section1_();
  }

  protected void writeBegin(Sink sink, String key, Locale locale) {
    sink.section1();
    sink.sectionTitle1();
    sink.anchor(key);
    sink.text(getI18nString(locale, key + ".name"));
    sink.anchor_();
    sink.sectionTitle1_();
    sink.paragraph();
    sink.text(getI18nString(locale, key + ".description"));
    sink.paragraph_();
  }

}
