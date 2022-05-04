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
 * @file SCLProject.java
 *
 * @brief Model class for the open project.
 */

package de.comlet.releasefab;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.api.plugin.ACLTreeVisitor;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLObservableCollection;
import de.comlet.releasefab.library.model.CCLParameter;
import de.comlet.releasefab.library.model.CCLTuple;
import de.comlet.releasefab.library.model.SCLProjectHelper;
import de.comlet.releasefab.library.settings.SCLSettings;
import de.comlet.releasefab.library.settings.SCLSettings.ECLSettingsType;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.jdom2.Attribute;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the model - implemented as singleton This is the class which
 * contains and manages all information about the project.
 */
public final class SCLProject
{
   /**
    * Singleton Pattern! sInstance is the only instance of SCLProject.
    *
    * @see getInstance()
    */
   private static volatile SCLProject sInstance;

   /** Lock object */
   private static Object sLockObject = new Object();

   /** Indicates if project needs saving */
   private static boolean sNeedsSaving;

   /**
    * Access to the user settings. File has to be in root directory of
    * application. Every other dependency should be embedded in this file
    */
   private static String sOpenFileName = "";

   /** Path to the project specific settings file. */
   private static String sProjectSettingsPath;

   /** Path to the user specific settings file. */
   private static String sUserSettingsPath;

   /**
    * Application is placed in a project structure and called by a script. If
    * this is the case our root is defined in the first application parameter.
    */
   private static String sProjectRoot = "";

   /**
    * Application is placed in a project structure and called by a script. If
    * the configuration file for the project is not in that place, a custom
    * path relative to {@link SCLProject#sProjectRoot} can be passed.
    */
   private static String sConfigRoot = "";

   /**
    * Location of executable, if a project root is passed as program parameter
    * it can be different.
    */
   private static String sExecutableRoot;
   
   private static final String IMPORTERS = "importers";
   private static final String PARAMETERS = "parameters";
   private static final String VERSION = "version";
   private static final String PROJECT_FILE_NAME = "PROJECT_FILE_NAME";
   private static final String DELIVERY = "delivery";
   
   /**
    * Indicates if we just want a preview of data without any changes to model
    */
   private static boolean sTestMode;

   /**
    * Component root allows access to the component tree. Every other component
    * in the tree can be reached over this node. The root component is not shown,
    * it just serves as the entry point to the component tree.
    */
   private static volatile CCLComponent sComponentRoot;

   /**
    * Contains all the plugins that are referenced in the open file with 
    * version information but are not part of the Modulepath.
    */
   private static Set<String> sMissingPlugins = new HashSet<String>();

   /** Initialize logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(SCLProject.class);

   /**
    * Parameter "name" for XML actions
    */
   private static final String PARAMETER_NAME = "name";

   /** Creation report as XML element */
   private Element mCreationReport;

   /**
    * The global delivery information. Name, Date and Integrator are the same
    * for all components taking part in a delivery. More delivery information
    * is stored in the components and is accessible by their name.
    */
   private CCLObservableCollection<CCLDelivery> mDeliveries = new CCLObservableCollection<>(new TreeSet<CCLDelivery>());

   /**
    * Private default constructor is called once at startup time and creates the
    * only instance of the class.
    */
   private SCLProject()
   {
      try
      {
         // Load general settings
         String settingsPath = getExecutableRoot() + "settings.xml";
         LOGGER.trace("Path to general settings file: {}", settingsPath);
         SCLSettings.loadGeneralSettings(settingsPath);
         getProjectSettings();
         getUserSettings();

         // Load importers
         loadPlugins();
      }
      catch (CCLInternalException e)
      {
         LOGGER.error("Error during SCLProject creation: {}", e.getMessage(), e);
      }

   }

   /**
    * Lazy Singleton (late creation)!
    *
    * @return the only instance of SCLProject
    */
   public static SCLProject getInstance()
   {
      if (null == sInstance)
      {
         synchronized (sLockObject)
         {
            if (null == sInstance)
            {
               sInstance = new SCLProject();
            }
         }
      }

      return sInstance;
   }

   /**
    * Loads project settings out of a XML file. This file can be in the 
    * executable directory or in a project directory passed as program
    * arguments
    *
    * @throws CCLInternalException s
    */
   private static void getProjectSettings() throws CCLInternalException
   {
      try
      {
         sProjectSettingsPath = "";

         if (null != sProjectRoot && !sProjectRoot.isEmpty())
         {
            if (null == sConfigRoot || sConfigRoot.isEmpty())
            {
               sConfigRoot = "." + File.separator + "config";
            }

            sProjectSettingsPath = SCLProjectHelper.getAbsoluteFilePath(
                  sConfigRoot + File.separator + SCLSettings.get(PROJECT_FILE_NAME), sProjectRoot);
         }

         File projectSettingsFile = new File(sProjectSettingsPath);
         if (!projectSettingsFile.exists())
         {
            LOGGER.trace("Could not find project config file at the given position. " + 
                         "Using executable path instead. Passed parameters are \"" + sProjectRoot + "\" and \"" + 
                         sConfigRoot + "\".");

            // also set ProjectRoot to ExecutableRoot
            sProjectRoot = getExecutableRoot();

            // if the project configuration file does not exist in the passed location,
            // try to find it in the root folder of the application.
            sProjectSettingsPath = SCLProjectHelper.getAbsoluteFilePath(SCLSettings.get("PROJECT_FILE_PATH"), sProjectRoot) +
                         SCLSettings.get(PROJECT_FILE_NAME);
         }

         LOGGER.trace("Project root path: {}", sProjectRoot);

         // now check if the project settings file exists
         projectSettingsFile = new File(sProjectSettingsPath);
         if (!projectSettingsFile.exists())
         {
            throw new CCLInternalException(SCLSettings.get(PROJECT_FILE_NAME) + 
                  " not found! Has to be in execution directory or alternative path " + 
                  "has to be passed by program parameters! Passed parameters are \"" + 
                  sProjectRoot + "\" and \"" + 
                  sConfigRoot + "\".");
         }

         // load project specific settings
         LOGGER.trace("Path to project settings file: {}", sProjectSettingsPath);
         SCLSettings.loadProjectSettings(projectSettingsFile);
      }
      catch (CCLInternalException | JDOMException | IOException | RuntimeException e)
      {
         LOGGER.error("Couldn't load project specific settings. {}", e.getMessage(), e);
         throw new CCLInternalException("Loading project specific settings failed!");
      }
   }

