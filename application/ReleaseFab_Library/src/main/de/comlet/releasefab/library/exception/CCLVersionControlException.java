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
 * @file CCLVersionControlException.java
 *
 * @brief Exception thrown by VCS classes.
 */

package de.comlet.releasefab.library.exception;

/**
 * Exception class specific to Version Control Services and Plugins.
 *
 * @author FrBlinn
 *
 */
public class CCLVersionControlException extends CCLInternalException
{
   /**
    * The {@link #serialVersionUID} is used in the process of Serialization and
    * Deserialization. It allows the Java VM to distinguish between different
    * serialized objects and deserialize them into the correct object.
    */
   private static final long serialVersionUID = 490883839533303874L;

   /**
    * Constructor. Calls super class with a custom message.
    */
   public CCLVersionControlException(String message)
   {
      super(message);
   }

   /**
    * Constructor. Calls super class with an existing exception.
    */
   public CCLVersionControlException(Exception e)
   {
      super(e.getMessage());
   }
}
