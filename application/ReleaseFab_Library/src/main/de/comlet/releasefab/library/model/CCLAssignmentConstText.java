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
 * @file CCLAssignmentConstText.java
 *
 * @brief Constant text assignment strategy.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.List;
import org.jdom2.Element;

/**
 * Implementation of an assignment strategy to assign a constant text.
 */
public class CCLAssignmentConstText extends ACLAssignmentStrategy
{
   private static final String NAME = "ConstText";
   private static final int NUMBER_OF_PARAMETERS = 1;
   private static final String USAGE_MESSAGE = "Constant Text:\n" + "Job: Assigns a given text.\n" + 
         "Parameter 1: Text to assign";

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
    * Assigns a given text. Parameter 1: Text to assign.
    */
   @Override
   public Element getData(List<CCLParameter> aParameters, CCLComponent aComponent, CCLDelivery aDelivery,
         CCLDelivery formerDelivery, ACLImportStrategy aImporter, String projectRoot, CCLComponent initialComponent)
   {
      String text = aParameters.get(0).getValue().trim();
      String errorHeader = aComponent + ":" + aImporter + ":" + getName() + ":";

      Element desc = new Element("content");

      if (text.equals(""))
      {
         String error = "Missing Parameter 1";
         LOGGER.error("{} {}", errorHeader, error);
         desc.addContent(SCLXMLUtil.createElement("error", error));
         return desc;
      }

      desc.addContent(SCLXMLUtil.createElement("string", text));

      return desc;
   }

   /**
    * Overload of method
    * {@link #getData(List, CCLComponent, CCLDelivery, CCLDelivery, ACLImportStrategy, String, CCLComponent)}
    * without an initial component.
    */
   @Override
   public Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot)
   {
      return getData(parameters, component, delivery, formerDelivery, importer, projectRoot, null);
   }

}
