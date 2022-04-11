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
 * @file CCLCommandRemoveComponent.java
 *
 * @brief Remove component command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.model.CCLComponent;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Removes the selected component if that component has no delivery information
 * or subcomponents. Otherwise the user will be informed with an error message.
 */
public class CCLCommandRemoveComponent extends ACLCommand
{
   /** Selected component */
   private CCLComponent mComponent;

   /**
    * Removes the selected component if that component has no delivery
    * information or subcomponents. Otherwise the user will be informed with an
    * error message.
    * 
    * @param component
    */
   public CCLCommandRemoveComponent(CCLComponent component)
   {
      super("Remove component");

      mComponent = component;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      CCLComponent parentComponent = mComponent.getParent();
      List<CCLComponent> parentSubComponents = parentComponent.getSubComponents();

      String error = "";

      // check if this component has information
      if (mComponent.hasDeliveryInformation())
      {
         error = "It's not possible to delete a component with delivery information.\n";
      }

      // check if this component has subcomponents
      if (mComponent.hasSubComponents())
      {
         error += "It's not possible to delete a component with subcomponents. Please remove all subcomponents before removing this component.";
      }

      if (error.isEmpty())
      {
         // remove component if no error has occured
         parentSubComponents.remove(mComponent);
         parentComponent.setSubComponents(parentSubComponents);

         SCLProject.setNeedsSaving(true);
      }
      else
      {
         // inform the user that he's not able to remove this component
         Shell shell = new Shell(Display.getCurrent());
         MessageBox errorMessageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
         errorMessageBox.setMessage(error);
         errorMessageBox.setText("Error");
         errorMessageBox.open();
      }
   }
}
