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
 * @file CCLParameter.java
 *
 * @brief Data structure of assignment strategy parameters.
 */

package de.comlet.releasefab.library.model;

/**
 * An array of {@link CCLParameter} serves as input for the abstract getData
 * method of ACLAssignmentStrategy. Using a dedicated data type allows for more
 * flexibility and future expansion. It is important to document the whole
 * purpose of the parameter in {@link #mInfo} to help the developer implement
 * the getData() method. The current type of {@link #mValue} is String, later on
 * it might be replaced by a generic type.
 */
public class CCLParameter
{
   /** Value of the parameter. */
   private String mValue = "";

   /**
    * Information about the parameter<br>
    * Which kind of data type is it and how should it be used. What needs to be
    * taken care of?
    */
   private String mInfo = "";

   /**
    * Constructor. An array of {@link CCLParameter} serves as input for the
    * abstract getData method of ACLAssignmentStrategy. Using a dedicated data
    * type allows for more flexibility and future expansion. It is important to
    * document the whole purpose of the parameter in {@link #mInfo} to help the
    * developer implement the getData() method. The current type of
    * {@link #mValue} is String, later on it might be replaced by a generic
    * type.
    */
   public CCLParameter()
   {
   }

   /**
    * Default Constructor. An array of {@link CCLParameter} serves as input for
    * the abstract getData method of ACLAssignmentStrategy. Using a dedicated
    * data type allows for more flexibility and future expansion. It is
    * important to document the whole purpose of the parameter in {@link #mInfo}
    * to help the developer implement the getData() method. The current type of
    * {@link #mValue} is String, later on it might be replaced by a generic
    * type.
    * 
    * @param value
    * @param info
    */
   public CCLParameter(String value, String info)
   {
      mValue = value;
      mInfo = info;
   }

   public String getValue()
   {
      return mValue;
   }

   public void setValue(String value)
   {
      mValue = value;
   }

   public String getInfo()
   {
      return mInfo;
   }

   public void setInfo(String info)
   {
      mInfo = info;
   }
}
