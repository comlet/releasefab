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
 * @brief Module descriptor of module releasefab.git.service.
 */

import de.comlet.releasefab.api.vcsservice.ICLVersionControlUtility;
import de.comlet.releasefab.git.service.CCLGitUtility;

module releasefab.git.service
{
   requires transitive releasefab.git.classes;
   requires org.eclipse.jgit;
   requires org.apache.commons.lang3;
   
   provides ICLVersionControlUtility with CCLGitUtility; 
}