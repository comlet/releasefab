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
 * @file CCLCommandExportDocbook.java
 *
 * @brief Export docbook command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.ui.dialogs.CCLDeliveryListDialog;
import de.comlet.releasefab.ui.dialogs.CCLProgressDialog;
import de.comlet.releasefab.ui.dialogs.CCLProgressDialog.ACLBackgroundWorker;
import java.io.IOException;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Export to docbook.
 */
public class CCLCommandExportDocbook extends ACLCommand
{
   /**
    * Export to docbook.
    */
   public CCLCommandExportDocbook()
   {
      super("Export Docbook");
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      Display display = Display.getCurrent();
      Shell shell = new Shell(display);
      
      // get list of deliveries that should be exported
      CCLDeliveryListDialog dialog = new CCLDeliveryListDialog(shell, SCLProject.getInstance().getDeliveries());
      final Set<CCLDelivery> deliveries = dialog.open();
      final boolean isForCustomer = dialog.isForCustomer();

      if (null != deliveries && !deliveries.isEmpty())
      {
         final String fileName = getFilenameForDocbookExport(shell);

         if (null != fileName && !fileName.isEmpty())
         {
            try
            {
               doDocbookExport(shell, deliveries, isForCustomer, fileName);
            }
            catch (CCLInternalException ex)
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

   /**
    * Opens a Dialog to enter a filename and location to save the exported Docbook-File.
    * 
    * @param shell The shell to open the FileDialog with
    * @return An absolute path to export the Docbook-File to
    */
   private String getFilenameForDocbookExport(Shell shell)
   {
      // ask user to chose location for export file
      FileDialog saveFileDialog = new FileDialog(shell, SWT.SAVE);
      saveFileDialog.setText("Save As...");
      // hack: not working without adding a static string to the
      // ProjectRoot string (SWT)
      saveFileDialog.setFilterPath(SCLProject.getProjectRoot() + " ");
      saveFileDialog.setFileName("export_docbook.xml");
      String[] filterExt = { "*.xml", "*.*" };
      saveFileDialog.setFilterExtensions(filterExt);

      return saveFileDialog.open();
   }

   /**
    * Starts the export of the Docbook-File on a background thread.
    * Also handles possible exceptions and shows a success dialog.
    * 
    * @param shell The shell to open the progress dialog with
    * @param deliveries The selected deliveries to be exported
    * @param isForCustomer User selection if the export is for a customer or not
    * @param fileName The absolute path where the Docbook-File shall be exported to
    * @throws CCLInternalException
    */
   private void doDocbookExport(Shell shell, final Set<CCLDelivery> deliveries, final boolean isForCustomer,
         final String fileName) throws CCLInternalException
   {
      final CCLProgressDialog progress = new CCLProgressDialog(shell);

      // define background worker
      ACLBackgroundWorker worker = new ACLBackgroundWorker(progress)
      {
         @Override
         public void doWork()
         {
            try
            {
               // export docbook
               SCLProject.exportDocbook(fileName, deliveries, isForCustomer);
               setSucceeded(true);
            }
            catch (IOException | RuntimeException e)
            {
               setSucceeded(false);
               setResult(e);
            }
         }
      };

      // start background worker thread and open progress dialog
      progress.open(worker);

      // error handling if export failed
      if (!worker.isSucceeded())
      {
         String exceptionMessage = "Unknown error";

         Object result = worker.getResult();
         if (result instanceof Exception)
         {
            Exception exception = (Exception) result;
            String message = exception.getMessage();
            if (null != message && !message.isEmpty())
            {
               exceptionMessage = message;
            }
            else
            {
               exceptionMessage = exception.toString();
            }
         }

         throw new CCLInternalException(exceptionMessage);
      }

      MessageBox savedMessageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
      savedMessageBox.setMessage("Docbook export finsished successfully!");
      savedMessageBox.setText("Docbook Export");
      savedMessageBox.open();
   }
}
