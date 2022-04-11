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
 * @file CCLSettingsTuple.java
 *
 * @brief Tuple setting class.
 */

package de.comlet.releasefab.library.settings;

import de.comlet.releasefab.library.model.CCLTuple;
import de.comlet.releasefab.library.model.SCLClassInstantiationHelper;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Contains a {@link CCLTuple} used to store settings in a file.
 */
public class CCLSettingsTuple implements ICLSettingsType
{
   private static final Class<?> CONTENT_CLASS = CCLTuple.class;

   private CCLTuple<ICLSettingsType, ICLSettingsType> mValue = new CCLTuple<>();

   @Override
   public CCLTuple<Object, Object> getValue()
   {
      return new CCLTuple<>(mValue.getFirst().getValue(), mValue.getSecond().getValue());
   }

   @Override
   public void setValue(Object value)
   {
      if (SCLClassInstantiationHelper.inheritsOrImplements(value.getClass(), CONTENT_CLASS))
      {
         CCLTuple<?, ?> tuple = CCLTuple.class.cast(value);
         mValue.setFirst(SCLSettingsTransformer.createFromObject(tuple.getFirst()));
         mValue.setSecond(SCLSettingsTransformer.createFromObject(tuple.getSecond()));
      }
   }

   @Override
   public Element getElement(String name, Map<ECLAttributeToAdd, String> attributesToAdd)
   {
      Element element = SCLXMLUtil.createElement(name,
            attributesToAdd.containsKey(ECLAttributeToAdd.NAME) ? new Attribute(CCLXMLConstants.XML_ATTRIBUTE_NAME, attributesToAdd.get(ECLAttributeToAdd.NAME)) : null,
            attributesToAdd.containsKey(ECLAttributeToAdd.TYPE) ? new Attribute(CCLXMLConstants.XML_ATTRIBUTE_TYPE, CONTENT_CLASS.getName()) : null);

      EnumMap<ECLAttributeToAdd, String> attributeMap = new EnumMap<>(ECLAttributeToAdd.class);
      attributeMap.put(ECLAttributeToAdd.TYPE, null);
      element.addContent(mValue.getFirst().getElement(CCLXMLConstants.XML_TUPLE_FIRST, attributeMap));
      element.addContent(mValue.getSecond().getElement(CCLXMLConstants.XML_TUPLE_SECOND, attributeMap));
      return element;
   }

   @Override
   public Element getElement(String name)
   {
      return getElement(name, new HashMap<ECLAttributeToAdd, String>());
   }

   @Override
   public void setValueFromElement(Element elemWithValue)
   {
      mValue.setFirst(SCLSettingsTransformer.createFromElement(elemWithValue.getChild(CCLXMLConstants.XML_TUPLE_FIRST)));
      mValue.setSecond(SCLSettingsTransformer.createFromElement(elemWithValue.getChild(CCLXMLConstants.XML_TUPLE_SECOND)));
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
