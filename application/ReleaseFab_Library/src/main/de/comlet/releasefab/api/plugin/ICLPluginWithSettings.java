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
 * @file ICLPluginWithSettings.java
 *
 * @brief Interface for plugin dependent on settings.
 */

package de.comlet.releasefab.api.plugin;

import java.util.List;

/**
 * Interface for a plugin which is reliant on settings.
 */
public interface ICLPluginWithSettings extends ICLPlugin
{
   /**
    * Returns the list of all project specific settings this plugin depends on.
    *
    * @return
    */
   List<ICLPluginSetting> getRequiredProjectSettings();

   /**
    * Returns the list of all user specific settings this plugin depends on.
    *
    * @return
    */
   List<ICLPluginSetting> getRequiredUserSettings();

   /**
   * Adds an instance of {@link #CCLPluginSettings} to the Plugin
   * 
   * @param projectSettingsPath Path to projectconfig.xml
   */
   void addPluginSettings(String projectSettingsPath);
}
