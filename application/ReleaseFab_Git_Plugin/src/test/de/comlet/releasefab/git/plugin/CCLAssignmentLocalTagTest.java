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
 * @file CCLAssignmentLocalTagTest.java
 *
 * @brief Unit-test class for {@link #CCLAssignmentLocalTag}.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategyExt;
import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLParameter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CCLAssignmentLocalTagTest
{
   /**
    * Component to use for testing.
    */
   private static final String COMPONENT_NAME = "Test-Component";

   /**
    * Instance of the Assignment Strategy that will be instantiated before the
    * tests. The tests will be performed with this object.
    */
   private static ACLAssignmentStrategyExt sAssignmentStrat;
   
   /**
    * List with empty parameters to pass to the method under test.
    */
   private static List<CCLParameter> sListOfParameters;

   /**
    * Instance of the Importer that will be instantiated before the tests. The
    * tests will be performed with this object.
    */
   private static ACLImportStrategy sImporter;

   /**
    * The delivery to perform tests with. Will be instantiated before the tests.
    * Uses the delivery defined in {@link #DELIVERY_NAME}.
    */
   private static CCLDelivery sDelivery;

   /**
    * The component to perform tests with. Will be instantiated before all
    * tests. Uses the component defined in {@link #COMPONENT_NAME}.
    */
   private static CCLComponent sComponent;

   /**
    * A delivery previous to {@link #sDelivery}.
    */
   private static CCLDelivery sFormerDelivery;

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
    * Initializes the objects used in the tests and declared at the top.
    * {@link #sDelivery} is assigned to the delivery declared in
    * {@link #DELIVERY_NAME} by getting it from the instance of SCLProject. The
    * same is done for {@link #sComponent} with the component declared in
    * {@link #COMPONENT_NAME} and {@link #sAssignmentStrategy} with the importer
    * defined in {@link #NAME}. {@link #sImporter} is instantiated by
    * calling the constructor of the Importer class corresponding to the current
    * plugin.
    */
   private static void initializeTestObjects() throws JDOMException, IOException
   {
      // Initialize deliveries under test
      sDelivery = new CCLDelivery("1.0.0.0", "Test-Integrator");
      sFormerDelivery = new CCLDelivery("0.0.5.0", "Igor-Integrator");
      
      sImporter = new CCLImportGitCommits();
      sAssignmentStrat = new de.comlet.releasefab.git.plugin.CCLAssignmentLocalTag();
      
      // Initialize a basic component
      sComponent = new CCLComponent();
      sComponent.setName(COMPONENT_NAME);
      sComponent.getAssignmentStrategies().put(sImporter.getName(), sAssignmentStrat);
      
      sListOfParameters = new ArrayList<CCLParameter>();
      sListOfParameters.add(new CCLParameter());
      sListOfParameters.add(new CCLParameter());

      ACLDeliveryInformation deliveryInformation = new CCLDeliveryGitCommits();
      deliveryInformation.setInformation(createDeliveryInformation());
      sComponent.setDeliveryInformation(sDelivery.getName() + sImporter.getName(), deliveryInformation);
   }
   
   /**
    * Creates a generic XML-Element containing delivery information.
    * 
    * @return Generic XML-Element containing delivery information.
    * @throws JDOMException
    * @throws IOException
    */
   private static Element createDeliveryInformation() throws JDOMException, IOException
   {
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(new StringReader("<deliveryInformation name=\"1.0.0.0\" isNew=\"true\">\r\n" +
            "            <content>\r\n" +
            "              <tag hash=\"e7847c60478282e08bf3de15ded9c56019b0d028\" target=\"e7847c60478282e08bf3de15ded9c56019b0d028\" type=\"former\">V3.6.2.0</tag>\r\n" +
            "              <tag hash=\"e7847c60478282e08bf3de15ded9c56019b0d028\" target=\"e7847c60478282e08bf3de15ded9c56019b0d028\" type=\"latest\">V3.7.0.0</tag>\r\n" +
            "              <branch>release</branch>\r\n" +
            "              <commit>\r\n" +
            "                <hash>47c60478</hash>\r\n" +
            "                <time>1534429955</time>\r\n" +
            "                <alm-id>1234</alm-id>\r\n" +
            "                <synopsis>adding new feature</synopsis>\r\n" +
            "                <internal-doc>adding a new feature that will improve the application</internal-doc>\r\n" +
            "                <external-doc />\r\n" +
            "              </commit>" +
            "               </content>" +
            "               </deliveryInformation>"));
      return doc.getRootElement();
   }

   /**
    * As long as there is no local Repository passed, this test can
    * only ensure that there is no other Exception or Error produced. This means
    * as soon as a local Repository HEAD gets passed as parameter, this test will
    * and has to fail.
    */
   @Disabled
   @Test
   void testGetData()
   {
      Element result = sAssignmentStrat.getData(sListOfParameters, sComponent, sDelivery, sFormerDelivery,
            sImporter, "", new CCLComponent());

      String res = result.getChild("error").getText();

      // Substring is necessary because the path to the repository is also in the error string
      assertEquals("GIT: No TAG found", res);
   }
}
