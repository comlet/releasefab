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
 * @file SCLProjectTest.java
 *
 * @brief Unit-tests of {@link #SCLProject}.
 */

package de.comlet.releasefab;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLObservableCollection;
import de.comlet.releasefab.library.model.CCLTuple;
import de.comlet.releasefab.library.settings.SCLSettings;
import de.comlet.releasefab.library.settings.SCLSettings.ECLSettingsType;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.test.util.CCLCredentialsHelper;
import de.comlet.releasefab.test.util.CCLDirectoryHelper;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static de.comlet.releasefab.test.util.CCLXMLDiff.assertXMLEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The test methods are ordered because certain deliveries must 
 * be deleted before they can be added again.
 */
@TestMethodOrder(OrderAnnotation.class)
class SCLProjectTest
{
   /** Initialize logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(SCLProjectTest.class);

   /**
    * Object containing all of the necessary absolute directories relative to
    * the "working directory"
    */
   private static CCLDirectoryHelper sDirectories;

   private static final String RELEASEFAB = "releasefab";
   private static final int NUMBER_OF_OSS_LINES = 97;
   
   /**
    * Directory containing the control XML-Files for the tests of this class.
    * The path is relative to the "Working Directory".
    */
   private static final String DIRECTORY_IN = "application" + File.separator + 
                                                "ReleaseFab_Application" + File.separator + 
                                                "src" + File.separator + 
                                                "test" + File.separator + 
                                                "de" + File.separator + 
                                                "comlet" + File.separator + 
                                                RELEASEFAB + File.separator;

   /**
    * Path to the folder where the products of the tests should be exported to.
    * The path is relative to the root of the "_products" folder which is used
    * for all the outputs.
    */
   private static final String DIRECTORY_OUT = "bin" + File.separator + 
                                                "ReleaseFab_Application" + File.separator + 
                                                "bin" + File.separator + 
                                                "test" + File.separator + 
                                                "de" + File.separator + 
                                                "comlet" + File.separator + 
                                                RELEASEFAB + File.separator;

   /**
    * Name of the file containing the control XML-Document under
    * {@link #DIRECTORY_IN} and the product of the {@link #testExportDocbook}
    * method under {@link #DIRECTORY_OUT}.
    */
   private static final String FILENAME_EXPORT_DOCBOOK = "export_docbook_test.xml";

   /**
    * Name of the file containing the control XML-Document under
    * {@link #DIRECTORY_IN} and the product of the {@link #testSave} method
    * under {@link #DIRECTORY_OUT}.
    */
   private static final String FILENAME_VERSIONS = "versions_unit_test.xml";

   /**
    * Time the first delivery under test was created
    */
   private static final long BASE_CREATION_TIME = 987654321000L;

   /**
    * Number of the plugins included in the OSS version.
    */
   private static final int NUMBER_OF_BASIC_PLUGINS = 3;

   /**
    * Name of the file containing the control XML-Document under
    * {@link #DIRECTORY_IN} and the product of the {@link #testExportDelivery}
    * method under {@link #DIRECTORY_OUT}.
    */
   private static final String FILENAME_DELIVERY = "delivery_test.xml";

   /**
    * The delivery to be used when a single CCLDelivery is needed during a test.
    */
   private static final String DELIVERY_UNDER_TEST = "2.0.0.0";

   /**
    * Declares the names of all deliveries currently under test. Must not
    * include the newest one, because it is still subject to change. These
    * values should only be changed when the test concept is changed, because
    * therefore new control XML-Files need to be exported.
    */
   private static final List<String> DELIVERIES_UNDER_TEST = List.of("1.0.0.0", DELIVERY_UNDER_TEST, "3.0.0.0");

   /**
    * Holds the actual CCLDelivery instances for the deliveries defined in
    * {@link #DELIVERIES_UNDER_TEST}.
    */
   private static CCLObservableCollection<CCLDelivery> sDeliveries;

   /**
    * Holds the actual CCLDelivery instance for the single delivery defined in
    * {@link #DELIVERY_UNDER_TEST}.
    */
   private static CCLDelivery sDelivery;

   /**
    * Calls multiple methods to setup the environment needed for the tests.
    * 
    * @throws IOException
    * @throws JDOMException
    */
   @BeforeAll
   static void setUpBeforeClass() throws JDOMException, IOException
   {
      initializeDirectories();
      
      String localRunWithoutALM = System.getenv("LOCAL_RUN_WITHOUT_ALM");

      if (null == localRunWithoutALM || localRunWithoutALM.equals("false"))
      {
         loginToALMService();
      }

      setCorrectOrder();

      initializeDeliveriesUnderTest();
   }

   /**
    * Initializes {@link #sDirectories} with the absolute path of the current
    * working directory. Then the root path of the project is set in SCLProject
    * just like "Main" would do normally. When running the tests under Eclipse
    * the "Working Directory" must be set to the root of the ReleaseFab
    * repository in the "Run Configurations". This process is explained further
    * in the ECLIPSE_IMPORT_README.docx.
    */
   private static void initializeDirectories()
   {
      sDirectories = new CCLDirectoryHelper(new File("").getAbsolutePath(), DIRECTORY_IN, DIRECTORY_OUT);
   }

   /**
    * Provides the Login Credentials for the ALM Service and adds them to the
    * instance of SCLSettings. For the detailed documentation on how these
    * values are gathered take a look at
    * {@link CCLCredentialsHelper#getCredentials()}.
    */
   private static void loginToALMService()
   {      
      Map<String, String> credentialsMap = CCLCredentialsHelper.getCredentials();

      SCLSettings.add(CCLXMLConstants.XML_ALM_USER_KEY, credentialsMap.get(CCLXMLConstants.XML_ALM_USER_KEY),
            EnumSet.of(ECLSettingsType.USER));
      SCLSettings.add(CCLXMLConstants.XML_ALM_PASSWORD_KEY, credentialsMap.get(CCLXMLConstants.XML_ALM_PASSWORD_KEY),
            EnumSet.of(ECLSettingsType.VOLATILE));
      SCLSettings.add(CCLXMLConstants.XML_ALM_SERVER, credentialsMap.get(CCLXMLConstants.XML_ALM_SERVER),
            EnumSet.of(ECLSettingsType.PROJECT));

      if (null != credentialsMap.get(CCLXMLConstants.XML_ALM_WINDOWS_CERTIFICATE_PATH))
      {
         SCLSettings.add(CCLXMLConstants.XML_ALM_WINDOWS_CERTIFICATE_PATH,
               credentialsMap.get(CCLXMLConstants.XML_ALM_WINDOWS_CERTIFICATE_PATH),
               EnumSet.of(ECLSettingsType.PROJECT));
      }

      if (null != credentialsMap.get(CCLXMLConstants.XML_ALM_UNIX_CERTIFICATE_PATH))
      {
         SCLSettings.add(CCLXMLConstants.XML_ALM_UNIX_CERTIFICATE_PATH,
               credentialsMap.get(CCLXMLConstants.XML_ALM_UNIX_CERTIFICATE_PATH), EnumSet.of(ECLSettingsType.PROJECT));
      }
   }

   /**
    * Sets VIEW_ORDER and EXPORT_ORDER of the plugins in order to do integration
    * tests without requiring a file with settings.
    */
   private static void setCorrectOrder()
   {
      SCLSettings.clearSettings();
      List<CCLTuple<String, Boolean>> orderList = new ArrayList<CCLTuple<String, Boolean>>();
      orderList.add(new CCLTuple<String, Boolean>("VERSION", true));
      orderList.add(new CCLTuple<String, Boolean>("IMPORTANT_INFORMATION", true));
      orderList.add(new CCLTuple<String, Boolean>("GIT_COMMITS", true));

      SCLSettings.addList("VIEW_ORDER", orderList, EnumSet.of(ECLSettingsType.PROJECT));
      SCLSettings.addList("EXPORT_ORDER", orderList, EnumSet.of(ECLSettingsType.PROJECT));
   }

   /**
    * Initializes {@link #sDeliveries} with exactly the deliveries declared in
    * {@link #DELIVERIES_UNDER_TEST}. The method also initializes
    * {@link #sDelivery} with the delivery declared in
    * {@link #DELIVERY_UNDER_TEST} which needs to be included in the list
    * {@link #DELIVERIES_UNDER_TEST}.
    * 
    * @throws IOException
    * @throws JDOMException
    */
   private static void initializeDeliveriesUnderTest() throws JDOMException, IOException
   {
      CCLComponent comp = SCLProject.getInstance().getInitialComponent();
      
      setCorrectOrder();
      loginToALMService();
      
      SCLSettings.add(CCLXMLConstants.XML_ROOT_FORMAT, RELEASEFAB, EnumSet.of(ECLSettingsType.PROJECT));
      comp.setName("Test-Component");

      // A TreeSet is used to allow the Visitor Pattern to function properly
      Set<CCLDelivery> setOfDeliveriesUnderTest = new TreeSet<CCLDelivery>();

      List<ACLImportStrategy> plugins = SCLProject.getInstance().getImportStrategiesInViewOrder();

      for (int i = 0; i < DELIVERIES_UNDER_TEST.size(); i++)
      {
         CCLDelivery delToAdd = new CCLDelivery(DELIVERIES_UNDER_TEST.get(i), "Test-Integrator");
         delToAdd.setCreated(new Date(BASE_CREATION_TIME + i));

         List<ACLDeliveryInformation> deliveryInformation = getDeliveryInformationInViewOrder();
         for (int j = 0; j < deliveryInformation.size(); j++)
         {
            comp.setDeliveryInformation(DELIVERIES_UNDER_TEST.get(i) + plugins.get(j).getName(),
                  deliveryInformation.get(j));
         }

         SCLProject.getInstance().getDeliveries().add(delToAdd);
         setOfDeliveriesUnderTest.add(delToAdd);

         if (DELIVERIES_UNDER_TEST.get(i).equals(DELIVERY_UNDER_TEST))
         {
            sDelivery = delToAdd;
         }
      }

      CCLComponent root = SCLProject.getComponentRoot();
      root.getSubComponents().add(comp);
      SCLProject.setComponentRoot(root);
      sDeliveries = new CCLObservableCollection<CCLDelivery>(setOfDeliveriesUnderTest);
   }

   /**
    * Saves all deliveries of {@link #sDeliveries} to {@link #FILENAME_VERSIONS}
    * under the {@link #DIRECTORY_OUT}. Then the test compares the generated
    * output with the control XML-Document in {@link #DIRECTORY_IN} and asserts
    * that the two are identical. For more information consult the test case
    * specification.
    * 
    * @throws IOException
    * @throws JDOMException
    */
   @Order(1) @Test
   void testSave() throws JDOMException, IOException
   {
      try
      {
         SCLProject.save(sDirectories.getOut() + FILENAME_VERSIONS, sDeliveries);
      }
      catch (IOException e)
      {
         LOGGER.error("IOException: ", e);
         fail("IOException" + e.toString());
      }

      String toAppend = "</importers></component></components></releasefab>";

      assertXMLEquals(sDirectories.getIn(), sDirectories.getOut(), FILENAME_VERSIONS, NUMBER_OF_OSS_LINES, toAppend);
   }

   /**
    * Exports all deliveries as Docbook of {@link #sDeliveries} to
    * {@link #FILENAME_EXPORT_DOCBOOK} under the {@link #DIRECTORY_OUT}. Then
    * the test compares the generated output with the control XML-Document in
    * {@link #DIRECTORY_IN} and asserts that the two are identical. For more
    * information consult the test case specification.
    */
   @Order(2) @Test @EnabledIfEnvironmentVariable(named = "ENABLE_ALM_TESTS", matches = "true")
   void testExportDocbook()
   {
      try
      {
         SCLProject.exportDocbook(sDirectories.getOut() + FILENAME_EXPORT_DOCBOOK, sDeliveries, Boolean.FALSE);
      }
      catch (IOException e)
      {
         LOGGER.error("Exception: ", e);
         fail("Exception" + e.toString());
      }

      assertXMLEquals(sDirectories.getIn(), sDirectories.getOut(), FILENAME_EXPORT_DOCBOOK);
   }

   /**
    * Exports all deliveries of {@link #sDeliveries} to
    * {@link #FILENAME_DELIVERY} under the {@link #DIRECTORY_OUT}. Then the test
    * compares the generated output with the control XML-Document in
    * {@link #DIRECTORY_IN} and asserts that the two are identical. For more
    * information consult the test case specification. Would test the same
    * methods as {@link #testSave()} -> Disabled.
    */
   @Order(3) @Disabled @Test
   void testExportDelivery()
   {
      try
      {
         SCLProject.getInstance().exportDelivery(sDirectories.getOut() + FILENAME_DELIVERY, sDelivery);
      }
      catch (IOException e)
      {
         LOGGER.error("IOException: ", e);
         fail("IOException" + e.toString());
      }
      assertXMLEquals(sDirectories.getOut(), sDirectories.getIn(), FILENAME_DELIVERY);
   }

   /**
    * Removes an existing delivery {@link #sDelivery} and checks if it has been
    * removed successfully.
    */
   @Order(4) @Test
   void testRemoveExistentDelivery()
   {
      Integer initialSize = SCLProject.getInstance().getDeliveries().size();
      Boolean removeExistentsuccess = SCLProject.removeDelivery(SCLProject.getComponentRoot(), sDelivery);

      Boolean removeSuccess = SCLProject.removeDelivery(SCLProject.getComponentRoot(), new CCLDelivery());
      Integer finalSize = SCLProject.getInstance().getDeliveries().size();

      assertAll(() -> assertTrue(initialSize > finalSize), () -> assertTrue(removeExistentsuccess),
            () -> assertTrue(removeSuccess));
   }

   /**
    * Adds a delivery. If {@link #sDelivery} does exist the test asserts that
    * the Addition fails. Otherwise the test asserts that the Addition succeeds.
    */
   @Order(5) @Test
   void testAddDeliveries()
   {
      if (null != SCLProject.getInstance().getDeliveryByName(sDelivery.getName()))
      {
         // delivery to be added already exists, cannot be added
         Integer initialSize = SCLProject.getInstance().getDeliveries().size();
         Boolean successA = SCLProject.getInstance().getDeliveries().add(sDelivery);
         Boolean successB = SCLProject.addDeliveries(SCLProject.getComponentRoot(), sDelivery);

         Integer finalSize = SCLProject.getInstance().getDeliveries().size();

         assertAll(() -> assertFalse(initialSize < finalSize), () -> assertFalse(successA), () -> assertTrue(successB));
      }
      else
      {
         // delivery to be added does not exist yet -> add it
         Integer initialSize = SCLProject.getInstance().getDeliveries().size();
         Boolean successA = SCLProject.getInstance().getDeliveries().add(new CCLDelivery());
         Integer finalSize = SCLProject.getInstance().getDeliveries().size();

         assertAll(() -> assertTrue(initialSize < finalSize), () -> assertTrue(successA));
      }
   }

   /**
    * Tests if the reset of a project is working.
    */
   @Order(7) @Test
   void testReset()
   {
      SCLProject.getInstance().reset();
      SCLProject.getInstance();
      assertAll(() -> assertTrue(SCLProject.getInstance().getDeliveries().isEmpty()),
            () -> assertTrue(SCLProject.getComponentRootData().isEmpty()),
            () -> assertTrue(SCLProject.getComponentRoot().getSubComponents().isEmpty()),
            () -> assertFalse(SCLProject.getNeedsSaving()),
            () -> assertEquals(null, SCLProject.getInstance().getCreationReport()),
            () -> assertEquals("", SCLProject.getOpenFileName()));
   }

   private static List<ACLDeliveryInformation> getDeliveryInformationInViewOrder() throws JDOMException, IOException
   {
      SAXBuilder builder = new SAXBuilder();

      List<String> informationList = new ArrayList<String>();
      informationList.add("<content><string>W.X.Y.Z</string></content>");
      informationList.add("<content><string>Important Information</string></content>");
      informationList.add("<content>" +
            "<tag hash=\"e7847c60478282e08bf3de15ded9c56019b0d028\" target=\"e7847c60478282e08bf3de15ded9c56019b0d028\" type=\"former\">V3.6.2.0</tag>" +
            "<tag hash=\"e7847c60478282e08bf3de15ded9c56019b0d028\" target=\"e7847c60478282e08bf3de15ded9c56019b0d028\" type=\"latest\">V3.7.0.0</tag>" +
            "<branch>release</branch><commit>" + "<hash>47c60478</hash><time>1534429955</time>" +
            "<alm-id>1234</alm-id><synopsis>adding new feature</synopsis>" +
            "<internal-doc>adding a new feature that will improve the application</internal-doc>" +
            "<external-doc /></commit></content>");

      List<ACLDeliveryInformation> resultList = new ArrayList<ACLDeliveryInformation>();

      List<ACLImportStrategy> importStrategies = SCLProject.getInstance().getImportStrategiesInViewOrder();
      for (int i = 0; i < NUMBER_OF_BASIC_PLUGINS; i++)
      {
         ACLDeliveryInformation deliveryInformation = SCLPluginLoader.getInstance().getDeliveryInformation(
               importStrategies.get(i).getDeliveryInformationName());
         Element informationElement = builder.build(new StringReader(informationList.get(i))).getRootElement().detach();
         deliveryInformation.setInformation(informationElement);
         resultList.add(deliveryInformation);
      }

      return resultList;
   }
}
