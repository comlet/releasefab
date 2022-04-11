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
 * @file ACLAssignmentStrategyExt.java
 *
 * @brief Abstract class for external assignment strategies.
 */

package de.comlet.releasefab.api.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for external assignment strategies. An assignment
 * strategy is responsible for the extraction of information from a specific
 * source. External assignment strategies can be added to other plugins. They
 * can be registered to other plugins via the name of their module.
 */
public abstract class ACLAssignmentStrategyExt extends ACLAssignmentStrategy
{
   private String mName;
   private int mNumParams;
   private String mUsageInfo;
   private List<String> mExternalPlugins;

   /**
    * Constructor.
    */
   public ACLAssignmentStrategyExt(String name, int numParams, String usageInfo)
   {
      this.mName = name;
      this.mNumParams = numParams;
      this.mUsageInfo = usageInfo;
      this.mExternalPlugins = new ArrayList<String>();
   }

   /** Name of the assignment strategy */
   @Override
   public String getName()
   {
      return mName;
   }

   /**
    * Returns the number of parameters defined by the external assignment
    * strategy.
    */
   @Override
   public int getNrOfParameters()
   {
      return mNumParams;
   }

   /**
    * Returns the defined information on how to use the parameters of the
    * external assignment strategy.
    */
   @Override
   public String getUsageInfo()
   {
      return mUsageInfo;
   }

   /**
    * Returns a list of all module names to which an external assignment
    * strategy is registered to.
    * 
    * @return List of external plugins (the name of their modules)
    */
   public List<String> getExternalPlugins()
   {
      return mExternalPlugins;
   }

   /**
    * Adds the external assignment strategy to the module with the given name.
    * 
    * @param pluginToAdd Name of the plugin the external assignment strategy
    * wants to be added to
    */
   public void addToExternalPlugin(String pluginToAdd)
   {
      mExternalPlugins.add(pluginToAdd);
   }
}
