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
 * @file CCLCommandSaveAs.java
 *
 * @brief Save as new file command.
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Asks the user to select a file to save changes to.
 */
public class CCLCommandSaveAs extends ACLCommand
{
   /** Main window */
   private CCLMainWindow mSource;

   /**
    * Asks the user to select a file to save changes to.
    *
    * @param source main window
    */
   public CCLCommandSaveAs(CCLMainWindow source)
   {
      super("Save As...");

      mSource = source;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      Display display = Display.getCurrent();
      Shell shell = new Shell(display);
      shell.setImage(new Image(shell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));

      FileDialog saveFileDialog = new FileDialog(shell, SWT.SAVE);
      saveFileDialog.setText("Save As...");
      // hack: not working without adding a static string to the ProjectRoot string (SWT)
      saveFileDialog.setFilterPath(SCLProject.getProjectRoot() + " ");
      String[] filterExt = { "*.xml", "*.*" };
      saveFileDialog.setFilterExtensions(filterExt);

      String fileName = saveFileDialog.open();

      if (null != fileName)
      {
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
}
