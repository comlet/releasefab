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
 * @file CCLXMLGitSink.java
 *
 * @brief Git XML sink.
 */

package de.comlet.releasefab.git.service;

import de.comlet.releasefab.api.vcsservice.ICLTagContainer;
import de.comlet.releasefab.git.classes.CCLGitTagContainer;
import de.comlet.releasefab.git.classes.CCLXMLGitConstants;
import de.comlet.releasefab.library.xml.CCLXMLSinkBase;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Provide an XML-Sink with additional functionality for TAGs and branch names.
 */
public class CCLXMLGitSink extends CCLXMLSinkBase
{
   /**
    * Universal ID for serialization purposes.
    */
   private static final long serialVersionUID = 7370216106810932694L;

   /**
    * Constructor.
    */
   public CCLXMLGitSink()
   {
      super();
   }

   /**
    * Add branch name.
    */
   public void addBranch(String branchName)
   {
      addElement(CCLXMLGitConstants.XML_GIT_BRANCH, branchName);
   }

   /**
    * Add former and current TAG.
    */
   public void addTags(ICLTagContainer formerTag, ICLTagContainer currentTag)
   {
      if (formerTag != null)
      {
         Element tagElement = tagToXML(formerTag);
         tagElement.setAttribute(new Attribute(CCLXMLGitConstants.XML_GIT_TAG_ATTR_TYPE,
               CCLXMLGitConstants.XML_GIT_TAG_ATTR_TYPE_FORMER));
         this.addContent(tagElement);
      }

      if (currentTag != null)
      {
         Element tagElement = tagToXML(currentTag);
         tagElement.setAttribute(new Attribute(CCLXMLGitConstants.XML_GIT_TAG_ATTR_TYPE,
               CCLXMLGitConstants.XML_GIT_TAG_ATTR_TYPE_LATEST));
         this.addContent(tagElement);
      }
   }

   /**
    * Do transformation from ICLTagContainer to XML-Element.
    */
   private static Element tagToXML(ICLTagContainer tag)
   {
      Element tagElement = new Element(CCLXMLGitConstants.XML_GIT_TAG);
      tagElement.addContent(tag.getName());

      tagElement.setAttribute(
            new Attribute(CCLXMLGitConstants.XML_GIT_TAG_ATTR_HASH, ((CCLGitTagContainer) tag).getHash()));
      tagElement.setAttribute(
            new Attribute(CCLXMLGitConstants.XML_GIT_TAG_ATTR_TARGET, ((CCLGitTagContainer) tag).getPointsToHash()));

      return tagElement;
   }
}
