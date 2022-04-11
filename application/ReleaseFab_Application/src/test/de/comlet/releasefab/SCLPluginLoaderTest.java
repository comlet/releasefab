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
 * @file SCLPluginLoaderTest.java
 *
 * @brief Unit-tests of {@link #SCLPluginLoader}.
 */

package de.comlet.releasefab;

import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SCLPluginLoaderTest
{
   /**
    * Name of plugin Git Commits
    */
   private static final String GIT_COMMITS_PLUGIN = "Git Commits";

   /**
    * Name of plugin Important Information
    */
   private static final String IMPORTANT_INFORMATION_PLUGIN = "Important Information";

   /**
    * Name of plugin Version
    */
   private static final String VERSION_PLUGIN = "Version";

   /**
    * Checks that the Singleton instance of SCLPluginLoader is returned.
    */
   @Test
   void testGetInstance()
   {
      assertNotNull(SCLPluginLoader.getInstance());
   }

   /**
    * Loads all plugins and checks if the keys of the returned plugin map match
    * the set of plugins which should be loaded.
    */
   @Test
   void testGetImportStrategiesMap()
   {
      Map<String, ACLImportStrategy> impStratMap = SCLPluginLoader.getInstance().getImportStrategiesMap();

      assertAll(() -> assertTrue(impStratMap.keySet().contains(VERSION_PLUGIN)),
    		  () -> assertTrue(impStratMap.keySet().contains(IMPORTANT_INFORMATION_PLUGIN)),
    		  () -> assertTrue(impStratMap.keySet().contains(GIT_COMMITS_PLUGIN)));
   }

   /**
    * Tests if all subclasses of {@link ACLDeliveryInformation} on the
    * Modulepath are found.
    */
   @Test
   void testGetDeliveryInformation()
   {
      assertAll(() -> assertNotNull(SCLPluginLoader.getInstance().getDeliveryInformation("Delivery Version")),
            () -> assertNotNull(SCLPluginLoader.getInstance().getDeliveryInformation("Delivery Important Information")),
            () -> assertNotNull(SCLPluginLoader.getInstance().getDeliveryInformation("Delivery Git Commits")));
   }
}
