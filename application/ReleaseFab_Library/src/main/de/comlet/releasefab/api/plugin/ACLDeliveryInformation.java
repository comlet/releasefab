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
 * @file ACLDeliveryInformation.java
 *
 * @brief Abstract class for delivery information.
 */

package de.comlet.releasefab.api.plugin;

import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for delivery information. A component can contain more
 * than one delivery information at delivery time. Version number or important
 * information are just two examples. Every derived class represent another type
 * of delivery information. See implementation of CCLDeliveryVersion for an
 * example.
 */
public abstract class ACLDeliveryInformation
{
   /** Initialize logger for this class. */
   protected static final Logger LOGGER = LoggerFactory.getLogger(ACLDeliveryInformation.class);

   /** Content of the information. */
   protected Element mInformation;

   /** Indicates if this information is new. */
   protected boolean mIsNew;

   /** Fires state event if a bound state is changed. */
   protected final PropertyChangeSupport mObservable = new PropertyChangeSupport(this);

   /** Default constructor. */
   public ACLDeliveryInformation()
   {
      mIsNew = false;
   }

   /**
    * Adds a {@link PropertyChangeListener} to {@link #mObservable}.
    * 
    * @param pcl the {@link PropertyChangeListener} to be added.
    */
   public void addPropertyChangeListener(PropertyChangeListener pcl)
   {
      mObservable.addPropertyChangeListener(pcl);
   }

   /**
    * Removes a {@link PropertyChangeListener} from {@link #mObservable}.
    * 
    * @param pcl the {@link PropertyChangeListener} to be removed
    */
   public void removePropertyChangeListener(PropertyChangeListener pcl)
   {
      mObservable.removePropertyChangeListener(pcl);
   }

   /** Name of the information. */
   public abstract String getName();

   /**
    * Add Information from other delivery.
    * 
    * @param aOther
    * @return true if information has been added successfully
    */
   public abstract boolean addInformation(Element aOther);

   /**
    * Add Information for the export in Docbook-Format.
    * 
    * @param element
    * @param component
    * @param other
    * @param forCustomer
    * @return true if information has been added successfully
    */
   public abstract boolean addDocbookSection(Element element, CCLComponent component, CCLDelivery other,
         boolean forCustomer);

   /**
    * Checks if this instance of a specific delivery information contains
    * information in {@link #mInformation}.
    * 
    * @see hasChanged()
    * 
    * @return False if there is information. True if the information is null,
    * empty or equals "-".
    */
   public boolean isInfoNullOrEmpty()
   {
      boolean isNullOrEmpty = true;
      // is there information that we can compare?
      if (null != mInformation)
      {
         String str = mInformation.getValue();
         isNullOrEmpty = !(null != str && !str.isEmpty() && !str.equals("-"));
      }

      return isNullOrEmpty;
   }

   /**
    * Allows subclasses to compare the content of the information if necessary.
    * If a subclass does not override this method, false will be returned.
    * 
    * @param other delivery information to compare with this information
    * @return true if this information is the same as the given delivery
    * information, false otherwise.
    */
   protected boolean compareInfo(ACLDeliveryInformation other)
   {
      return false;
   }

   /**
    * Compares the information of two deliveries.
    * 
    * @param other delivery information to compare with this information
    * @return true if it has changed, false if it has not.
    */
   public boolean hasChanged(ACLDeliveryInformation other)
   {
      // is there information and is that information different from the given
      // information (the latter is important too)
      return !(isInfoNullOrEmpty() || compareInfo(other));
   }

   public Element getInformation()
   {
      return mInformation;
   }

   public void setInformation(Element information)
   {
      Element oldValue = mInformation;
      mInformation = information;
      mObservable.firePropertyChange("mInformation", oldValue, mInformation);
   }

   public boolean isNew()
   {
      return mIsNew;
   }

   public void setNew(boolean isNew)
   {
      boolean oldValue = mIsNew;
      mIsNew = isNew;
      mObservable.firePropertyChange("mIsNew", oldValue, mIsNew);
   }
}
