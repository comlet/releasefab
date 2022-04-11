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
 * @file SCLSettingsTransformer.java
 *
 * @brief Transform settings to XML.
 */

package de.comlet.releasefab.library.settings;

import de.comlet.releasefab.library.model.SCLClassInstantiationHelper;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms the persisted settings from a file into the internal data types.
 */
public final class SCLSettingsTransformer
{
   private static final Logger LOGGER = LoggerFactory.getLogger(SCLSettingsTransformer.class);

   private static List<Class<? extends ICLSettingsType>> sSettingsList = Arrays.asList(
         CCLSettingsString.class,
         CCLSettingsInteger.class,
         CCLSettingsList.class,
         CCLSettingsMap.class,
         CCLSettingsBoolean.class,
         CCLSettingsTuple.class
         );

   private SCLSettingsTransformer()
   {
   }

   public static boolean addNewSettingsType(Class<? extends ICLSettingsType> newType)
   {
      return sSettingsList.add(newType);
   }

   public static ICLSettingsType createFromObject(Object value)
   {
      ICLSettingsType theCreated = null;
      if (null != value)
      {
         if (!ICLSettingsType.class.isInstance(value))
         {
            Class<? extends ICLSettingsType> settingsClass = findSettingsClass(value.getClass());
            theCreated = createSettingsObject(settingsClass, value);
         }
         else
         {
            theCreated = ICLSettingsType.class.cast(value);
         }
      }
      return theCreated;
   }

   public static ICLSettingsType createFromElement(Element element)
   {
      Class<?> contentClass = SCLClassInstantiationHelper.getClassFromAttributeOfElement(element, CCLXMLConstants.XML_ATTRIBUTE_TYPE);
      return createFromElement(element, contentClass);
   }

   public static ICLSettingsType createFromElement(Element element, Class<?> contentClass)
   {
      ICLSettingsType createdSetting = null;
      Class<? extends ICLSettingsType> settingsClass = findSettingsClass(contentClass);
      createdSetting = createSettingsObject(settingsClass);
      if (null != createdSetting)
      {
         createdSetting.setValueFromElement(element);
      }
      return createdSetting;
   }

   public static <T> T getValueFromSetting(ICLSettingsType setting, Class<T> type)
   {
      T toReturn = null;
      if (null != setting &&
            SCLClassInstantiationHelper.inheritsOrImplements(setting.getContentClass(), type))
      {
         toReturn = type.cast(setting.getValue());
      }
      return toReturn;
   }

   public static Class<? extends ICLSettingsType> findSettingsClass(Class<?> type)
   {
      Class<? extends ICLSettingsType> settingsClass = null;
      for (Class<? extends ICLSettingsType> theClass : sSettingsList)
      {
         Class<?> test;
         try
         {
            test = Class.class.cast(theClass.getDeclaredMethod("getClassForContent").invoke(null));
            if (SCLClassInstantiationHelper.inheritsOrImplements(type, test))
            {
               settingsClass = theClass;
            }
         }
         catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
               NoSuchMethodException | SecurityException e)
         {
            LOGGER.error("Can't find settings class for {}", type.getName(), e);
         }
      }
      return settingsClass;
   }

   public static ICLSettingsType createSettingsObject(Class<? extends ICLSettingsType> settingsClass)
   {
      ICLSettingsType theCreated = null;
      if (null != settingsClass)
      {
         try
         {
            theCreated = settingsClass.getDeclaredConstructor().newInstance();
         }
         catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
               InvocationTargetException | NoSuchMethodException | SecurityException e)
         {
            LOGGER.error("Creating Settings object failed for {}", settingsClass.getName(), e);
         }
      }
      return theCreated;
   }

   private static ICLSettingsType createSettingsObject(Class<? extends ICLSettingsType> settingsClass, Object value)
   {
      ICLSettingsType theCreated = createSettingsObject(settingsClass);
      if (null != theCreated)
      {
         theCreated.setValue(value);
      }
      return theCreated;
   }

}
