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
 * @file CCLGitConfig.java
 *
 * @brief Git configuration.
 */

package de.comlet.releasefab.git.service;

/**
 * Encapsulates the configuration for Git. Might be expanded in the future.
 *
 */
public class CCLGitConfig
{
   private String mPath;

   public CCLGitConfig(String path)
   {
      this.mPath = path;
   }

   public String getPath()
   {
      return mPath;
   }
}
