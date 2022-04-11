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
 * @file CCLComponent.java
 *
 * @brief Data structure of a component.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A name of the component, in case of ReleaseFab an entry in the tree. In
 * the GUI it is always shown as the first column of every tab. The properties
 * of the component are displayed in the following columns of the tree list
 * view.
 *
 * --Comp1Name prop1 prop2 ... propN | ---Comp2Name prop1 prop2 ... propN . .
 * ---CompNName prop1 prop2 ... propN
 *
 * A component is a recursive structure, implemented as a tree. Every component
 * has a parent and a list of subcomponents. If we want to visualize this tree
 * we just have to bind it to the UI widgets. SCLProject contains the root
 * component. This class implements the interface
 * {@link PropertyChangeListener}. This interface is used to implement the
 * Observer pattern between the model and the GUI.
 */
public class CCLComponent implements PropertyChangeListener
{
   /** Number of instances. */
   private static int sCounter;

   /** Unique ID of this component. */
   private UUID mId;

   /** Name of this component. */
   private String mName;

   /** Parent component. */
   private CCLComponent mParent;

   /** Indicates if this component is relevant for a customer. */
   private boolean mIsCustomerRelevant;

   /** Fires state event if a bound state is changed. */
   private PropertyChangeSupport mObservable;

   /**
    * A component can have different information about a delivery, therefore we
    * have to use a collection to store them. In this case a Map is used to
    * access the information by its name and the name of a delivery.
    */
   private Map<String, ACLDeliveryInformation> mDeliveryInformation = new HashMap<>();

   /**
    * An information for a new delivery can be assigned by a special strategy.
    * Extraction out of a file etc.
    */
   private Map<String, ACLAssignmentStrategy> mAssignmentStrategies = new HashMap<>();

   /**
    * A component has a bunch of parameters for every assignment strategy
    * selected for an import strategy
    */
   private Map<String, List<CCLParameter>> mParameters = new HashMap<>();

   /**
    * List containing all subcomponents of the component. Implementation of the
    * component tree.
    */
   private List<CCLComponent> mSubComponents = new CCLObservableList<CCLComponent>(this);

   /** Default constructor creates an empty component. */
   public CCLComponent()
   {
      mName = "NewComponent" + sCounter++;
      mId = UUID.randomUUID();
      mIsCustomerRelevant = true;
      mObservable = new PropertyChangeSupport(this);
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

   /**
    * Visitor Pattern! accept() allows a visitor to traverse over the component
    * tree.
    *
    * @param visitor
    * @param target
    * @return
    */
   public <RETURN, P2> RETURN accept(ICLVisitor<RETURN, P2> visitor, P2 target)
   {
      return accept(visitor, target, false);
   }

   /**
    * Visitor Pattern! accept() allows a visitor to traverse over the component
    * tree.
    *
    * @param visitor
    * @param target
    * @param quickReturn return on the first hit? (this is useful for search
    * algorithms)
    * @return
    */
   public <RETURN, P2> RETURN accept(ICLVisitor<RETURN, P2> visitor, P2 target, boolean quickReturn)
   {
      return visitor.visit(this, target, quickReturn);
   }

   public String getFullName()
   {
      StringBuilder stringBuilder = new StringBuilder();
      CCLComponent component = this;
      stringBuilder.append(component.getName());

      // because of not visible parent node c.Parent.Parent
      while (component.mParent != null && component.mParent.mParent != null)
      {
         component = component.mParent;
         stringBuilder.insert(0, '\\');
         stringBuilder.insert(0, component.mName);
      }

      return stringBuilder.toString();
   }

   public boolean hasSubComponents()
   {
      return !mSubComponents.isEmpty();
   }

   /**
    * Checks if there is delivery information in that component.
    *
    * @return
    */
   public boolean hasDeliveryInformation()
   {
      boolean hasInformation = false;
      for (ACLDeliveryInformation deliveryInfo : mDeliveryInformation.values())
      {
         if (!deliveryInfo.isInfoNullOrEmpty())
         {
            hasInformation = true;
            break;
         }
      }

      return hasInformation;
   }

   public ACLAssignmentStrategy getAssignmentStrategy(String name)
   {
      return mAssignmentStrategies.get(name);
   }

   public void setAssignmentStrategy(String name, ACLAssignmentStrategy strategy)
   {
      mAssignmentStrategies.put(name, strategy);
   }

   public List<CCLParameter> getParameters(String name)
   {
      return mParameters.get(name);
   }

   public void setParameters(String name, List<CCLParameter> parameters)
   {
      mParameters.put(name, parameters);
   }

   public ACLDeliveryInformation getDeliveryInformation(String name)
   {
      return mDeliveryInformation.get(name);
   }

   public void setDeliveryInformation(String name, ACLDeliveryInformation deliveryInformation)
   {
      Map<String, ACLDeliveryInformation> oldValue = mDeliveryInformation;
      mDeliveryInformation.put(name, deliveryInformation);

      mObservable.firePropertyChange("mDeliveryInformation", oldValue, mDeliveryInformation);
   }

   public Map<String, ACLDeliveryInformation> getDeliveryInformation()
   {
      return mDeliveryInformation;
   }

   public void setDeliveryInformation(Map<String, ACLDeliveryInformation> deliveryInformation)
   {
      mDeliveryInformation = deliveryInformation;
   }

   public UUID getId()
   {
      return mId;
   }

   public void setId(UUID id)
   {
      mId = id;
   }

   public String getName()
   {
      return mName;
   }

   public void setName(String name)
   {
      String oldValue = mName;
      mName = name;

      mObservable.firePropertyChange("mName", oldValue, name);
   }

   public CCLComponent getParent()
   {
      return mParent;
   }

   public void setParent(CCLComponent parent)
   {
      CCLComponent oldValue = mParent;
      mParent = parent;

      mObservable.addPropertyChangeListener(mParent);

      mObservable.firePropertyChange("mParent", oldValue, parent);
   }

   public boolean getIsCustomerRelevant()
   {
      return mIsCustomerRelevant;
   }

   public void setIsCustomerRelevant(boolean isRelevant)
   {
      boolean oldValue = mIsCustomerRelevant;
      mIsCustomerRelevant = isRelevant;

      mObservable.firePropertyChange("mIsCustomerRelevant", oldValue, isRelevant);
   }

   public Map<String, ACLAssignmentStrategy> getAssignmentStrategies()
   {
      return mAssignmentStrategies;
   }

   public void setAssignmentStrategies(Map<String, ACLAssignmentStrategy> assignmentStrategies)
   {
      mAssignmentStrategies = assignmentStrategies;
   }

   public Map<String, List<CCLParameter>> getParameters()
   {
      return mParameters;
   }

   public void setParameters(Map<String, List<CCLParameter>> parameters)
   {
      mParameters = parameters;
   }

   public List<CCLComponent> getSubComponents()
   {
      return mSubComponents;
   }

   public void setSubComponents(List<CCLComponent> subComponents)
   {
      mSubComponents = subComponents;
      mObservable.firePropertyChange("mSubComponents", null, mSubComponents);
   }

   public static void resetCounter()
   {
      sCounter = 0;
   }

   @Override
   public String toString()
   {
      return mName;
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt)
   {
      if (null != mParent)
      {
         mParent.mObservable.firePropertyChange(evt);
      }
      else
      {
         mObservable.firePropertyChange(evt);
      }
   }
}