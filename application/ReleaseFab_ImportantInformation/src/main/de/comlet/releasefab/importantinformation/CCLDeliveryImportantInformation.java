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
 * @file CCLDeliveryImportantInformation.java
 *
 * @brief Delivery of Important Information.
 */

package de.comlet.releasefab.importantinformation;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_LITERALLAYOUT;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_SECTION;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_STRING;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_TITLE;

public class CCLDeliveryImportantInformation extends ACLDeliveryInformation
{
   private static final String NAME = "Delivery Important Information";
      
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
      String target = mInformation.getChildText(XML_STRING) + "\n" + other.getChildText(XML_STRING);

      mInformation.getChild(XML_STRING).setContent(new Text(target));

      return true;
   }

   /**
    * Provide Docbook output in the passed XML-Element 'element'.
    * 
    * @param element XML-Element to store information in
    * @param component Component to be documented
    * @param other Delivery which is not used to document Important Information
    * @param forCustomer Defines whether the export is for a customer
    */
   @Override
   public boolean addDocbookSection(Element element, CCLComponent component, CCLDelivery other, boolean forCustomer)
   {
      String str = mInformation.getChildText(XML_STRING);
      if (null != str && !str.isEmpty() && !str.equals("-"))
      {
         Element section = SCLXMLUtil.createElement(XML_SECTION, SCLXMLUtil.createElement(XML_TITLE, component.getFullName()));

         // if the string is in XML format...
         if (str.matches("(?s)(.*?)<(\\S+?)(.*?)>(.*?)</\\2>(.*?)"))
         {
            try
            {
               // ... parse the XML string ...
               SAXBuilder builder = new SAXBuilder();
               Reader in = new StringReader("<content>" + str + "</content>");
               Document doc = builder.build(in);

               List<Content> elements = new ArrayList<>(doc.getRootElement().getContent());
               for (Content content : elements)
               {
                  // ... and append the elements contained in that string
                  section.addContent(content.detach());
               }
            }
            catch (JDOMException | IOException e)
            {
               LOGGER.info("{} {}", getName(), e.getMessage(), e);
               section.addContent(SCLXMLUtil.createElement(XML_LITERALLAYOUT, str));
            }
         }
         else
         {
            // if the string is not in XML format, wrap the whole string in a
            // new paragraph and append that
            section.addContent(SCLXMLUtil.createElement(XML_LITERALLAYOUT, str));
         }

         // append the new section to the given element
         element.addContent(section);
      }

      return true;
   }
}
