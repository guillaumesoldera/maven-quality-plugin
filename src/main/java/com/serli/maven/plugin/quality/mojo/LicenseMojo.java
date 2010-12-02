package com.serli.maven.plugin.quality.mojo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.validator.UrlValidator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Goal which checks licenses and headers.
 * 
 * @goal license
 * @requiresDependencyResolution test
 * @phase verify
 */
public class LicenseMojo extends AbstractMojo {

  /**
   * The Maven project to analyze.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Dependency tree builder component.
   * 
   * @since 2.1
   * @component
   */
  private DependencyTreeBuilder dependencyTreeBuilder;

  /**
   * Local Repository.
   * 
   * @parameter expression="${localRepository}"
   * @required
   * @readonly
   */
  protected ArtifactRepository localRepository;

  /**
   * Artifact metadata source component.
   * 
   * @component
   */
  protected ArtifactMetadataSource artifactMetadataSource;

  /**
   * Artifact collector component.
   * 
   * @component
   */
  private ArtifactCollector collector;

  /**
   * Artifact Factory component.
   * 
   * @component
   */
  protected ArtifactFactory factory;

  /**
   * Maven Project Builder component.
   * 
   * @component
   */
  private MavenProjectBuilder mavenProjectBuilder;

  /**
   * Remote repositories used for the project.
   * 
   * @since 2.1
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @required
   * @readonly
   */
  private List remoteRepositories;

  /**
   * Will be filled with license name / set of projects.
   */
  private Map licenseMap = new HashMap() {
    /** {@inheritDoc} */
    public Object put(Object key, Object value) {
      // handle multiple values as a set to avoid duplicates
      SortedSet valueList = (SortedSet) get(key);
      if (valueList == null) {
        valueList = new TreeSet();
      }
      valueList.add(value);
      return super.put(key, valueList);
    }
  };

  private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] { "http", "https" });

  /** Random used to generate a UID */
  private static SecureRandom RANDOM;

  private List allDependencies;

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      RANDOM = SecureRandom.getInstance("SHA1PRNG");
    } catch (Exception e) {
      e.printStackTrace();
    }
    // TODO s'inspirer de
    // http://maven-license-plugin.googlecode.com/svn/branches/maven-license-plugin-1.x.x/src/main/java/com/google/code/mojo/license/LicenseCheckMojo.java
    // TODO extends de AbstractLicenseMojo, mais ecrire dans un fichier les
    // 'missingHeader'

    // TODO voir aussi le project-info pour avoir les licenses des dependances
    // (determiner la compatibilite des licenses ?)
    // TODO voir comment ils gerent avec le DependenciesRenderer :
    // http://svn.apache.org/viewvc/maven/plugins/tags/maven-project-info-reports-plugin-2.2/src/main/java/org/apache/maven/report/projectinfo/dependencies/renderer/DependenciesRenderer.java?view=markup

    List licenses = project.getModel().getLicenses();
    for (Iterator i = licenses.iterator(); i.hasNext();) {
      License license = (License) i.next();

      String url = license.getUrl();

      URL licenseUrl = null;
      try {
        licenseUrl = getLicenseURL(project, url);
        System.out.println("licenseURL = " + licenseUrl);
      } catch (MalformedURLException e) {
        getLog().error(e.getMessage());
      } catch (IOException e) {
        getLog().error(e.getMessage());
      }

      if (licenseUrl != null && licenseUrl.getProtocol().equals("file")) {
      }

    }
    DependencyNode dependencyTreeNode = resolveProject();
    getAllDependencies(dependencyTreeNode);
    printDependencyListing(dependencyTreeNode);

