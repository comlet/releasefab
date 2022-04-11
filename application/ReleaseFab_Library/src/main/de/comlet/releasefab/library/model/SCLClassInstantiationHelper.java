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
 * @file SCLClassInstantiationHelper.java
 *
 * @brief Helper class to check type hierarchies.
 */

package de.comlet.releasefab.library.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

/**
 * Encapsulates some functionality concerning the selection of the correct
 * types.
 *
 */
public final class SCLClassInstantiationHelper
{
   private static Map<String, Class<?>> sSimpleNameToClassMap;

   static
   {
      Map<String, Class<?>> aMap = new HashMap<>();
      aMap.put(String.class.getSimpleName().toLowerCase(), String.class);
      aMap.put(Integer.class.getSimpleName().toLowerCase(), Integer.class);
      aMap.put(List.class.getSimpleName().toLowerCase(), List.class);
      aMap.put(Map.class.getSimpleName().toLowerCase(), Map.class);
      sSimpleNameToClassMap = aMap;
   }

   /** Initialize logger for this class */
   private static final Logger UNINTERESTING = NOPLogger.NOP_LOGGER;

   private SCLClassInstantiationHelper()
   {
   }

   public static Class<?> getClassFromAttributeOfElement(Element element, String attributeName)
   {
      String className = element.getAttributeValue(attributeName, String.class.getName());
      Class<?> theClass = getClassForName(className);
      if (null == theClass)
      {
         theClass = sSimpleNameToClassMap.get(className.toLowerCase());
      }
      if (null == theClass)
      {
         theClass = String.class;
      }
      return theClass;
   }

   public static Class<?> getClassForName(String className)
   {
      Class<?> theClass = null;
      try
      {
         theClass = Class.forName(className);
      }
      catch (ClassNotFoundException e)
      {
         UNINTERESTING.trace("", e);
      }
      return theClass;
   }

   public static boolean inheritsOrImplements(Class<?> classToCheck, Class<?> other)
   {
      boolean ok = true;
      try
      {
         classToCheck.asSubclass(other);
      }
      catch (ClassCastException e)
      {
         UNINTERESTING.debug("Exception by design. Not need to log it.", e);
         ok = false;
      }
      return ok;
   }
}
