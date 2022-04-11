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
 * @file CCLCommandDeliveryAdd.java
 *
 * @brief Add new delivery command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.ui.CCLMainWindow;
import de.comlet.releasefab.ui.dialogs.CCLDeliveryCreateDialog;
import de.comlet.releasefab.ui.dialogs.CCLProgressDialog;
import de.comlet.releasefab.ui.dialogs.CCLProgressDialog.ACLBackgroundWorker;
import de.comlet.releasefab.ui.dialogs.CCLReportDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Adds a new delivery.
 */
public class CCLCommandDeliveryAdd extends ACLCommand
{
   private CCLMainWindow mSource;

   /**
    * Adds a new delivery.
    *
    * @param source
    */
   public CCLCommandDeliveryAdd(CCLMainWindow source)
   {
      super("Add new delivery");

      mSource = source;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      final Display display = Display.getCurrent();
      Shell shell = new Shell(display);

      try
      {
         CCLDeliveryCreateDialog dialog = new CCLDeliveryCreateDialog(shell);
         final CCLDelivery delivery = dialog.open();

         if (null != delivery)
         {
            // check if a delivery with the given name already exists
            if (SCLProject.getInstance().checkDeliveryExists(delivery.getName()))
            {
               throw new CCLInternalException("A delivery with this name already exists.");
            }

            // we have to prevent the gui from listening for changes in the
            // model, because we don't want the gui to update until we're done
            mSource.stopListeningForChanges();

            final CCLProgressDialog progress = new CCLProgressDialog(shell);

            // start background worker thread and open progress dialog
            progress.open(new ACLBackgroundWorker(progress)
            {
               @Override
               public void doWork()
               {
                  // add the new delivery to the project
                  SCLProject.getInstance().getDeliveries().add(delivery);
                  SCLProject.addDeliveries(SCLProject.getComponentRoot(), delivery);
               }
            });

            // if something went wrong, show a dialog with the report what
            // happened and remove the delivery from the project
            if (0 < SCLProject.getInstance().getCreationReport().getContentSize())
            {
               SCLProject.getInstance().getDeliveries().remove(delivery);
               SCLProject.removeDelivery(SCLProject.getComponentRoot(), delivery);

               CCLReportDialog reportDialog = new CCLReportDialog(shell, SCLProject.getInstance().getCreationReport());
               reportDialog.open();
            }

            // re-enable gui's update mechanism
            mSource.startListeningForChanges();

            // refresh gui
            mSource.refresh();
         }
      }
      catch (CCLInternalException | RuntimeException ex)
      {
         MessageBox errorMessageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
         errorMessageBox.setMessage(ex.getMessage());
         errorMessageBox.setText("Error");
         errorMessageBox.open();
         LOGGER.info("{}: widgetSelected - Error", this.getClass().getCanonicalName(), ex);
      }
   }
}
