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
 * @file CCLGitCommitContainer.java
 *
 * @brief Data structure of Git Commits.
 */

package de.comlet.releasefab.git.classes;

import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;

/**
 * Store the parsed Git commits (synopsis, internal doc, ...)
 */
public class CCLGitCommitContainer implements ICLCommitContainer
{
   private String mItemId;
   private String mHash;
   private int mCommitTime;
   private String mShortDescription;
   private String mInternalDoc;
   private String mExternalDoc;
   private String mReviewer;
   private boolean mApiModification;

   /**
    * Default Constructor. Initialize itemId and apiModification member only.
    */
   public CCLGitCommitContainer()
   {
      this.mItemId = "0";
      this.mApiModification = false;
   }

   /**
    * Constructor. Initialize all members.
    */
   public CCLGitCommitContainer(String hash, String itemId, int time, String shortDescription, String internalDoc,
         String externalDoc, String reviewer, boolean apiModification)
   {
      this.mHash = hash;
      this.mItemId = itemId;
      this.mCommitTime = time;
      this.mShortDescription = shortDescription;
      this.mInternalDoc = internalDoc;
      this.mExternalDoc = externalDoc;
      this.mReviewer = reviewer;
      this.mApiModification = apiModification;
   }

   /**
    * Provide a string representation of the item id.
    */
   public String idAsString()
   {
      return String.valueOf(mItemId);
   }

   /**
    * Provide a string representation of the commit time.
    */
   public String timeAsString()
   {
      return String.valueOf(mCommitTime);
   }

   /**
    * Provide a string representation of the Git commit for debugging.
    */
   @Override
   public String toString()
   {
      return ("Hash: " + mHash + "\nItemId: " + mItemId + "\nShort description: " + mShortDescription);
   }

   @Override
   public String getCommitId()
   {
      return mItemId;
   }

   public void setItemId(String mItemId)
   {
      this.mItemId = mItemId;
   }

   public String getHash()
   {
      return mHash;
   }

   public void setHash(String mHash)
   {
      this.mHash = mHash;
   }

   public int getCommitTime()
   {
      return mCommitTime;
   }

   public void setCommitTime(int mCommitTime)
   {
      this.mCommitTime = mCommitTime;
   }

   public String getShortDescription()
   {
      return mShortDescription;
   }

   public void setShortDescription(String mShortDescription)
   {
      this.mShortDescription = mShortDescription;
   }

   public String getInternalDoc()
   {
      return mInternalDoc;
   }

   public void setInternalDoc(String mInternalDoc)
   {
      this.mInternalDoc = mInternalDoc;
   }

   public String getExternalDoc()
   {
      return mExternalDoc;
   }

   public void setExternalDoc(String mExternalDoc)
   {
      this.mExternalDoc = mExternalDoc;
   }

   public String getReviewer()
   {
      return mReviewer;
   }

   public void setReviewer(String mReviewer)
   {
      this.mReviewer = mReviewer;
   }

   public boolean getApiModification()
   {
      return mApiModification;
   }

   public void setApiModification(boolean mApiModification)
   {
      this.mApiModification = mApiModification;
   }
}
