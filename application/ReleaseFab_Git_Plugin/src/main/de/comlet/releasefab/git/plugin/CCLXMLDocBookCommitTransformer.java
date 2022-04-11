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
 * @file CCLXMLDocBookCommitTransformer.java
 *
 * @brief Transform Git commits to Docbook XML.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.library.xml.ACLXMLTransformIterable;
import org.jdom2.Element;

/**
 * Transform ICLCommitContainer to Docbook-XML.
 */
public class CCLXMLDocBookCommitTransformer extends ACLXMLTransformIterable<ICLCommitContainer, Element>
{
   /**
    * Transform ICLCommitContainer to Docbook-XML.
    */
   CCLXMLDocBookCommitTransformer(Iterable<ICLCommitContainer> source)
   {
      super(source);
   }

   /**
    * Do the transformation from ICLCommitContainer to Docbook-XML.
    */
   @Override
   public Element transform(ICLCommitContainer commit)
   {
      return CCLCommitTransformer.toDocbookXML(commit);
   }
}
