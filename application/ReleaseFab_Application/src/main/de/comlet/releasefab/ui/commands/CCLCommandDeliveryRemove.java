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
 * @file CCLCommandDeliveryRemove.java
 *
 * @brief Remove delivery command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.ui.dialogs.CCLDeliveryChooseDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Removes a delivery
 */
public class CCLCommandDeliveryRemove extends ACLCommand
{
   /**
    * Removes a delivery
    */
   public CCLCommandDeliveryRemove()
   {
      super("Remove delivery");
   }

   @Override
   public void widgetSelected(SelectionEvent arg0)
   {
      Display display = Display.getCurrent();
      Shell shell = new Shell(display);

      try
      {
         // get delivery that should be deleted
         CCLDeliveryChooseDialog dialog = new CCLDeliveryChooseDialog(shell, SCLProject.getInstance().getDeliveries());
         CCLDelivery delivery = dialog.open();

         if (null != delivery)
         {
            MessageBox confirmMessageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            confirmMessageBox.setMessage("Do you really want to delete this delivery?\n" + delivery.getName());
            confirmMessageBox.setText("Confirm");

            if (SWT.YES == confirmMessageBox.open())
            {
               // Remove delivery entry for every component which takes part of
               // delivery
               SCLProject.removeDelivery(SCLProject.getComponentRoot(), delivery);
            }
         }
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
