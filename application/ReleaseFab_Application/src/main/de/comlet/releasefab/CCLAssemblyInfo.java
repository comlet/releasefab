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
 * @file CCLAssemblyInfo.java
 *
 * @brief Information about the current version of the application.
 */

package de.comlet.releasefab;

/**
 * General information about an assembly. Change these values to modify the
 * information associated with an assembly.
 */
public final class CCLAssemblyInfo
{
   private static final String PRODUCT_NAME = "ReleaseFab";
   private static final String COMPANY = "comlet Verteilte Systeme GmbH";
   private static final String COPYRIGHT = "Copyright © 2023";
   private static final String VERSION = "1.0.1";

   private CCLAssemblyInfo()
   {
   }

   /**
    * Product name
    * 
    * @return product name
    */
   public static String getProductName()
   {
      return PRODUCT_NAME;
   }

   /**
    * Company name
    * 
    * @return company name
    */
   public static String getCompany()
   {
      return COMPANY;
   }

   /**
    * Copyright
    * 
    * @return copyright
    */
   public static String getCopyright()
   {
      return COPYRIGHT;
   }

   /**
    * Product version
    * 
    * @return product version
    */
   public static String getVersion()
   {
      return VERSION;
   }
}