   /**
    * Loads user settings from a XML-file.
    *
    * @throws CCLInternalException
    */
   private void getUserSettings() throws CCLInternalException
   {
      try
      {
         StringBuilder sbSettingsPath = new StringBuilder();
         sbSettingsPath.append(System.getProperty("user.home"));
         sbSettingsPath.append(File.separator);
         sbSettingsPath.append(SCLSettings.get("USER_SETTINGS_PATH"));
         sbSettingsPath.append(SCLSettings.get("USER_SETTINGS_FILE"));

         sUserSettingsPath = sbSettingsPath.toString();

         LOGGER.trace("Path to user settings file: {}", sUserSettingsPath);

         File userSettingsFile = new File(sUserSettingsPath);
         if (userSettingsFile.exists())
         {
            SCLSettings.loadUserSettings(userSettingsFile);

            // get default configuration
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            // throw away default configuration
            loggerContext.reset();

            // set user defined logger path
            loggerContext.putProperty("loggerPath", SCLSettings.get("LOGGER_PATH"));

            try
            {
               // reconfigure with new logger path
               JoranConfigurator joranConfigurator = new JoranConfigurator();
               joranConfigurator.setContext(loggerContext);
               joranConfigurator.doConfigure(getExecutableRoot() + "/logback.xml");
            }
            catch (JoranException je)
            {
               LOGGER.error(je.getMessage(), je);
            }

            StatusPrinter.printIfErrorsOccured(loggerContext);
         }
         else
         {
            LOGGER.trace("No user settings found.");
         }
      }
      catch (JDOMException | IOException | CCLInternalException | RuntimeException e)
      {
         LOGGER.error("Couldn't load user specific settings. {}", e.getMessage(), e);
         throw new CCLInternalException("Loading user specific settings failed!");
      }
   }

   /**
    * If there are any Importer plugins on the Modulepath load them and add
    * them to the list of import strategies
    */
   private static void loadPlugins()
   {
      SCLPluginLoader.getInstance();
   }

   /**
    * Loads a file defined as the startup file in the project configuration.
    * 
    * @throws CCLInternalException
    * @throws ParseException
    * @throws IOException
    * @throws JDOMException
    * @throws IllegalAccessException
    * @throws InstantiationException
    *
    */
   public Set<String> loadStartupFile() throws CCLInternalException, JDOMException, IOException, ParseException
   {
      // If there is a startup file load it!
      String startUpFileLocation = SCLSettings.get("STARTUP_FILE");
      startUpFileLocation = SCLProjectHelper.getAbsoluteFilePath(startUpFileLocation, sProjectRoot);
      File startUpFile = new File(startUpFileLocation);

      if (!startUpFile.exists())
      {
         throw new CCLInternalException("Couldn't load startup file.");
      }

      return load(startUpFile);
   }

   /**
    * Tries to open the file at the given location.
    *
    * @param filePath path to the file to open
    * @throws ParseException
    * @throws IOException
    * @throws JDOMException
    * @throws CCLInternalException
    * @throws IllegalAccessException
    * @throws InstantiationException
    */
   public static Set<String> open(String filePath) throws InstantiationException, IllegalAccessException, CCLInternalException,
         JDOMException, IOException, ParseException
   {
      File file = new File(filePath);
      if (file.exists())
      {
         SCLProject.getInstance().reset();
         return SCLProject.load(file);
      }
      else
      {
         throw new IllegalArgumentException("A file with the specified path (\"" + filePath + "\") does not exist.");
      }
   }

   /**
    * Saves all data into a XML-file
    *
    * @param filePath file path
    * @param deliveries deliveries that need to be saved
    * @throws IOException
    */
   public static void save(String filePath, Collection<CCLDelivery> deliveries) throws IOException
   {
      Document doc = new Document();
      Element project = SCLXMLUtil.createElement(SCLSettings.get(CCLXMLConstants.XML_ROOT_FORMAT), new Attribute(VERSION, CCLAssemblyInfo.getVersion()));
      doc.addContent(project);

      // add deliveries to document
      Element deliveriesXML = new Element("deliveries");
      for (CCLDelivery delivery : deliveries)
      {
         String strCreated = SCLProjectHelper.getDateFormatter().format(delivery.getCreated());

         deliveriesXML.addContent(SCLXMLUtil.createElement(DELIVERY, new Attribute(PARAMETER_NAME, delivery.getName()), new Attribute("integrator", delivery.getIntegrator()), new Attribute("created", strCreated)));
      }
      
      project.addContent(deliveriesXML);

      // add components to document
      Element components = new Element(CCLXMLConstants.XML_COMPONENTS);
      saveComponentTree(getComponentRoot(), components, deliveries);
      project.addContent(components);

      // save document
      SCLXMLUtil.saveDocument(filePath, doc);

      sOpenFileName = filePath;
      sNeedsSaving = false;
   }

   /**
    * Traverse the component tree recursively and save its information as an XMl-Element.
    *
    * @param rootComponent component to traverse over in the current recursion
    * @param xmlTarget XML-Element to append information to
    * @param deliveries deliveries that need to be saved
    */
   private static void saveComponentTree(CCLComponent rootComponent, Element xmlTarget,
         Collection<CCLDelivery> deliveries)
   {
      for (CCLComponent component : rootComponent.getSubComponents())
      {
         Element componentXML = SCLXMLUtil.createElement(CCLXMLConstants.XML_COMPONENT,
               new Attribute(PARAMETER_NAME, component.getName()),
               new Attribute("relevant", Boolean.toString(component.getIsCustomerRelevant())));

         Element importersXML = new Element(IMPORTERS);

         for (ACLImportStrategy importer : SCLProject.getInstance().getImportStrategiesInViewOrder())
         {
            ACLAssignmentStrategy assignmentStrategy = component.getAssignmentStrategy(importer.getName());
            
            Element impXML = getAssignmentStrategyXML(component, importer, assignmentStrategy);
            
            for (CCLDelivery delivery : deliveries)
            {
               ACLDeliveryInformation deliveryInformation = component.getDeliveryInformation(delivery.getName() + importer.getName());

               if (null != deliveryInformation)
               {
                  boolean isNew = deliveryInformation.isNew();

                  Element deliveryInformationXML = SCLXMLUtil.createElement("deliveryInformation",
                        new Attribute(PARAMETER_NAME, delivery.getName()),
                        new Attribute("isNew", Boolean.toString(isNew)));

                  Element information = deliveryInformation.getInformation();
                  if (null != information)
                  {
                     information.detach();
                  }
                  deliveryInformationXML.addContent(deliveryInformation.getInformation());
                  impXML.addContent(deliveryInformationXML);
               }
            }

            importersXML.addContent(impXML);
         }
         
         for (String nameOfMissingPlugin : sMissingPlugins)
         {
            Element missingImporter = getImporterXMLFromOpenFile(component.getName(), nameOfMissingPlugin);
            if (null != missingImporter)
            {
               importersXML.addContent(missingImporter);
            }
         }
         
         componentXML.addContent(importersXML);

         if (component.hasSubComponents())
         {
            saveComponentTree(component, componentXML, deliveries);
         }

         xmlTarget.addContent(componentXML);
      }
   }

