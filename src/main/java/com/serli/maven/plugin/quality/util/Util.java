package com.serli.maven.plugin.quality.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;

import com.serli.maven.plugin.quality.model.DependencyLocation;

public final class Util {

  private Util() {
    
  }
  
  public static List<Integer> getDefinitionsLine(List<DependencyLocation> dependencyLocationList, Dependency dependency) {
    List<Integer> definitionsLine = new ArrayList<Integer>();
    
    for (DependencyLocation dependencyLocation : dependencyLocationList) {
      int line = getLine(dependencyLocation, dependency);
      if (line != -1) {
        definitionsLine.add(dependencyLocation.getLine());
      }
    }
    
    return definitionsLine;
  }

  
  private static int getLine(DependencyLocation dependencyLocation, Dependency dependency) {
    int result = -1;
    Dependency dependencySaved = dependencyLocation.getDependency();
    String groupId = dependencySaved.getGroupId();
    String artifactId = dependencySaved.getArtifactId();
    if (dependency.getGroupId().equals(groupId) && dependency.getArtifactId().equals(artifactId)) {
      result = dependencyLocation.getLine();
    }
    return result;
  }
  
  public static int getLastDefinitionLine(List<DependencyLocation> dependencyLocationList, Dependency dependency) {
    int result = -1;
    for (int i = dependencyLocationList.size() - 1; i>=0; i--) {
      int line = getLine(dependencyLocationList.get(i), dependency);
      if (line != -1) {
        result = line;
        break;
      }
    }
    return result;
  }

  public static int getLastDefinitionLine(List<DependencyLocation> dependencyLocationList, String groupId, String artifactId) {
    Dependency dependency = new Dependency();
    dependency.setArtifactId(artifactId);
    dependency.setGroupId(groupId);
    return getLastDefinitionLine(dependencyLocationList, dependency);
  }
  
}
