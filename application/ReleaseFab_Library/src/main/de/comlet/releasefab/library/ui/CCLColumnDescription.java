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
 * @file CCLColumnDescription.java
 *
 * @brief Description of a GUI column.
 */

package de.comlet.releasefab.library.ui;

/**
 * Describes a column in the UI of the application.
 */
public class CCLColumnDescription
{
   private String mDescription;
   private String mXmlTag;
   private boolean mEditable;
   private int mWidth;

   /**
    * Constructor. Describes a column in the UI of the application.
    */
   public CCLColumnDescription(String description,
                             String xmlTag,
                             int width,
                             boolean editable    )
   {
      this.mDescription = description;
      this.mXmlTag = xmlTag;
      this.mEditable = editable;
      this.mWidth = width;
   }

   public String getDescription()
   {
      return mDescription;
   }

   public String getXmlTag()
   {
      return mXmlTag;
   }

   public boolean getEditable()
   {
      return mEditable;
   }

   public int getWidth()
   {
      return mWidth;
   }
}
