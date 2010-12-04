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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.plexus.util.StringUtils;

import com.serli.maven.plugin.quality.model.License;
import com.serli.maven.plugin.quality.model.MavenProject;
import com.serli.maven.plugin.quality.util.Util;

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
  private org.apache.maven.project.MavenProject project;

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

  private Map<License, SortedSet<MavenProject>> resultLicenseMap = new HashMap<License, SortedSet<MavenProject>>();
  
  private SortedSet<MavenProject> setArtifactNoLicense = new TreeSet<MavenProject>();
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

  

  /** Random used to generate a UID */
  private static SecureRandom RANDOM;

  private List allDependencies = new ArrayList();

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
//    System.out.println("-------------dependencies--------------------");
//    for (int i = 0; i<allDependencies.size(); i++) {
//      Artifact a = (Artifact) allDependencies.get(i);
//      System.out.println(a.getId());
//    }
    
    printDependencyListing(dependencyTreeNode);

//    printGroupedLicenses();
    printLicenses();

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
  protected static URL getLicenseURL(org.apache.maven.project.MavenProject project, String url) throws IOException {
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
//    System.out.println("id = " + id);
    

    if (!Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) {
      try {
        MavenProject artifactProject = new MavenProject(getMavenProjectFromRepository(artifact));
        String artifactDescription = artifactProject.getDescription();
        String artifactUrl = artifactProject.getUrl();
        List licenses = artifactProject.getLicenses();
        
        
        if (StringUtils.isNotEmpty(artifactDescription)) {
//          System.out.println("artifactDescription : " + artifactDescription);
        }

        if (StringUtils.isNotEmpty(artifactUrl)) {
          if (Util.isArtifactUrlValid(artifactUrl)) {
//            System.out.println("artifactURL : " + artifactUrl);
          } else {
//            System.out.println("artifactURL : " + artifactUrl);
          }
        }

        if (!licenses.isEmpty()) {
          for (Iterator iter = licenses.iterator(); iter.hasNext();) {
            org.apache.maven.model.License element = (org.apache.maven.model.License) iter.next();
            String licenseName = element.getName();
            String licenseUrl = element.getUrl();
            License license = new License();
            if (StringUtils.isEmpty(licenseName)) {
              licenseName = "Unnamed";
            }
            license.setName(licenseName);
            license.setUrl(licenseUrl);
//            System.out.println(artifactProject.getId() + " - ");
//            System.out.println("licenseName : " + licenseName + "   - licenseUrl : " + licenseUrl);
            put(license, artifactProject);
          }
        } else {
          setArtifactNoLicense.add(artifactProject);
        }
      } catch (ProjectBuildingException e) {
        getLog().error("ProjectBuildingException error : ", e);
      }
    }

  }

  private void printLicenses() {
    // TODO afficher au format XML et dans un fichier ou non
    Set<License> keys = resultLicenseMap.keySet();
    for (License license : keys) {
      String url = license.getUrl();
      String name = license.getName();
      if (StringUtils.isEmpty(name)) {
        name = "Unnamed";
      }
      System.out.println("Licence " + name + " - " + url);
      SortedSet<MavenProject> sortedSet = resultLicenseMap.get(license);
      for (MavenProject artifact : sortedSet) {
        System.out.println("\t " + artifact.getId());
      }
      
    }
    
    System.out.println("Inconnu : ");
    for (MavenProject project : setArtifactNoLicense) {
      System.out.println("\t " + project.getId());
    }
    
  }
  
  private void printGroupedLicenses() {
    for (Iterator iter = licenseMap.keySet().iterator(); iter.hasNext();) {
      String licenseName = (String) iter.next();
      if (StringUtils.isEmpty(licenseName)) {
        System.out.println("unnamed");
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
  public org.apache.maven.project.MavenProject getMavenProjectFromRepository(Artifact artifact) throws ProjectBuildingException {
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
//    if (allDependencies != null) {
//      return allDependencies;
//    }

    for (Iterator i = dependencyTreeNode.getChildren().iterator(); i.hasNext();) {
      DependencyNode dependencyNode = (DependencyNode) i.next();
//      System.out.println("\t " + dependencyNode.getArtifact().getId());
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

  
  
  private void put(License key, MavenProject projectArtifact) {
    // handle multiple values as a set to avoid duplicates
    SortedSet<MavenProject> valueList = null;
    Set<License> keySet = resultLicenseMap.keySet();
    String url = key.getUrl();
    for (License license : keySet) {
      String urlLicense = license.getUrl();
      String nameLicense = license.getName();
      if (license.equals(key)) {
        valueList = resultLicenseMap.remove(license);
        if (Util.isArtifactUrlValid(urlLicense)) {
          key.setUrl(urlLicense);
        }
        if (StringUtils.isNotEmpty(nameLicense)) {
          key.setName(nameLicense);
        }
        break;
      }
    }
    
    if (valueList == null) {
      valueList = new TreeSet<MavenProject>();
    }
    valueList.add(projectArtifact);
    resultLicenseMap.put(key, valueList);
  }
}
