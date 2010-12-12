package com.serli.maven.plugin.quality.model.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name ="naming")
public class NamingConventions {

  private GroupId groupId;
  
  private ArtifactId artifactId;
  
  public NamingConventions() {
   
  }
  
  @XmlElement(name="groupId")
  public GroupId getGroupId() {
    return groupId;
  }

  public void setGroupId(GroupId groupId) {
    this.groupId = groupId;
  }

  @XmlElement(name="artifactId")
  public ArtifactId getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(ArtifactId artifactId) {
    this.artifactId = artifactId;
  }
  
}
