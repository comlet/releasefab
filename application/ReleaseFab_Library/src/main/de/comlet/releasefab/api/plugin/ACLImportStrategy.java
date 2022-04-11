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
 * @file ACLImportStrategy.java
 *
 * @brief Abstract class for importer.
 */

package de.comlet.releasefab.api.plugin;

import de.comlet.releasefab.library.model.CCLAssignmentIgnore;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.jdom2.Element;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_SUBTITLE;

/**
 * Abstract base class of all import strategies. All derived classes serve as
 * entry points to the functionality of each plugin. The different assignment
 * strategies and detailed information can be reached from here.
 */
public abstract class ACLImportStrategy implements ICLPlugin
{
   /**
    * Ways of presentation in the UI.<br>
    * <br>
    * <b>NONE</b> - This importer won't be displayed on overview pages. But it
    * might still have a detail view.<br>
    * <b>LABEL</b> - The information of this importer will be displayed as a
    * label on overview pages.<br>
    * <b>ICON</b> - This importer will display an icon on overview pages
    * indicating that there is information, that can be viewed in a detail view.
    */
   public enum PresentationType
   {
      NONE, LABEL, ICON
   }

   /** Way of presentation preferred by this plugin. */
   protected PresentationType mPreferredPresentationType = PresentationType.NONE;

   /** Name of this plugin. For example "Version Import". */
   protected String mImporterName;

   /** Version of this plugin */
   protected String mImporterVersion;

   /** License of this plugin */
   protected String mImporterLicense;
   
   /** Source of {@link #mImporterLicense} */
   protected String mImporterLicenseSource;

   /**
    * Connects a type of delivery information of a component with an importer.
    */
   protected Class<? extends ACLDeliveryInformation> mDeliveryClass;

   /**
    * Indicates whether or not the information from all selected deliveries is
    * needed while exporting to in Docbook-Format. The default value is true.
    */
   protected boolean mNeedsAllSelectedDeliveries;

   /** A list of assignment strategies for a specific importer */
   protected List<ACLAssignmentStrategy> mAssignmentStrategies = new ArrayList<>();

   /**
    * A list of detailed information about components that have been requested
    * so far.
    */
   protected Map<String, ACLDetailedInformation> mDetailedInformation = new HashMap<>();

   /**
    * Constructor. Adds the assignment strategy {@link CCLAssignmentIgnore} to
    * the list of assignment strategies. Sets the indicator
    * {@link #mNeedsAllSelectedDeliveries}.
    *
    * @param importerName name of this plugin
    * @param importerVersion version of this plugin
    * @param deliveryClass type of the delivery information for this plugin
    * @param preferredType preferred way of presentation
    */
   public ACLImportStrategy(String importerName, String importerVersion, String importerLicense, String licenseSource,
         Class<? extends ACLDeliveryInformation> deliveryClass, PresentationType preferredType)
   {
      this(importerName, importerVersion, importerLicense, licenseSource, deliveryClass, true, preferredType);
   }

   /**
    * Constructor. Adds the assignment strategy CCLAssignmentIgnore to the list
    * of assignment strategies. Also gathers all external assignment strategies
    * which have been registered to the name of the module.
    *
    * @param importerName name of this plugin
    * @param importerVersion version of this plugin
    * @param deliveryClass type of the delivery information for this plugin
    * @param needsAll indicates whether or not you need the information from all
    * selected deliveries when exporting to docbook
    * @param preferredType preferred way of presentation
    */
   public ACLImportStrategy(String importerName, String importerVersion, String license, String licenseSource,
         Class<? extends ACLDeliveryInformation> deliveryClass, boolean needsAll, PresentationType preferredType)
   {
      mImporterName = importerName;
      mImporterVersion = importerVersion;
      mImporterLicense = license;
      mImporterLicenseSource = licenseSource;
      mDeliveryClass = deliveryClass;
      mNeedsAllSelectedDeliveries = needsAll;
      mPreferredPresentationType = preferredType;

      mAssignmentStrategies.add(new CCLAssignmentIgnore());
      ServiceLoader<ACLAssignmentStrategyExt> loader = ServiceLoader.load(ACLAssignmentStrategyExt.class);
      for (ACLAssignmentStrategyExt strat : loader)
      {
         for (String plugin : strat.getExternalPlugins())
         {
            if (plugin.equals(this.getClass().getModule().getName()))
            {
               mAssignmentStrategies.add(strat);
            }
         }
      }
   }

