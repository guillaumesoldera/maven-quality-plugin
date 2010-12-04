package com.serli.maven.plugin.quality.model;

import org.codehaus.plexus.util.StringUtils;

import com.serli.maven.plugin.quality.util.Util;

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
    boolean isEquals = false;
    License oLicence = (License) obj;
    if (oLicence != null && oLicence instanceof License) {
      if (getUrl() != null) {
        if (oLicence.getUrl() != null) {
          if (Util.isArtifactUrlValid(getUrl()) && Util.isArtifactUrlValid(oLicence.getUrl())) {
            isEquals = getUrl().equals(oLicence.getUrl());
            if (isEquals) {
              if (StringUtils.isEmpty(getName()) || getName().equalsIgnoreCase("unnamed")) {
                setName(oLicence.getName());
              }
            }
          } else {
            if (StringUtils.isNotEmpty(getName())) {
              isEquals = getName().equals(oLicence.getName());
              if (isEquals) {
                if (!Util.isArtifactUrlValid(getUrl()) && Util.isArtifactUrlValid(oLicence.getUrl())) {
                  setUrl(oLicence.getUrl());
                }
              }
            } else {
              isEquals = false;
            }
          }
        } else {
          isEquals = false;
        }
      } else {
        if (getUrl() == null && oLicence.getUrl() == null) {
          isEquals = true;
        } else {
          isEquals = false;
        }
      }
    } else {
      isEquals = false;
    }
    return isEquals;
  }

  @Override
  public String toString() {

    return getName() + " - " + getUrl();
  }

}
