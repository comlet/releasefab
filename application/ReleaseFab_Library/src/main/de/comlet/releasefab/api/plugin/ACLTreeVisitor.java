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
 * @file ACLTreeVisitor.java
 *
 * @brief Abstract class for Visitor Pattern.
 */

package de.comlet.releasefab.api.plugin;

import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.ICLVisitor;

/**
 * Visitor Pattern! Abstract class which is used for tree traversal. Code for
 * traversing over the tree is always the same, but the action is changing.
 * Possible actions are the removal or addition of components. This abstract
 * class allows the parameterization of this process. Generics are used because
 * the parameters can change depending on the action. E.g. the addition of a
 * delivery has an object of CCLDelivery as P2 and returns a boolean. The action
 * to find a component by its ID has an Integer as P2 and returns a
 * CCLComponent.
 */
public abstract class ACLTreeVisitor<RETURN, P2> implements ICLVisitor<RETURN, P2>
{
   @Override
   public RETURN visit(CCLComponent component, P2 target, boolean quickReturn)
   {
      RETURN res = null;

      for (CCLComponent subComponent : component.getSubComponents())
      {
         res = doIt(subComponent, target);

         // is it ok to return on the first hit? (improves the efficiency of the
         // search algorithm)
         // if it is ok, is there a result yet?
         if (quickReturn && null != res)
         {
            return res;
         }

         // Does this component have subcomponents?
         if (subComponent.hasSubComponents())
         {
            // recursion!
            res = subComponent.accept(this, target, quickReturn);

            if (quickReturn && null != res)
            {
               return res;
            }
         }
      }

      return res;
   }

   public abstract RETURN doIt(CCLComponent component, P2 target);
}
