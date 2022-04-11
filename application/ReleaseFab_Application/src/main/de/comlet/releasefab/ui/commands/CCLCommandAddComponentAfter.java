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
 * @file CCLCommandAddComponentAfter.java
 *
 * @brief Add component after command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.model.CCLComponent;
import java.util.List;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Adds a new component after the selected component.
 */
public class CCLCommandAddComponentAfter extends ACLCommand
{
   /** Selected component */
   private CCLComponent mComponent;

   /**
    * Adds a new component after the selected component.
    * 
    * @param component selected component
    */
   public CCLCommandAddComponentAfter(CCLComponent component)
   {
      super("Add component after");
      
      mComponent = component;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      CCLComponent parentComponent = mComponent.getParent();
      List<CCLComponent> parentSubComponents = parentComponent.getSubComponents();

      // get the position of the selected component
      int pos = parentSubComponents.indexOf(mComponent);

      // create a new component and add it to the list of subcomponents after
      // the selected component
      CCLComponent newComponent = SCLProject.getInstance().getInitialComponent();
      parentSubComponents.add(pos + 1, newComponent);
      newComponent.setParent(parentComponent);

      SCLProject.setNeedsSaving(true);
   }
}