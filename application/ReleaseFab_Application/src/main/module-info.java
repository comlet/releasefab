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
 * @file module-info.java
 *
 * @brief Module descriptor for module releasefab.application.
 */

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;

module releasefab.application
{
   requires transitive releasefab.library;
   requires org.junit.jupiter.api;

   uses ACLDeliveryInformation;
   uses ACLImportStrategy;
}