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
 * @file CCLCommandExit.java
 *
 * @brief Exit application command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Is called when the user closes the application by pressing the close button
 * of the main window or the exit button in the main menu.<br>
 * Warns the user if there are unsaved changes and allows him/her to abort
 * closing.
 */
public class CCLCommandExit extends ACLCommand implements Listener
{
   /**
    * Is called when the user closes the application by pressing the close
    * button of the main window or the exit button in the main menu.<br>
    * Warns the user if there are unsaved changes and allows the abortion 
    * of the process.
    */
   public CCLCommandExit()
   {
      super("Exit");
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      close(null);
   }

   @Override
   public void handleEvent(Event e)
   {
      close(e);
   }

   /**
    * Closes the application.<br>
    * Warns the user if there are unsaved changes and allows the abortion 
    * of the process.
    *
    * @param e
    */
   private void close(Event e)
   {
      if (SCLProject.getNeedsSaving())
      {
         Display display = Display.getCurrent();
         Shell shell = new Shell(display);

         MessageBox closeMessageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
         closeMessageBox.setMessage("Do you really want to close ReleaseFab without saving? All changes will be lost.");
         closeMessageBox.setText("Close without saving?");

         int res = closeMessageBox.open();

         if (res == SWT.NO)
         {
            // abort closing
            if (null != e)
            {
               e.doit = false;
            }

            return;
         }
      }

      Display.getCurrent().dispose();
      System.exit(0);
   }
}
