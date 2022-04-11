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
 * @file CCLObservableCollection.java
 *
 * @brief Generic observable collection.
 */

package de.comlet.releasefab.library.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Decorator for collections. Notifies all observers whenever the collection
 * changes.
 * 
 * @param <E>
 */
public class CCLObservableCollection<E> implements Collection<E>
{
   private static final String NAME_OF_COLLECTION = "mCollection";

   /** Inner collection. */
   protected Collection<E> mCollection;

   /** Fires state event if a bound state is changed. */
   protected PropertyChangeSupport mObservable;

   /**
    * Constructor. Uses the given collection as inner collection.
    * 
    * @param collection
    */
   public CCLObservableCollection(Collection<E> collection)
   {
      this(collection, null);
   }

   /**
    * Constructor. Uses the given collection as inner collection and adds the
    * given observer to the list of observers.
    * 
    * @param collection
    * @param pcl
    */
   public CCLObservableCollection(Collection<E> collection, PropertyChangeListener pcl)
   {
      mCollection = collection;
      mObservable = new PropertyChangeSupport(this);

      if (null != pcl)
      {
         mObservable.addPropertyChangeListener(pcl);
      }
   }

   /**
    * Register the {@link PropertyChangeListener} pcl to the
    * {@link CCLObservableCollection}.
    * 
    * @param pcl {@link PropertyChangeListener} to be registered
    */
   public final synchronized void addPropertyChangeListener(PropertyChangeListener pcl)
   {
      mObservable.addPropertyChangeListener(pcl);
   }

   /**
    * Remove the {@link PropertyChangeListener} pcl from the
    * {@link CCLObservableCollection}.
    * 
    * @param pcl {@link PropertyChangeListener} to be removed
    */
   public void removePropertyChangeListener(PropertyChangeListener pcl)
   {
      mObservable.removePropertyChangeListener(pcl);
   }

   /**
    * Ensures that this collection contains the specified element (optional
    * operation). Returns true if this collection changed as a result of the
    * call. (Returns false if this collection does not permit duplicates and
    * already contains the specified element.)<br>
    * <br>
    * Collections that support this operation may place limitations on what
    * elements may be added to this collection. In particular, some collections
    * will refuse to add null elements, and others will impose restrictions on
    * the type of elements that may be added. Collection classes should clearly
    * specify any restrictions on what elements may be added in their
    * documentation .<br>
    * <br>
    * If a collection refuses to add a particular element for any reason other
    * than that it already contains the element, it must throw an exception
    * (rather than returning false). This preserves the invariant that a
    * collection always contains the specified element after this call
    * returns.<br>
    * <br>
    * Notifies all observers when the element has been successfully added to the
    * collection.
    */
   @Override
   public boolean add(E e)
   {
      boolean res = mCollection.add(e);

      if (res)
      {
         mObservable.firePropertyChange(NAME_OF_COLLECTION, new ArrayList<>(), mCollection);
      }

      return res;
   }

   /**
    * Adds all of the elements in the specified collection to this collection
    * (optional operation). The behavior of this operation is undefined if the
    * specified collection is modified while the operation is in progress. (This
    * implies that the behavior of this call is undefined if the specified
    * collection is this collection, and this collection is nonempty.)<br>
    * <br>
    * Notifies all observers when the elements have been successfully added to
    * the collection.
    */
   @Override
   public boolean addAll(Collection<? extends E> c)
   {
      boolean res = mCollection.addAll(c);

      if (res)
      {
         mObservable.firePropertyChange(NAME_OF_COLLECTION, new ArrayList<>(), mCollection);
      }

      return res;
   }

   /**
    * Removes all of the elements from this collection (optional operation). The
    * collection will be empty after this method returns.<br>
    * <br>
    * Notifies all observers.
    */
   @Override
   public void clear()
   {
      mCollection.clear();

      mObservable.firePropertyChange(NAME_OF_COLLECTION, new ArrayList<>(), mCollection);
   }

   @Override
   public boolean contains(Object o)
   {
      return mCollection.contains(o);
   }

   @Override
   public boolean containsAll(Collection<?> c)
   {
      return mCollection.containsAll(c);
   }

   @Override
   public boolean isEmpty()
   {
      return mCollection.isEmpty();
   }

   @Override
   public Iterator<E> iterator()
   {
      return mCollection.iterator();
   }

   /**
    * Removes a single instance of the specified element from this collection,
    * if it is present (optional operation). More formally, removes an element e
    * such that (o==null ? e==null : o.equals(e)), if this collection contains
    * one or more such elements. Returns true if this collection contained the
    * specified element (or equivalently, if this collection changed as a result
    * of the call).<br>
    * <br>
    * Notifies all observers when the element has been successfully removed.
    */
   @Override
   public boolean remove(Object o)
   {
      boolean res = mCollection.remove(o);

      if (res)
      {
         mObservable.firePropertyChange(NAME_OF_COLLECTION, new ArrayList<>(), mCollection);
      }

      return res;
   }

   /**
    * Removes all of this collection's elements that are also contained in the
    * specified collection (optional operation). After this call returns, this
    * collection will contain no elements in common with the specified
    * collection.<br>
    * <br>
    * Notifies all observers when all elements have been successfully removed.
    */
   @Override
   public boolean removeAll(Collection<?> c)
   {
      boolean res = mCollection.removeAll(c);

      if (res)
      {
         mObservable.firePropertyChange(NAME_OF_COLLECTION, new ArrayList<>(), mCollection);
      }

      return res;
   }

   /**
    * Retains only the elements in this collection that are contained in the
    * specified collection (optional operation). In other words, removes from
    * this collection all of its elements that are not contained in the
    * specified collection.<br>
    * <br>
    * Notifies all observers when all elements that are not contained in the
    * specified collection have been successfully removed.
    */
   @Override
   public boolean retainAll(Collection<?> c)
   {
      boolean res = mCollection.retainAll(c);

      if (res)
      {
         mObservable.firePropertyChange(NAME_OF_COLLECTION, new ArrayList<>(), mCollection);
      }

      return res;
   }

   @Override
   public int size()
   {
      return mCollection.size();
   }

   @Override
   public Object[] toArray()
   {
      return mCollection.toArray();
   }

   @Override
   public <T> T[] toArray(T[] a)
   {
      return mCollection.toArray(a);
   }
}
