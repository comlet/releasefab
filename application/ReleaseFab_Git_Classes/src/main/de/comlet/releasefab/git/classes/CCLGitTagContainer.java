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
 * @file CCLGitTagContainer.java
 *
 * @brief Data structure of Git Tag.
 */

package de.comlet.releasefab.git.classes;

import de.comlet.releasefab.api.vcsservice.ICLTagContainer;
import java.util.Date;

/**
 * Store information about a Git TAG.
 */
public class CCLGitTagContainer implements ICLTagContainer
{
   private String mName;
   private Date mDate;
   private String mHash;
   private String mPointsToHash;

   /**
    * Default Constructor.
    */
   public CCLGitTagContainer()
   {
      this.mName = "";
      this.mDate = null;
      this.mHash = "";
      this.mPointsToHash = "";
   }

   /**
    * Constructor. Initialize members.
    */
   public CCLGitTagContainer(String name, Date date, String hash, String pointsToHash)
   {
      this.mName = name;
      this.mDate = date;
      this.mHash = hash;
      this.mPointsToHash = pointsToHash;
   }

   /**
    * Getter for the name of the tag container.
    */
   @Override
   public String getName()
   {
      return mName;
   }

   /**
    * Getter for the hash of the tag container.
    */
   public String getHash()
   {
      return mHash;
   }

   /**
    * Getter for mPointsToHash.
    */
   public String getPointsToHash()
   {
      return mPointsToHash;
   }

   /**
    * Provide a String representation for debugging.
    */
   @Override
   public String toString()
   {
      return ("Name: " + mName + ", Date: " + mDate);
   }
}
