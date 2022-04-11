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
 * @file CCLDeliveryVersion.java
 *
 * @brief Version delivery information.
 */

package de.comlet.releasefab.version;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import org.jdom2.Attribute;
import org.jdom2.Element;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_ROLE;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_EMPHASIS;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ENTRY;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_PARA;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ROW;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_STRING;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_TABLE;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_TBODY;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_TGROUP;

/**
 * Provides Docbook sections for Version information in XML-Format.
 */
public class CCLDeliveryVersion extends ACLDeliveryInformation
{
   private static final String NAME = "Delivery Version";
   
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
      return true;
   }

   /**
    * Provide Docbook output in the passed XML-Element 'element'.
    * 
    * @param element XML-Element to store information in
    * @param component Component to be documented
    * @param other Delivery which is not used to document ALM tracker items
    * @param forCustomer Defines whether the export is for a customer
    */
   @Override
   public boolean addDocbookSection(Element element, CCLComponent component, CCLDelivery other, boolean forCustomer)
   {
      if (null == element || null == mInformation)
      {
         return false;
      }

      Element row = new Element(XML_ROW);
      row.addContent(SCLXMLUtil.createElement(XML_ENTRY, SCLXMLUtil.createElement(XML_PARA, component.getFullName())));

      Element toCompare = null;

      if (null != other && component.getDeliveryInformation().containsKey(other.getName() + CCLImportVersion.NAME))
      {
         toCompare = component.getDeliveryInformation(other.getName() +  CCLImportVersion.NAME).getInformation();
      }

      // if there is another delivery
      if (null != toCompare)
      {
         // are there any changes?
         if (!mInformation.getChildText(XML_STRING).equals(toCompare.getChildText(XML_STRING)))
         {
            // highlight changes
            row.addContent(SCLXMLUtil.createElement(XML_ENTRY,
                  SCLXMLUtil.createElement(XML_EMPHASIS, new Attribute(XML_ATTRIBUTE_ROLE, "bold"),
                        SCLXMLUtil.createElement(XML_PARA, mInformation.getChildText(XML_STRING)))));
         }
         else
         {
            // no changes, don't highlight this row
            row.addContent(SCLXMLUtil.createElement(XML_ENTRY,
                  SCLXMLUtil.createElement(XML_PARA, mInformation.getChildText(XML_STRING))));
         }

         // ... and add a new column for that delivery
         row.addContent(SCLXMLUtil.createElement(XML_ENTRY,
               SCLXMLUtil.createElement(XML_PARA, toCompare.getChildText(XML_STRING))));
      }
      else
      {
         // there's only one delivery to show
         row.addContent(SCLXMLUtil.createElement(XML_ENTRY,
               SCLXMLUtil.createElement(XML_PARA, mInformation.getChildText(XML_STRING))));
      }

      element.getChild(XML_TABLE).getChild(XML_TGROUP).getChild(XML_TBODY).addContent(row);

      return true;
   }

   /**
    * Compares this delivery information with the given delivery information.
    *
    * @param other delivery information to compare with this information
    * @return false if the information in the given delivery information object
    * is different from this object's information or if the given information
    * does not exist, true if they are the same.
    */
   @Override
   protected boolean compareInfo(ACLDeliveryInformation other)
   {
      // We don't need to compare this information if the other delivery or its
      // information doesn't even exist. So check that first.
      if (null != other && !other.isInfoNullOrEmpty())
      {
         String str = mInformation.getValue();
         String otherStr = other.getInformation().getValue();

         // is that information the same?
         if (str.equals(otherStr))
         {
            return true;
         }
      }

      // this information is different from the other delivery's
      // information
      return false;
   }
}
