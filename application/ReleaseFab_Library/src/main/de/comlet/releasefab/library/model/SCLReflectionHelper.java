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
 * @file SCLReflectionHelper.java
 *
 * @brief Helper class for de- and serializing settings.
 */

package de.comlet.releasefab.library.model;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SCLReflectionHelper
{
   private SCLReflectionHelper()
   {
   }

   /**
    * Get the underlying class for a type, or null if the type is a variable type.
    *
    * @param type the type.
    * @return the underlying class.
    */
   private static Class<?> getClass(Type type)
   {
      Class<?> theClass = null;
      if (type instanceof Class)
      {
         theClass = (Class<?>) type;
      }
      else if (type instanceof ParameterizedType)
      {
         theClass = getClass(((ParameterizedType) type).getRawType());
      }
      else if (type instanceof GenericArrayType)
      {
         Type componentType = ((GenericArrayType) type).getGenericComponentType();
         Class<?> componentClass = getClass(componentType);
         if (null != componentClass)
         {
            theClass = Array.newInstance(componentClass, 0).getClass();
         }
      }
      return theClass;
   }

   /**
    * Get the actual type arguments a child class has used to extend a generic base class.
    *
    * @param baseClass the base class.
    * @param childClass the child class.
    * @return a list o the raw classes for the actual type arguments.
    */
   public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass)
   {
      Map<Type, Type> resolvedTypes = new HashMap<>();
      Type type = childClass;
      while (getClass(type) != null && !getClass(type).equals(baseClass))
      {
         if (type instanceof Class)
         {
            type = ((Class<?>) type).getGenericSuperclass();
         }
         else
         {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();

            updateResolvedTypes(resolvedTypes, parameterizedType, rawType);

            if (!rawType.equals(baseClass))
            {
               type = rawType.getGenericSuperclass();
            }
         }
      }

      // finally, for each actual type argument provided to baseClass,
      // determine (if possible) the raw type for that type argument.
      return createTypeArgumentsAsClasses(resolvedTypes, type);
   }

   private static void updateResolvedTypes(Map<Type, Type> resolvedTypes, ParameterizedType parameterizedType,
         Class<?> rawType)
   {
      Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
      for (int i = 0; i < actualTypeArguments.length; ++i)
      {
         resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
      }
   }

   private static List<Class<?>> createTypeArgumentsAsClasses(Map<Type, Type> resolvedTypes, Type type)
   {
      Type[] actualTypeArguments;
      if (type instanceof Class)
      {
         actualTypeArguments = ((Class<?>) type).getTypeParameters();
      }
      else
      {
         actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
      }
      List<Class<?>> typeArgumentsAsClasses = new ArrayList<>();
      // resolve types by chasing down type variables.
      for (Type baseType : actualTypeArguments)
      {
         while (resolvedTypes.containsKey(baseType))
         {
            baseType = resolvedTypes.get(baseType);
         }
         typeArgumentsAsClasses.add(getClass(baseType));
      }
      return typeArgumentsAsClasses;
   }
}
