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
 * @file ACLXMLTransformIterable.java
 *
 * @brief Abstract class of XML transformer.
 */

package de.comlet.releasefab.library.xml;

import java.util.Iterator;

/**
 * Provides an iterable source, which can also do a transformation. E.g. iterate
 * XML-Input and transform it into a container object. Needs an iterable source
 * as input.
 */
public abstract class ACLXMLTransformIterable<I, O> implements Iterable<O>, ICLTransformable<I, O>
{
   private Iterable<I> mSource;

   /**
    * Internal iterator.
    */
   protected class CCLTransformIterator implements Iterator<O>
   {
      private Iterator<I> mIt;
      private I mNextItem;

      /**
       * Constructor. Remember source.
       */
      CCLTransformIterator(Iterator<I> it)
      {
         this.mIt = it;
         this.mNextItem = null;
      }

      /**
       * Check if there is another item.
       */
      @Override
      public boolean hasNext()
      {
         while (this.mIt.hasNext() && (mNextItem == null))
         {
            this.mNextItem = this.mIt.next();
         }
         return (this.mNextItem != null);
      }

      /**
       * Retrieve the next item.
       */
      @Override
      public O next()
      {
         I temp = this.mNextItem;
         this.mNextItem = null;
         return ACLXMLTransformIterable.this.transform(temp);
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

   /**
    * Constructor. Remember source.
    */
   public ACLXMLTransformIterable(Iterable<I> source)
   {
      this.mSource = source;
   }

   /**
    * Create an iterator instance.
    */
   @Override
   public Iterator<O> iterator()
   {
      return new CCLTransformIterator(mSource.iterator());
   }
}