   /**
    * Collects the information about the AssignmentStrategy.
    * Adds existing information from open file if an AssignmentStrategy 
    * is part of a missing plugin.
    * 
    * @param component current component to collect information on
    * @param importer importer used to collect the information
    * @param assignmentStrategy the AssignmentStrategy to be saved
    * @return
    */
   private static Element getAssignmentStrategyXML(CCLComponent component, ACLImportStrategy importer,
         ACLAssignmentStrategy assignmentStrategy)
   {
      Element impXML = SCLXMLUtil.createElement(CCLXMLConstants.XML_IMPORTER, new Attribute(PARAMETER_NAME, importer.getName()),
            new Attribute(VERSION, importer.getVersion()));
      
      Element paramXML = null;
      
      if (null != assignmentStrategy)
      {
         Element assignXML = SCLXMLUtil.createElement(CCLXMLConstants.XML_ASSIGNER,
               new Attribute(PARAMETER_NAME, assignmentStrategy.getName()));

         impXML.addContent(assignXML);

         paramXML = SCLXMLUtil.createElement(PARAMETERS,
               new Attribute("number", Integer.toString(assignmentStrategy.getNrOfParameters())));

         List<CCLParameter> parameters = component.getParameters(importer.getName());

         for (int i = 0; i < assignmentStrategy.getNrOfParameters(); i++)
         {
            String paramValue = parameters.get(i).getValue();
            paramXML.addContent(SCLXMLUtil.createElement("parameter", paramValue));
         }
      }
      else
      {
         Element existingImporterXML = getImporterXMLFromOpenFile(component.getName(), importer.getName());
         
         Element assignXML = existingImporterXML.getChild(CCLXMLConstants.XML_ASSIGNER);
         paramXML = existingImporterXML.getChild(PARAMETERS);
         
         assignXML.detach();
         paramXML.detach();
         
         impXML.addContent(assignXML);
      }

      if (null != paramXML)
      {
         impXML.addContent(paramXML);
      }
      
      return impXML;
   }
   
   /**
    * Takes the existing XML-Element from the open file which matches the given name.
    * This element is used in the event of a missing plugin while saving information.
    * Otherwise data that could not be loaded at startup would be overwritten while saving. 
    * 
    * @param componentName
    * @param importerName
    * @return
    */
   private static Element getImporterXMLFromOpenFile(String componentName, String importerName)
   {
      Document existingDoc = null;
      
      try
      {
         existingDoc = SCLXMLUtil.loadDocument(sOpenFileName);
      }
      catch (JDOMException | IOException e)
      {
         LOGGER.debug("File \"" + sOpenFileName + "\" could not be loaded while saving" , e);
      }
      
      Element existingXMLData = existingDoc.getRootElement();
      Element allComponents = existingXMLData.getChild(CCLXMLConstants.XML_COMPONENTS);
      
      return getImporterXML(componentName, importerName, allComponents);
   }

   /**
    * Gets the XML-Element matching the passed componentName and importerName from a XML-Element 
    * containing multiple components.
    * 
    * @param componentName name of the component to look for
    * @param importerName name of the importer to look for
    * @param allComponents XML-Element containing a component tree
    * @return
    */
   private static Element getImporterXML(String componentName, String importerName, Element allComponents)
   {
      Element missingXML = null;
      for (Element component : allComponents.getDescendants(new ElementFilter(CCLXMLConstants.XML_COMPONENT)))
      {
         if (component.getAttributeValue("name").equals(componentName))
         {
            // get all importers
            Element importersXML = component.getChild(IMPORTERS);
            for (Element impData : importersXML.getDescendants(new ElementFilter(CCLXMLConstants.XML_IMPORTER)))
            {
               if (impData.getAttributeValue("name").equals(importerName))
               {
                  missingXML = impData.clone();
                  missingXML.detach();
                  break;
               }
            }
         }
         
         if (null == missingXML)
         {
            missingXML = getImporterXML(componentName, importerName, component);
         }
      }
      return missingXML;
   }

   /**
    * Load component tree from xml file
    *
    * @param source source file
    * @return Set of names of missing plugins
    * @throws CCLInternalException
    * @throws IOException
    * @throws JDOMException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws ParseException
    */
   public static Set<String> load(File source) throws CCLInternalException, JDOMException, IOException, ParseException
   {
      Document doc = SCLXMLUtil.loadDocument(source);
      Element xmlData = doc.getRootElement();

      checkXMLFormat(source, xmlData);

      // In case the loading process fails, the existing deliveries have to be restored.
      TreeSet<CCLDelivery> oldDeliveries = new TreeSet<>(SCLProject.getInstance().getDeliveries());

      try
      {
         // load all deliveries from the document
         Element delXML = xmlData.getChild("deliveries");
         for (Element delivery : delXML.getChildren(DELIVERY))
         {
            CCLDelivery d = new CCLDelivery();

            d.setName(delivery.getAttributeValue(PARAMETER_NAME));
            d.setIntegrator(delivery.getAttributeValue("integrator"));
            d.setCreated(SCLProjectHelper.getDateFormatter().parse(delivery.getAttributeValue("created")));

            if (!SCLProject.getInstance().getDeliveries().contains(d))
            {
               SCLProject.getInstance().getDeliveries().add(d);
            }
            else
            {
               throw new CCLInternalException("There's already a delivery named \"" + d.getName() + 
                     "\". Therefore the import was canceled to prevent a loss of information.");
            }
         }

         // load all components from the document
         Element compRoot = xmlData.getChild(CCLXMLConstants.XML_COMPONENTS);

         sOpenFileName = source.getAbsolutePath();
         sNeedsSaving = false;
         
         return loadComponentTree(getComponentRoot(), compRoot);
      }
      catch (CCLInternalException | ParseException | RuntimeException e)
      {
         // Remove all deliveries that were added before the import failed.
         // To avoid java.util.ConcurrentModificationException an iterator is
         // used. removeDelivery() does not remove the delivery from mDeliveries. 
         // It is done inside the while loop.
         Iterator<CCLDelivery> deliveryIterator = SCLProject.getInstance().getDeliveries().iterator();
         while (deliveryIterator.hasNext())
         {
            CCLDelivery delivery = deliveryIterator.next();
            if (!oldDeliveries.contains(delivery))
            {
               removeDelivery(sComponentRoot, delivery, false);
               deliveryIterator.remove();
            }
         }

         sNeedsSaving = false;

         // re-throw exception
         throw e;
      }
   }

