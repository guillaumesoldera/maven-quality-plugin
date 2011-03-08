package com.serli.maven.plugin.quality.model;

import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

public class MismatchDepMgtModel {

  private Map<Artifact, Dependency> mismatch;
  
  private List<Artifact> exclusionErrors;

  public Map<Artifact, Dependency> getMismatch() {
    return mismatch;
  }

  public void setMismatch(Map<Artifact, Dependency> mismatch) {
    this.mismatch = mismatch;
  }

  public List<Artifact> getExclusionErrors() {
    return exclusionErrors;
  }

  public void setExclusionErrors(List<Artifact> exclusionErrors) {
    this.exclusionErrors = exclusionErrors;
  }
  
  public boolean canIterate() {
    boolean result = false;
    
    if (mismatch != null) {
      result = true;
    }
    return result;
  }
  
  public boolean hasExclusionErrors() {
    boolean result = false;
    
    if (exclusionErrors != null && exclusionErrors.size() > 0) {
      result = true;
    }
    return result;
  }
  
}
