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
 * @file ICLVersionControlUtility.java
 *
 * @brief Interface for VCS utility.
 */

package de.comlet.releasefab.api.vcsservice;

import de.comlet.releasefab.api.almservice.ICLALMUtility;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.xml.CCLXMLSinkBase;
import org.jdom2.Element;

/**
 * Interface for a Version Control Service.
 */
public interface ICLVersionControlUtility extends AutoCloseable
{
   /**
    * Handles the initialization of a handler for the implementation of the
    * version control service.
    * 
    * @param path Path to the local source code directory under version control
    * @throws CCLInternalException
    */
   void initializeHandler(String path) throws CCLInternalException;

   /**
    * Get iterator for commits that complies with the required rules.
    * 
    * @param formerTag
    * @return iterator.
    * @throws CCLInternalException
    */
   Iterable<ICLCommitContainer> getIterator(ICLTagContainer formerTag) throws CCLInternalException;

   /**
    * Check if HEAD is synched to a TAG.
    * 
    * @throws CCLInternalException
    */
   ICLTagContainer isSyncedToTag() throws CCLInternalException;

   /**
    * Get name of the current branch.
    * 
    * @throws CCLInternalException 
    */
   String getCurrentBranch() throws CCLInternalException;

   /**
    * Get commit containers filtered by status.
    * 
    * @throws CCLInternalException
    */
   Iterable<ICLCommitContainer> getCommitFilter(Iterable<ICLCommitContainer> source, ICLALMUtility cbutil)
         throws CCLInternalException;

   /**
    * Get former documented tag.
    * 
    * @return TAG or null if no former TAG has been found
    */
   ICLTagContainer getFormerTag(CCLComponent component, CCLDelivery currentDelivery, CCLDelivery formerDelivery);

   /**
    * Create String with branch information for the UI.
    */
   String getBranchInformation(Element information);

   /**
    * Create string with TAG information for the UI.
    */
   String getTagInformation(Element information);

   /**
    * Constructs a CCLXMLSinkBase with basic information about the current revision.
    */
   CCLXMLSinkBase getVersionControlXMLSink(ICLTagContainer formerTag, ICLTagContainer currentTag)
         throws CCLInternalException;

   /**
    * Closes VCS handler.
    */
   @Override
   void close();
}
