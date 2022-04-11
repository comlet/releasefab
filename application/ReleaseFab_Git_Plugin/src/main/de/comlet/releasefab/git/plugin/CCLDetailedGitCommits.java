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
 * @file CCLDetailedGitCommits.java
 *
 * @brief Detailed Git commits.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLDetailedInformation;
import de.comlet.releasefab.api.vcsservice.ICLVersionControlUtility;
import de.comlet.releasefab.git.classes.CCLXMLGitConstants;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLObservableCollection;
import de.comlet.releasefab.library.ui.CCLColumnDescription;
import de.comlet.releasefab.library.ui.CCLRowIterable;
import de.comlet.releasefab.library.ui.CCLTableEditor;
import java.util.List;
import java.util.ServiceLoader;
import org.jdom2.Element;

/**
 * User interface. List all Git commits and allow editing.
 */
public class CCLDetailedGitCommits extends ACLDetailedInformation
{
   private static final int COLUMN_WIDTH_HASH = 70;
   private static final int COLUMN_WIDTH_ID = 50;
   private static final int COLUMN_WIDTH_SYNOPSIS = 550;

   CCLTableEditor mUi;

   /**
    * Constructor. Call super class with basic information.
    *
    * @param importerName
    * @param component
    * @param delivery
    */
   public CCLDetailedGitCommits(String importerName, CCLComponent component, CCLDelivery delivery)
   {
      super(importerName, component, delivery);
   }

   /**
    * Initialize UI and provide detailed information as a table.
    */
   @Override
   public void fillInfoBox(Object obj)
   {
      // get delivery information
      ACLDeliveryInformation info = mComponent.getDeliveryInformation(mDelivery.getName() + mImporterName);
      Element information = info.getInformation();
      
      List<Element> listOfCommits = information.getChildren(CCLXMLGitConstants.XML_GIT_COMMIT);
      Element commit = null;
      String almIdName = "";

      if (!listOfCommits.isEmpty())
      {
         commit = listOfCommits.get(0);

         for (Element potentialName : commit.getChildren())
    	   {
            if (potentialName.getName().contains("id"))
    		   {
               almIdName = potentialName.getName();
    		   }
    	   }
      }
      else
      {
    	   // There are no commits in this component
         almIdName = CCLXMLGitConstants.XML_GIT_ALM_ID;
      }
      
      final CCLColumnDescription[] columns = {
              new CCLColumnDescription("Hash", CCLXMLGitConstants.XML_GIT_HASH, COLUMN_WIDTH_HASH, false),
              new CCLColumnDescription("Id", almIdName, COLUMN_WIDTH_ID, false),
              new CCLColumnDescription("Synopsis", CCLXMLGitConstants.XML_GIT_SYNOPSIS, COLUMN_WIDTH_SYNOPSIS, true) };
      
      // fill UI
      mUi = new CCLTableEditor();
      ServiceLoader<ICLVersionControlUtility> vcsLoader = ServiceLoader.load(ICLVersionControlUtility.class);
      try (ICLVersionControlUtility vcsUtil = vcsLoader.findFirst().get())
      {
         mUi.init(obj, columns, vcsUtil.getBranchInformation(information), vcsUtil.getTagInformation(information));
      }
      catch (Exception e)
      {
         LOGGER.debug(e.getMessage(), e);
      }

      CCLRowIterable rowIterable = new CCLRowIterable(information.getChildren(CCLXMLGitConstants.XML_GIT_COMMIT));
      mUi.addRows(rowIterable);
      mUi.enableEditor();
   }

   /**
    * Store modifications.
    */
   @Override
   public void saveInfoBox(CCLObservableCollection<CCLDelivery> deliveries) throws CCLInternalException
   {
      // save current editor's text to table cell to make sure that every change
      // will be saved, even if the TextBox is still editable.
      mUi.saveTextToTableCell();

      // get delivery information
      ACLDeliveryInformation info = mComponent.getDeliveryInformation(mDelivery.getName() + mImporterName);
      Element commitInfo = info.getInformation();

      CCLRowIterable rowIterator = new CCLRowIterable(commitInfo.getChildren(CCLXMLGitConstants.XML_GIT_COMMIT));
      mUi.saveItems(rowIterator);
   }
}
