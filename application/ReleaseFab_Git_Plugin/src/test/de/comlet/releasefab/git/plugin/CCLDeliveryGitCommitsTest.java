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
 * @file CCLDeliveryGitCommitsTest.java
 *
 * @brief Unit-test class for {@link #CCLDeliveryGitCommits}.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.test.util.CCLVisitorPatternTestHelper;
import org.jdom2.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static de.comlet.releasefab.test.util.CCLXMLDiff.assertXMLEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class CCLDeliveryGitCommitsTest
{
   /** Init logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLDeliveryGitCommitsTest.class);

   /**
    * Name of the Importer under test - must be the same as defined in the
    * importer class of the plugin under test.
    */
   private static final String IMPORTER_NAME = "Git Commits";

   /**
    * Expected name of the delivery information class.
    */
   private static final String DELIVERY_INFORMATION_NAME = "Delivery Git Commits";

   /**
    * Upper bound of the interval of versions to be tested.
    */
   private static final String NEWEST_DELIVERY_NAME = "2.0.0.0";

   /**
    * Lower bound of the interval of versions to be tested.
    */
   private static final String OLDEST_DELIVERY_NAME = "1.0.0.0";

   /**
    * Define whether the export is for a customer or not. Release Notes for
    * ReleaseFab are not exported for customers, therefore this variable is set
    * to false.
    */
   private static final Boolean IS_FOR_CUSTOMER = false;

   /**
    * Instance of the importer that will be instantiated before the tests. The
    * tests will be performed with this object.
    */
   private static ACLImportStrategy sImporter;

   /**
    * Variable to store the DeliveryInformation provided by the test.
    */
   private static ACLDeliveryInformation sDeliveryInfo;

   /**
    * A new delivery under test.
    */
   private static CCLDelivery sNewestDelivery;

   /**
    * An old delivery under test.
    */
   private static CCLDelivery sOldestDelivery;

   /**
    * Calls multiple methods to setup the environment needed for the tests.
    */
   @BeforeAll
   static void setUpBeforeClass()
   {
      initializeTestObjects();
   }

   /**
    * Instantiates {@link #sImporter} by a call to the constructor of the
    * Importer class of the plugin under test. The method also initializes
    * {@link #sDeliveryInfo}.
    */
   private static void initializeTestObjects()
   {
      sImporter = new CCLImportGitCommits();

      sDeliveryInfo = new CCLDeliveryGitCommits();

      sOldestDelivery = new CCLDelivery(OLDEST_DELIVERY_NAME, "Jane Doe");
      sNewestDelivery = new CCLDelivery(NEWEST_DELIVERY_NAME, "Igor Integrator");
   }

   /**
    * Test is currently disabled because the method under test has obvious flaws
    * and is not used anywhere in the project.
    */
   @Disabled @Test
   void testAddInformation()
   {
      Element elementToAdd = new Element("Test");
      Boolean success = false;
      try
      {
         success = sDeliveryInfo.addInformation(elementToAdd);
      }
      catch (Exception e)
      {
         LOGGER.error("Exception: ", e);
         fail("Exception" + e.toString());
      }
      assertFalse(success);
   }

   /**
    * This test calls addDocbookSection just like SCLProject does to get all of
    * the information from the different plugins wrapped in an XML-Section
    * Element. To be able to traverse through the component which is implemented
    * using the Visitor Pattern, the {@link CCLVisitorPatternTestHelper} is
    * used. The test asserts that the plugin returns the same section as defined
    * by the content of the file {@link #FILENAME_ADD_DOCBOOK_SECTION_CB_ITEMS}.
    */
   @Test
   void testAddDocbookSection()
   {
      final Element section = sImporter.getDocbookSectionTemplate(sOldestDelivery, sNewestDelivery);

      CCLVisitorPatternTestHelper visitorPatternTestHelper = new CCLVisitorPatternTestHelper(IS_FOR_CUSTOMER, section,
            IMPORTER_NAME, sOldestDelivery);

      new CCLComponent().accept(visitorPatternTestHelper.new VisitorFillSection(), sNewestDelivery);

      assertXMLEquals(sImporter.getDocbookSectionTemplate(sOldestDelivery, sNewestDelivery), section);
   }

   /**
    * Tests whether the information of the plugin is set correctly. Asserts that
    * the information for this plugin is not null or empty.
    */
   @Test
   void testIsInfoNullOrEmpty()
   {
      assertTrue(sDeliveryInfo.isInfoNullOrEmpty());
   }

   /**
    * In most cases getters do not need to be tested. In this case the name of
    * the delivery information acts as a key to the information. To avoid
    * accidental changes to that name, it needs to be tested.
    */
   @Test
   void testGetName()
   {
      assertEquals(DELIVERY_INFORMATION_NAME, sDeliveryInfo.getName());
   }
}
