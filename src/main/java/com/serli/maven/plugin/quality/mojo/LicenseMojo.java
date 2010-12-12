package com.serli.maven.plugin.quality.mojo;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.validator.UrlValidator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;

import com.google.code.mojo.license.AbstractLicenseMojo;
import com.google.code.mojo.license.Callback;
import com.google.code.mojo.license.document.Document;
import com.google.code.mojo.license.header.Header;
import com.serli.maven.plugin.quality.model.LightLicense;
import com.serli.maven.plugin.quality.model.MavenProjectComparable;
import com.serli.maven.plugin.quality.util.Util;

/**
 * Goal which checks licenses and headers.
 * 
 * @goal license
 * @requiresDependencyResolution test
 * @phase verify
 */
public class LicenseMojo extends AbstractLicenseMojo {

  /**
   * Whether to fail the build if some file miss license header
   * 
   * @parameter expression="${license.failIfMissing}" default-value="false"
   */
  protected boolean failIfMissing;

  /**
   * Output file.
   * 
   * @parameter expression="${outputFile}"
   */
  private File outputFile;

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

  private Map<LightLicense, SortedSet<MavenProjectComparable>> resultLicenseMap = new HashMap<LightLicense, SortedSet<MavenProjectComparable>>();

  private SortedSet<MavenProjectComparable> setArtifactNoLicense = new TreeSet<MavenProjectComparable>();

  public final Collection<File> missingHeaders = new ConcurrentLinkedQueue<File>();

  /**
   * Will be filled with license name / set of projects.
   */
//  private Map licenseMap = new HashMap() {
//    /** {@inheritDoc} */
//    public Object put(Object key, Object value) {
//      // handle multiple values as a set to avoid duplicates
//      SortedSet valueList = (SortedSet) get(key);
//      if (valueList == null) {
//        valueList = new TreeSet();
//      }
//      valueList.add(value);
//      return super.put(key, valueList);
//    }
//  };

  private List<LightLicense> projectLicenses = null;

  /** Random used to generate a UID */
  private static SecureRandom RANDOM;

  private List<Artifact> allDependencies = new ArrayList<Artifact>();

  @SuppressWarnings("unchecked")
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
    // TODO choper le fichier nommé par l'URL

    // TODO voir aussi le project-info pour avoir les licenses des dependances
    // (determiner la compatibilite des licenses ?)
    // TODO voir comment ils gerent avec le DependenciesRenderer :
    // http://svn.apache.org/viewvc/maven/plugins/tags/maven-project-info-reports-plugin-2.2/src/main/java/org/apache/maven/report/projectinfo/dependencies/renderer/DependenciesRenderer.java?view=markup
    projectLicenses = new ArrayList<LightLicense>();
    List<License> licenses = project.getModel().getLicenses();
    for (Iterator<License> i = licenses.iterator(); i.hasNext();) {
      License license = i.next();
      LightLicense projectLicense = new LightLicense();
      projectLicense.setName(license.getName());
      projectLicense.setUrl(license.getUrl());
      header = projectLicense.getUrl();
      projectLicenses.add(projectLicense);

      // TODO que faire si plusieurs licenses pour le projet

      // URL licenseUrl = null;
      // try {
      // licenseUrl = getLicenseURL(project, license.getUrl());
      // System.out.println("licenseURL = " + licenseUrl);
      // } catch (MalformedURLException e) {
      // getLog().error(e.getMessage());
      // } catch (IOException e) {
      // getLog().error(e.getMessage());
      // }
      //
      // if (licenseUrl != null && licenseUrl.getProtocol().equals("file")) {
      // }

    }

    checkHeaders();

    DependencyNode dependencyTreeNode = resolveProject();
    getAllDependencies(dependencyTreeNode);

