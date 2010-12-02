package com.serli.maven.plugin.quality.model;

public class License extends org.apache.maven.model.License {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public License() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    License oLicence = (License) obj;
    if (oLicence != null && oLicence instanceof License) {
      if (getUrl() != null) {
        if (oLicence.getUrl() != null) {
          return getUrl().equals(oLicence.getUrl());
        } else {
          return false;
        }
      } else {
        if (getUrl() == null && oLicence.getUrl() == null) {
          return true;
        } else {
          return false;
        }
      }
    } else {
      return false;
    }
  }

}