   /**
    * Checks if the file that has to be opened matches the format of this application.
    * 
    * @param source file to be checked
    * @param xmlData XML-Element containing the format information
    * @throws CCLInternalException
    */
   private static void checkXMLFormat(File source, Element xmlData) throws CCLInternalException
   {
      boolean xmlFormatOrLegacy = xmlData.getName().equals(SCLSettings.get(CCLXMLConstants.XML_ROOT_FORMAT)) || SCLSettings.get("LEGACY").equalsIgnoreCase("true");
      if (!xmlFormatOrLegacy)
      {
         throw new CCLInternalException("Wrong XML format in " + source.getAbsolutePath() + " !");
      }
   }

   /**
    * Recursively loads all components from a XML-Element and stores it in a component tree.
    *
    * @param parentComponent component to append information to
    * @param element XML-Element containing the component information
    * @throws CCLInternalException
    * @throws IllegalAccessException
    * @throws InstantiationException
    */
   private static Set<String> loadComponentTree(CCLComponent parentComponent, Element element)
         throws CCLInternalException
   {
      for (Element childElement : element.getChildren(CCLXMLConstants.XML_COMPONENT))
      {
         boolean isNewComponent = false;

         String componentName = childElement.getAttributeValue(PARAMETER_NAME);

         CCLComponent component = SCLProjectHelper.getComponentByName(parentComponent, componentName);
         if (null == component)
         {
            isNewComponent = true;

            component = SCLProject.getInstance().getInitialComponent();
            component.setName(componentName);
            component.setParent(parentComponent);
            component.setIsCustomerRelevant(Boolean.parseBoolean(childElement.getAttributeValue("relevant")));
         }

         // get all importers
         Element importersXML = childElement.getChild(IMPORTERS);
         for (Element impData : importersXML.getDescendants(new ElementFilter(CCLXMLConstants.XML_IMPORTER)))
         {
            // get importer name & version
            String impName = impData.getAttributeValue(PARAMETER_NAME);
            String impVersion = impData.getAttributeValue(VERSION);

            // get importer by it's name
            ACLImportStrategy importer = SCLPluginLoader.getInstance().getImportStrategiesMap().get(impName);
            
            try 
            {
               checkImporter(impName, impVersion, importer);
            }
            catch (CCLInternalException e)
            {
               LOGGER.info(e.getMessage(), e);
               sMissingPlugins.add(impName);
               continue;
            }

            // get assignment strategy and add it to the component's assignment
            // strategies
            String assigner = impData.getChild(CCLXMLConstants.XML_ASSIGNER).getAttributeValue(PARAMETER_NAME);
            ACLAssignmentStrategy assignmentStrategy = importer.getAssignmentStrategy(assigner);
            component.setAssignmentStrategy(importer.getName(), assignmentStrategy);
            
            // get all parameters for the selected strategy
            getParametersOfImportStrategy(component, impData, importer);

            // get information about all deliveries
            getDeliveryInformation(component, impData, importer);
         }

         if (null != component && null != parentComponent)
         {
            if (isNewComponent)
            {
               component.setParent(parentComponent);
               parentComponent.getSubComponents().add(component);
            }

            // recursion! repeat for all components
            sMissingPlugins.addAll(loadComponentTree(component, childElement));
         }
      }
      return sMissingPlugins;
   }

   /**
    * Checks if an importer exists and throws an CCLExternalException if it does
    * not exist or if {@link #checkVersions(String, String)} does return false.
    * 
    * @param impName Name of the importer to be checked
    * @param impVersion Version of the importer to be checked
    * @param importer Importer to be checked
    * @throws CCLInternalException
    */
   private static void checkImporter(String impName, String impVersion, ACLImportStrategy importer)
         throws CCLInternalException
   {
      if (null == importer)
      {
         throw new CCLInternalException("No such importer found: " + impName);
      }
      else if (!checkVersions(importer.getVersion(), impVersion))
      {
         throw new CCLInternalException("The version of the importer plugin \"" + importer.getName() + 
               "\" is too old to open this document. In order to open it update the plugin to version " + 
               impVersion + 
               " or higher. Current version is: " + 
               importer.getVersion());
      }
   }

   /**
    * Gets all parameters that are set in the passed XML-Element impData.
    * 
    * @param component The component containing the parameters
    * @param impData The XML-Element containing the parameters
    * @param importer The importer of which the parameters are needed
    */
   private static void getParametersOfImportStrategy(CCLComponent component, Element impData,
         ACLImportStrategy importer)
   {
      int i = 0;
      Element parametersXML = impData.getChild(PARAMETERS);
      for (Element paramXML : parametersXML.getDescendants(new ElementFilter("parameter")))
      {
         List<CCLParameter> parameters = component.getParameters(importer.getName());

         parameters.get(i).setValue(paramXML.getText());
         i++;
      }
   }

   /**
    * Gets all the information of a delivery for a specific ImportStrategy and
    * adds it to the component.
    * 
    * @param component The component to add the information to
    * @param impData The XML-Element containing the delivery information
    * @param importer The importer used for importing the delivery information
    * @throws CCLInternalException
    */
   private static void getDeliveryInformation(CCLComponent component, Element impData, ACLImportStrategy importer)
         throws CCLInternalException
   {
      for (Element d : impData.getDescendants(new ElementFilter("deliveryInformation")))
      {
         // get delivery name
         String deliveryName = d.getAttributeValue(PARAMETER_NAME);

         // get delivery by its name
         CCLDelivery delivery = SCLProject.getInstance().getDeliveryByName(deliveryName);
         if (null == delivery)
         {
            continue;
         }

         // get delivery information for this delivery
         ACLDeliveryInformation deliveryInformation = component.getDeliveryInformation(delivery.getName() + importer.getName());
         
         // if there's not already a delivery information for this
         // delivery...
         if (null == deliveryInformation)
         {
            // ... create a new instance of the correspondent delivery
            // information class for the given importer
            deliveryInformation = SCLPluginLoader.getInstance().getDeliveryInformation(importer.getDeliveryInformationName());
            
            // ... and add it to the component's list
            component.setDeliveryInformation(delivery.getName() + importer.getName(), deliveryInformation);
         }

         deliveryInformation.setNew(Boolean.parseBoolean(d.getAttributeValue("isNew")));
         deliveryInformation.setInformation(d.getChild("content"));
      }
   }

