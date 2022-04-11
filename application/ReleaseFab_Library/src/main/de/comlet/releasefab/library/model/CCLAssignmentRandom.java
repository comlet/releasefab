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
 * @file CCLAssignmentRandom.java
 *
 * @brief Random assignment strategy.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.List;
import org.jdom2.Element;

/**
 * Implements an assignment strategy to assign a random number.
 * For debugging purposes.
 */
public class CCLAssignmentRandom extends ACLAssignmentStrategy
{

   private static final String NAME = "Random";
   private static final int NUMBER_OF_PARAMETERS = 2;
   private static final String USAGE_MESSAGE = "Assignment Random Number:\n " +
                                               "Job: Assign a random number in a specific range\n" +
                                               "Parameter 1: min value\n" +
                                               "Parameter 2: max value";
   private static final int DEFAULT_RANGE_START = 1;
   private static final int DEFAULT_RANGE_END = 255;

   @Override
   public String getName()
   {
      return NAME;
   }

   @Override
   public int getNrOfParameters()
   {
      return NUMBER_OF_PARAMETERS;
   }

   @Override
   public String getUsageInfo()
   {
      return USAGE_MESSAGE;
   }

   /**
    * Return a random number in a specific range.<br>
    * parameters[0]: min value<br>
    * parameters[1]: max value
    */
   @Override
   public Element getData(List<CCLParameter> aParameters, CCLComponent aComponent, CCLDelivery aDelivery, CCLDelivery formerDelivery, ACLImportStrategy aImporter, String projectRoot, CCLComponent initialComponent)
   {
      int rangeStart;
      int rangeEnd;

      try
      {
         rangeStart = Integer.parseInt(aParameters.get(0).getValue().trim());
         rangeEnd = Integer.parseInt(aParameters.get(1).getValue().trim());
      }
      catch (RuntimeException ex)
      {
         // use default values
         rangeStart = DEFAULT_RANGE_START;
         rangeEnd = DEFAULT_RANGE_END;
         LOGGER.debug("{} getData: using default values!", getName(), ex);
      }

      /** 
       * Because Math.random() generates values 0.0 <= value < 1.0;
       * Math.random() * (rangeEnd - rangeStart) + rangeStart
       * produces random numbers rangeStart to (rangeEnd - 1). To
       * get values from rangeStart to rangeEnd (including rangeEnd) the
       * correct calculation is:
       */
      
      int rand = (int) (Math.random() * (rangeEnd - rangeStart + 1)) + rangeStart;

      Element desc = new Element("content");
      desc.addContent(SCLXMLUtil.createElement("string", Integer.toString(rand)));
      return desc;
   }
   
   /**
    * Overload of method
    * {@link #getData(List, CCLComponent, CCLDelivery, CCLDelivery, ACLImportStrategy, String, CCLComponent)}
    * without an initial component.
    */
   @Override public Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot)
   {
      return getData(parameters, component, delivery, formerDelivery, importer, projectRoot, null);
   }
}
