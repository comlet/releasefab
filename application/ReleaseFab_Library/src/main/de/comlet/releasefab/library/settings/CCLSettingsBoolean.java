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
 * @file CCLSettingsBoolean.java
 *
 * @brief Boolean setting class.
 */

package de.comlet.releasefab.library.settings;

import org.jdom2.Element;

/**
 * Implementation of {@link ACLSettingsForBaseTypes} for the base type
 * {@link Boolean}.
 */
public class CCLSettingsBoolean extends ACLSettingsForBaseTypes<Boolean>
{
   private static final Class<?> CONTENT_CLASS = Boolean.class;

   public CCLSettingsBoolean()
   {
      mValue = false;
   }

   @Override
   public void setValue(Object value)
   {
      if (value instanceof Boolean)
      {
         mValue = Boolean.class.cast(value);
      }
   }

   @Override
   public void setValueFromElement(Element elemWithValue)
   {
      mValue = Boolean.valueOf(elemWithValue.getTextTrim());
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
