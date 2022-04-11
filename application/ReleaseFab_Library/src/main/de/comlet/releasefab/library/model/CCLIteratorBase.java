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
 * @file CCLIteratorBase.java
 *
 * @brief Basic Iterator.
 */

package de.comlet.releasefab.library.model;

import java.util.Iterator;

/**
 * Base class for iterators.
 */
public class CCLIteratorBase<T> implements Iterator<T>
{
   protected T mNextItem;
   protected Iterator<T> mSource;

   /**
    * Default Constructor.
    */
   protected CCLIteratorBase(Iterator<T> source)
   {
      this.mNextItem = null;
      this.mSource = source;
   }

   /**
    * Check if there is another item in the iterator.
    */
   @Override
   public boolean hasNext()
   {
      return (this.mNextItem != null);
   }

   /**
    * Get the next item.
    */
   @Override
   public T next()
   {
      T temp = this.mNextItem;
      this.mNextItem = null;
      return temp;
   }

   /**
    * This operation is not supported.
    */
   @Override
   public void remove()
   {
      throw new UnsupportedOperationException();
   }
}
