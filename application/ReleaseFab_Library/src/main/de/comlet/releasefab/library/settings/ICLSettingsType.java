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
 * @file ICLSettingsType.java
 *
 * @brief Interface of setting types.
 */

package de.comlet.releasefab.library.settings;

import java.util.Map;
import org.jdom2.Element;

public interface ICLSettingsType
{
   enum ECLAttributeToAdd
   {
      NONE, TYPE, NAME
   }

   Object getValue();

   Element getElement(String name, Map<ECLAttributeToAdd, String> attributesToAdd);

   Element getElement(String name);

   void setValue(Object value);

   void setValueFromElement(Element elemWithValue);

   Class<?> getContentClass();
}
