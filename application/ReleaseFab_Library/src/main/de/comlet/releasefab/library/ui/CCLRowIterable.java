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
 * @file CCLRowIterable.java
 *
 * @brief Iterable of GUI rows.
 */

package de.comlet.releasefab.library.ui;

import java.util.Iterator;
import org.jdom2.Element;

/**
 * Class to update a table row.
 */
public class CCLRowIterable implements Iterable<CCLRowAccessor>
{
   protected Iterable<Element> mInfo;

   /**
    * Iterator containing table rows.
    */
   protected class CCLRowIterator implements Iterator<CCLRowAccessor>
   {
      Iterator<Element> mIt;

      /**
       * Constructor.
       */
      CCLRowIterator(Iterator<Element> it)
      {
         this.mIt = it;
      }

      /**
       * True, if the embedded iterator has another element.
       */
      @Override
      public boolean hasNext()
      {
         return this.mIt.hasNext();
      }

      /**
       * Provide an updater for the entry.
       */
      @Override
      public CCLRowAccessor next()
      {
         return new CCLRowAccessor(this.mIt.next());
      }

      /**
       * Not supported.
       */
      @Override
      public void remove()
      {
         throw new UnsupportedOperationException();
      }
   }

   /**
    * Contructor. Store xml element.
    */
   public CCLRowIterable(Iterable<Element> info)
   {
      this.mInfo = info;
   }

   /**
    * Provide iterator object.
    */
   @Override
   public Iterator<CCLRowAccessor> iterator()
   {
      return new CCLRowIterator(mInfo.iterator());
   }
}
