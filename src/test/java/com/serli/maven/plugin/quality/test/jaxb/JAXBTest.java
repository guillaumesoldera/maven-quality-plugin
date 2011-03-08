package com.serli.maven.plugin.quality.test.jaxb;

import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import com.serli.maven.plugin.quality.model.jaxb.Group;
import com.serli.maven.plugin.quality.model.jaxb.MavenConventions;
import com.serli.maven.plugin.quality.model.jaxb.Tag;
import com.serli.maven.plugin.quality.mojo.MavenConventionsCheckMojo;
import com.serli.maven.plugin.quality.util.Util;

public class JAXBTest extends TestCase {


  public void testUnMarshall() {
      Class clazz = MavenConventionsCheckMojo.class;
      try {
      MavenConventions conventions = Util.getMavenConventions("maven-conventions.xml", clazz);
      assertNotNull(conventions);
      List<Tag> listTags = conventions.getListTags();
      assertNotNull(listTags);
      assertEquals(listTags.size() > 0, true);
      
      Group group = conventions.getGroup("project");
      assertNotNull(group);
      System.out.println(group.getSkipLine());
    } catch (JAXBException e) {
      e.printStackTrace();
      fail();
    }
  }
}