//    Iterator iterator = licenseMap.keySet().iterator();
//    while (iterator.hasNext()) {
//      System.out.println("-------------------------------------------------");
//      String licenseName = (String) iterator.next();
//      System.out.println("licence : " + licenseName);
//      SortedSet projects = (SortedSet) licenseMap.get(licenseName);
//      StringBuffer buffer = new StringBuffer();
//      Iterator iteratorProjets = projects.iterator();
//      while (iteratorProjets.hasNext()) {
//        String projectName = (String) iteratorProjets.next();
//        buffer.append(projectName);
//        if (iteratorProjets.hasNext()) {
//          buffer.append(",");
//        }
//      }
//    }
    printGroupedLicenses();

    // Dependencies dependencies = new Dependencies( project,
    // dependencyTreeNode, classesAnalyzer );

    // Artifact artifact = node.getArtifact();
    // MavenProject artifactProject = repoUtils.getMavenProjectFromRepository(
    // artifact );
    // String artifactDescription = artifactProject.getDescription();
    // String artifactUrl = artifactProject.getUrl();
    // String artifactName = artifactProject.getName();
    // List licenses2 = artifactProject.getLicenses();

  }

  /**
   * @param project
   *          not null
   * @param url
   *          not null
   * @return a valid URL object from the url string
   * @throws IOException
   *           if any
   */
  protected static URL getLicenseURL(MavenProject project, String url) throws IOException {
    URL licenseUrl = null;
    UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);
    // UrlValidator does not accept file URLs because the file
    // URLs do not contain a valid authority (no hostname).
    // As a workaround accept license URLs that start with the
    // file scheme.
    if (urlValidator.isValid(url) || StringUtils.defaultString(url).startsWith("file://")) {
      try {
        licenseUrl = new URL(url);
      } catch (MalformedURLException e) {
        throw new MalformedURLException("The license url '" + url + "' seems to be invalid: " + e.getMessage());
      }
    } else {
      File licenseFile = new File(project.getBasedir(), url);
      if (!licenseFile.exists()) {
        // Workaround to allow absolute path names while
        // staying compatible with the way it was...
        licenseFile = new File(url);
      }
      if (!licenseFile.exists()) {
        throw new IOException("Maven can't find the file '" + licenseFile + "' on the system.");
      }
      try {
        licenseUrl = licenseFile.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new MalformedURLException("The license url '" + url + "' seems to be invalid: " + e.getMessage());
      }
    }

    return licenseUrl;
  }

  /**
   * @return resolve the dependency tree
   */
  private DependencyNode resolveProject() {
    try {
      ArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_TEST);
      return dependencyTreeBuilder
          .buildDependencyTree(project, localRepository, factory, artifactMetadataSource, artifactFilter, collector);
    } catch (DependencyTreeBuilderException e) {
      getLog().error("Unable to build dependency tree.", e);
      return null;
    }
  }

  private void printDependencyListing(DependencyNode node) {
    Artifact artifact = node.getArtifact();
    String id = artifact.getId();
    String dependencyDetailId = getUUID();
    String imgId = getUUID();

    
    printDescriptionsAndURLs(node, dependencyDetailId);

    if (!node.getChildren().isEmpty()) {
      boolean toBeIncluded = false;
      List subList = new ArrayList();
      for (Iterator deps = node.getChildren().iterator(); deps.hasNext();) {
        DependencyNode dep = (DependencyNode) deps.next();

        if (!allDependencies.contains(dep.getArtifact())) {
          continue;
        }

        subList.add(dep);
        toBeIncluded = true;
      }

      if (toBeIncluded) {
        for (Iterator deps = subList.iterator(); deps.hasNext();) {
          DependencyNode dep = (DependencyNode) deps.next();

          printDependencyListing(dep);
        }
      }
    }

  }

  private void printDescriptionsAndURLs(DependencyNode node, String uid) {
    Artifact artifact = node.getArtifact();
    String id = artifact.getId();
    String unknownLicenseMessage = "licenses inconnues";

    if (!Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) {
      try {
        MavenProject artifactProject = getMavenProjectFromRepository(artifact);
        String artifactDescription = artifactProject.getDescription();
        String artifactUrl = artifactProject.getUrl();
        String artifactName = artifactProject.getName();
        List licenses = artifactProject.getLicenses();

        if (StringUtils.isNotEmpty(artifactDescription)) {
//          System.out.println("artifactDescription : " + artifactDescription);
        }

        if (StringUtils.isNotEmpty(artifactUrl)) {
          if (isArtifactUrlValid(artifactUrl)) {
//            System.out.println("artifactURL : " + artifactUrl);
          } else {
//            System.out.println("artifactURL : " + artifactUrl);
          }
        }

//        System.out.println("licenses title " + ": ");
        if (!licenses.isEmpty()) {
          for (Iterator iter = licenses.iterator(); iter.hasNext();) {
            License element = (License) iter.next();
            String licenseName = element.getName();
            String licenseUrl = element.getUrl();
            
            if (licenseUrl != null) {
//              System.out.println("licenseUrl : " + licenseUrl);
            }
//            System.out.println("licenseName ; " + licenseName);

            licenseMap.put(licenseName, artifactName);
          }
        } else {
//          System.out.println("license nolicense");

          licenseMap.put(unknownLicenseMessage, artifactName);
        }
      } catch (ProjectBuildingException e) {
        getLog().error("ProjectBuildingException error : ", e);
      }
    } else {
//      System.out.println("id : " + id);
//      System.out.println("column.description : ");
//      System.out.println("index nodescription");

      if (artifact.getFile() != null) {
//        System.out.println("column.url " + ": ");
//        System.out.println(artifact.getFile().getAbsolutePath());
      }
    }

  }

  private void printGroupedLicenses() {
    for (Iterator iter = licenseMap.keySet().iterator(); iter.hasNext();) {
      String licenseName = (String) iter.next();
      if (StringUtils.isEmpty(licenseName)) {
        System.out.println("unamed");
      } else {
        System.out.println(licenseName + " : ");
      }

      SortedSet projects = (SortedSet) licenseMap.get(licenseName);

      StringBuffer buffer = new StringBuffer();
      for (Iterator iterator = projects.iterator(); iterator.hasNext();) {
        String projectName = (String) iterator.next();
        buffer.append(projectName);
        if (iterator.hasNext()) {
          buffer.append(", ");
        }
      }
      System.out.println(buffer.toString());

    }
  }

  /**
   * @return a valid HTML ID respecting <a
   *         href="http://www.w3.org/TR/xhtml1/#C_8">XHTML 1.0 section C.8.
   *         Fragment Identifiers</a>
   */
  private static String getUUID() {
    return "_" + Math.abs(RANDOM.nextInt());
  }

  /**
   * Get the <code>Maven project</code> from the repository depending the
   * <code>Artifact</code> given.
   * 
   * @param artifact
   *          an artifact
   * @return the Maven project for the given artifact
   * @throws ProjectBuildingException
   *           if any
   */
  public MavenProject getMavenProjectFromRepository(Artifact artifact) throws ProjectBuildingException {
    Artifact projectArtifact = artifact;

    boolean allowStubModel = false;
    if (!"pom".equals(artifact.getType())) {
      projectArtifact = factory.createProjectArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
          artifact.getScope());
      allowStubModel = true;
    }

    // TODO: we should use the MavenMetadataSource instead
    return mavenProjectBuilder.buildFromRepository(projectArtifact, remoteRepositories, localRepository, allowStubModel);
  }

  /**
   * @return a list of included <code>Artifact</code> returned by the dependency
   *         tree.
   */
  public List getAllDependencies(DependencyNode dependencyTreeNode) {
    if (allDependencies != null) {
      return allDependencies;
    }

    allDependencies = new ArrayList();
    for (Iterator i = dependencyTreeNode.getChildren().iterator(); i.hasNext();) {
      DependencyNode dependencyNode = (DependencyNode) i.next();

      if (dependencyNode.getState() != DependencyNode.INCLUDED) {
        continue;
      }

      if (dependencyNode.getArtifact().getGroupId().equals(project.getGroupId())
          && dependencyNode.getArtifact().getArtifactId().equals(project.getArtifactId())
          && dependencyNode.getArtifact().getVersion().equals(project.getVersion())) {
        continue;
      }

      if (!allDependencies.contains(dependencyNode.getArtifact())) {
        allDependencies.add(dependencyNode.getArtifact());
      }
      getAllDependencies(dependencyNode);
    }

    return allDependencies;
  }

  /**
   * @param url
   *          not null
   * @return <code>true</code> if the url is valid, <code>false</code>
   *         otherwise.
   */
  public static boolean isArtifactUrlValid(String url) {
    if (StringUtils.isEmpty(url)) {
      return false;
    }

    return URL_VALIDATOR.isValid(url);
  }
}
