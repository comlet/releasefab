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
 * @file CCLXMLCommitTransformer.java
 *
 * @brief Transform Git commits to XML.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.library.xml.ACLXMLTransformIterable;
import org.jdom2.Element;

/**
 * Transform from ICLCommitContainer to XML-Element.
 */
public class CCLXMLCommitTransformer extends ACLXMLTransformIterable<ICLCommitContainer, Element>
{
   /**
    * Transform from ICLCommitContainer to XML-Element.
    */
   CCLXMLCommitTransformer(Iterable<ICLCommitContainer> commits)
   {
      super(commits);
   }

   /**
    * Do the transformation from ICLCommitContainer to XML-Element.
    */
   @Override
   public Element transform(ICLCommitContainer commit)
   {
      return CCLCommitTransformer.toXML(commit);
   }
}
