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
 * @file CCLDirectoryHelper.java
 *
 * @brief Test directory helper.
 */

package de.comlet.releasefab.test.util;

import java.io.File;

public class CCLDirectoryHelper
{
   /**
    * Needs to be added to the root of the project to get to the root of the
    * "_products" directory.
    */
   private static final String DIRECTORY_PRODUCTS_ROOT = "_products" + File.separator;

   /**
    * Absolute path to the root of the project.
    */
   private String mProjectRoot;

   /**
    * Absolute path to the directory containing the control XML-Files.
    */
   private String mIn;

   /**
    * Absolute path to the directory containing the output XML-Files.
    */
   private String mOut;

   /**
    * Constructor of the class. Concatenates the parameters with the defined
    * constants to create the absolute paths stored in the member variables.
    * 
    * @param rootOfProject The current "Working Directory"
    * @param directoryIn The relative path to the directory containing the
    * control XML-Files
    * @param directoryOut The relative path to the directory containing the
    * output XML-Files
    */
   public CCLDirectoryHelper(String rootOfProject, String directoryIn, String directoryOut)
   {
      mProjectRoot = rootOfProject + File.separator;
      mIn = mProjectRoot + directoryIn;
      mOut = rootOfProject + DIRECTORY_PRODUCTS_ROOT + directoryOut;
   }

   /**
    * Getter for {@link #mProjectRoot}.
    * @return {@link #mProjectRoot}
    */
   public final String getProjectRoot()
   {
      return mProjectRoot;
   }

   /**
    * Getter for {@link #mIn}.
    * @return {@link #mIn}
    */
   public final String getIn()
   {
      return mIn;
   }

   /**
    * Getter for {@link #mOut}.
    * @return {@link #mOut}
    */
   public final String getOut()
   {
      return mOut;
   }
}
