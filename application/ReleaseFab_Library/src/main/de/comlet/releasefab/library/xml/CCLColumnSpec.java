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
 * @file CCLColumnSpec.java
 *
 * @brief Specification of an XML column.
 */

package de.comlet.releasefab.library.xml;

/**
 * Container which describes a table column.
 */
public class CCLColumnSpec
{
   private String mName;
   private int mWidth;

   /**
    * Default Constructor.
    */
   public CCLColumnSpec(String name, int width)
   {
      this.mName = name;
      this.mWidth = width;
   }

   public String getName()
   {
      return mName;
   }

   public int getWidth()
   {
      return mWidth;
   }
}
