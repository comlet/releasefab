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
 * @file ACLSettingsForBaseTypes.java
 *
 * @brief Abstract setting class.
 */

package de.comlet.releasefab.library.settings;

import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Abstract base class of Settings for base types.
 */
public abstract class ACLSettingsForBaseTypes<T> implements ICLSettingsType
{
   protected T mValue;

   @Override
   public int hashCode()
   {
      return mValue.hashCode();
   }

   @Override
   public boolean equals(Object other)
   {
      return mValue.equals(other);
   }

   @Override
   public T getValue()
   {
      return mValue;
   }

   @Override
   public Element getElement(String name, Map<ECLAttributeToAdd, String> attributesToAdd)
   {
      return SCLXMLUtil.createElement(name,
            attributesToAdd.containsKey(ECLAttributeToAdd.NAME) ? new Attribute(CCLXMLConstants.XML_ATTRIBUTE_NAME, attributesToAdd.get(ECLAttributeToAdd.NAME)) : null,
            attributesToAdd.containsKey(ECLAttributeToAdd.TYPE) ? new Attribute(CCLXMLConstants.XML_ATTRIBUTE_TYPE, getContentClass().getName()) : null,
            mValue.toString());
   }

   @Override
   public Element getElement(String name)
   {
      return getElement(name, new HashMap<ECLAttributeToAdd, String>());
   }
}
