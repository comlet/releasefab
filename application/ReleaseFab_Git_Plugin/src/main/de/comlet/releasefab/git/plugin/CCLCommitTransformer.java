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
 * @file CCLCommitTransformer.java
 *
 * @brief Transform Git commits.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.git.classes.CCLGitCommitContainer;
import de.comlet.releasefab.git.classes.CCLXMLGitConstants;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.List;
import org.jdom2.Element;

/**
 * Transform CCLCommitContainer to XML-Element and vice versa.
 */
public final class CCLCommitTransformer
{
   private CCLCommitTransformer()
   {
   }

   /**
    * Do transformation from ICLCommitContainer to XML-Element.
    */
   public static Element toXML(ICLCommitContainer commit)
   {
      Element commitElement = new Element(CCLXMLGitConstants.XML_GIT_COMMIT);

      // Downcasts are guaranteed to be save in this module
      SCLXMLUtil.addElement(commitElement, CCLXMLGitConstants.XML_GIT_HASH,          ((CCLGitCommitContainer) commit).getHash());
      SCLXMLUtil.addElement(commitElement, CCLXMLGitConstants.XML_GIT_TIME,          ((CCLGitCommitContainer) commit).timeAsString());
      SCLXMLUtil.addElement(commitElement, CCLXMLGitConstants.XML_GIT_ALM_ID, 		 ((CCLGitCommitContainer) commit).idAsString());
      SCLXMLUtil.addElement(commitElement, CCLXMLGitConstants.XML_GIT_SYNOPSIS,      ((CCLGitCommitContainer) commit).getShortDescription());
      SCLXMLUtil.addElement(commitElement, CCLXMLGitConstants.XML_GIT_INTERNAL_DOC,  ((CCLGitCommitContainer) commit).getInternalDoc());
      SCLXMLUtil.addElement(commitElement, CCLXMLGitConstants.XML_GIT_EXTERNAL_DOC,  ((CCLGitCommitContainer) commit).getExternalDoc());

      return commitElement;
   }

   /**
    * Do transformation from ICLCommitContainer to Docbook-XML.
    */
   public static Element toDocbookXML(ICLCommitContainer commit)
   {
      return SCLXMLUtil.createElement(CCLXMLConstants.XML_ROW,
            SCLXMLUtil.createElement(CCLXMLConstants.XML_ENTRY, SCLXMLUtil.createElement(CCLXMLConstants.XML_PARA, ((CCLGitCommitContainer) commit).getHash())),
            SCLXMLUtil.createElement(CCLXMLConstants.XML_ENTRY, SCLXMLUtil.createElement(CCLXMLConstants.XML_PARA, ((CCLGitCommitContainer) commit).getShortDescription())));
   }

   /**
    * Do transformation from Element to ICLCommitContainer.
    */
   public static ICLCommitContainer toContainer(Element commit)
   {
      int time        = Integer.parseInt(commit.getChildText(CCLXMLGitConstants.XML_GIT_TIME));
      
      List<Element> listOfChildren = commit.getChildren();
      String almIdName = "";
      
      for (Element potentialName : listOfChildren)
      {
    	  if (potentialName.getName().contains("id"))
    	  {
    		  almIdName = potentialName.getName();
    	  }
      }

      return new CCLGitCommitContainer(
            commit.getChildText(CCLXMLGitConstants.XML_GIT_HASH),
            commit.getChildText(almIdName),
            time,
            commit.getChildText(CCLXMLGitConstants.XML_GIT_SYNOPSIS),
            commit.getChildText(CCLXMLGitConstants.XML_GIT_INTERNAL_DOC),
            commit.getChildText(CCLXMLGitConstants.XML_GIT_EXTERNAL_DOC),
            "" /* reviewer */,
            false /* api mod */
            );
   }
}
