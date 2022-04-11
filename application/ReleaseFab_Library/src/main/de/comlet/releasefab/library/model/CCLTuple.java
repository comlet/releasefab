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
 * @file CCLTuple.java
 *
 * @brief Generic tuple.
 */

package de.comlet.releasefab.library.model;

/**
* Implementation of a generic Tuple.
*/
public class CCLTuple <T, U>
{
   private T mFirst;
   private U mSecond;

   /**
    * Constructor. Initialize members with null.
    */
   public CCLTuple()
   {
      this.mFirst = null;
      this.mSecond = null;
   }

   /**
    * Default Constructor. Initialize members with given parameters.
    */
   public CCLTuple(T first, U second)
   {
      this.mFirst = first;
      this.mSecond = second;
   }


   public T getFirst()
   {
      return mFirst;
   }


   public void setFirst(T mFirst)
   {
      this.mFirst = mFirst;
   }


   public U getSecond()
   {
      return mSecond;
   }


   public void setSecond(U mSecond)
   {
      this.mSecond = mSecond;
   }
}
