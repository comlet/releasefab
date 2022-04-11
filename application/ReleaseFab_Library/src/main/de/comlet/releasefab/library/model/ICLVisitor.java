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
 * @file ICLVisitor.java
 *
 * @brief Visitor interface.
 */

package de.comlet.releasefab.library.model;

/**
 * Interface for the visitor pattern.
 * 
 * @param <RETURN> return type
 * @param <P2> type of the 2nd parameter
 */
public interface ICLVisitor<RETURN, P2>
{
   RETURN visit(CCLComponent component, P2 target, boolean quickReturn);
}
