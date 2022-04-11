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
 * @file CCLAssignmentSubtree.java
 *
 * @brief Subtree assignment strategy.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.settings.SCLSettings;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_NAME;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_RELEVANT;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_COMPONENT;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_COMPONENTS;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_CONTENT;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_DELIVERY_INFORMATION;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ERROR;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_IMPORTER;

/**
 * Implements an assignment strategy to assign data of a subtree.
 */
public class CCLAssignmentSubtree extends ACLAssignmentStrategy
{
   private static final String NAME = "Import Subtree";
   private static final int NUMBER_OF_PARAMETERS = 3;
   private static final String USAGE_MESSAGE = "Assignment of imported delivery information:\n " + 
         "Job: Reads components with delivery information from file.\n" + "Parameter 1: File with exported delivery\n" + 
         "Parameter 2: Name of the root node\n" +
         "Parameter 3: If this parameter is set to \"only\" only the root node is imported.";

   private static final String LOGMESSAGE_FORMAT = "{} {}";

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
    * Starts the assignment process.
    */
   @Override
   public Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot, CCLComponent initialComponent)
   {
      String filename = parameters.get(0).getValue();
      String rootNode = parameters.get(1).getValue();
      String only = parameters.get(2).getValue();

      String errorHeader = component + ":" + importer + ":" + getName() + ":";
      String error = "";

      Element desc = null;

      if (filename.isEmpty())
      {
         error = "Missing Parameter 1";
         LOGGER.error(LOGMESSAGE_FORMAT, errorHeader, error);
         desc = new Element(XML_CONTENT);
         desc.addContent(SCLXMLUtil.createElement(XML_ERROR, error));
      }
      else
      {
         filename = SCLProjectHelper.getAbsoluteFilePath(filename, projectRoot);
         File file = new File(filename);
         desc = new Element(XML_CONTENT);

         if (!file.exists())
         {
            error = "File \"" + filename + "\" does not exist!";
            LOGGER.error(LOGMESSAGE_FORMAT, errorHeader, error);
            desc.addContent(SCLXMLUtil.createElement(XML_ERROR, error));
         }
         else if (rootNode.isEmpty())
         {
            error = "Missing Parameter 2";
            LOGGER.error(LOGMESSAGE_FORMAT, errorHeader, error);
            desc.addContent(SCLXMLUtil.createElement(XML_ERROR, error));
         }
         else
         {
            tryToCreateElementFromFile(file, rootNode, component, desc, delivery, importer, errorHeader,
                  initialComponent, only);
         }
      }
      return desc;
   }

   /**
    * Reads information from a file with version inormation.
    * 
    * @param file
    * @param aRootNode
    * @param component
    * @param desc
    * @param delivery
    * @param importer
    * @param errorHeader
    * @param initialComponent
    */
   private void tryToCreateElementFromFile(File file, String aRootNode, CCLComponent component, Element desc,
         CCLDelivery delivery, ACLImportStrategy importer, String errorHeader, CCLComponent initialComponent, String only)
   {
      try
      {
         String error = "";
         String rootNode = aRootNode;

         Document doc = SCLXMLUtil.loadDocument(file);

         // check if the document has the right format
         Element xmlData = doc.getRootElement();
         boolean xmlFormatOrLegacy = xmlData.getName().equals(SCLSettings.get(CCLXMLConstants.XML_ROOT_FORMAT)) || SCLSettings.get("LEGACY").equalsIgnoreCase("true");
         if (!xmlFormatOrLegacy)
         {
            error = "Wrong XML format in " + file.getAbsolutePath() + " !";
            LOGGER.error(LOGMESSAGE_FORMAT, errorHeader, error);
            desc.addContent(SCLXMLUtil.createElement(XML_ERROR, error));
         }
         else
         {
            extractDataFromXMLDoc(rootNode, xmlData, component, desc, delivery, importer, errorHeader,
                  initialComponent, only);
         }
      }
      catch (JDOMException | IOException | CCLInternalException | RuntimeException e)
      {
         LOGGER.error(LOGMESSAGE_FORMAT, errorHeader, e.getMessage(), e);
         desc.addContent(SCLXMLUtil.createElement(XML_ERROR, e.getMessage()));
      }
   }

   private void extractDataFromXMLDoc(String aRootNode, Element xmlData, CCLComponent component, Element desc,
         CCLDelivery delivery, ACLImportStrategy importer, String errorHeader, CCLComponent initialComponent, String only)
         throws CCLInternalException
   {
      Element compRoot = xmlData.getChild(XML_COMPONENTS);
      String rootNode = aRootNode;
      boolean justRoot = false;

      // If the root node ends with a dot only the root will be imported.
      // Otherwise all of its children will also be imported.
      if (only.equalsIgnoreCase("only"))
      {
         justRoot = true;
      }

      // Get the XML-Element which name is equal to the given root node
      Element res = null;
      for (Element element : compRoot.getDescendants(new ElementFilter(XML_COMPONENT)))
      {
         if (rootNode.equals(element.getAttributeValue(XML_ATTRIBUTE_NAME)))
         {
            res = element;
            break;
         }
      }

      // Does the given XML-Document contain the given root node
      if (null == res)
      {
         String error = "Component \"" + rootNode + "\" not found!";
         LOGGER.error(LOGMESSAGE_FORMAT, errorHeader, error);
         desc.addContent(SCLXMLUtil.createElement(XML_ERROR, error));
      }

      component.setName(res.getAttributeValue(XML_ATTRIBUTE_NAME));

      // update components delivery information according to the content
      // of the given XML-Element
      updateComponentsDeliveryInformation(res, component, delivery, importer, true, desc);

      if (!justRoot)
      {
         // Repeat with child elements
         copyTree(res, component, delivery, importer, initialComponent);
      }

      // Otherwise report that the import is ready
      else
      {
         desc.addContent(SCLXMLUtil.createElement("string", "\nSubtree ready to be imported!"));
      }
   }

   /**
    * Searches for components in the source XML-Element. If that component does
    * not already exist, it will be created as a child of the target component.
    * Its information will be updated according to the content of the
    * XML-Element.
    *
    * @param source XML-Element in which this method searches for components
    * @param targetComponent parent component to add components to
    * @param delivery
    * @param importer importer to search for
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   private void copyTree(Element source, CCLComponent targetComponent, CCLDelivery delivery, ACLImportStrategy importer,
         CCLComponent initialComponent) throws CCLInternalException
   {
      CCLComponent initial = initialComponent;
      for (Element childElement : source.getChildren(XML_COMPONENT))
      {
         String componentName = childElement.getAttributeValue(XML_ATTRIBUTE_NAME);
         boolean isNewComp = false;

         // Try to load component by name or create a new one if that
         // component does not already exist
         CCLComponent component = SCLProjectHelper.getComponentByName(targetComponent, componentName);
         if (component == null)
         {
            isNewComp = true;
            component = initialComponent;

            // Fill the new component with the information gathered from the
            // given XML-Element
            component.setName(componentName);
            component.setParent(targetComponent);
            component.setIsCustomerRelevant(
                  Boolean.parseBoolean(childElement.getAttributeValue(XML_ATTRIBUTE_RELEVANT)));
         }

         // Update components delivery information according to the content
         // of the given XML-Element
         updateComponentsDeliveryInformation(childElement, component, delivery, importer, false, null);

         if (targetComponent != null)
         {
            // Add the component as a child of the target component
            component.setParent(targetComponent);

            if (isNewComp)
            {
               targetComponent.getSubComponents().add(component);
            }

            // Repeat with child elements
            copyTree(childElement, component, delivery, importer, initial);
         }
      }
   }

   /**
    * Searches in the given XML-Element for a child element whose name matches
    * that of the given importer. Creates an instance of the correspondent
    * delivery information class and fills it with the content of the
    * XML-Element. This method then adds that delivery information to the given
    * list of components.
    *
    * @param xmlElement XML-Element in which this method searches for the given
    * import strategy
    * @param component component to update
    * @param delivery delivery in which this subtree will be added
    * @param importer importer to search for
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   private void updateComponentsDeliveryInformation(Element xmlElement, CCLComponent component, CCLDelivery delivery,
         ACLImportStrategy importer, boolean updateDesc, Element desc) throws CCLInternalException
   {
      // Get the XML-Element of the given importer
      Element imp = findElementForImporter(importer, xmlElement);

      // Get the first XML-Element of delivery information of that importer
      Element deliverInfo = getFirstDeliveryInformationOfImporterElement(imp);

      if (null != deliverInfo)
      {
         if (updateDesc)
         {
            // Update of "root" component. The root component is stored in SCLProject.
            // Therefore the "desc" object is updated here and returned to SCLProject.
            // Otherwise SCLProject adds delivery information without "content".
            if (null != desc)
            {
               desc.addContent(deliverInfo.getChild(XML_CONTENT).cloneContent());
            }
         }
         else
         {
            // Create a new instance of the correspondent delivery information
            // class for the given importer
            ACLDeliveryInformation deliveryInformation = null;

            ServiceLoader<ACLDeliveryInformation> deliveryInformationLoader = ServiceLoader.load(
                  ACLDeliveryInformation.class);
            for (ACLDeliveryInformation delInfo : deliveryInformationLoader)
            {
               if (delInfo.getName().equals(importer.getDeliveryInformationName()))
               {
                  deliveryInformation = delInfo;
               }
            }

            // Put the content gathered from the XML-Element into the delivery
            // information object
            deliveryInformation.setInformation(deliverInfo.getChild(XML_CONTENT));
            
            // Add that delivery information to the component's list
            component.setDeliveryInformation(delivery.getName() + importer.getName(), deliveryInformation);
         }
      }
   }

   private Element findElementForImporter(ACLImportStrategy importer, Element xmlElement)
   {
      Element imp = null;
      for (Element element : xmlElement.getDescendants(new ElementFilter(XML_IMPORTER)))
      {
         if (importer.getName().equals(element.getAttributeValue(XML_ATTRIBUTE_NAME)))
         {
            imp = element;
            break;
         }
      }
      return imp;
   }

   private Element getFirstDeliveryInformationOfImporterElement(Element importerElement)
   {
      Element deliverInfo = null;
      if (null != importerElement)
      {
         IteratorIterable<Element> elementIt = importerElement.getDescendants(
               new ElementFilter(XML_DELIVERY_INFORMATION));
         if (elementIt.hasNext())
         {
            deliverInfo = elementIt.next();
         }
      }
      return deliverInfo;
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