   /**
    * Docbook section template describes how the section should look like.
    *
    * @param from start with this delivery
    * @param to stop with this delivery
    * @return Docbook section template as a XML-Element
    */
   public abstract Element getDocbookSectionTemplate(CCLDelivery from, CCLDelivery to);

   /**
    * Returns the message to display in an empty docbook section. This is used
    * when there is no information to be exported.
    *
    * @return empty Docbook section template as a XML-Element
    */
   public Element getDocbookSectionEmptyMessage()
   {
      return SCLXMLUtil.createElement(XML_SUBTITLE, "N/A");
   }

   /**
    * Returns detailed information about a given component in a given delivery.
    *
    * @param component
    * @param delivery
    * @return
    */
   public abstract ACLDetailedInformation getDetailedInformation(CCLComponent component, CCLDelivery delivery);

   /**
    * Get name of this plugin. For example "Version Import".
    */
   @Override
   public String getName()
   {
      return getImporterName();
   }

   /**
    * Returns the return value of the corresponding override of
    * {@link ACLDeliveryInformation#getName()}.
    * 
    * @return Name as declared in corresponding delivery information class
    */
   public abstract String getDeliveryInformationName();

   /**
    * Creates an InputStream of an image file which shall be displayed in the
    * UI. If the importer should not be represented by an image, this method
    * must return null.
    * 
    * @return InputStream of image or null
    */
   public abstract InputStream getImporterImage();

   /**
    * Get version of this plugin.
    */
   @Override
   public String getVersion()
   {
      return mImporterVersion;
   }
   
   /**
    * Return license of this plugin.
    */
   public final String getLicense()
   {
      return mImporterLicense;
   }
   
   /**
    * Return the source of the license of this plugin.
    */
   public final String getLicenseSource()
   {
      return mImporterLicenseSource;
   }

   /**
    * Connects an information type of a component with an importer.
    *
    * @see getDeliveryInformation() in SCLProject
    * @return class extending ACLDeliveryInformation for this import strategy
    */
   public Class<? extends ACLDeliveryInformation> getDeliveryInformationType()
   {
      return mDeliveryClass;
   }

   /**
    * Gets an assigner by its name.
    *
    * @param assignmentName name of assignment strategy
    * @return assignment strategy or null
    */
   public ACLAssignmentStrategy getAssignmentStrategy(String assignmentName)
   {
      ACLAssignmentStrategy assignmentStrategy = null;
      for (ACLAssignmentStrategy assigner : getAssignmentStrategies())
      {
         if (assigner.getName().equals(assignmentName))
         {
            assignmentStrategy = assigner;
            break;
         }
      }

      return assignmentStrategy;
   }

   /**
    * Indicates whether or not you need the information from all selected
    * deliveries when exporting in Docbook-Format.
    *
    * @return true if you need all selected deliveries for a Docbook export
    */
   public boolean needsAllSelectedDeliveriesForDocbookExport()
   {
      return mNeedsAllSelectedDeliveries;
   }

   public PresentationType getPreferredPresentationType()
   {
      return mPreferredPresentationType;
   }

   public List<ACLAssignmentStrategy> getAssignmentStrategies()
   {
      return mAssignmentStrategies;
   }

   public void setAssignmentStrategies(List<ACLAssignmentStrategy> strategies)
   {
      mAssignmentStrategies = strategies;
   }

   public void addAssignmentStrategy(ACLAssignmentStrategy strategy)
   {
      mAssignmentStrategies.add(strategy);
   }

   public String getImporterName()
   {
      return mImporterName;
   }
}
