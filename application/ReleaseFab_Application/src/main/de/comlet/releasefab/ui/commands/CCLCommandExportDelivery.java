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
 * @file CCLCommandExportDelivery.java
 *
 * @brief Export delivery command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.ui.dialogs.CCLDeliveryListDialog;
import java.io.IOException;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Exports a delivery or a set of deliveries
 */
public class CCLCommandExportDelivery extends ACLCommand
{
   public CCLCommandExportDelivery()
   {
      super("Export Deliveries");
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      Display display = Display.getCurrent();
      Shell shell = new Shell(display);

      try
      {
         // get list of deliveries that should be exported
         CCLDeliveryListDialog dialog = new CCLDeliveryListDialog(shell, SCLProject.getInstance().getDeliveries());
         Set<CCLDelivery> deliveries = dialog.open();

         if (null != deliveries && !deliveries.isEmpty())
         {
            // ask user to chose location for export file
            FileDialog saveFileDialog = new FileDialog(shell, SWT.SAVE);
            saveFileDialog.setText("Save As...");
            // hack: not working without adding a static string to the ProjectRoot string (SWT)
            saveFileDialog.setFilterPath(SCLProject.getProjectRoot() + " ");
            saveFileDialog.setFileName("delivery.xml");
            String[] filterExt = { "*.xml", "*.*" };
            saveFileDialog.setFilterExtensions(filterExt);

            String fileName = saveFileDialog.open();

            if (null != fileName && !fileName.isEmpty())
            {
               // export deliveries
               SCLProject.save(fileName, deliveries);

               MessageBox savedMessageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
               savedMessageBox.setMessage("Delivery export finsished successfully!");
               savedMessageBox.setText("Delivery Export");
               savedMessageBox.open();
            }
         }
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
