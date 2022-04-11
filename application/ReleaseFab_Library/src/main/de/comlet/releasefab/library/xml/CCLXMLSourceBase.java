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
 * @file CCLXMLSourceBase.java
 *
 * @brief Basic XML source.
 */

package de.comlet.releasefab.library.xml;

import java.util.Iterator;
import org.jdom2.Element;

/**
 * Read data from XML and transform it.
 */
public class CCLXMLSourceBase<T> implements Iterable<T>
{
   protected Element mInformation;
   protected String mChildTag;
   protected ICLTransformable<Element, T> mTransformer;

   /**
    * Internal iterator.
    */
   protected abstract static class AIteratorBase<O> implements Iterator<O>
   {
      protected Iterator<Element> mCursor;

      /**
       * Default Constructor.
       */
      public AIteratorBase(Iterator<Element> cursor)
      {
         this.mCursor = cursor;
      }

      /**
       * Check if there is another item available.
       */
      @Override
      public boolean hasNext()
      {
         return this.mCursor.hasNext();
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

   private static class SourceIterator<Q> extends AIteratorBase<Q>
   {
      ICLTransformable<Element, Q> mInternTransformer;

      /**
       * Default Constructor.
       */
      public SourceIterator(Iterator<Element> cursor, ICLTransformable<Element, Q> transformer)
      {
         super(cursor);
         this.mInternTransformer = transformer;
      }

      /**
       * Check if there is another item available.
       */
      @Override
      public Q next()
      {
         return this.mInternTransformer.transform(mCursor.next());
      }
   }

   /**
    * Constructor. Provide input data as XML.
    */
   public CCLXMLSourceBase(Element information, String childTag, ICLTransformable<Element, T> transformer)
   {
      this.mInformation = information;
      this.mChildTag = childTag;
      this.mTransformer = transformer;
   }

   /**
    * Provide iterator to retrieve items.
    */
   @Override
   public Iterator<T> iterator()
   {
      return new SourceIterator<>(mInformation.getChildren(this.mChildTag).iterator(), mTransformer);
   }
}
