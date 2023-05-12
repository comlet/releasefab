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
 * @file SCLSettings.java
 *
 * @brief Container of all settings.
 */

package de.comlet.releasefab.library.settings;

import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLTuple;
import de.comlet.releasefab.library.settings.ICLSettingsType.ECLAttributeToAdd;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_NAME;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_CONFIGURATION;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_SETTING;

/**
 * Loads the settings files. Differentiates between general settings, project
 * specific settings and user specific settings.
 */
public final class SCLSettings
{
   private static final String UNCHECKED = "unchecked";

   /**
    * Indicates if the settings are project or user specific or if they are
    * general settings.
    */
   public enum ECLSettingsType
   {
      GENERAL(1 << 0), PROJECT(1 << 1), USER(1 << 2), VOLATILE(1 << 3);

      private final int mSettingsTypeValue;

      ECLSettingsType(int settingsTypeValue)
      {
         mSettingsTypeValue = settingsTypeValue;
      }

      public int getSettingsTypeValue()
      {
         return mSettingsTypeValue;
      }
   }

   /** General settings */
   private static Map<String, ICLSettingsType> sGeneralSettings = new LinkedHashMap<>();

   /** Project specific settings */
   private static Map<String, ICLSettingsType> sProjectSettings = new LinkedHashMap<>();

   /** User specific settings */
   private static Map<String, ICLSettingsType> sUserSettings = new LinkedHashMap<>();

   /** Volatile settings */
   /** Used for storing passwords */
   private static Map<String, ICLSettingsType> sVolatileSettings = new LinkedHashMap<>();

   /** Init logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(SCLSettings.class);

   /** Hide default constructor */
   private SCLSettings()
   {
   }
   
   public static void clearSettings()
   {
      sGeneralSettings.clear();
      sProjectSettings.clear();
      sUserSettings.clear();
      sVolatileSettings.clear();
   }

   public static Set<ECLSettingsType> getSettingTypes(int settingType)
   {
      EnumSet<ECLSettingsType> settingTypes = EnumSet.noneOf(ECLSettingsType.class);
      for (ECLSettingsType st : ECLSettingsType.values())
      {
         int flagValue = st.getSettingsTypeValue();
         if ((flagValue & settingType) == flagValue)
         {
            settingTypes.add(st);
         }
      }
      return settingTypes;
   }

   public static int getSettingsTypeValue(Set<ECLSettingsType> flags)
   {
      int settingTypeValue = 0;
      for (ECLSettingsType st : flags)
      {
         settingTypeValue |= st.getSettingsTypeValue();
      }
      return settingTypeValue;
   }

   /**
    * Gets the setting value with the given name from one of the setting
    * maps.<br>
    * If a setting with the given name exists in more than one map, user
    * settings will override plugin settings, plugin settings will override
    * project settings and project settings will override general settings.
    *
    * @param settingName
    * @param clazz Class specifying the data type of the setting
    * @return setting value or null if no setting with the given name and type
    * could be found
    */
   public static <T> T getT(String settingName, Class<T> clazz)
   {
      ICLSettingsType setting;

      // first look if there is a user setting with the given name
      if (sUserSettings.containsKey(settingName))
      {
         setting = sUserSettings.get(settingName);
      }
      // if there is no user setting with the given name, look if there is a
      // project setting
      else if (sProjectSettings.containsKey(settingName))
      {
         setting = sProjectSettings.get(settingName);
      }
      // if there is no project setting with the given name, look if there is a
      // general setting
      else if (sGeneralSettings.containsKey(settingName))
      {
         setting = sGeneralSettings.get(settingName);
      }
      else
      {
         setting = sVolatileSettings.get(settingName);
      }
      return SCLSettingsTransformer.getValueFromSetting(setting, clazz);
   }

