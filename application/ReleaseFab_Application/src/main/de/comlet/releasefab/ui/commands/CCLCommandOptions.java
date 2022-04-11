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
 * @file CCLCommandOptions.java
 *
 * @brief Open options dialog command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.ui.dialogs.CCLOptionsDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Opens the options dialog that allows users to edit project and user specific
 * settings.
 */
public class CCLCommandOptions extends ACLCommand
{
   /**
    * Opens the options dialog that allows users to edit project and user
    * specific settings.
    */
   public CCLCommandOptions()
   {
      super("Options");
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      Shell shell = new Shell(Display.getCurrent());
      CCLOptionsDialog optionsDialog = new CCLOptionsDialog(shell);
      optionsDialog.open();
   }
}
