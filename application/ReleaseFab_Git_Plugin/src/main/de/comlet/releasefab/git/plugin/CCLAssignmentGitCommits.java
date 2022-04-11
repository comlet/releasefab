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
 * @file CCLAssignmentGitCommits.java
 *
 * @brief Assignment of Git commits.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategyExt;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.api.vcsservice.ICLTagContainer;
import de.comlet.releasefab.api.vcsservice.ICLVersionControlUtility;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.exception.CCLVersionControlException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLParameter;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.CCLXMLSinkBase;
import java.util.List;
import java.util.ServiceLoader;
import org.jdom2.Element;

/**
 * Provide commits from a local Git repository.
 */
public class CCLAssignmentGitCommits extends ACLAssignmentStrategyExt
{
   private static final String NAME = "Git Commits";
   private static final int NUMBER_OF_PARAMETERS = 1;
   private static final String USAGE_MESSAGE = "Assignment Local Git Tasks:\n" + 
         "Job: Assign tasks extracted out of git repository\n" + 
         "Parameter 1: - optional - Git repository (e.g. E:\\augusta-git2\\augusta_sw_src\n";

   /**
    * Initialize information.
    */
   public CCLAssignmentGitCommits()
   {
      super(NAME, NUMBER_OF_PARAMETERS, USAGE_MESSAGE);
   }

   /**
    * Provide commit information from a local Git repository. This method only
    * does error handling.
    */
   @Override
   public Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot, CCLComponent initialComponent)
   {
      Element result = null;
      try
      {
         result = getFormerVCSTag(parameters, component, delivery, formerDelivery, projectRoot);
      }
      catch (CCLInternalException e)
      {
         String errorHeader = component + ":" + importer + ":" + getName() + ":";
         LOGGER.error("{} {}", errorHeader, e.getMessage(), e);
         result = new Element(CCLXMLConstants.XML_ERROR);
         result.addContent(e.getMessage());

         // Not possible to show a message box here, because this method is not
         // called in the display thread.
      }

      return result;
   }

   /**
    * Get former TAG. Create Git configuration object.
    * 
    * @param parameters parameters entered by the user
    * @param component component to be documented
    * @param delivery delivery to document the component in
    * @param formerDelivery delivery before the current delivery
    * @param projectRoot root path of the project
    * @return XML-Element.
    */
   protected Element getFormerVCSTag(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, String projectRoot) throws CCLInternalException
   {
      String path = parameters.isEmpty() ? projectRoot : parameters.get(0).getValue();

      // Starting point: former tag or git root node.
      ICLTagContainer formerTag = null;

      ServiceLoader<ICLVersionControlUtility> versionControlLoader = ServiceLoader.load(ICLVersionControlUtility.class);

      try (ICLVersionControlUtility gitutil = versionControlLoader.findFirst().get())
      {
         gitutil.initializeHandler(path);
         formerTag = gitutil.getFormerTag(component, delivery, formerDelivery);
      }

      if (formerTag == null)
      {
         LOGGER.warn("Git warning: No former tag available. Starting from root.");
      }
      return getGitData(path, formerTag, versionControlLoader);
   }

   /**
    * Query for commits. Starting from the latest TAG until head revision.
    * 
    * @param config configuration for Git handler class
    * @param formerTag TAG of the last delivery
    * @param versionControlLoader provides access to a VCS service
    * @return XML-Element
    */
   protected Element getGitData(String path, ICLTagContainer formerTag,
         ServiceLoader<ICLVersionControlUtility> versionControlLoader) throws CCLInternalException
   {
      try (ICLVersionControlUtility gitutil = versionControlLoader.findFirst().get())
      {
         gitutil.initializeHandler(path);
         ICLTagContainer latestTag = gitutil.isSyncedToTag();
         if (latestTag == null)
         {
            throw new CCLVersionControlException("Head is not synched to a tag. Documentation stopped.");
         }

         Iterable<ICLCommitContainer> commits = gitutil.getIterator(formerTag);

         CCLXMLSinkBase sink = gitutil.getVersionControlXMLSink(formerTag, latestTag);
         return processInput(commits, sink);
      }
   }

   /**
    * Generate XML-Output.
    * 
    * @param commits Commits to be stored
    * @param sink XML-Sink to store the commits in
    * @return
    */
   protected CCLXMLSinkBase processInput(Iterable<ICLCommitContainer> commits, CCLXMLSinkBase sink)
   {
      sink.addItems(new CCLXMLCommitTransformer(commits));
      return sink;
   }

   /**
    * Overload of method
    * {@link #getData(List, CCLComponent, CCLDelivery, CCLDelivery, ACLImportStrategy, String, CCLComponent)}
    * without an initial component.
    */
   @Override
   public Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot)
   {
      return getData(parameters, component, delivery, formerDelivery, importer, projectRoot, null);
   }
}
