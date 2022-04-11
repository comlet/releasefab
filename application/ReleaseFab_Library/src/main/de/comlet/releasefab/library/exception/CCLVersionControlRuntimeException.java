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
 * @file CCLVersionControlRuntimeException.java
 *
 * @brief Exception thrown by VCS classes during runtime.
 */

package de.comlet.releasefab.library.exception;

public class CCLVersionControlRuntimeException extends CCLInternalRuntimeException
{
   /**
    * The {@link #serialVersionUID} is used in the process of Serialization and
    * Deserialization. It allows the Java VM to distinguish between different
    * serialized objects and deserialize them into the correct object.
    */
   private static final long serialVersionUID = 8107432565007222014L;

   /**
    * Constructor. Calls super class with a custom message.
    */
   public CCLVersionControlRuntimeException(String message)
   {
      super(message);
   }
}
