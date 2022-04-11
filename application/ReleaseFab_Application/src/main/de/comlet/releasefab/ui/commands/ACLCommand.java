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
 * @file ACLCommand.java
 *
 * @brief Base class of commands.
 */

package de.comlet.releasefab.ui.commands;

import org.eclipse.swt.events.SelectionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass of a command for the graphical user interface that has a
 * title and an icon.
 */
public abstract class ACLCommand extends SelectionAdapter
{
   /** Initialize logger for this class */
   protected static final Logger LOGGER = LoggerFactory.getLogger(ACLCommand.class);
   
   /**
    * Title of the command. It will be displayed as label or as a tool tip.
    */
   private String mTitle;

   /**
    * Path to an image that should be used as the icon for this command.
    */
   private String mImagePath;

   /**
    * Standard constructor of an abstract command.
    * 
    * @param title text that will be displayed as a label or as a tool tip.
    */
   public ACLCommand(String title)
   {
      this(title, null);
   }

   /**
    * Common constructor of an abstract command.
    * 
    * @param title text that will be displayed as a label or as a tool tip
    * @param imagePath path to the image that will be used as an icon
    */
   public ACLCommand(String title, String imagePath)
   {
      mTitle = title;
      mImagePath = imagePath;
   }

   public String getTitle()
   {
      return mTitle;
   }

   public String getImagePath()
   {
      return mImagePath;
   }
}
