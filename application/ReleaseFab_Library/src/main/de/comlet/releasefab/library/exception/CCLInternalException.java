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
 * @file CCLInternalException.java
 *
 * @brief Exception thrown by internal classes.
 */

package de.comlet.releasefab.library.exception;

public class CCLInternalException extends Exception
{
   /**
    * The {@link #serialVersionUID} is used in the process of Serialization and
    * Deserialization. It allows the Java VM to distinguish between different
    * serialized objects and deserialize them into the correct object.
    */
   private static final long serialVersionUID = 4795426733470346922L;

   /**
    * Constructor. Calls super class.
    */
   public CCLInternalException()
   {
   }

   /**
    * Constructor. Calls super class with a custom message.
    */
   public CCLInternalException(String error)
   {
      super(error);
   }

   /**
    * Constructor. Calls super class with an existing Throwable.
    */
   public CCLInternalException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Constructor. Calls super class with a custom message and an existing
    * Throwable.
    */
   public CCLInternalException(String error, Throwable cause)
   {
      super(error, cause);
   }
}
