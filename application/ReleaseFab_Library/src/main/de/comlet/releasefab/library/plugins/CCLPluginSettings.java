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
 * @file CCLPluginSettings.java
 *
 * @brief Contains all settings.
 */

package de.comlet.releasefab.library.plugins;

import de.comlet.releasefab.api.plugin.ACLPluginSetting;
import de.comlet.releasefab.api.plugin.ICLPluginSetting;
import de.comlet.releasefab.library.settings.SCLSettings;
import de.comlet.releasefab.library.settings.SCLSettings.ECLSettingsType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Container for instances of {@link ICLPluginSetting}.
 */
public class CCLPluginSettings
{
   /** List of all project specific settings this plugin depends on. */
   protected List<ICLPluginSetting> mRequiredProjectSettings = new ArrayList<>();

   /** List of all user specific settings this plugin depends on. */
   protected List<ICLPluginSetting> mRequiredUserSettings = new ArrayList<>();

   /** Path to projectconfig.xml */
   private String mProjectSettingsPath;

   /**
    * Empty Constructor.
    */
   public CCLPluginSettings()
   {
   }

   /**
    * Default Constructor. Initialization with the path to the project settings
    * file.
    * 
    * @param projectSettingsPath
    */
   public CCLPluginSettings(String projectSettingsPath)
   {
      mProjectSettingsPath = projectSettingsPath;
   }

   /**
    * Returns the list of all project specific settings this plugin depends on.
    *
    * @return
    */
   public List<ICLPluginSetting> getRequiredProjectSettings()
   {
      return mRequiredProjectSettings;
   }

   /**
    * Adds a project specific setting of type <code>String</code> to the list of
    * settings this plugin depends on.
    *
    * @param name name of this setting. Used to get the value of this setting
    * @param label whenever this setting is shown in an options dialog, this
    * label is displayed
    * @param defaultValue default value for this setting
    * @param isVolatile set to <code>true</code> if this setting is volatile and
    * should not be persisted.
    */
   private void addRequiredProjectSetting(String name, String label, String defaultValue, boolean isVolatile)
   {
      Set<ECLSettingsType> settingType = EnumSet.of(ECLSettingsType.PROJECT);
      if (isVolatile)
      {
         settingType.add(ECLSettingsType.VOLATILE);
      }
      ACLPluginSetting<String> setting = new ACLPluginSetting<String>(name, label, defaultValue, settingType)
      {
      };
      addRequiredSetting(setting, mRequiredProjectSettings);
   }

   /**
    * Adds a project specific setting of type <code>List</code> to the list of
    * settings this plugin depends on.
    *
    * @param name name of this setting. Used to get the value of this setting
    * @param label whenever this setting is shown in an options dialog, this
    * label is displayed
    * @param defaultValue default value for this setting
    * @param isVolatile set to <code>true</code> if this setting is volatile and
    * should not be persisted.
    */
   public void addRequiredProjectSetting(String name, String label, List<String> defaultValue, boolean isVolatile)
   {
      Set<ECLSettingsType> settingType = EnumSet.of(ECLSettingsType.PROJECT);
      if (isVolatile)
      {
         settingType.add(ECLSettingsType.VOLATILE);
      }
      ACLPluginSetting<List<String>> setting = new ACLPluginSetting<List<String>>(name, label, defaultValue,
            settingType)
      {
      };
      addRequiredSetting(setting, mRequiredProjectSettings);
   }

   /**
    * Adds a project specific setting of type <code>String</code> to the list of
    * settings this plugin depends on.
    *
    * @param name name of this setting, this is used to get the value of this
    * setting
    * @param label whenever this setting is shown in an options dialog display
    * this label
    * @param defaultValue default value for this setting
    */
   public void addRequiredProjectSetting(String name, String label, String defaultValue)
   {
      addRequiredProjectSetting(name, label, defaultValue, false);
   }

   /**
    * Returns the list of all user specific settings this plugin depends on.
    *
    * @return
    */
   public List<ICLPluginSetting> getRequiredUserSettings()
   {
      return mRequiredUserSettings;
   }

   /**
    * Adds a user specific setting of type <code>String</code> to the list of
    * settings this plugin depends on.
    *
    * @param name name of this setting, this is used to get the value of this
    * setting
    * @param label whenever this setting is shown in an options dialog display
    * this label
    * @param defaultValue default value for this setting
    *
    * @param isVolatile set to <code>true</code> if this setting is volatile and
    * shouldn't be persist to disk
    */
   public void addRequiredUserSetting(String name, String label, String defaultValue, boolean isVolatile)
   {
      Set<ECLSettingsType> settingType = EnumSet.of(ECLSettingsType.USER);
      if (isVolatile)
      {
         settingType.add(ECLSettingsType.VOLATILE);
      }
      ACLPluginSetting<String> setting = new ACLPluginSetting<String>(name, label, defaultValue, settingType)
      {
      };
      addRequiredSetting(setting, mRequiredUserSettings);
   }

   /**
    * Adds a user specific setting of type <code>String</code> to the list of
    * settings this plugin depends on.
    *
    * @param name name of this setting, this is used to get the value of this
    * setting
    * @param label whenever this setting is shown in an options dialog display
    * this label
    * @param defaultValue default value for this setting
    */
   public void addRequiredUserSetting(String name, String label, String defaultValue)
   {
      addRequiredUserSetting(name, label, defaultValue, false);
   }

   /**
    * Adds a required setting to the list of settings.
    * 
    * @param <T> type of the setting
    * @param setting setting to be added
    * @param settingsList list of settings to add the setting to
    * @param settingsFileName name of the file containing the settings
    */
   private <T> void addRequiredSetting(ACLPluginSetting<T> setting, List<ICLPluginSetting> settingsList)
   {
      settingsList.add(setting);

      if (null == SCLSettings.get(setting.getName()))
      {
         SCLSettings.add(setting.getName(), setting.getDefaultValue(), setting.getSettingType());
      }
   }

   public String getProjectSettingsPath()
   {
      return mProjectSettingsPath;
   }

   public void setProjectSettingsPath(String projectSettingsPath)
   {
      this.mProjectSettingsPath = projectSettingsPath;
   }
}
