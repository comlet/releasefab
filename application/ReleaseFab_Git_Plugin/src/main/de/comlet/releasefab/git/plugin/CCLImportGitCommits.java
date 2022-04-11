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
 * @file CCLImportGitCommits.java
 *
 * @brief Importer for Git commits.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.plugin.ACLDetailedInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.model.CCLAssignmentConstText;
import de.comlet.releasefab.library.model.CCLAssignmentSubtree;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.io.InputStream;
import org.jdom2.Element;

/**
 * Importer for Git commits. Main class of plugin.
 */
public class CCLImportGitCommits extends ACLImportStrategy
{
   private static final String NAME = "Git Commits";
   private static final String VERSION = "1.0.0";
   private static final String LICENSE = "Eclipse Public License - v 2.0";
   private static final String LICENSE_SOURCE = "https://www.eclipse.org/legal/epl-2.0/";

   /**
    * Initialize available assignment strategies.
    */
   public CCLImportGitCommits()
   {
      super(NAME, VERSION, LICENSE, LICENSE_SOURCE, CCLDeliveryGitCommits.class, true, PresentationType.ICON);

      mAssignmentStrategies.add(new CCLAssignmentGitCommits());
      mAssignmentStrategies.add(new CCLAssignmentConstText());
      mAssignmentStrategies.add(new CCLAssignmentSubtree());
   }

   /**
    * Return detailed information to be shown in the UI.
    */
   @Override
   public ACLDetailedInformation getDetailedInformation(CCLComponent component, CCLDelivery delivery)
   {
      ACLDetailedInformation detailedInfo = mDetailedInformation.get(component.getName() + delivery.getName());
      if (detailedInfo != null)
      {
         return detailedInfo;
      }

      detailedInfo = new CCLDetailedGitCommits(getName(), component, delivery);
      mDetailedInformation.put(component.getName() + delivery.getName(), detailedInfo);

      return detailedInfo;
   }

   /**
    * Create a template for the Docbook section as a XML-Element.
    */
   @Override
   public Element getDocbookSectionTemplate(CCLDelivery from, CCLDelivery to)
   {
      return SCLXMLUtil.createElement("section", SCLXMLUtil.createElement("title", getName()), new Element("para"));
   }

   @Override
   public String getDeliveryInformationName()
   {
      return new CCLDeliveryGitCommits().getName();
   }

   @Override
   public InputStream getImporterImage()
   {
      return this.getClass().getResourceAsStream("icon_git_commits.png");
   }
}
