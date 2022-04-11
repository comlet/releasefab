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
 * @file CCLAssignmentIgnore.java
 *
 * @brief Ignore assignment strategy.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.List;
import org.jdom2.Element;

/**
 * Implements an assignment strategy to assign the String "-". It indicates that
 * no assignment strategy was selected at the time of delivery creation.
 */
public class CCLAssignmentIgnore extends ACLAssignmentStrategy
{
   private static final String NAME = "Ignore";
   private static final int NUMBER_OF_PARAMETERS = 0;
   private static final String USAGE_MESSAGE = "If you don't want to assign anything!";

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

   /** Returns the String "-". No parameters needed. */
   @Override
   public Element getData(List<CCLParameter> aParameters, CCLComponent aComponent, CCLDelivery aDelivery,
         CCLDelivery formerDelivery, ACLImportStrategy aImporter, String projectRoot, CCLComponent initialComponent)
   {
      Element desc = new Element("content");
      desc.addContent(SCLXMLUtil.createElement("string", "-"));
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
