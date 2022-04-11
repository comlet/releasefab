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
 * @file CCLDeliveryGitCommits.java
 *
 * @brief Delivery of Git commits.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.almservice.ICLALMUtility;
import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.api.vcsservice.ICLVersionControlUtility;
import de.comlet.releasefab.git.classes.CCLXMLGitConstants;
import de.comlet.releasefab.library.exception.CCLALMException;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.exception.CCLVersionControlRuntimeException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.xml.CCLXMLDocBookSink;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import org.jdom2.Element;

/**
 * Provide Docbook XML-Element for Git commits.
 */
public class CCLDeliveryGitCommits extends ACLDeliveryInformation
{
   private static final String NAME = "Delivery Git Commits";

   /**
    * Provide the name of this delivery class.
    */
   @Override
   public String getName()
   {
      return NAME;
   }

   /**
    * Adds the XML-Element other to the stored information.
    * 
    * @param other XML-Element to add
    */
   @Override
   public boolean addInformation(Element other)
   {
      mInformation.addContent(other.getChild(CCLXMLGitConstants.XML_GIT_COMMIT));
      return true;
   }

   /**
    * Provide docbook output in the passed XML-Element 'element'. Only error
    * handling in this method.
    * 
    * @param element XML-Element to store information in
    * @param component Component to be documented
    * @param other Delivery which is not used to document Git commits
    * @param forCustomer Defines whether the export is for a customer
    */
   @Override
   public boolean addDocbookSection(Element element, CCLComponent component, CCLDelivery other, boolean forCustomer)
   {
      boolean isInfoAvailable = false;
      try
      {
         isInfoAvailable = addDocbookSectionIntern(element, component);
      }
      catch (CCLALMException e)
      {
         String errorHeader = component + ":" + ":" + getName() + ":";
         LOGGER.error("{} {}", errorHeader, e.getMessage(), e);

         // Not possible to show a message box here, because this method is not
         // called in the display thread. But a runtime exception is shown in a
         // message box
         throw new CCLVersionControlRuntimeException(e.getMessage());
      }
      return isInfoAvailable;
   }

   /**
    * Provide Docbook-Output in 'element'.
    */
   public boolean addDocbookSectionIntern(Element element, CCLComponent component) throws CCLALMException
   {
      // check if there is any information
      boolean isInfoAvailable = !isInfoNullOrEmpty();
      if (isInfoAvailable)
      {
         // setup ALM Service
         Iterator<ICLALMUtility> almLoader = ServiceLoader.load(ICLALMUtility.class).iterator();
         Iterator<ICLVersionControlUtility> gitLoader = ServiceLoader.load(ICLVersionControlUtility.class).iterator();

         Optional<ICLALMUtility> almOptional = (almLoader.hasNext()) ? Optional.of(almLoader.next()) : Optional.empty();
         Optional<ICLVersionControlUtility> gitOptional = (gitLoader.hasNext()) ? Optional.of(gitLoader.next()) : 
               Optional.empty();
         try (ICLALMUtility almUtil = (!almOptional.isEmpty()) ? almOptional.get() : null;
               ICLVersionControlUtility gitutil = (!gitOptional.isEmpty()) ? gitOptional.get() : null)
         {
            // setup source for Git commits: Section from XML-File
            Iterable<ICLCommitContainer> source = new CCLXMLCommitSource(mInformation);

            // setup ALM filter
            Iterable<ICLCommitContainer> filter;
            try
            {
               filter = gitutil.getCommitFilter(source, almUtil);
            }
            catch (CCLInternalException e)
            {
               filter = source;
               LOGGER.info("No ALM Plugin loaded, Commits will be unordered!");
               LOGGER.debug("No ALM Plugin", e);
            }

            // sort commits
            CCLCommitSortTransformer sorter = new CCLCommitSortTransformer(filter);

            // Transform Container to Docbook table row
            CCLXMLDocBookCommitTransformer transformer = new CCLXMLDocBookCommitTransformer(sorter);

            // setup sink for the Docbook document
            CCLXMLDocBookSink sink = new CCLXMLDocBookSink(element, component.getFullName(), "Id", "Synopsis");
            sink.addItems(transformer);
         }
      }

      return isInfoAvailable;
   }
}
