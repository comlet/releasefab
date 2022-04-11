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
 * @file CCLDelivery.java
 *
 * @brief Data structure of a delivery.
 */

package de.comlet.releasefab.library.model;

import java.util.Date;

/**
 * A delivery is a snapshot of information about components at a certain time. A
 * delivery has a unique name. This name is used as a key in
 * {@link CCLComponent}. This class contains the global delivery data - public
 * properties. These properties are the same for every component taking part in
 * the delivery. The additional information about the delivery is saved in the
 * component itself and can be accessed by the name of the delivery. The number
 * of additional information in a component is dependent on the number of
 * classes derived from {@link ACLDeliveryInformation}.
 */
public class CCLDelivery implements Comparable<CCLDelivery>
{
   /** Name of delivery. */
   private String mName;

   /** Name of integrator. */
   private String mIntegrator;

   /** Date and time of delivery creation. */
   private Date mCreated;

   /** Default constructor creates an initial delivery. */
   public CCLDelivery()
   {
      this("Name", (null != System.getenv("FULL_USER_NAME")) ? System.getenv("FULL_USER_NAME") : 
         System.getProperty("user.name"));
   }

   /**
    * Constructor. Creates a delivery with the given name created by the
    * integrator.
    * 
    * @param deliveryName Name of this delivery
    * @param integratorName Name of the user that created the delivery
    */
   public CCLDelivery(String deliveryName, String integratorName)
   {
      mName = deliveryName;
      mIntegrator = integratorName;
      mCreated = new Date();
   }

   /** Compares deliveries by date of creation (descending!). */
   @Override
   public int compareTo(CCLDelivery other)
   {
      if (null == other)
      {
         return 1;
      }

      return -1 * this.mCreated.compareTo(other.mCreated);
   }

   public String getName()
   {
      return mName;
   }

   public void setName(String name)
   {
      mName = name;
   }

   public String getIntegrator()
   {
      return mIntegrator;
   }

   public void setIntegrator(String integrator)
   {
      mIntegrator = integrator;
   }

   public Date getCreated()
   {
      return mCreated;
   }

   public void setCreated(Date creationTime)
   {
      mCreated = creationTime;
   }
}