    stockAllArtifactAndItsLicense(dependencyTreeNode);
    // TODO enlever le projet des autres listes et maps
    // printGroupedLicenses();
    // printLicenses();
    StringBuffer buffer = printResults();
    try {
      Util.writeFile(buffer.toString(), outputFile, getLog());
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private StringBuffer printResults() {
    StringWriter out = new StringWriter();
    PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(out);
    writer.startElement("licenses");
    writer = printProjectLicense(writer);
    writer = printMissingHeader(writer);
    writer = printDependenciesLicense(writer);
    writer.endElement();
    return out.getBuffer();
  }

  private PrettyPrintXMLWriter printMissingHeader(PrettyPrintXMLWriter writer) {
    writer.startElement("missingHeaders");
    for (File file : missingHeaders) {
      writer.startElement("file");
      writer.writeText(file.getName());
      writer.endElement();
    }
    writer.endElement();
    return writer;
  }

  private PrettyPrintXMLWriter printDependenciesLicense(PrettyPrintXMLWriter writer) {
    writer.startElement("dependenciesLicense");
    if (resultLicenseMap != null && resultLicenseMap.size() > 0) {
      Set<LightLicense> keys = resultLicenseMap.keySet();
      for (LightLicense license : keys) {
        writer.startElement("license");
        String url = license.getUrl();
        String name = license.getName();
        if (StringUtils.isEmpty(name)) {
          name = "Unnamed";
        }
        if (StringUtils.isEmpty(url)) {
          url = "unavailable";
        }
        writer.startElement("name");
        writer.writeText(name);
        writer.endElement();
        writer.startElement("url");
        writer.writeText(url);
        writer.endElement();
        SortedSet<MavenProjectComparable> sortedSet = resultLicenseMap.get(license);
        for (MavenProject artifact : sortedSet) {
          writer.startElement("dependency");
          String id = artifact.getModel().getName();
          if (StringUtils.isEmpty(id)) {
            id = artifact.getArtifactId();
          }
          writer.startElement("name");
          writer.writeText(id);
          writer.endElement();
          writer.endElement();
        }
        writer.endElement();
      }
    }

    writer.startElement("unknownLicense");
    for (MavenProject project : setArtifactNoLicense) {
      writer.startElement("dependency");
      String id = project.getModel().getName();
      if (StringUtils.isEmpty(id)) {
        id = project.getArtifactId();
      }
      writer.startElement("name");
      writer.writeText(id);
      writer.endElement();
      writer.endElement();
    }
    writer.endElement();
    writer.endElement();
    return writer;
  }

  private PrettyPrintXMLWriter printProjectLicense(PrettyPrintXMLWriter writer) {
    writer.startElement("projectLicense");
      if (projectLicenses != null && projectLicenses.size() > 0) {
        for (LightLicense license : projectLicenses) {
          writer.startElement("license");
            writer.startElement("name");
            String name = license.getName();
            if (StringUtils.isEmpty(name)) {
              name = "Unnamed";
            }
            writer.writeText(name);
            writer.endElement();
            String url = license.getUrl();
            if (StringUtils.isNotEmpty(url)) {
              writer.startElement("url");
              writer.writeText(url);
              writer.endElement();
            }
          writer.endElement();
        }
      }
    writer.endElement();
    return writer;
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

  @SuppressWarnings("unchecked")
  private void stockAllArtifactAndItsLicense(DependencyNode node) {
    String dependencyDetailId = getUUID();

    stockArtifactAndItsLicense(node, dependencyDetailId);

    if (!node.getChildren().isEmpty()) {
      boolean toBeIncluded = false;
      List<DependencyNode> subList = new ArrayList<DependencyNode>();
      for (Iterator<DependencyNode> deps = node.getChildren().iterator(); deps.hasNext();) {
        DependencyNode dep = deps.next();

        if (!allDependencies.contains(dep.getArtifact())) {
          continue;
        }

        subList.add(dep);
        toBeIncluded = true;
      }

      if (toBeIncluded) {
        for (Iterator<DependencyNode> deps = subList.iterator(); deps.hasNext();) {
          DependencyNode dep = deps.next();

          stockAllArtifactAndItsLicense(dep);
        }
      }
    }

  }

  @SuppressWarnings("unchecked")
  private void stockArtifactAndItsLicense(DependencyNode node, String uid) {
    Artifact artifact = node.getArtifact();
    String id = artifact.getId();
    // System.out.println("id = " + id);
    if (!id.equals(project.getId())) {

      if (!Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) {
        try {
          MavenProjectComparable artifactProject = new MavenProjectComparable(getMavenProjectFromRepository(artifact));
          String artifactDescription = artifactProject.getDescription();
          String artifactUrl = artifactProject.getUrl();
          List<License> licenses = artifactProject.getLicenses();

          if (StringUtils.isNotEmpty(artifactDescription)) {
            // System.out.println("artifactDescription : " +
            // artifactDescription);
          }

          if (StringUtils.isNotEmpty(artifactUrl)) {
            if (Util.isArtifactUrlValid(artifactUrl)) {
              // System.out.println("artifactURL : " + artifactUrl);
            } else {
              // System.out.println("artifactURL : " + artifactUrl);
            }
          }

          if (!licenses.isEmpty()) {
            for (Iterator<License> iter = licenses.iterator(); iter.hasNext();) {
              License element = (License) iter.next();
              String licenseName = element.getName();
              String licenseUrl = element.getUrl();
              LightLicense license = new LightLicense();
              if (StringUtils.isEmpty(licenseName)) {
                licenseName = "Unnamed";
              }
              license.setName(licenseName);
              license.setUrl(licenseUrl);
              // System.out.println(artifactProject.getId() + " - ");
              // System.out.println("licenseName : " + licenseName +
              // "   - licenseUrl : " + licenseUrl);
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

  }

//  private void printLicenses() {
//    // TODO afficher au format XML et dans un fichier ou non
//    Set<LightLicense> keys = resultLicenseMap.keySet();
//    for (LightLicense license : keys) {
//      String url = license.getUrl();
//      String name = license.getName();
//      if (StringUtils.isEmpty(name)) {
//        name = "Unnamed";
//      }
//      System.out.println("Licence " + name + " - " + url);
//      SortedSet<MavenProjectComparable> sortedSet = resultLicenseMap.get(license);
//      for (MavenProject artifact : sortedSet) {
//        System.out.println("\t " + artifact.getId());
//      }
//
//    }
//
//    System.out.println("Inconnu : ");
//    for (MavenProject project : setArtifactNoLicense) {
//      System.out.println("\t " + project.getId());
//    }
//
//  }
//
//  private void printGroupedLicenses() {
//    for (Iterator iter = licenseMap.keySet().iterator(); iter.hasNext();) {
//      String licenseName = (String) iter.next();
//      if (StringUtils.isEmpty(licenseName)) {
//        System.out.println("unnamed");
//      } else {
//        System.out.println(licenseName + " : ");
//      }
//
//      SortedSet projects = (SortedSet) licenseMap.get(licenseName);
//
//      StringBuffer buffer = new StringBuffer();
//      for (Iterator iterator = projects.iterator(); iterator.hasNext();) {
//        String projectName = (String) iterator.next();
//        buffer.append(projectName);
//        if (iterator.hasNext()) {
//          buffer.append(", ");
//        }
//      }
//      System.out.println(buffer.toString());
//
//    }
//  }

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
  @SuppressWarnings("unchecked")
  public List<Artifact> getAllDependencies(DependencyNode dependencyTreeNode) {
    // if (allDependencies != null) {
    // return allDependencies;
    // }

    for (Iterator<DependencyNode> i = dependencyTreeNode.getChildren().iterator(); i.hasNext();) {
      DependencyNode dependencyNode = (DependencyNode) i.next();
      // System.out.println("\t " + dependencyNode.getArtifact().getId());
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

  private void put(LightLicense key, MavenProjectComparable projectArtifact) {
    // handle multiple values as a set to avoid duplicates
    SortedSet<MavenProjectComparable> valueList = null;
    Set<LightLicense> keySet = resultLicenseMap.keySet();
    for (LightLicense license : keySet) {
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
      valueList = new TreeSet<MavenProjectComparable>();
    }
    valueList.add(projectArtifact);
    resultLicenseMap.put(key, valueList);
  }

  private void checkHeaders() throws MojoExecutionException, MojoFailureException {
    super.basedir = project.getBasedir();
    getLog().info("Checking headers...");
    missingHeaders.clear();
    super.project = project;
    execute(new Callback() {
      public void onHeaderNotFound(Document document, Header header) {
        File file = document.getFile();
        info("Missing header in: %s", file.getAbsolutePath());
        missingHeaders.add(file);
      }

      public void onExistingHeader(Document document, Header header) {
        debug("Header OK in: %s", document.getFile());
      }
    });

    if (!missingHeaders.isEmpty()) {
      if (failIfMissing) {
        throw new MojoExecutionException("Some files do not have the expected license header");
      } else {
        getLog().warn("Some files do not have the expected license header");
      }
    }

  }

}
