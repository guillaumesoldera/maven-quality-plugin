package com.serli.maven.plugin.quality.model;

import java.util.List;
import java.util.Map;

public class MismatchDepMgtModel {

  private Map mismatch;
  
  private List exclusionErrors;

  public Map getMismatch() {
    return mismatch;
  }

  public void setMismatch(Map mismatch) {
    this.mismatch = mismatch;
  }

  public List getExclusionErrors() {
    return exclusionErrors;
  }

  public void setExclusionErrors(List exclusionErrors) {
    this.exclusionErrors = exclusionErrors;
  }
  
  public boolean hasMismatched() {
    boolean result = false;
    
    if (mismatch != null && mismatch.size() > 0) {
      result = true;
    }
    if (exclusionErrors != null && exclusionErrors.size() > 0) {
      result = true;
    }
    return result;
  }
  
}
