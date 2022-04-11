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
 * @file CCLCommitSortTransformer.java
 *
 * @brief Sort and transform Git commits.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.plugin.ACLSortTransformer;
import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.git.classes.CCLGitCommitContainer;

/**
 * Sorts a list of Git commits.
 */
public class CCLCommitSortTransformer extends ACLSortTransformer<ICLCommitContainer>
{
   /**
    * Create a collection from the input and sort items.
    */
   CCLCommitSortTransformer(Iterable<ICLCommitContainer> source)
   {
      super(source);
   }

   /**
    * Provide sort order for the container class.
    */
   @Override
   protected int compare(ICLCommitContainer c1, ICLCommitContainer c2)
   {
      return ((CCLGitCommitContainer) c2).getCommitTime() - ((CCLGitCommitContainer) c1).getCommitTime();
   }
}
