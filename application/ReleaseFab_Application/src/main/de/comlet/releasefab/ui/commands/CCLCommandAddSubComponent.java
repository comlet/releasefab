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
 * @file CCLCommandAddSubComponent.java
 *
 * @brief Add sub-component command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.model.CCLComponent;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Adds a new component as subcomponent of the selected component.
 */
public class CCLCommandAddSubComponent extends ACLCommand
{
   /** Selected component */
   private CCLComponent mParentComponent;

   /**
    * Adds a new component as subcomponent of the selected component.
    * 
    * @param parentComponent selected parent component
    */
   public CCLCommandAddSubComponent(CCLComponent parentComponent)
   {
      super("Add subcomponent");
      
      mParentComponent = parentComponent;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      CCLComponent component = SCLProject.getInstance().getInitialComponent();
      mParentComponent.getSubComponents().add(component);
      component.setParent(mParentComponent);

      SCLProject.setNeedsSaving(true);
   }
}
