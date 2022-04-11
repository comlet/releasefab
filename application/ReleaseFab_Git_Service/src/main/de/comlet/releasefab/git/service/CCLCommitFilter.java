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
 * @file CCLCommitFilter.java
 *
 * @brief Filter for Git commits.
 */

package de.comlet.releasefab.git.service;

import de.comlet.releasefab.api.almservice.ICLALMUtility;
import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.git.classes.CCLGitCommitContainer;
import de.comlet.releasefab.library.model.CCLIteratorBase;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Filter commits. ALM item status must match one of the configured ALM status.
 */
public class CCLCommitFilter implements Iterable<ICLCommitContainer>
{
   Iterable<ICLCommitContainer> mSource;
   ICLALMUtility mALMutil;

   /**
    * Constructor. Remember source Iterable and ALM handler.
    */
   public CCLCommitFilter(Iterable<ICLCommitContainer> source, ICLALMUtility almUtil)
   {
      this.mALMutil = almUtil;
      this.mSource = source;
   }

   /**
    * Iterate over source and filter all commits, that do not match the required
    * ALM item state.
    */
   private class CommitFilterIterator extends CCLIteratorBase<ICLCommitContainer>
   {
      private HashSet<String> mAllowedIds;
      private HashSet<String> mFilteredIds;

      /**
       * Remember source iterator. Initialize HashSets.
       */
      protected CommitFilterIterator(Iterator<ICLCommitContainer> source)
      {
         super(source);
         this.mAllowedIds = new HashSet<>();
         this.mFilteredIds = new HashSet<>();
      }

      /**
       * Check if there is another item available.
       */
      @Override
      public boolean hasNext()
      {
         findNext();
         return super.hasNext();
      }

      /**
       * Find next item which matches one of the configured status.
       */
      protected void findNext()
      {
         while (mSource.hasNext() && (this.mNextItem == null))
         {
            CCLGitCommitContainer cc = (CCLGitCommitContainer) mSource.next();
            final String itemId = cc.getCommitId();

            if (mAllowedIds.contains(itemId))
            {
               this.mNextItem = cc; // accept
               break;
            }
            else if (!mFilteredIds.contains(itemId))
            {
               // check item in ALM Service
               if (CCLCommitFilter.this.checkTrackerItem(cc.getCommitId()))
               {
                  mAllowedIds.add(itemId); // accept
                  this.mNextItem = cc;
               }
               else
               {
                  mFilteredIds.add(itemId); // decline; find next
               }
            }
         }
      }
   }

   /**
    * Check ALM item: Does it exist and match one of the status in the filter
    * list?
    */
   protected boolean checkTrackerItem(String itemId)
   {
      return mALMutil.checkTrackerItem(itemId);
   }

   /**
    * Provide iterator object.
    */
   @Override
   public Iterator<ICLCommitContainer> iterator()
   {
      return new CommitFilterIterator(mSource.iterator());
   }
}
