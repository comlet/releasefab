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
 * @file CCLCommandComponentRelevant.java
 *
 * @brief Make component customer relevant command.
 */

package de.comlet.releasefab.ui.commands;

import de.comlet.releasefab.library.model.CCLComponent;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Allows the user to choose whether or not the selected component is relevant
 * for customers.
 */
public class CCLCommandComponentRelevant extends ACLCommand
{
   /** Selected component */
   private final CCLComponent mComponent;

   /**
    * Allows the user to choose whether or not the selected component is relevant
    * for customers.
    * 
    * @param component selected component
    */
   public CCLCommandComponentRelevant(CCLComponent component)
   {
      super("Costomer relevant");
      
      mComponent = component;
   }

   @Override
   public void widgetSelected(SelectionEvent e)
   {
      mComponent.setIsCustomerRelevant(!mComponent.getIsCustomerRelevant());
   }
}
