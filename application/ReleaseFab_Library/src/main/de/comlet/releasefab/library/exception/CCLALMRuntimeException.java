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
 * @file CCLALMRuntimeException.java
 *
 * @brief Exception thrown by ALM classes during runtime.
 */

package de.comlet.releasefab.library.exception;

/**
 * Runtime exception class for Application Lifecycle Management Services. Used
 * to throw an exception, if there is no throwable clause allowed.
 */
public class CCLALMRuntimeException extends CCLInternalRuntimeException
{
   /**
    * The {@link #serialVersionUID} is used in the process of Serialization and
    * Deserialization. It allows the Java VM to distinguish between different
    * serialized objects and deserialize them into the correct object.
    */
   private static final long serialVersionUID = -961949205938271259L;

   /**
    * Constructor. Calls super class with a custom message.
    */
   public CCLALMRuntimeException(String message)
   {
      super(message);
   }
}
