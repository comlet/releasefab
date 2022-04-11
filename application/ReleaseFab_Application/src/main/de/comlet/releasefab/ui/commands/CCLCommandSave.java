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
 * @file CCLCommandSave.java
 *
 * @brief Save open file command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.ui.CCLMainWindow;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import java.io.IOException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Saves all changes made to the opened file. If there is no opened file, the user 
 * is asked to select a new file to save the changes to (-> CCLCommandSaveAs).
 */
public class CCLCommandSave extends ACLCommand
{
   /** Main window */
   private CCLMainWindow mSource;

   /**
    * Saves all changes made to the opened file. If there is no opened file, the user 
    * is asked to select a new file to save the changes to (-> CCLCommandSaveAs).
    */
   public CCLCommandSave(CCLMainWindow source)
   {
      super("Save", "images/disk.png");

      mSource = source;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      Display display = Display.getCurrent();
      Shell shell = new Shell(display);
      shell.setImage(new Image(shell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));

      String fileName = SCLProject.getOpenFileName();

      if (null == fileName || fileName.isEmpty())
      {
         new CCLCommandSaveAs(mSource).widgetSelected(e);
         return;
      }

      try
      {
         SCLProject.save(fileName, SCLProject.getInstance().getDeliveries());

         mSource.setWindowTitle(SCLProject.getOpenFileName());

         MessageBox savedMessageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
         savedMessageBox.setMessage("All changes have been successfully saved.");
         savedMessageBox.setText("Saving successful");
         savedMessageBox.open();
      }
      catch (IOException | RuntimeException ex)
      {
         MessageBox errorMessageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
         errorMessageBox.setMessage(ex.getMessage());
         errorMessageBox.setText("Error");
         errorMessageBox.open();
         LOGGER.info("{}: widgetSelected - Error", this.getClass().getCanonicalName(), ex);
      }
   }
}
