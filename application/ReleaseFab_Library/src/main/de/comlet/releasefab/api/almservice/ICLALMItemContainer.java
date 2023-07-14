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
 * @file ICLALMItemContainer.java
 *
 * @brief Interface for ALM container.
 */

package de.comlet.releasefab.api.almservice;

import java.util.Optional;

/**
 * Basic interface for Items of an Application Lifecycle Management System.
 */
public interface ICLALMItemContainer
{
   String getALMItemID();
   
   Optional<Integer> getIdAsInteger();
}
