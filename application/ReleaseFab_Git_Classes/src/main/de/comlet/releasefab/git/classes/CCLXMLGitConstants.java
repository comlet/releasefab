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
 * @file CCLXMLGitConstants.java
 *
 * @brief Constants concerning the Git plugin.
 */

package de.comlet.releasefab.git.classes;

public final class CCLXMLGitConstants
{
   public static final String IMPORTER_NAME = "Git Commits";

   public static final String XML_GIT_COMMIT = "commit";
   public static final String XML_GIT_HASH = "hash";
   public static final String XML_GIT_TIME = "time";
   public static final String XML_GIT_ALM_ID = "alm-id";
   public static final String XML_GIT_SYNOPSIS = "synopsis";
   public static final String XML_GIT_INTERNAL_DOC = "internal-doc";
   public static final String XML_GIT_EXTERNAL_DOC = "external-doc";

   // For a Git TAG
   public static final String XML_GIT_TAG = "tag";
   public static final String XML_GIT_TAG_ATTR_HASH = "hash";
   public static final String XML_GIT_TAG_ATTR_TARGET = "target";
   public static final String XML_GIT_TAG_ATTR_TYPE = "type";

   // For a Git branch
   public static final String XML_GIT_BRANCH = "branch";

   // Attribute constants for 'type'
   public static final String XML_GIT_TAG_ATTR_TYPE_FORMER = "former";
   public static final String XML_GIT_TAG_ATTR_TYPE_LATEST = "latest";

   private CCLXMLGitConstants()
   {
   }
}
