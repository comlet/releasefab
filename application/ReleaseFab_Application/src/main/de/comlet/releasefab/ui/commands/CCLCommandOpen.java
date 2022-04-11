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
 * @file CCLCommandOpen.java
 *
 * @brief Open file command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.ui.CCLMainWindow;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import java.io.IOException;
import java.text.ParseException;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.jdom2.JDOMException;

/**
 * Opens a XML file if it has the correct format and creates the model.
 */
public class CCLCommandOpen extends ACLCommand
{
   /** Main window */
   private CCLMainWindow mSource;

   /**
    * Opens a XML-File if it has the correct format and creates the model.
    *
    * @param source main window
    */
   public CCLCommandOpen(CCLMainWindow source)
   {
      super("Open", "images/folder_page_white.png");

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
         FileDialog openFileDialog = new FileDialog(shell, SWT.OPEN);
         openFileDialog.setText("Open");
         // hack: not working without adding a static string to the ProjectRoot string (SWT)
         openFileDialog.setFilterPath(SCLProject.getProjectRoot() + " ");
         String[] filterExt = { "*.xml", "*.*" };
         openFileDialog.setFilterExtensions(filterExt);

         String fileName = openFileDialog.open();

         if (null != fileName && !fileName.isEmpty())
         {
            mSource.stopListeningForChanges();
            
            Set<String> missingPlugins = SCLProject.open(fileName);
            
            if (!missingPlugins.isEmpty())
            {
               MessageBox errorMessageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
               StringBuilder sb = new StringBuilder("The following plugins are referenced in the startup file but do not exist in the plugin folder: \n");
               for (String pluginName : missingPlugins)
               {
                  sb.append("- " + pluginName + "\n");
               }
               sb.append("\nThe information from these plugins will not be included in any export!");
               errorMessageBox.setMessage(sb.toString());
               errorMessageBox.setText("Missing Plugins");
               errorMessageBox.open();
            }

            mSource.setWindowTitle(SCLProject.getOpenFileName());

            // rebuild GUI
            mSource.fillTabFolder();
         }
      }
      catch (IOException |
             InstantiationException |
             IllegalAccessException |
             CCLInternalException |
             JDOMException |
             ParseException |
             RuntimeException ex)
      {
         MessageBox errorMessageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
         errorMessageBox.setMessage(ex.getMessage());
         errorMessageBox.setText("Loading failed");
         errorMessageBox.open();
         LOGGER.info("{}: widgetSelected - Loading failed", this.getClass().getCanonicalName(), ex);

         SCLProject.getInstance().reset();
      }
   }
}
