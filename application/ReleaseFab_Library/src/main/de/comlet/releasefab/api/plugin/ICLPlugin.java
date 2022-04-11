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
 * @file ICLPlugin.java
 *
 * @brief Interface for plugins.
 */

package de.comlet.releasefab.api.plugin;

/**
 * Each plugin must extend ICLPlugin to allow the application to recognize it as a
 * plugin.
 */
public interface ICLPlugin
{
   /**
    * Get name of this plugin. For example "Version Import".
    *
    * @return name of this plugin
    */
   String getName();

   /**
    * Get version of this plugin.
    *
    * @return version of this plugin as String
    */
   String getVersion();
}
