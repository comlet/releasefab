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
 * @file ICLCommitContainer.java
 *
 * @brief Interface for commit container.
 */

package de.comlet.releasefab.api.vcsservice;

/**
 * Basic interface for a commit in a version control service.
 */
public interface ICLCommitContainer
{
   String getCommitId();
}
