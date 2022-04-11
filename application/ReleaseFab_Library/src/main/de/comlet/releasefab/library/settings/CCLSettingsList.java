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
 * @file CCLSettingsList.java
 *
 * @brief List setting class.
 */

package de.comlet.releasefab.library.settings;

import de.comlet.releasefab.library.model.SCLClassInstantiationHelper;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Contains a {@link List} used to store settings in a file.
 */
public class CCLSettingsList implements ICLSettingsType
{
   private static final Class<?> CONTENT_CLASS = List.class;

   private List<ICLSettingsType> mValue = new ArrayList<>();

   @Override
   public List<Object> getValue()
   {
      List<Object> toReturn = new ArrayList<>();
      for (ICLSettingsType value : mValue)
      {
         toReturn.add(value.getValue());
      }
      return toReturn;
   }

   @Override
   public void setValue(Object value)
   {
      mValue.clear();
      if (SCLClassInstantiationHelper.inheritsOrImplements(value.getClass(), CONTENT_CLASS))
      {
         for (Object o : List.class.cast(value))
         {
            mValue.add(SCLSettingsTransformer.createFromObject(o));
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
         ICLSettingsType firstElement = mValue.get(0);
         element.setAttribute(CCLXMLConstants.XML_ATTRIBUTE_CONTENT_TYPE, firstElement.getContentClass().getName());
      }

      for (ICLSettingsType obj : mValue)
      {
         Element item = obj.getElement(CCLXMLConstants.XML_ITEM);
         element.addContent(item);
      }
      return element;
   }

   @Override
   public Element getElement(String name)
   {
      return getElement(name, new HashMap<ECLAttributeToAdd, String>());
   }

   private Class<?> getClassFromElement(Element element)
   {
      Class<?> theClass = String.class;
      if (element.getName().equals(CCLXMLConstants.XML_SETTING) &&
            element.getAttributeValue(CCLXMLConstants.XML_ATTRIBUTE_TYPE).equals(CONTENT_CLASS.getName()))
      {
         theClass = SCLClassInstantiationHelper.getClassFromAttributeOfElement(element, CCLXMLConstants.XML_ATTRIBUTE_CONTENT_TYPE);
      }
      return theClass;
   }

   @Override
   public void setValueFromElement(Element elemWithValue)
   {
      mValue.clear();
      final Class<?> valueType = getClassFromElement(elemWithValue);
      final Class<? extends ICLSettingsType> settingsClass = SCLSettingsTransformer.findSettingsClass(valueType);
      for (Element e : elemWithValue.getChildren(CCLXMLConstants.XML_ITEM))
      {
         ICLSettingsType item = SCLSettingsTransformer.createSettingsObject(settingsClass);
         if (null != item)
         {
            item.setValueFromElement(e);
            mValue.add(item);
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
