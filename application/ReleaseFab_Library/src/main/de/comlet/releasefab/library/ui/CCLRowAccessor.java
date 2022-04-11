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
 * @file CCLRowAccessor.java
 *
 * @brief Accessor of a GUI row.
 */

package de.comlet.releasefab.library.ui;

import org.jdom2.Element;
import org.jdom2.Text;

/**
 * Update XML-Element using the modified element from the table in the UI.
 */
public class CCLRowAccessor
{
   protected Element mElement;

   /**
    * Constructor. Initialized with the element to be modified.
    */
   CCLRowAccessor(Element element)
   {
      this.mElement = element;
   }

   /**
    * Update data.
    */
   void update(String tag, String newData)
   {
      mElement.getChild(tag).setContent(new Text(newData));
   }

   /**
    * Get data by Tag.
    */
   String get(String tag)
   {
      return mElement.getChildText(tag);
   }
}
