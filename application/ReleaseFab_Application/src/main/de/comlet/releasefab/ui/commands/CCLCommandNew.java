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
 * @file CCLCommandNew.java
 *
 * @brief New project command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.ui.CCLMainWindow;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Creates a new project.<br>
 * Warns the user if there are unsaved changes in the old project and allows
 * him/her to abort the creation.
 */
public class CCLCommandNew extends ACLCommand
{
   /** Main window */
   private CCLMainWindow mSource;

   /**
    * Creates a new project.<br>
    * Warns the user if there are unsaved changes in the old project and allows
    * the abortion of the process.
    *
    * @param source main window
    */
   public CCLCommandNew(CCLMainWindow source)
   {
      super("New", "images/page_white.png");

      mSource = source;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      Shell shell = new Shell(Display.getCurrent());
      shell.setImage(new Image(shell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));

      if (SCLProject.getNeedsSaving())
      {
         MessageBox savingMessageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
         savingMessageBox.setMessage("Data has been changed, do you want to save?");
         savingMessageBox.setText("Save changes?");

         if (SWT.YES == savingMessageBox.open())
         {
            new CCLCommandSave(mSource).widgetSelected(e);
         }
      }

      try
      {
         // if we want a new project we have to reset the project
         SCLProject.getInstance().reset();
      }
      catch (RuntimeException ex)
      {
         MessageBox errorMessageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
         errorMessageBox.setMessage(ex.getMessage());
         errorMessageBox.setText("Error");
         errorMessageBox.open();
         LOGGER.info("{}: widgetSelected - Error", this.getClass().getCanonicalName(), ex);
      }
   }
}
