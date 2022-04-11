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
 * @file CCLCommandImportDelivery.java
 *
 * @brief Import delivery command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.ui.CCLMainWindow;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import java.io.File;
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
 * Imports deliveries.
 */
public class CCLCommandImportDelivery extends ACLCommand
{
   private CCLMainWindow mSource;

   /**
    * Imports deliveries.
    */
   public CCLCommandImportDelivery(CCLMainWindow source)
   {
      super("Import Deliveries");

      mSource = source;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      Shell shell = new Shell(Display.getCurrent());
      shell.setImage(new Image(shell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
      
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

            Set<String> missingPlugins = SCLProject.load(new File(fileName));

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
            
            // rebuild GUI
            mSource.fillTabFolder();
         }
      }
      catch (CCLInternalException |
             JDOMException |
             IOException |
             ParseException |
             RuntimeException ex)
      {
         mSource.startListeningForChanges();

         MessageBox errorMessageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
         errorMessageBox.setMessage(ex.getMessage());
         errorMessageBox.setText("Import failed");
         errorMessageBox.open();
         LOGGER.info("{}: widgetSelected - Import failed!", this.getClass().getCanonicalName(), ex);
      }
   }
}
