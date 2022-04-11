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
 * @file CCLCommandAbout.java
 *
 * @brief About Command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.ui.dialogs.CCLAboutDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CCLCommandAbout extends ACLCommand
{
   public CCLCommandAbout()
   {
      super("About");
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      Display display = Display.getCurrent();
      Shell shell = new Shell(display);
      
      CCLAboutDialog aboutDialog = new CCLAboutDialog(shell);
      aboutDialog.open();
   }
}
