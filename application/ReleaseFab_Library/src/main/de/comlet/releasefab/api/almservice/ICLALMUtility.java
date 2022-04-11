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
 * @file ICLALMUtility.java
 *
 * @brief Interface for ALM utility.
 */

package de.comlet.releasefab.api.almservice;

import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.settings.SCLSettings;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interface for Application Lifecycle Management System Services.
 */
public interface ICLALMUtility extends AutoCloseable
{
   /**
    * Filter and sort ALM Items according to custom specifications.
    * 
    * @param source ALM Items to be sorted
    * @return The filtered and sorted Iterable of ICLALMItemContainer
    * @throws CCLInternalException
    */
   Iterable<ICLALMItemContainer> filterAndSortTrackerItems(Iterable<ICLALMItemContainer> source)
         throws CCLInternalException;

   /**
    * Get ALM item containers with the specified IDs.
    * 
    * @throws CCLInternalException
    */
   Iterable<ICLALMItemContainer> getTrackerItemIterable(Iterable<String> source, Set<String> filterList)
         throws CCLInternalException;

   /**
    * Method is overridden here, because it should not be throwing exceptions.
    * This enables an easier usage.
    */
   @Override
   void close();

   /**
    * Retrieves the allowed search status configured in an external file.
    */
   static Set<String> getSearchStatus(String nameOfSearchStatusSetting)
   {
      return getList(nameOfSearchStatusSetting);
   }

   /**
    * Check ALM item: Does it exist and is its status part of the filter list?
    */
   boolean checkTrackerItem(String itemId);

   /**
    * Retrieves the allowed import search status strings configured in an
    * external file.
    */
   static Set<String> getImportSearchStatus(String nameOfImportStatusSetting)
   {
      return getList(nameOfImportStatusSetting);
   }

   /**
    * Retrieves the allowed export search status strings configured in an
    * external file.
    */
   static Set<String> getExportSearchStatus(String nameOfExportStatusSetting)
   {
      return getList(nameOfExportStatusSetting);
   }

   /**
    * Retrieves a setting by its name.
    */
   private static HashSet<String> getList(String type)
   {
      List<String> temp = SCLSettings.getStringList(type, EnumSet.of(SCLSettings.ECLSettingsType.PROJECT));

      HashSet<String> list = new HashSet<>();
      list.addAll(temp);
      return list;
   }
}