   private static Map<String, ICLSettingsType> getSettingsMap(Set<ECLSettingsType> settingsType, boolean volatileValid)
   {
      Map<String, ICLSettingsType> selectedMap = null;
      // if VOLATILE flag is set in settings type we need to provide
      // sVolatileSettings
      if (volatileValid && settingsType.contains(ECLSettingsType.VOLATILE))
      {
         selectedMap = sVolatileSettings;
      }
      else if (settingsType.contains(ECLSettingsType.USER))
      {
         selectedMap = sUserSettings;
      }
      else if (settingsType.contains(ECLSettingsType.PROJECT))
      {
         selectedMap = sProjectSettings;
      }
      else if (settingsType.contains(ECLSettingsType.GENERAL))
      {
         selectedMap = sGeneralSettings;
      }
      return selectedMap;
   }

   private static Map<String, ICLSettingsType> getSettingsMap(Set<ECLSettingsType> settingsType)
   {
      return getSettingsMap(settingsType, true);
   }

   /**
    * Gets the setting value with the given name from the settings map of the
    * given type.
    *
    * @param settingName
    * @param settingsType
    * @param clazz Class specifying the data type of the setting
    * @return setting value or null if no setting with the given name and type
    * could be found
    */
   public static <T> T getT(String settingName, Set<ECLSettingsType> settingsType, Class<T> clazz)
   {
      Map<String, ICLSettingsType> settingsMap = getSettingsMap(settingsType);
      ICLSettingsType setting = null;
      if (null != settingsMap)
      {
         setting = settingsMap.get(settingName);
      }

      return SCLSettingsTransformer.getValueFromSetting(setting, clazz);
   }

   /**
    * Gets the list setting with the given name from one of the setting
    * maps.<br>
    * If a setting with the given name exists in more than one map, user
    * settings will override plugin settings, plugin settings will override
    * project settings and project settings will override general settings.
    *
    *
    * @param settingName
    * @return setting value or null if no setting with the given name and type
    * could be found
    */
   @SuppressWarnings(UNCHECKED)
   public static <T> List<T> getListT(String settingName)
   {
      // Due to generic type erasure there is no parameterized List class -e.g.
      // List<String>.class - hence the "hacky" cast
      List<T> list = getT(settingName, (Class<List<T>>) (Class<?>) List.class);
      return list != null ? list : Collections.<T>emptyList();
   }

   /**
    * Gets the list setting value with the given name from the settings map of
    * the given type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value or null if no setting with the given name and type
    * could be found
    */
   @SuppressWarnings(UNCHECKED)
   public static <T> List<T> getListT(String settingName, Set<ECLSettingsType> settingsType)
   {
      // Due to generic type erasure there is no parameterized Map class -e.g.
      // Map<String, String>.class - hence the "hacky" cast
      List<T> list = getT(settingName, settingsType, (Class<List<T>>) (Class<?>) List.class);
      return list != null ? list : Collections.<T>emptyList();
   }

   /**
    * Gets the map setting with the given name from one of the setting maps.<br>
    * If a setting with the given name exists in more than one map, user
    * settings will override plugin settings, plugin settings will override
    * project settings and project settings will override general settings.
    *
    * @param settingName
    * @param settingsType
    * @return setting value or null if no setting with the given name and type
    * could be found
    */
   @SuppressWarnings(UNCHECKED)
   public static <T> Map<String, T> getMapT(String settingName)
   {
      // Due to generic type erasure there is no real parameterized Map class, -
      // e.g. Map<String, String>.class - hence the "hacky" cast.
      Map<String, T> map = getT(settingName, (Class<Map<String, T>>) (Class<?>) Map.class);
      return map != null ? map : Collections.<String, T>emptyMap();
   }

   /**
    * Gets the map setting value with the given name from the settings map of
    * the given type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value or null if no setting with the given name and type
    * could be found
    */

   @SuppressWarnings(UNCHECKED)
   public static <T> Map<String, T> getMapT(String settingName, Set<ECLSettingsType> settingsType)
   {
      // Due to generic type erasure there is no parameterized List class -e.g.
      // List<String>.class - hence the "hacky" cast
      Map<String, T> map = getT(settingName, settingsType, (Class<Map<String, T>>) (Class<?>) Map.class);
      return map != null ? map : Collections.<String, T>emptyMap();
   }

