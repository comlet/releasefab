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
 * @file CCLAssignmentLocalTag.java
 *
 * @brief Assignment of Git Tag.
 */

package de.comlet.releasefab.git.plugin;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategyExt;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.api.vcsservice.ICLTagContainer;
import de.comlet.releasefab.api.vcsservice.ICLVersionControlUtility;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLParameter;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.List;
import java.util.ServiceLoader;
import org.jdom2.Element;

/**
 * Assigns the local Git TAG. Therefore the HEAD of the local repository must be
 * synched to this TAG. The location of the repository is either configured or
 * the project root is used. This is an external AssignmentStrategy. It can be
 * registered to another plugin using the method
 * {@link #addToExternalPlugin(String)}.
 */
public class CCLAssignmentLocalTag extends ACLAssignmentStrategyExt
{
   private static final String NAME = "Local git Tag";
   private static final int NUMBER_OF_PARAMETERS = 1;
   private static final String USAGE_MESSAGE = "Local Tag:\n " + "Job: Assigns local git Tag for a given repository.\n" + 
         "Optional Parameter 1: Name  of the local repository (e.g. test~mustermann).\n" + 
         "Info: If Parameter 1 is empty baseline of repository root is used if available!";

   /**
    * Initialize information.
    */
   public CCLAssignmentLocalTag()
   {
      super(NAME, NUMBER_OF_PARAMETERS, USAGE_MESSAGE);
      addToExternalPlugin("releasefab.version");
   }

   @Override
   public String getName()
   {
      return NAME;
   }

   @Override
   public int getNrOfParameters()
   {
      return NUMBER_OF_PARAMETERS;
   }

   @Override
   public String getUsageInfo()
   {
      return USAGE_MESSAGE;
   }

   /**
    * Finds local Git TAG if the HEAD is synched to it.<br>
    * parameters[0]: A project name is optional
    */
   @Override
   public Element getData(List<CCLParameter> aParameters, CCLComponent aComponent, CCLDelivery aDelivery,
         CCLDelivery formerDelivery, ACLImportStrategy aImporter, String projectRoot, CCLComponent initialComponent)
   {
      String nameOfProject = aParameters.get(INDEX_ZERO).getValue().trim();
      String baseline = aParameters.get(INDEX_ONE).getValue().trim();
      String errorHeader = aComponent + ":" + aImporter + ":" + getName() + ":";
      
      Element desc = new Element("content");

      ICLTagContainer latestTag = null;

      ServiceLoader<ICLVersionControlUtility> vcsLoader = ServiceLoader.load(ICLVersionControlUtility.class);
      try (ICLVersionControlUtility vcsUtility = vcsLoader.findFirst().get())
      {
         vcsUtility.initializeHandler(nameOfProject);
         if (null == baseline || baseline.isEmpty())
         {
            latestTag = vcsUtility.isSyncedToTag();

            if (null == latestTag || latestTag.getName().isEmpty())
            {
               throw new CCLInternalException("GIT: No TAG found");
            }
            desc.addContent(SCLXMLUtil.createElement("string", latestTag.getName()));
         }
         else
         {
            desc.addContent(SCLXMLUtil.createElement("string", baseline));
         }

      }
      catch (CCLInternalException | RuntimeException e)
      {
         LOGGER.error("{} {}", errorHeader, e.getMessage(), e);
         desc.addContent(SCLXMLUtil.createElement("error", e.getMessage()));
      }

      return desc;
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
