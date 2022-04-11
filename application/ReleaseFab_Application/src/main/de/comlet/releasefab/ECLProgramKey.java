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
 * @file ECLProgramKey.java
 *
 * @brief Contains the defined command line keys.
 */

package de.comlet.releasefab;

public enum ECLProgramKey
{
   ADDDELIVERY("delivery_name"),
   CLI("-cli"),
   CONFIG("config"),
   CUSTOMERDOCBOOK("-customerdocbook"),
   DOCBOOK("-docbook"),
   FROM("from"),
   HELP("-help"),
   PW("pw"),
   RESULTFILE("resultfile"),
   SOURCE("source"),
   TO("to"),
   USER("user"),
   GENERALSETTINGS("generalsettings");


   /**
    * Associates a key to an enum-value.
    */
   private final String mKey;

   private ECLProgramKey(final String keyName)
   {
      this.mKey = keyName;
   }

   /**
    * Returns the key associated with this enum constant, as declared.
    * 
    * @return The key associated with this enum constant
    */
   @Override
   public final String toString()
   {
      return mKey;
   }

   /**
    * @param content
    * @return the ECLProgramKey containing content or null if none was found.
    */
   public static ECLProgramKey getEnumFromContent(String content)
   {
      for (ECLProgramKey key : ECLProgramKey.values())
      {
         if (key.mKey.equals(content))
         {
            return key;
         }
      }
      return null;
   }
}