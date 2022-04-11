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
 * @file ACLDetailedInformation.java
 *
 * @brief Abstract class for detailed information.
 */

package de.comlet.releasefab.api.plugin;

import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLObservableCollection;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class of detailed delivery information gathered by an
 * importer.<br>
 * Allows the graphical representation of delivery information.
 */
public abstract class ACLDetailedInformation
{
   /** Initialize logger for this class. */
   protected static final Logger LOGGER = LoggerFactory.getLogger(ACLDetailedInformation.class);

   /**
    * Name of the importer plugin that gathered the information we want to
    * display.
    */
   protected String mImporterName;

   /** Selected component. */
   protected CCLComponent mComponent;

   /** Selected delivery. */
   protected CCLDelivery mDelivery;

   /**
    * Constructor. Detailed information gathered by an importer plugin.<br>
    * Allows the graphical representation of delivery information.
    *
    * @param importerName
    * @param component
    * @param delivery
    */
   public ACLDetailedInformation(String importerName, CCLComponent component, CCLDelivery delivery)
   {
      mImporterName = importerName;
      mComponent = component;
      mDelivery = delivery;
   }

   /**
    * Fills the given composite of UI widgets with the information gathered by
    * the plugin.
    *
    * @param obj composite
    */
   public abstract void fillInfoBox(Object obj);

   /**
    * Saves changes to the model.
    *
    * @throws CCLInternalException
    */
   public abstract void saveInfoBox(CCLObservableCollection<CCLDelivery> deliveries)
         throws CCLInternalException, ParseException;
}
