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
 * @file CCLInternalRuntimeException.java
 *
 * @brief Exception thrown by internal classes during runtime.
 */

package de.comlet.releasefab.library.exception;

public class CCLInternalRuntimeException extends RuntimeException
{
   /**
    * The {@link #serialVersionUID} is used in the process of Serialization and
    * Deserialization. It allows the Java VM to distinguish between different
    * serialized objects and deserialize them into the correct object.
    */
   private static final long serialVersionUID = 4581836045352627950L;

   private static final String UNSPECIFIED_EXCEPTION = "ReleaseFab unspecified internal RuntimeException";
   private static final String SPECIFIED_EXCEPTION = "ReleaseFab internal RuntimeException";

   /**
    * Constructor. Calls super class with a predefined message.
    */
   public CCLInternalRuntimeException()
   {
      super(UNSPECIFIED_EXCEPTION);
   }

   /**
    * Constructor. Calls super class with a custom message.
    */
   public CCLInternalRuntimeException(String error)
   {
      super(String.format("%s: %s", SPECIFIED_EXCEPTION, error));
   }

   /**
    * Constructor. Calls super class with an existing exception.
    */
   public CCLInternalRuntimeException(Exception cause)
   {
      super(UNSPECIFIED_EXCEPTION, cause);
   }

   /**
    * Constructor. Calls super class with a custom message and an existing
    * exception.
    */
   public CCLInternalRuntimeException(String error, Exception cause)
   {
      super(String.format("%s: %s", SPECIFIED_EXCEPTION, error), cause);
   }
}
