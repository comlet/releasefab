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
 * @file CCLXMLCommitSource.java
 *
 * @brief Source of Git commits.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.git.classes.CCLXMLGitConstants;
import de.comlet.releasefab.library.xml.ACLXMLTransformIterable;
import org.jdom2.Element;

/**
 * Provide an Iterable. Read commit information from XML-Element and provide it
 * as an ICLCommitContainer object.
 */
public class CCLXMLCommitSource extends ACLXMLTransformIterable<Element, ICLCommitContainer>
{
   /**
    * Constructor. Initialize information.
    */
   public CCLXMLCommitSource(Element information)
   {
      super(information.getChildren(CCLXMLGitConstants.XML_GIT_COMMIT));
   }

   /**
    * Read data from XML-Element and transform it to an ICLCommitContainer.
    */
   @Override
   public ICLCommitContainer transform(Element input)
   {
      return CCLCommitTransformer.toContainer(input);
   }
}