   /**
    * Returns a delivery of a given name.
    *
    * @param name name of the delivery
    * @return delivery or null if it does not exist
    */
   public CCLDelivery getDeliveryByName(String name)
   {
      for (CCLDelivery delivery : mDeliveries)
      {
         if (delivery.getName().equals(name))
         {
            return delivery;
         }
      }

      return null;
   }

   /**
    * Returns a component of a given ID
    *
    * @param component parent component to start search with
    * @param id id of the component
    * @return component or null if it does not exist
    */
   public CCLComponent getComponentById(CCLComponent component, UUID id)
   {
      // visitor definition
      class VisitorGetComponent extends ACLTreeVisitor<CCLComponent, UUID>
      {
         @Override
         public CCLComponent doIt(CCLComponent component, UUID id)
         {
            if (component.getId().equals(id))
            {
               return component;
            }

            return null;
         }
      }
      // end of visitor definition

      return component.accept(new VisitorGetComponent(), id, true);
   }

   /**
    * Checks if a delivery with the given name already exists.
    *
    * @param deliveryName
    * @return true if a delivery with this name already exists
    */
   public boolean checkDeliveryExists(String deliveryName)
   {
      for (CCLDelivery del : mDeliveries)
      {
         if (deliveryName.equals(del.getName()))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Compares the current version with the needed version. This method is
    * private and can therefore not be easily tested. If changes need to be
    * made, CCLDeliveryVersionsTest from the commit "Added test for
    * SCLProject.checkVersions() used for development" can be used.
    *
    * @param currentVersion Version that should be greater to yield "true"
    * @param neededVersion Version that should be smaller to yield "true"
    * @return true if the current version is greater than or equal to the needed
    * version, false otherwise
    */
   private static boolean checkVersions(String currentVersion, String neededVersion)
   {
      if (!SCLSettings.get("LEGACY").equalsIgnoreCase("true"))
      {
         // Convert to String arrays by splitting on "." which needs to be escaped
         String[] currentVersionSplit = currentVersion.split("\\.");
         String[] neededVersionSplit = neededVersion.split("\\.");

         // Get the length of the longer version number
         int maxLength = Math.max(currentVersionSplit.length, neededVersionSplit.length);

         // Check all items
         for (int i = 0; i < maxLength; i++)
         {
            Integer current = i < currentVersionSplit.length ? Integer.parseInt(currentVersionSplit[i]) : 0;
            Integer needed = i < neededVersionSplit.length ? Integer.parseInt(neededVersionSplit[i]) : 0;

            Integer res = current.compareTo(needed);

            if (res > 0)
            {
               // Current version is greater
               return true;
            }
            else if (res < 0)
            {
               // Current version is smaller
               return false;
            }
         }
      }

      // Current version is equal or "LEGACY" is true
      return true;
   }

   /**
    * Access to the detailed information about a delivery of a component.
    *
    * @param component component containing the information
    * @param delivery delivery containing the information
    * @param importer importer used to get the information
    * @return detailed information about a delivery of a component
    */
   public static ACLDeliveryInformation getDeliveryInformation(CCLComponent component, CCLDelivery delivery,
         ACLImportStrategy importer)
   {
      ACLDeliveryInformation deliveryInformation = null;

      ACLAssignmentStrategy strat = component.getAssignmentStrategy(importer.getName());
      
      if (null != strat)
      {
         try
         {
            deliveryInformation = SCLPluginLoader.getInstance().getDeliveryInformation(importer.getDeliveryInformationName());
            
            List<CCLParameter> parameters = component.getParameters(importer.getName());

            deliveryInformation.setInformation(strat.getData(parameters, component, delivery,
                  getFormerDelivery(delivery), importer, getProjectRoot()));

            if (!SCLProject.getInstance().getDeliveries().isEmpty() && !deliveryInformation.isNew())
            {
               SCLProjectHelper.markDeliveryInformationIfNew(SCLProject.getInstance().getDeliveries(), component,
                     delivery, importer.getName(), deliveryInformation);
            }
         }
         catch (RuntimeException e)
         {
            String errorMessage = "Could not create instance of ACLDeliveryInformation for importer \"" + 
                                  importer.getName() + "\": " + e.getMessage();
            LOGGER.error(errorMessage, e);
         }
      }

      return deliveryInformation;
   }

   /**
    * Docbook export! Most information is received from the components. They
    * know exactly which kind of information they have and how to export it.
    *
    * @param filePath file path
    * @param deliveries deliveries to export
    * @param forCustomer only export information that is relevant for a
    * customer or also internal information
    * @throws IOException
    */
   public static void exportDocbook(String filePath, Collection<CCLDelivery> deliveries, final Boolean forCustomer)
         throws IOException
   {
      Document doc = new Document();
      doc.setDocType(new DocType("article", "-//OASIS//DTD DocBook XML V4.5//EN",
            "http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd"));

      Element article = new Element("article");
      doc.addContent(article);

      int numDeliveries = deliveries.size();

      CCLDelivery[] deliveriesArray = new CCLDelivery[numDeliveries];
      deliveriesArray = deliveries.toArray(deliveriesArray);

      final CCLDelivery newestDelivery = deliveriesArray[0];
      final CCLDelivery oldestDelivery;
      if (1 < numDeliveries)
      {
         oldestDelivery = deliveriesArray[numDeliveries - 1];
      }
      else
      {
         oldestDelivery = null;
      }

      String strCreated = SCLProjectHelper.getDateFormatter().format(newestDelivery.getCreated());

      article.addContent(SCLXMLUtil.createElement("title", newestDelivery.getName()));
      article.addContent(SCLXMLUtil.createElement("subtitle",
            "Build date: " + strCreated + ", Integrator: " + newestDelivery.getIntegrator()));

      // every section in document represents information provided by an
      // importer
      Map<String, Boolean> enabledMap = SCLProject.getInstance().getEnabledStatesInOrderList("EXPORT_ORDER");
      for (final ACLImportStrategy importer : SCLProject.getInstance().getImportStrategiesInExportOrder())
      {
         // check if the data of this importer should be exported
         Boolean enabled = enabledMap.get(importer.getName().replace(' ', '_').toUpperCase());
         if (enabled != null && !enabled.booleanValue())
         {
            continue;
         }

         final Element section = importer.getDocbookSectionTemplate(oldestDelivery, newestDelivery);

         createDocbookSection(deliveries, forCustomer, deliveriesArray, newestDelivery,
               oldestDelivery, importer, section);

         // add to article
         article.addContent(section);
      }
      
      // save docbook
      SCLXMLUtil.saveDocument(filePath, doc);
   }

   /**
    * Creates the Docbook section for the passed delivery and importer.
    * The information is stored in the section parameter.
    * 
    * @param deliveries All deliveries to export
    * @param forCustomer Whether or not the export is for a customer
    * @param deliveriesArray All deliveries in an array
    * @param newestDelivery
    * @param oldestDelivery
    * @param importer
    * @param section XML-Element to store the delivery information in
    */
   private static void createDocbookSection(Collection<CCLDelivery> deliveries, final Boolean forCustomer,
         CCLDelivery[] deliveriesArray, final CCLDelivery newestDelivery, final CCLDelivery oldestDelivery,
         final ACLImportStrategy importer, final Element section)
   {
      boolean isSectionEmpty = true;
      // do we need the information from all selected deliveries?
      if (importer.needsAllSelectedDeliveriesForDocbookExport())
      {
         // exclude oldest delivery
         if (null != oldestDelivery)
         {
            deliveries.remove(oldestDelivery);
         }

         // check if there is any information to be put in the current
         // section
         for (int i = 0; i < deliveries.size() && isSectionEmpty; i++)
         {
            isSectionEmpty = emptySection(forCustomer, importer, deliveriesArray[i]);
         }

         if (!isSectionEmpty)
         {
            for (CCLDelivery delivery : deliveries)
            {
               fillSection(forCustomer, section, importer, oldestDelivery, delivery);
            }
         }
      }
      else
      {
         isSectionEmpty = emptySection(forCustomer, importer, newestDelivery);

         if (!isSectionEmpty)
         {
            // we only need the information from the newest (and maybe the
            // oldest) delivery
            fillSection(forCustomer, section, importer, oldestDelivery, newestDelivery);
         }
      }

      if (isSectionEmpty)
      {
         section.addContent(importer.getDocbookSectionEmptyMessage());
      }
   }
   
   /**
    * Fills the Docbook section of a provided importer with delivery information.
    * 
    * @param forCustomer Is the export for a customer
    * @param section The XML-Element to be filled
    * @param importer importer containing the delivery information
    * @param oldestDelivery oldest delivery for comparison purposes
    * @param deliveryToDocument delivery to be documented
    */
   private static void fillSection(Boolean forCustomer, Element section, ACLImportStrategy importer, CCLDelivery oldestDelivery, CCLDelivery deliveryToDocument)
   {
      // visitor definition
      class VisitorFillSection extends ACLTreeVisitor<Boolean, CCLDelivery>
      {
         @Override
         public Boolean doIt(CCLComponent component, CCLDelivery delivery)
         {
            if (!component.getDeliveryInformation().isEmpty())
            {
               // don't export any information which customer shouldn't
               // see
               if (null != forCustomer && forCustomer.booleanValue() && !component.getIsCustomerRelevant())
               {
                  return false;
               }

               ACLDeliveryInformation info = component.getDeliveryInformation(delivery.getName() + importer.getName());

               // we pass an additional delivery (para oldestDelivery)
               // in case we have to compare e.g. Version info
               info.addDocbookSection(section, component, oldestDelivery, forCustomer);
            }

            return true;
         }
      }
      // end of visitor definition
      
      getComponentRoot().accept(new VisitorFillSection(), deliveryToDocument);
   }
   
   /**
    * Fills empty Docbook section if the importer does not provide any information.
    * 
    * @param forCustomer
    * @param importer
    * @param deliveryToDocument
    * @return Whether or not the section needs to be empty
    */
   private static Boolean emptySection(Boolean forCustomer, ACLImportStrategy importer, CCLDelivery deliveryToDocument)
   {
      // visitor definition
      class VisitorEmptySection extends ACLTreeVisitor<Boolean, CCLDelivery>
      {
         @Override
         public Boolean doIt(CCLComponent component, CCLDelivery delivery)
         {
            // ignore non customer relevant information in check
            // true means it is empty
            if (null != forCustomer && forCustomer.booleanValue() && !component.getIsCustomerRelevant())
            {
               return true;
            }

            // check if this components delivery information is empty
            ACLDeliveryInformation info = component.getDeliveryInformation(delivery.getName() + importer.getName());
            return info.isInfoNullOrEmpty();
         }

         @Override
         public Boolean visit(CCLComponent component, CCLDelivery target, boolean quickReturn)
         {
            boolean res = true;

            List<CCLComponent> subComponentList = component.getSubComponents();

            for (int i = 0; i < component.getSubComponents().size() && res; i++)
            {
               CCLComponent subComponent = subComponentList.get(i);

               res = doIt(subComponent, target);

               // cancel search on first hit
               if (res && subComponent.hasSubComponents())
               {
                  res = subComponent.accept(this, target, true);
               }
            }
            return res;
         }
      }
      // end of visitor definition
      
      return getComponentRoot().accept(new VisitorEmptySection(), deliveryToDocument, true);
   }

   /**
    * Export a delivery as an XML-file.
    *
    * @param target file path
    * @param delivery delivery to export
    * @throws IOException
    */
   public void exportDelivery(String target, CCLDelivery delivery) throws IOException
   {
      Set<CCLDelivery> deliveries = new TreeSet<>();
      deliveries.add(delivery);
      save(target, deliveries);
   }

   /**
    * Add a new delivery entry to every component of the tree.
    *
    * @param component parent component
    * @param delivery new delivery
    * @return success
    */
   public static Boolean addDeliveries(CCLComponent component, CCLDelivery delivery)
   {
      SCLProject.getInstance().setCreationReport(new Element("report"));

      // visitor definition
      class VisitorAddDelivery extends ACLTreeVisitor<Boolean, CCLDelivery>
      {
         @Override
         public Boolean doIt(CCLComponent component, CCLDelivery delivery)
         {
            for (ACLImportStrategy importer : SCLPluginLoader.getInstance().getImportStrategiesMap().values())
            {
               try
               {
                  if (!sTestMode)
                  {
                     ACLDeliveryInformation info = getDeliveryInformation(component, delivery, importer);

                     if (!component.getDeliveryInformation().containsKey(delivery.getName() + importer.getName()) && null != info)
                     {
                        for (Element element : info.getInformation().getDescendants(new ElementFilter("error")))
                        {
                           element.setAttribute(DELIVERY, delivery.getName());
                           element.setAttribute(CCLXMLConstants.XML_COMPONENT, component.getName());
                           element.setAttribute(CCLXMLConstants.XML_IMPORTER, importer.getName());
                           element.setAttribute(CCLXMLConstants.XML_ASSIGNER,
                                 component.getAssignmentStrategy(importer.getName()).getName());
                           element.detach();
                           SCLProject.getInstance().mCreationReport.addContent(element);
                        }

                        component.setDeliveryInformation(delivery.getName() + importer.getName(), info);
                     }
                  }
               }
               catch (RuntimeException e)
               {
                  // CleanUp
                  LOGGER.error(importer.getName() + ":" + e.getMessage(), e);

                  SCLProject.getInstance().mDeliveries.remove(delivery);
                  removeDelivery(getComponentRoot(), delivery);

                  return false;
               }
            }

            return true;
         }
      }
      // end of visitor definition

      Boolean res = component.accept(new VisitorAddDelivery(), delivery);

      sNeedsSaving = true;
      return res;
   }

   /**
    * Remove a delivery.
    *
    * @param component parent component
    * @param delivery delivery to remove
    * @param removeFromDeliveries Should the delivery be removed from {@link #mDeliveries} as well or not
    * @return success
    */
   public static Boolean removeDelivery(CCLComponent component, CCLDelivery delivery, boolean removeFromDeliveries)
   {
      // visitor definition
      class VisitorRemove extends ACLTreeVisitor<Boolean, CCLDelivery>
      {
         @Override
         public Boolean doIt(CCLComponent component, CCLDelivery delivery)
         {
            for (ACLImportStrategy importer : SCLPluginLoader.getInstance().getImportStrategiesMap().values())
            {
               component.getDeliveryInformation().remove(delivery.getName() + importer.getName());
            }

            return true;
         }
      }
      // end of visitor definition

      Boolean res = component.accept(new VisitorRemove(), delivery);

      if (removeFromDeliveries)
      {
         SCLProject.getInstance().mDeliveries.remove(delivery);
      }

      sNeedsSaving = true;
      return res;
   }

   public static Boolean removeDelivery(CCLComponent component, CCLDelivery delivery)
   {
      return removeDelivery(component, delivery, true);
   }

   /**
    * Resets project. Removes all components and deliveries. Results in a new
    * empty project.
    */
   public void reset()
   {
      mDeliveries.clear();
      mCreationReport = null;

      getComponentRootData().clear();
      
      // A method which causes an update of the GUI needs to be called
      getComponentRoot().setSubComponents(new ArrayList<CCLComponent>());
      CCLComponent.resetCounter();
      sNeedsSaving = false;
      sOpenFileName = "";
   }

   public static CCLComponent getComponentRoot()
   {
      // late creation of component root object
      // double checking pattern
      if (sComponentRoot == null)
      {
         // lock it to make it thread save
         synchronized (sLockObject)
         {
            if (sComponentRoot == null)
            {
               sComponentRoot = SCLProject.getInstance().getInitialComponent();
            }
         }
      }
      return sComponentRoot;
   }

   public static void setComponentRoot(CCLComponent componentRoot)
   {
      if (sComponentRoot == null)
      {
         synchronized (sLockObject)
         {
            if (sComponentRoot == null)
            {
               sComponentRoot = SCLProject.getInstance().getInitialComponent();
            }
         }
      }

      sComponentRoot = componentRoot;
      sNeedsSaving = true;
   }

   public static Collection<CCLComponent> getComponentRootData()
   {
      return getComponentRoot().getSubComponents();
   }

   /**
    * Creates and returns an initial component.
    * 
    * @return an empty component containing the initial information
    */
   public CCLComponent getInitialComponent()
   {
      CCLComponent initialComponent = new CCLComponent();
      for (ACLImportStrategy importer : SCLPluginLoader.getInstance().getImportStrategiesMap().values())
      {
         List<ACLAssignmentStrategy> assignmentStrategies = new ArrayList<>(importer.getAssignmentStrategies());

         // get default assignment strategy and max number of parameters
         ACLAssignmentStrategy defaultStrategy = assignmentStrategies.get(0);
         int maxParameters = 0;

         for (ACLAssignmentStrategy strat : assignmentStrategies)
         {
            // get assignment strategy "Ignore"
            if (strat.getName().equals("Ignore"))
            {
               defaultStrategy = strat;
            }

            // get max number of parameters
            if (strat.getNrOfParameters() > maxParameters)
            {
               maxParameters = strat.getNrOfParameters();
            }
         }

         // set importer's default strategy to "Ignore" if available
         // if it's not available use the first assignment strategy in the list
         // as default
         initialComponent.getAssignmentStrategies().put(importer.getName(), defaultStrategy);

         // init parameters
         initialComponent.getParameters().put(importer.getName(), new ArrayList<CCLParameter>());

         for (int i = 0; i < maxParameters; i++)
         {
            initialComponent.getParameters().get(importer.getName()).add(new CCLParameter());
         }

         // set delivery information for this importer for every delivery
         for (CCLDelivery delivery : SCLProject.getInstance().getDeliveries())
         {
            if (!initialComponent.getDeliveryInformation().containsKey(delivery.getName() + importer.getName()))
            {
               ACLDeliveryInformation info = SCLProject.getDeliveryInformation(initialComponent, delivery, importer);
               initialComponent.getDeliveryInformation().put(delivery.getName() + importer.getName(), info);
            }
         }
      }
      return initialComponent;
   }

   /**
    * Returns the path to the root folder of this application.
    *
    * @return path to root folder of this application
    */
   public static String getExecutableRoot()
   {
      if (null == sExecutableRoot)
      {
         // set executable root to the path to SCLProject
         sExecutableRoot = SCLProject.class.getProtectionDomain().getCodeSource().getLocation().getPath();

         // if the project is started from a JAR-File, the jar itself is part of
         // the Path and must be removed.
         if (sExecutableRoot.endsWith(".jar"))
         {
            sExecutableRoot = new File((sExecutableRoot)).getParentFile().getParent();
         }
         // we still remain in the "bin/<platform>" folder and have to select
         // the
         // corresponding parents to get the real root-folder
         sExecutableRoot = new File(sExecutableRoot).getParent();
      }

      if (!sExecutableRoot.endsWith("/") || !sExecutableRoot.endsWith("\\"))
      {
         // then append a separator at the end of the path
         sExecutableRoot = sExecutableRoot + File.separator;
      }
      return sExecutableRoot;
   }

   public static void setExecutableRoot(String executableRoot)
   {
      sExecutableRoot = executableRoot;
   }

   public static void setComponentRootData(List<CCLComponent> componentRootData)
   {
      getComponentRoot().setSubComponents(componentRootData);
   }

   public static boolean getNeedsSaving()
   {
      return sNeedsSaving;
   }

   public static void setNeedsSaving(boolean needsSaving)
   {
      sNeedsSaving = needsSaving;
   }

   public static boolean getTestMode()
   {
      return sTestMode;
   }

   public static void setTestMode(boolean isTest)
   {
      sTestMode = isTest;
   }

   public static String getOpenFileName()
   {
      return sOpenFileName;
   }

   public static void setOpenFileName(String openFileName)
   {
      sOpenFileName = openFileName;
   }

   public static String getProjectRoot()
   {
      return sProjectRoot;
   }

   public static void setProjectRoot(String projectRoot)
   {
      sProjectRoot = projectRoot;
   }

   public static String getConfigRoot()
   {
      return sConfigRoot;
   }

   public static void setConfigRoot(String configRoot)
   {
      sConfigRoot = configRoot;
   }

   public Element getCreationReport()
   {
      return mCreationReport;
   }

   public void setCreationReport(Element creationReport)
   {
      mCreationReport = creationReport;
   }

   public CCLObservableCollection<CCLDelivery> getDeliveries()
   {
      return mDeliveries;
   }

   public void setDeliveries(CCLObservableCollection<CCLDelivery> deliveries)
   {
      mDeliveries = deliveries;
   }

   public List<ACLImportStrategy> getImportStrategiesInViewOrder()
   {
      return getImportStrategiesInOrder("VIEW_ORDER");
   }

   public List<ACLImportStrategy> getImportStrategiesInExportOrder()
   {
      return getImportStrategiesInOrder("EXPORT_ORDER");
   }

   public List<ACLImportStrategy> getImportStrategiesInOrder(String orderSettings)
   {
      List<String> exportOrder;
      try
      {
         exportOrder = getPluginOrderFromSettingsTuple(orderSettings);
      }
      catch (RuntimeException e) // for compatibility with old configuration files
      {
         LOGGER.trace("Old settings found! Try to load these!", e);
         exportOrder = SCLSettings.getStringList(orderSettings, EnumSet.of(ECLSettingsType.PROJECT));
      }

      List<ACLImportStrategy> importStrategies = new ArrayList<ACLImportStrategy>(SCLPluginLoader.getInstance().getImportStrategiesMap().values());

      int maxStrategies = Math.max(exportOrder.size(), importStrategies.size());

      ACLImportStrategy[] orderedStrategies = new ACLImportStrategy[maxStrategies];
      List<ACLImportStrategy> unknownStrategies = new ArrayList<>();

      for (ACLImportStrategy strat : importStrategies)
      {
         String importerName = strat.getImporterName().replace(' ', '_').toUpperCase();

         if (exportOrder.contains(importerName))
         {
            int i = exportOrder.indexOf(importerName);
            orderedStrategies[i] = strat;
         }
         else
         {
            unknownStrategies.add(strat);
         }
      }

      List<ACLImportStrategy> result = new ArrayList<>(Arrays.asList(orderedStrategies));
      result.removeAll(Collections.singleton(null));
      result.addAll(unknownStrategies);
      return result;
   }

   public Map<String, Boolean> getEnabledStatesInOrderList(String orderSettings)
   {
      Map<String, Boolean> pluginEnabledValues = new HashMap<>();
      try
      {
         List<CCLTuple<String, Boolean>> order = SCLSettings.getTupleList(orderSettings,
               EnumSet.of(ECLSettingsType.PROJECT));
         for (CCLTuple<String, Boolean> plugin : order)
         {
            pluginEnabledValues.put(plugin.getFirst(), plugin.getSecond());
         }
      }
      catch (RuntimeException e) // for compatibility with old configuration files
      {
         LOGGER.trace("Old settings document! Try to create enable map with old setting names!", e);
         for (final ACLImportStrategy importer : SCLProject.getInstance().getImportStrategiesInExportOrder())
         {
            String pluginName = importer.getName().replace(' ', '_').toUpperCase();
            // check if the data of this importer should be exported
            String enabledStr = SCLSettings.get(pluginName + "_ENABLED");
            Boolean enabled = null != enabledStr ? Boolean.valueOf(enabledStr) : null;
            pluginEnabledValues.put(pluginName, enabled);
         }
      }
      return pluginEnabledValues;
   }

   /**
    * Gets a {@link List} of the names of the loaded plugins ordered by the configuration.
    * @param orderSettings
    * @return
    */
   private List<String> getPluginOrderFromSettingsTuple(String orderSettings)
   {
      List<CCLTuple<String, Boolean>> order = SCLSettings.getTupleList(orderSettings,
            EnumSet.of(ECLSettingsType.PROJECT));
      List<String> orderOfPluginNames = new ArrayList<>();
      for (CCLTuple<String, Boolean> plugin : order)
      {
         orderOfPluginNames.add(plugin.getFirst());
      }
      return orderOfPluginNames;
   }

   /**
    * Get former delivery, but ignore current delivery.
    * 
    * @return former delivery object or null, if nothing has been found.
    */
   public static CCLDelivery getFormerDelivery(CCLDelivery currentDelivery)
   {
      CCLDelivery target = null;

      Iterator<CCLDelivery> it = SCLProject.getInstance().getDeliveries().iterator();
      if (it != null)
      {
         while (it.hasNext())
         {
            CCLDelivery temp = it.next();

            // find the first delivery, that is not the current delivery
            if (target == null)
            {
               if (temp.getCreated().compareTo(currentDelivery.getCreated()) != 0)
               {
                  target = temp;
               }
            }
            else
            {
               // look for the youngest delivery
               if (0 < temp.getCreated().compareTo(target.getCreated()))
               {
                  target = temp;
               }
            }
         }
      }
      return target;
   }

   public static String getProjectSettingsPath()
   {
      return sProjectSettingsPath;
   }

   public static void setProjectSettingsPath(String projectSettingsPath)
   {
      sProjectSettingsPath = projectSettingsPath;
   }

   public static String getUserSettingsPath()
   {
      return sUserSettingsPath;
   }

   public static void setUserSettingsPath(String userSettingsPath)
   {
      sUserSettingsPath = userSettingsPath;
   }
}
