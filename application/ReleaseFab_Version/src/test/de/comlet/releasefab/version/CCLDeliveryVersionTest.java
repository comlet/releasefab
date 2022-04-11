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
 * @file CCLDeliveryVersionTest.java
 *
 * @brief Unit test class of {@link #CCLDeliveryVersion}.
 */

package de.comlet.releasefab.version;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.test.util.CCLVisitorPatternTestHelper;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class CCLDeliveryVersionTest
{
   /** Init logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLDeliveryVersionTest.class);

   /**
    * Name of the Importer under test - must be the same as defined in the
    * importer class of the plugin under test.
    */
   private static final String IMPORTER_NAME = "Version";

   /**
    * Expected name of the delivery information class.
    */
   private static final String DELIVERY_INFORMATION_NAME = "Delivery Version";

   /**
    * Upper bound of the interval of versions to be tested.
    */
   private static final String NEWEST_DELIVERY_NAME = "2.0.0.0";

   /**
    * Lower bound of the interval of versions to be tested.
    */
   private static final String OLDEST_DELIVERY_NAME = "1.0.0.0";
   
   /**
    * The number of deliveries used and expected while testing.
    */
   private static final int NUMBER_OF_DELIVERIES = 2;
   
   private static final int INDEX_ZERO = 0;
   private static final int INDEX_ONE = 1;
   private static final int INDEX_TWO = 2;

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
    * Calls multiple methods to setup the environment needed for the tests.
    * 
    * @throws IOException
    * @throws JDOMException
    */
   @BeforeAll
   static void setUpBeforeClass() throws JDOMException, IOException
   {
      initializeTestObjects();
   }

   /**
    * Instantiates {@link #sImporter} by a call to the constructor of the
    * Importer class of the plugin under test. The method also initializes
    * {@link #sDeliveryInfo}.
    * 
    * @throws IOException
    * @throws JDOMException
    */
   private static void initializeTestObjects() throws JDOMException, IOException
   {
      SAXBuilder builder = new SAXBuilder();
      builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      sImporter = new CCLImportVersion();

      sDeliveryInfo = new CCLDeliveryVersion();
      sDeliveryInfo.setInformation(builder.build(new StringReader("<deliveryInfo>not empty</deliveryInfo>")).getRootElement());
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
    * 
    * @throws IOException
    * @throws JDOMException
    */
   @Test
   void testAddDocbookSection()
   {
      CCLDelivery newDelivery = new CCLDelivery(NEWEST_DELIVERY_NAME, "John Doe");
      CCLDelivery oldDelivery = new CCLDelivery(OLDEST_DELIVERY_NAME, "Jane Buckland");
      CCLComponent rootComponent = new CCLComponent();
      rootComponent.setName("Test-Component");

      final Element section = sImporter.getDocbookSectionTemplate(newDelivery, oldDelivery);

      CCLVisitorPatternTestHelper visitorPatternTestHelper = new CCLVisitorPatternTestHelper(IS_FOR_CUSTOMER, section,
            IMPORTER_NAME, oldDelivery);

      rootComponent.accept(visitorPatternTestHelper.new VisitorFillSection(), newDelivery);

      Assertions.assertAll(() -> assertEquals(NUMBER_OF_DELIVERIES, section.getChildren().size()),
            () -> assertEquals("Component", getTextFromDocbookParameter(section, INDEX_ZERO)),
            () -> assertEquals(OLDEST_DELIVERY_NAME, getTextFromDocbookParameter(section, INDEX_ONE)),
            () -> assertEquals(NEWEST_DELIVERY_NAME, getTextFromDocbookParameter(section, INDEX_TWO)));
   }

   private String getTextFromDocbookParameter(final Element section, Integer indexInTable)
   {
      return section.getChild(CCLXMLConstants.XML_TABLE).getChild(CCLXMLConstants.XML_TGROUP).getChild(CCLXMLConstants.XML_THEAD).getChild(CCLXMLConstants.XML_ROW).getChildren(CCLXMLConstants.XML_ENTRY).get(
            indexInTable).getChild(CCLXMLConstants.XML_PARA).getText();
   }

   /**
    * Tests whether the information of the plugin is set correctly. Asserts that
    * the information for this plugin is not null or empty.
    */
   @Test
   void testIsInfoNullOrEmpty()
   {
      assertFalse(sDeliveryInfo.isInfoNullOrEmpty());
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
