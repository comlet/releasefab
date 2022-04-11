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
 * @file CCLSettingsMap.java
 *
 * @brief Map setting class.
 */

package de.comlet.releasefab.library.settings;

import de.comlet.releasefab.library.model.SCLClassInstantiationHelper;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains a {@link Map} used to store settings in a file.
 */
public class CCLSettingsMap implements ICLSettingsType
{
   private static final Class<?> CONTENT_CLASS = Map.class;
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLSettingsMap.class);

   private Map<ICLSettingsType, ICLSettingsType> mValue = new HashMap<>();

   @Override
   public Map<Object, Object> getValue()
   {
      Map<Object, Object> toReturn = new HashMap<>();
      for (Entry<ICLSettingsType, ICLSettingsType> entry : mValue.entrySet())
      {
         toReturn.put(entry.getKey().getValue(), entry.getValue().getValue());
      }
      return toReturn;
   }

   @Override
   public void setValue(Object value)
   {
      mValue.clear();
      if (SCLClassInstantiationHelper.inheritsOrImplements(value.getClass(), CONTENT_CLASS))
      {
         for (Entry<?, ?> e  : ((Map<?, ?>) value).entrySet())
         {
            mValue.put(SCLSettingsTransformer.createFromObject(e.getKey()), SCLSettingsTransformer.createFromObject(e.getValue()));
         }
      }
   }

   @Override
   public Element getElement(String name, Map<ECLAttributeToAdd, String> attributesToAdd)
   {
      Element element = SCLXMLUtil.createElement(name,
            attributesToAdd.containsKey(ECLAttributeToAdd.NAME) ? new Attribute(CCLXMLConstants.XML_ATTRIBUTE_NAME, attributesToAdd.get(ECLAttributeToAdd.NAME)) : null,
            attributesToAdd.containsKey(ECLAttributeToAdd.TYPE) ? new Attribute(CCLXMLConstants.XML_ATTRIBUTE_TYPE, CONTENT_CLASS.getName()) : null);

      if (!mValue.isEmpty())
      {
         ICLSettingsType keyValue = mValue.keySet().iterator().next();
         element.setAttribute(CCLXMLConstants.XML_ATTRIBUTE_KEY_TYPE, keyValue.getContentClass().getName());
         ICLSettingsType aValue = mValue.get(keyValue);
         element.setAttribute(CCLXMLConstants.XML_ATTRIBUTE_VALUE_TYPE, aValue.getContentClass().getName());
      }

      for (Entry<ICLSettingsType, ICLSettingsType> entry : mValue.entrySet())
      {
         Element item = SCLXMLUtil.createElement(CCLXMLConstants.XML_ITEM);
         Element key = entry.getKey().getElement(CCLXMLConstants.XML_MAP_KEY);
         item.addContent(key);
         Element value = entry.getValue().getElement(CCLXMLConstants.XML_MAP_VALUE);
         item.addContent(value);
         element.addContent(item);
      }
      return element;
   }

   @Override
   public Element getElement(String name)
   {
      return getElement(name, new HashMap<ECLAttributeToAdd, String>());
   }

   private static Class<?> getClassFromElement(Element element, String type)
   {
      Class<?> theClass = String.class;
      if (element.getName().equals(CCLXMLConstants.XML_SETTING) &&
            element.getAttributeValue(CCLXMLConstants.XML_ATTRIBUTE_TYPE).equals(CONTENT_CLASS.getName()))
      {
         theClass = SCLClassInstantiationHelper.getClassFromAttributeOfElement(element, type);
      }
      return theClass;
   }

   public static Class<?> getKeyClassFromElement(Element element)
   {
      return getClassFromElement(element, CCLXMLConstants.XML_ATTRIBUTE_KEY_TYPE);
   }

   public static Class<?> getValueClassFromElement(Element element)
   {
      return getClassFromElement(element, CCLXMLConstants.XML_ATTRIBUTE_VALUE_TYPE);
   }

   @Override
   public void setValueFromElement(Element elemWithValue)
   {
      mValue.clear();
      final Class<?> keyType = getKeyClassFromElement(elemWithValue);
      final Class<?> valueType = getValueClassFromElement(elemWithValue);
      final Class<? extends ICLSettingsType> keySettingsClass = SCLSettingsTransformer.findSettingsClass(keyType);
      final Class<? extends ICLSettingsType> valueSettingsClass = SCLSettingsTransformer.findSettingsClass(valueType);
      for (Element elem : elemWithValue.getChildren(CCLXMLConstants.XML_ITEM))
      {
         ICLSettingsType key = SCLSettingsTransformer.createSettingsObject(keySettingsClass);
         ICLSettingsType value = SCLSettingsTransformer.createSettingsObject(valueSettingsClass);
         if (null != key && null != value)
         {
            if (null != elem.getChild(CCLXMLConstants.XML_MAP_KEY))
            {
               key.setValueFromElement(elem.getChild(CCLXMLConstants.XML_MAP_KEY));
               value.setValueFromElement(elem.getChild(CCLXMLConstants.XML_MAP_VALUE));
            }
            else // no child element "key" found so handle old Map style
            {
               LOGGER.trace("Old styled map detected! Try to convert from old map style!");
               key = SCLSettingsTransformer.createSettingsObject(CCLSettingsString.class);
               key.setValue(elem.getAttribute("name").getValue());
               value.setValueFromElement(elem);
            }
            mValue.put(key, value);
         }
      }
   }

   @Override
   public Class<?> getContentClass()
   {
      return getClassForContent();
   }

   public static Class<?> getClassForContent()
   {
      return CONTENT_CLASS;
   }

}
