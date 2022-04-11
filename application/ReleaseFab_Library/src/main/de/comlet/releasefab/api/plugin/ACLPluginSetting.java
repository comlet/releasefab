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
 * @file ACLPluginSetting.java
 *
 * @brief Abstract class for plugin settings.
 */

package de.comlet.releasefab.api.plugin;

import de.comlet.releasefab.library.model.SCLReflectionHelper;
import de.comlet.releasefab.library.settings.SCLSettings.ECLSettingsType;
import java.util.EnumSet;
import java.util.Set;

/**
 * Abstract base class of a plugin specific setting.
 */
public abstract class ACLPluginSetting<T> implements ICLPluginSetting
{
   /** Name of this setting */
   private String mName;

   /** Label of this setting */
   private String mLabel;

   /** Default value for this setting */
   private T mDefaultValue;

   private Set<ECLSettingsType> mSettingType = EnumSet.noneOf(ECLSettingsType.class);

   /**
    * Constructor. Initialize plugin specific setting.
    *
    * @param name
    * @param label
    * @param defaultValue
    */
   public ACLPluginSetting(String name, String label, T defaultValue, Set<ECLSettingsType> settingType)
   {
      mName = name;
      mLabel = label;
      mDefaultValue = defaultValue;
      mSettingType = settingType;
   }

   @Override
   public String getName()
   {
      return mName;
   }

   @Override
   public void setName(String name)
   {
      mName = name;
   }

   @Override
   public String getLabel()
   {
      return mLabel;
   }

   @Override
   public void setLabel(String label)
   {
      mLabel = label;
   }

   @Override
   public T getDefaultValue()
   {
      return mDefaultValue;
   }

   @Override @SuppressWarnings("unchecked")
   public void setDefaultValue(Object defaultValue)
   {
      if (getContentClass().equals(defaultValue.getClass()))
      {
         mDefaultValue = (T) defaultValue;
      }
   }

   @Override
   public Set<ECLSettingsType> getSettingType()
   {
      return mSettingType;
   }

   @Override
   public void setSettingType(Set<ECLSettingsType> settingType)
   {
      mSettingType = settingType;
   }

   @Override
   public Class<?> getContentClass()
   {
      return SCLReflectionHelper.getTypeArguments(ACLPluginSetting.class, getClass()).get(0);
   }
}
