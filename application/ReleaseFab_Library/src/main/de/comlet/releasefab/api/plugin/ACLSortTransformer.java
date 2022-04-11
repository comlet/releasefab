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
 * @file ACLSortTransformer.java
 *
 * @brief Abstract class for sorting and transforming.
 */

package de.comlet.releasefab.api.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Abstract base class of a transformer that sorts a list of objects.
 */
public abstract class ACLSortTransformer<T> implements Iterable<T>
{
   private ArrayList<T> mList;

   /**
    * Create a collection from the input and sort items.
    */
   public ACLSortTransformer(Iterable<T> source)
   {
      mList = new ArrayList<>();
      for (T entry : source)
      {
         mList.add(entry);
      }
      Collections.sort(mList, new ContainerComparator());
   }

   /**
    * Provide sort order for the container class.
    */
   protected class ContainerComparator implements Comparator<T>
   {
      /**
       * Use compare method from outer class.
       */
      @Override
      public int compare(T c1, T c2)
      {
         return ACLSortTransformer.this.compare(c1, c2);
      }
   }
   
   /**
    * Do the comparison.
    */
   protected abstract int compare(T c1, T c2);

   /**
    * Provide an iterator for the sorted collection.
    */
   @Override
   public Iterator<T> iterator()
   {
      return mList.iterator();
   }
}
