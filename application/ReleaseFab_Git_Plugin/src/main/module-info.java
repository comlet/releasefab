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
 * @brief Module descriptor of module releasefab.git.plugin.
 */

import de.comlet.releasefab.api.almservice.ICLALMUtility;
import de.comlet.releasefab.api.plugin.ACLAssignmentStrategyExt;
import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.api.vcsservice.ICLVersionControlUtility;
import de.comlet.releasefab.git.plugin.CCLAssignmentLocalTag;
import de.comlet.releasefab.git.plugin.CCLDeliveryGitCommits;
import de.comlet.releasefab.git.plugin.CCLImportGitCommits;

module releasefab.git.plugin
{
   requires releasefab.git.classes;
   
   uses ICLALMUtility;
   uses ICLVersionControlUtility;
   
   provides ACLImportStrategy with CCLImportGitCommits;
   provides ACLDeliveryInformation with CCLDeliveryGitCommits;
   provides ACLAssignmentStrategyExt with CCLAssignmentLocalTag;
}