   /**
    * Get setting value with the given name from one of the setting lists.<br>
    * If a setting with the given name exists in more than one list, user
    * settings will override plugin settings, plugin settings will override
    * project settings and project settings will override general settings.
    *
    * @param settingName
    * @return setting value as string or null if no setting with the given name
    * could be found
    */
   public static String get(String settingName)
   {
      return getT(settingName, String.class);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as string or null if no setting with the given name
    * could be found
    */
   public static String get(String settingName, Set<ECLSettingsType> settingsType)
   {
      return getT(settingName, settingsType, String.class);
   }

   /**
    * Get setting value with the given name.<br>
    * If a setting with the given name exists in more than one list, user
    * settings will override plugin settings, plugin settings will override
    * project settings and project settings will override general settings.
    *
    * @param settingName
    * @return setting value as integer
    */
   public static int getInt(String settingName)
   {
      Integer i = getT(settingName, int.class);

      if (i == null)
      {
         try
         {
            i = Integer.parseInt(getT(settingName, String.class));
         }
         catch (NumberFormatException e)
         {
            i = null;
         }
      }

      return i;
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as integer
    */
   public static int getInt(String settingName, Set<ECLSettingsType> settingsType)
   {
      Integer i = getT(settingName, settingsType, int.class);

      if (i == null)
      {
         try
         {
            i = Integer.parseInt(getT(settingName, settingsType, String.class));
         }
         catch (NumberFormatException e)
         {
            i = null;
         }
      }

      return i;
   }

   /**
    * Get setting value with the given name.<br>
    * If a setting with the given name exists in more than one list, user
    * settings will override plugin settings, plugin settings will override
    * project settings and project settings will override general settings.
    *
    * @param settingName
    * @return setting value as List of Strings
    */
   public static List<String> getStringList(String settingName)
   {
      List<String> list = getListT(settingName);
      return Collections.unmodifiableList(list);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as List of Strings
    */
   public static List<String> getStringList(String settingName, Set<ECLSettingsType> settingsType)
   {
      List<String> list = getListT(settingName, settingsType);
      return Collections.unmodifiableList(list);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as List of Integers
    */
   public static List<Integer> getIntList(String settingName)
   {
      List<Integer> list = getListT(settingName);
      return Collections.unmodifiableList(list);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as List of Integers
    */
   public static List<Integer> getIntList(String settingName, Set<ECLSettingsType> settingsType)
   {
      List<Integer> list = getListT(settingName, settingsType);
      return Collections.unmodifiableList(list);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as List of Integers
    */
   public static <F, S> List<CCLTuple<F, S>> getTupleList(String settingName)
   {
      List<CCLTuple<F, S>> list = getListT(settingName);
      return Collections.unmodifiableList(list);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as List of Integers
    */
   public static <F, S> List<CCLTuple<F, S>> getTupleList(String settingName, Set<ECLSettingsType> settingsType)
   {
      List<CCLTuple<F, S>> list = getListT(settingName, settingsType);
      return Collections.unmodifiableList(list);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as Map from String to String
    */
   public static Map<String, String> getStringMap(String settingName)
   {
      Map<String, String> map = getMapT(settingName);
      return Collections.unmodifiableMap(map);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as Map from String to String
    */
   public static Map<String, String> getStringMap(String settingName, Set<ECLSettingsType> settingsType)
   {
      Map<String, String> map = getMapT(settingName, settingsType);
      return Collections.unmodifiableMap(map);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as Map from String to Integer
    */
   public static Map<String, Integer> getIntegerMap(String settingName)
   {
      Map<String, Integer> map = getMapT(settingName);
      return Collections.unmodifiableMap(map);
   }

   /**
    * Get setting value with the given name from the settings list of the given
    * type.
    *
    * @param settingName
    * @param settingsType
    * @return setting value as Map from String to Integer
    */
   public static Map<String, Integer> getIntegerMap(String settingName, Set<ECLSettingsType> settingsType)
   {
      Map<String, Integer> map = getMapT(settingName, settingsType);
      return Collections.unmodifiableMap(map);
   }

   /**
    * Adds a setting with the given name and value to the list of settings of
    * the given type.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    * @param settingsType
    */
   private static void addObject(String settingName, Object settingValue, Set<ECLSettingsType> settingsType)
   {
      Map<String, ICLSettingsType> settingsMap = getSettingsMap(settingsType);
      if (null != settingsMap)
      {
         settingsMap.put(settingName, SCLSettingsTransformer.createFromObject(settingValue));
      }
   }

   /**
    * Adds a setting with the given name and value to the list of settings of
    * the given type.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    * @param settingsType
    */
   public static <T> void add(String settingName, T settingValue, Set<ECLSettingsType> settingsType)
   {
      addObject(settingName, settingValue, settingsType);
   }

   /**
    * Adds a setting with the given name and value to the list of settings of
    * the given type.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    * @param settingsType
    */
   public static void addInt(String settingName, int settingValue, Set<ECLSettingsType> settingsType)
   {
      addObject(settingName, settingValue, settingsType);
   }

   /**
    * Adds a setting with the given name and value to the list of settings of
    * the given type.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    * @param settingsType
    */
   public static void addList(String settingName, List<?> settingValue, Set<ECLSettingsType> settingsType)
   {
      addObject(settingName, settingValue, settingsType);
   }

   /**
    * Adds a setting with the given name and value to the list of settings of
    * the given type.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    * @param settingsType
    */
   public static void addMap(String settingName, Map<String, ?> settingValue, Set<ECLSettingsType> settingsType)
   {
      addObject(settingName, settingValue, settingsType);
   }

   /**
    * Adds a setting with the given name and value to the list of general
    * settings.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    */
   public static void addGeneralSetting(String settingName, String settingValue)
   {
      addObject(settingName, settingValue, EnumSet.of(ECLSettingsType.GENERAL));
   }

   /**
    * Adds a setting with the given name and value to the list of general
    * settings.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    */
   public static void addGeneralSetting(String settingName, int settingValue)
   {
      addObject(settingName, Integer.toString(settingValue), EnumSet.of(ECLSettingsType.GENERAL));
   }

   /**
    * Adds a setting with the given name and value to the list of project
    * specific settings.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    */
   public static void addProjectSetting(String settingName, String settingValue)
   {
      addObject(settingName, settingValue, EnumSet.of(ECLSettingsType.PROJECT));
   }

   /**
    * Adds a setting with the given name and value to the list of project
    * specific settings.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    */
   public static void addProjectSetting(String settingName, int settingValue)
   {
      addObject(settingName, Integer.toString(settingValue), EnumSet.of(ECLSettingsType.PROJECT));
   }

   /**
    * Adds a setting with the given name and value to the list of user specific
    * settings.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    */
   public static void addUserSetting(String settingName, String settingValue)
   {
      addObject(settingName, settingValue, EnumSet.of(ECLSettingsType.USER));
   }

   /**
    * Adds a setting with the given name and value to the list of user specific
    * settings.<br>
    * If there is already a setting with this name, the value will be
    * overwritten with the new one.
    *
    * @param settingName
    * @param settingValue
    */
   public static void addUserSetting(String settingName, int settingValue)
   {
      addObject(settingName, Integer.toString(settingValue), EnumSet.of(ECLSettingsType.USER));
   }
   
   public static void loadSettingsFromCLI(Map<String, String> credentials)
   {
      add(CCLXMLConstants.XML_ALM_USER_KEY, credentials.get(CCLXMLConstants.XML_ALM_USER_KEY),
            EnumSet.of(ECLSettingsType.VOLATILE));
      add(CCLXMLConstants.XML_ALM_PASSWORD_KEY, credentials.get(CCLXMLConstants.XML_ALM_PASSWORD_KEY),
            EnumSet.of(ECLSettingsType.VOLATILE));
      add(CCLXMLConstants.XML_ALM_SERVER, credentials.get(CCLXMLConstants.XML_ALM_SERVER),
            EnumSet.of(ECLSettingsType.PROJECT));
   }

   /**
    * Loads settings from the file with the given file path.
    *
    * @param filePath
    * @throws CCLInternalException
    * @throws IOException
    * @throws JDOMException
    */
   public static void loadSettings(String filePath, ECLSettingsType settingsType)
         throws JDOMException, IOException, CCLInternalException
   {
      loadSettings(new File(filePath), settingsType);
   }

   /**
    * Loads settings from the given file.
    *
    * @param file settings file
    * @throws IOException
    * @throws JDOMException
    * @throws CCLInternalException
    */
   public static void loadSettings(File file, ECLSettingsType settingsType)
         throws JDOMException, IOException, CCLInternalException
   {
      // check if the given file really exists
      if (file.exists())
      {
         Document doc = SCLXMLUtil.loadDocument(file);
         Element rootNode = doc.getRootElement();

         // check file format
         if (!rootNode.getName().equals(XML_CONFIGURATION))
         {
            throw new CCLInternalException("Wrong format! The specified settings file (" + file.getAbsolutePath() + 
                  ") has an unknown file format.");
         }

         // load all key value settings
         for (Element child : rootNode.getChildren(XML_SETTING))
         {
            String settingName = child.getAttributeValue(XML_ATTRIBUTE_NAME);
            addObject(settingName, SCLSettingsTransformer.createFromElement(child), EnumSet.of(settingsType));
         }
      }
   }

   /**
    * Loads general settings from the file with the given file path.<br>
    * Returns the same result as calling loadSettings with GENERAL as
    * settingsType.
    *
    * @param filePath path to general settings file
    * @throws CCLInternalException
    * @throws IOException
    * @throws JDOMException
    */
   public static void loadGeneralSettings(String filePath) throws CCLInternalException
   {
      try
      {
         loadSettings(new File(filePath), ECLSettingsType.GENERAL);
      }
      catch (JDOMException | IOException | CCLInternalException | RuntimeException e)
      {
         LOGGER.error("Couldn't load general settings. {}", e.getMessage(), e);
         throw new CCLInternalException("{}.loadGeneralSettings(java.io.File) failed!");
      }
   }

   /**
    * Loads general settings from the given file.<br>
    * Returns the same result as calling loadSettings with GENERAL as
    * settingsType.
    *
    * @param file general settings file
    * @throws CCLInternalException
    * @throws IOException
    * @throws JDOMException
    */
   public static void loadGeneralSettings(File file) throws CCLInternalException
   {
      try
      {
         loadSettings(file, ECLSettingsType.GENERAL);
      }
      catch (JDOMException | IOException | CCLInternalException | RuntimeException e)
      {
         LOGGER.error("Couldn't load general settings. {}", e.getMessage(), e);
         throw new CCLInternalException("{}.loadGeneralSettings(java.io.File) failed!");
      }
   }

   /**
    * Loads project specific settings from the file with the given file
    * path.<br>
    * Returns the same result as calling loadSettings with PROJECT as
    * settingsType.
    *
    * @param filePath path to project specific settings file
    * @throws CCLInternalException
    * @throws IOException
    * @throws JDOMException
    */
   public static void loadProjectSettings(String filePath) throws JDOMException, IOException, CCLInternalException
   {
      loadSettings(new File(filePath), ECLSettingsType.PROJECT);
   }

   /**
    * Loads project specific settings from the given file.<br>
    * Returns the same result as calling loadSettings with PROJECT as
    * settingsType.
    *
    * @param file project specific settings file
    * @throws CCLInternalException
    * @throws IOException
    * @throws JDOMException
    */
   public static void loadProjectSettings(File file) throws JDOMException, IOException, CCLInternalException
   {
      loadSettings(file, ECLSettingsType.PROJECT);
   }

   /**
    * Loads user specific settings from the file with the given file path.<br>
    * Returns the same result as calling loadSettings with USER as settingsType.
    *
    * @param filePath path to user specific settings file
    * @throws CCLInternalException
    * @throws IOException
    * @throws JDOMException
    */
   public static void loadUserSettings(String filePath) throws JDOMException, IOException, CCLInternalException
   {
      loadSettings(new File(filePath), ECLSettingsType.USER);
   }

   /**
    * Loads user specific settings from the given file.<br>
    * Returns the same result as calling loadSettings with USER as settingsType.
    *
    * @param file user specific settings file
    * @throws CCLInternalException
    * @throws IOException
    * @throws JDOMException
    */
   public static void loadUserSettings(File file) throws JDOMException, IOException, CCLInternalException
   {
      loadSettings(file, ECLSettingsType.USER);
   }

   /**
    * Saves the settings of the given type to the file with the given file path.
    *
    * @param filePath
    * @param settingsType
    * @throws CCLInternalException
    * @throws IOException
    */
   public static void save(String filePath, ECLSettingsType settingsType) throws IOException, CCLInternalException
   {
      save(new File(filePath), settingsType);
   }

   /**
    * Saves the settings of the given type to the given file.
    *
    * @param file
    * @param settingsType
    * @throws IOException
    * @throws CCLInternalException
    */
   public static void save(File file, ECLSettingsType settingsType) throws IOException, CCLInternalException
   {
      Map<String, ICLSettingsType> settings;

      if (ECLSettingsType.USER == settingsType)
      {
         settings = sUserSettings;
      }
      else if (ECLSettingsType.PROJECT == settingsType)
      {
         settings = sProjectSettings;
      }
      else
      {
         settings = sGeneralSettings;
      }

      Document doc = new Document();
      Element root = new Element(XML_CONFIGURATION);
      doc.setRootElement(root);

      for (Entry<String, ICLSettingsType> entry : settings.entrySet())
      {
         final Map<ECLAttributeToAdd, String> attributeMap = new EnumMap<>(ECLAttributeToAdd.class);
         attributeMap.put(ECLAttributeToAdd.NAME, entry.getKey());
         attributeMap.put(ECLAttributeToAdd.TYPE, null);
         root.addContent(entry.getValue().getElement(XML_SETTING, attributeMap));
      }

      SCLXMLUtil.saveDocument(file, doc);
   }

   /**
    * Returns the classes simple name as lower case String.
    *
    * @param clazz
    * @return
    */
   public static String toLowerCaseSimple(Class<?> clazz)
   {
      return clazz.getSimpleName().toLowerCase();
   }

   /**
    * Saves general settings to the file with the given file path.
    *
    * @param filePath
    * @throws CCLInternalException
    * @throws IOException
    */
   public static void saveGeneralSettings(String filePath) throws IOException, CCLInternalException
   {
      save(new File(filePath), ECLSettingsType.GENERAL);
   }

   /**
    * Saves general settings to the given file.
    *
    * @param file
    * @throws CCLInternalException
    * @throws IOException
    */
   public static void saveGeneralSettings(File file) throws IOException, CCLInternalException
   {
      save(file, ECLSettingsType.GENERAL);
   }

   /**
    * Saves project specific settings to the file with the given file path.
    *
    * @param filePath
    * @throws CCLInternalException
    * @throws IOException
    */
   public static void saveProjectSettings(String filePath) throws IOException, CCLInternalException
   {
      save(new File(filePath), ECLSettingsType.PROJECT);
   }

   /**
    * Saves project specific settings to the given file.
    *
    * @param file
    * @throws CCLInternalException
    * @throws IOException
    */
   public static void saveProjectSettings(File file) throws IOException, CCLInternalException
   {
      save(file, ECLSettingsType.PROJECT);
   }

   /**
    * Saves user specific settings to the file with the given file path.
    *
    * @param filePath
    * @throws FileSystemException
    */
   public static void saveUserSettings(String filePath)
   {
      try
      {
         save(new File(filePath), ECLSettingsType.USER);
      }
      catch (IOException | CCLInternalException | RuntimeException fse)
      {
         LOGGER.error("Could not save user settings: {}", fse.getMessage(), fse);
      }
   }

   /**
    * Saves user specific settings to the given file.
    *
    * @param file
    * @throws CCLInternalException
    * @throws IOException
    */
   public static void saveUserSettings(File file) throws IOException, CCLInternalException
   {
      save(file, ECLSettingsType.USER);
   }
}
