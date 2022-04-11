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
 * @file ACLAssignmentStrategy.java
 *
 * @brief Abstract class for assignment strategies.
 */

package de.comlet.releasefab.api.plugin;

import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLParameter;
import java.util.List;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all assignment strategies. An assignment strategy is
 * responsible for the extraction of information from a specific source.
 */
public abstract class ACLAssignmentStrategy
{
   protected static final Logger LOGGER = LoggerFactory.getLogger(ACLAssignmentStrategy.class);

   /** Name of the assignment strategy */
   public abstract String getName();

   /**
    * Every derived class has to return the number of parameters, required for
    * the implemented assignment strategy
    * 
    * @return number of parameters, required for the implemented assignment
    * strategy
    */
   public abstract int getNrOfParameters();

   /**
    * Usage Information<br>
    * What does this assignment strategy do and which parameters are necessary?
    * 
    * @return usage info text
    */
   public abstract String getUsageInfo();

   /**
    * The parameters could be a filename and a regular expression for parsing.
    * The author of this method has to know the intent of the defined
    * parameters. Therefore it is very important to fill the information
    * (tool-tip) section of this property.
    * 
    * @param parameters
    * @param component
    * @param delivery
    * @param importer
    * @return
    */
   public abstract Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot, CCLComponent initialComponent);

   /**
    * Check if the number of parameters is correct for the assignment strategy
    */
   protected boolean wrongParameters(int param)
   {
      return (param < getNrOfParameters());
   }

   @Override
   public String toString()
   {
      return getName();
   }

   /**
    * Overload of method
    * {@link #getData(List, CCLComponent, CCLDelivery, CCLDelivery, ACLImportStrategy, String, CCLComponent)}
    * without an initial component.
    */
   public abstract Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot);
}
