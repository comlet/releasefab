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
 * @file SCLPluginLoader.java
 *
 * @brief Loads all plugins from the Modulepath.
 */

package de.comlet.releasefab;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.api.plugin.ICLPluginWithSettings;
import de.comlet.releasefab.library.model.SCLClassInstantiationHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public final class SCLPluginLoader
{
   /**
    * Map containing the only instance of all Import Strategies (Plugins).
    */
   private Map<String, ACLImportStrategy> mImportStrategiesMap = new HashMap<>();
   
   /** Whether the loading process is currently running or not. */
   private boolean mLoading;

   /**
    * Singleton Pattern with initialization.
    */
   private SCLPluginLoader()
   {
      loadAndInitPlugins();
   }

   private static final class SCLPluginLoaderSingleton
   {
      private static final SCLPluginLoader INSTANCE = new SCLPluginLoader();
      private SCLPluginLoaderSingleton()
      {
      }
   }

   public static SCLPluginLoader getInstance()
   {
      return SCLPluginLoaderSingleton.INSTANCE;
   }

   /**
    * Clear {@link #mImportStrategiesMap} and load the plugins.
    */
   private void loadAndInitPlugins()
   {
      if (!mLoading)
      {
         mLoading = true;
         clearPluginMaps();
         loadPlugins();
      }
      mLoading = false;
   }

   public Map<String, ACLImportStrategy> getImportStrategiesMap()
   {
      return mImportStrategiesMap;
   }

   /**
    * Clears {@link #mImportStrategiesMap}.
    */
   private void clearPluginMaps()
   {
      if (!mImportStrategiesMap.isEmpty())
      {
         mImportStrategiesMap.clear();
      }
   }

   /**
    * Loads all implementations of {@link ACLImportStrategy} from the Modulepath.
    */
   private void loadPlugins()
   {
      ServiceLoader<ACLImportStrategy> importStrategyLoader = ServiceLoader.load(ACLImportStrategy.class);
      
      for (ACLImportStrategy importStrat : importStrategyLoader)
      {
         mImportStrategiesMap.put(importStrat.getName(), importStrat);
      }
   }
   
   /**
    * Get an instance of {@link ACLDeliveryInformation} with the given name.
    * 
    * @param name Name of the implementation of {@link ACLDeliveryInformation} with the given name.
    * @return Instance of {@link ACLDeliveryInformation} matching the given name.
    */
   public ACLDeliveryInformation getDeliveryInformation(String name)
   {
      ACLDeliveryInformation delToReturn = null;
      ServiceLoader<ACLDeliveryInformation> deliveryInformationLoader = ServiceLoader.load(ACLDeliveryInformation.class);
      for (ACLDeliveryInformation delInfo : deliveryInformationLoader)
      {
         if (delInfo.getName().equals(name))
         {
            delToReturn = delInfo;
         }
      }
      
      return delToReturn;
   }
   
   /**
    * Get all implementations of {@link ICLPluginWithSettings}.
    * 
    * @return Collection of all instances that implement or inherit {@link ICLPluginWithSettings}.
    */
   public Collection<ICLPluginWithSettings> getPluginsWithSettings()
   {
      List<ICLPluginWithSettings> pluginsWithSettings = new ArrayList<>();
      for (ACLImportStrategy potentialCandidate : mImportStrategiesMap.values())
      {
         if (SCLClassInstantiationHelper.inheritsOrImplements(potentialCandidate.getClass(), ICLPluginWithSettings.class))
         {
            pluginsWithSettings.add((ICLPluginWithSettings) potentialCandidate);
         }
      }
      return pluginsWithSettings;
   }
}
