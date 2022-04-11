/**
 * ReleaseFab
 *
 * Copyright © 2022 comlet Verteilte Systeme GmbH
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * @file CCLImportGitCommitsTest.java
 *
 * @brief Unit-test class for {@link #CCLImportGitCommits}.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.plugin.ACLDetailedInformation;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import java.util.List;
import org.jdom2.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CCLImportGitCommitsTest
{
   /**
    * The number of child elements that should be provided in the docbook
    * section template.
    */
   private static final int NUMBER_OF_CHILDREN_IN_TEMPLATE = 2;

   /**
    * The component to perform tests with. It will be instantiated before all
    * tests by the default constructor.
    */
   private static CCLComponent sComponent;

   /**
    * The delivery to perform tests with. It will be instantiated before all
    * tests by the default constructor.
    */
   private static CCLDelivery sDelivery;

   /**
    * The importer which is under test.
    */
   private static CCLImportGitCommits sImportGitCommits;

   /**
    * Instantiates all objects needed for testing using their default
    * constructor.
    */
   @BeforeAll
   static void setUpBeforeClass()
   {
      sComponent = new CCLComponent();
      sDelivery = new CCLDelivery();

      sImportGitCommits = new CCLImportGitCommits();
   }

   /**
    * Gets the detailed information from {@link #sImportCBItems} and asserts
    * that it exists.
    */
   @Test
   void testGetDetailedInformation()
   {
      ACLDetailedInformation information = sImportGitCommits.getDetailedInformation(sComponent, sDelivery);
      assertNotNull(information);
   }

   /**
    * Tries to get all available Assignment Strategies and tests that only the
    * ones set for the current plugin do not return null.
    */
   @Test
   void testGetAssignmentStrategy()
   {
      Assertions.assertAll(() -> assertNotNull(sImportGitCommits.getAssignmentStrategy("Git Commits")),
            () -> assertNull(sImportGitCommits.getAssignmentStrategy("Command Executer")),
            () -> assertNotNull(sImportGitCommits.getAssignmentStrategy("ConstText")),
            () -> assertNull(sImportGitCommits.getAssignmentStrategy("File Parser")),
            () -> assertNotNull(sImportGitCommits.getAssignmentStrategy("Ignore")),
            () -> assertNull(sImportGitCommits.getAssignmentStrategy("Random")),
            () -> assertNotNull(sImportGitCommits.getAssignmentStrategy("Import Subtree")));
   }

   /**
    * Gets the template of the docbook section for the current plugin and checks
    * whether or not all of the defined arguments are set correctly.
    */
   @Test
   void testGetDocbookSectionTemplate()
   {
      Element testDocbookSectionTemplate = sImportGitCommits.getDocbookSectionTemplate(sDelivery, sDelivery);

      List<Element> testDocbookSectionTemplateChildren = testDocbookSectionTemplate.getChildren();
      Assertions.assertAll(() -> assertEquals("section", testDocbookSectionTemplate.getName()),
            () -> assertEquals(NUMBER_OF_CHILDREN_IN_TEMPLATE, testDocbookSectionTemplateChildren.size()),
            () -> assertEquals("Git Commits", testDocbookSectionTemplateChildren.get(0).getText()),
            () -> assertEquals("title", testDocbookSectionTemplateChildren.get(0).getName()),
            () -> assertEquals("para", testDocbookSectionTemplateChildren.get(1).getName()),
            () -> assertEquals("", testDocbookSectionTemplateChildren.get(1).getText()));
   }

   /**
    * Gets the template of an empty docbook section for the current plugin and
    * checks whether or not all of the defined arguments are set correctly.
    */
   @Test
   void testGetDocbookSectionEmptyMessage()
   {
      Element testDocbookSectionEmptyMessage = sImportGitCommits.getDocbookSectionEmptyMessage();

      List<Element> testDocbookSectionEmptyMessageChildren = testDocbookSectionEmptyMessage.getChildren();
      Assertions.assertAll(() -> assertEquals("subtitle", testDocbookSectionEmptyMessage.getName()),
            () -> assertEquals("N/A", testDocbookSectionEmptyMessage.getText()),
            () -> assertEquals(0, testDocbookSectionEmptyMessageChildren.size()));
   }
}
