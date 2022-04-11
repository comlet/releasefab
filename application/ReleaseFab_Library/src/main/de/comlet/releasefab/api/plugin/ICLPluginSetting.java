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
 * @file ICLPluginSetting.java
 *
 * @brief Interface for plugin settings.
 */

package de.comlet.releasefab.api.plugin;

import de.comlet.releasefab.library.settings.SCLSettings.ECLSettingsType;
import java.util.Set;

/**
 * Interface for a Setting which is used by a plugin.
 */
public interface ICLPluginSetting
{
   String getName();
   void setName(String name);

   String getLabel();
   void setLabel(String label);

   Object getDefaultValue();
   void setDefaultValue(Object defaultValue);

   Set<ECLSettingsType> getSettingType();
   void setSettingType(Set<ECLSettingsType> settingType);

   Class<?> getContentClass();

}
