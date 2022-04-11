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
 * @file SCLProjectHelper.java
 *
 * @brief Helper class for main project class.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLTreeVisitor;
import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Utility class containing functionality which is used in different modules.
 */
public final class SCLProjectHelper
{
   /**
    * Formatter to convert from {@link String} to {@link SimpleDateFormat} and
    * vice versa
    */
   private static SimpleDateFormat sDateFormatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS' GMT'XXX");

   private SCLProjectHelper()
   {
   }

   public static SimpleDateFormat getDateFormatter()
   {
      return sDateFormatter;
   }

   /**
    * Returns the absolute path to a file.
    *
    * @param path file location
    * @return absolute file location
    */
   public static String getAbsoluteFilePath(String path, String projectRoot)
   {
      String absoluteFilePath = path;
      absoluteFilePath = absoluteFilePath.replace('/', File.separatorChar);
      if (absoluteFilePath.startsWith("." + File.separator))
      {
         absoluteFilePath = path.substring(1);
         absoluteFilePath = projectRoot + absoluteFilePath;
      }

      return absoluteFilePath;
   }

   /**
    * Returns a component by name.
    *
    * @param component parent component to start search
    * @param name name of component to look for
    * @return component or null if it does not exist
    */
   public static CCLComponent getComponentByName(CCLComponent component, String name)
   {
      // visitor definition
      class VisitorGetComponent extends ACLTreeVisitor<CCLComponent, String>
      {
         @Override
         public CCLComponent doIt(CCLComponent component, String name)
         {
            if (component.getName().equals(name))
            {
               return component;
            }
            return null;
         }
      }
      // end of visitor definition

      return component.<CCLComponent, String>accept(new VisitorGetComponent(), name, true);
   }

   /**
    * Marks delivery information as new if it is different from the information
    * in the delivery prior to the current one or if it is the first delivery.
    *
    * @param component
    * @param delivery
    * @param importer
    * @param deliveryInformation
    */
   public static void markDeliveryInformationIfNew(CCLObservableCollection<CCLDelivery> deliveries,
         CCLComponent component, CCLDelivery delivery, String importerName, ACLDeliveryInformation deliveryInformation)
   {
      // get latest delivery prior to the given delivery
      CCLDelivery latestDelivery = null;

      // the set of deliveries is sorted by date of creation
      // (descending), so it is enough to just find the first delivery
      // that was created before the given one
      for (CCLDelivery del : deliveries)
      {
         if (del.getCreated().before(delivery.getCreated()))
         {
            latestDelivery = del;
            break;
         }
      }

      if (null != component)
      {
         ACLDeliveryInformation info = null;

         // get delivery information of the latest delivery
         if (null != latestDelivery)
         {
            info = component.getDeliveryInformation(latestDelivery.getName() + importerName);
         }

         // compare this delivery information to the one from the latest delivery
         // and mark it as new if they are different
         deliveryInformation.setNew(deliveryInformation.hasChanged(info));
      }
   }
}
