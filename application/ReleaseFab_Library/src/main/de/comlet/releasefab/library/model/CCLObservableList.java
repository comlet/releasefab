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
 * @file CCLObservableList.java
 *
 * @brief Generic observable List.
 */

package de.comlet.releasefab.library.model;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Decorator for lists. Notifies all observers whenever the list changes.
 * 
 * @param <E>
 */
public class CCLObservableList<E> extends CCLObservableCollection<E> implements List<E>
{
   private static final String NAME_OF_COLLECTION = "mCollection";

   /**
    * Constructor. Creates an ArrayList<E> as the inner collection.
    */
   public CCLObservableList()
   {
      super(new ArrayList<E>());
   }

   /**
    * Constructor. Creates an ArrayList<E> as the inner collection and adds the
    * given observer to the list of observers.
    * 
    * @param observer
    */
   public CCLObservableList(PropertyChangeListener pcl)
   {
      super(new ArrayList<E>(), pcl);
   }

   /**
    * Constructor. Uses given collection as the inner collection.
    * 
    * @param collection
    */
   public CCLObservableList(Collection<E> collection)
   {
      super(collection);
   }

   /**
    * Constructor. Uses given collection as the inner collection and adds the given
    * observer to the list of observers.
    * 
    * @param collection
    * @param observer
    */
   public CCLObservableList(Collection<E> collection, PropertyChangeListener pcl)
   {
      super(collection, pcl);
   }

   /**
    * Get inner collection and cast to List<E>
    * 
    * @return inner collection as List<E>
    */
   private List<E> getList()
   {
      return (List<E>) mCollection;
   }

   /**
    * Inserts all of the elements in the specified collection into this list at
    * the specified position (optional operation). Shifts the element currently
    * at that position (if any) and any subsequent elements to the right
    * (increases their indices). The new elements will appear in this list in
    * the order that they are returned by the specified collection's iterator.
    * The behavior of this operation is undefined if the specified collection is
    * modified while the operation is in progress. (Note that this will occur if
    * the specified collection is this list, and it's nonempty.)<br>
    * <br>
    * Notifies all observers when the elements have been successfully added to
    * the collection.
    */
   @Override
   public boolean addAll(int index, Collection<? extends E> c)
   {
      List<E> oldValue = getList();
      boolean res = getList().addAll(index, c);

      if (res)
      {
         this.mObservable.firePropertyChange(NAME_OF_COLLECTION, oldValue, getList());
      }

      return res;
   }

   @Override
   public E get(int index)
   {
      return getList().get(index);
   }

   /**
    * Replaces the element at the specified position in this list with the
    * specified element (optional operation).<br>
    * <br>
    * Notifies all observers when the element at the specified position has been
    * successfully replaced.
    */
   @Override
   public E set(int index, E element)
   {
      List<E> oldValue = getList();
      E res = getList().set(index, element);

      if (!element.equals(res))
      {
         this.mObservable.firePropertyChange(NAME_OF_COLLECTION, oldValue, getList());
      }

      return res;
   }

   /**
    * Inserts the specified element at the specified position in this list
    * (optional operation). Shifts the element currently at that position (if
    * any) and any subsequent elements to the right (adds one to their indices).<br>
    * <br>
    * Notifies all observers.
    */
   @Override
   public void add(int index, E element)
   {
      List<E> oldValue = getList();
      getList().add(index, element);

      this.mObservable.firePropertyChange(NAME_OF_COLLECTION, oldValue, getList());
   }

   /**
    * Removes the element at the specified position in this list (optional
    * operation). Shifts any subsequent elements to the left (subtracts one from
    * their indices). Returns the element that was removed from the list.<br>
    * <br>
    * Notifies all observers.
    */
   @Override
   public E remove(int index)
   {
      List<E> oldValue = getList();
      E res = getList().remove(index);

      this.mObservable.firePropertyChange(NAME_OF_COLLECTION, oldValue, getList());

      return res;
   }

   @Override
   public int indexOf(Object o)
   {
      return getList().indexOf(o);
   }

   @Override
   public int lastIndexOf(Object o)
   {
      return getList().lastIndexOf(o);
   }

   @Override
   public ListIterator<E> listIterator()
   {
      return getList().listIterator();
   }

   @Override
   public ListIterator<E> listIterator(int index)
   {
      return getList().listIterator(index);
   }

   @Override
   public List<E> subList(int fromIndex, int toIndex)
   {
      return getList().subList(fromIndex, toIndex);
   }
}
