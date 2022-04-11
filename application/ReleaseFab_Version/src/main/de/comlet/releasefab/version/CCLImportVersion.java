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
 * @file CCLImportVersion.java
 *
 * @brief Version importer.
 */

package de.comlet.releasefab.version;

import de.comlet.releasefab.api.plugin.ACLDetailedInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.model.CCLAssignmentCommandExecuter;
import de.comlet.releasefab.library.model.CCLAssignmentConstText;
import de.comlet.releasefab.library.model.CCLAssignmentFileParser;
import de.comlet.releasefab.library.model.CCLAssignmentRandom;
import de.comlet.releasefab.library.model.CCLAssignmentSubtree;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.io.FileInputStream;
import org.jdom2.Attribute;
import org.jdom2.Element;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_COLNUM;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_COLS;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_COLWIDTH;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_CONFORMANCE;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_PGWIDE;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ATTRIBUTE_TABSTYLE;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_COLSPEC;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ENTRY;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_PARA;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_ROW;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_SECTION;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_TABLE;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_TBODY;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_TGROUP;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_THEAD;
import static de.comlet.releasefab.library.xml.CCLXMLConstants.XML_TITLE;

/**
 * Importer for Version information. Main class of plugin.
 */
public class CCLImportVersion extends ACLImportStrategy
{
   protected static final String NAME = "Version";
   private static final String VERSION = "1.0.0";
   private static final String LICENSE = "Eclipse Public License - v 2.0";
   private static final String LICENSE_SOURCE = "https://www.eclipse.org/legal/epl-2.0/";

   private static final String COLWIDTH_3000 = "3000*";
   private static final String COLWIDTH_6000 = "6000*";
   private static final String CONFORMANCE_DIRECTSTART = "directstart";
   private static final String TABSTYLE_SMALLFONT = "smallfont";
   private static final String PARA_COMPONENT = "Component";
   private static final String VALUE_1 = "1";
   private static final String VALUE_2 = "2";
   private static final String VALUE_3 = "3";

   /**
    * Initialize available assignment strategies.
    */
   public CCLImportVersion()
   {
      super(NAME, VERSION, LICENSE, LICENSE_SOURCE, CCLDeliveryVersion.class, false, PresentationType.LABEL);

      mAssignmentStrategies.add(new CCLAssignmentRandom());
      mAssignmentStrategies.add(new CCLAssignmentConstText());
      mAssignmentStrategies.add(new CCLAssignmentCommandExecuter());
      mAssignmentStrategies.add(new CCLAssignmentFileParser());
      mAssignmentStrategies.add(new CCLAssignmentSubtree());
   }

   /**
    * Create a template for the Docbook section as a XML-Element.
    */
   @Override
   public Element getDocbookSectionTemplate(CCLDelivery from, CCLDelivery to)
   {
      Element section = null;
      if (from != null)
      {
         section = SCLXMLUtil.createElement(XML_SECTION, SCLXMLUtil.createElement(XML_TITLE, getName()),
               SCLXMLUtil.createElement(XML_TABLE, new Attribute(XML_ATTRIBUTE_CONFORMANCE, CONFORMANCE_DIRECTSTART),
                     new Attribute(XML_ATTRIBUTE_PGWIDE, VALUE_1),
                     new Attribute(XML_ATTRIBUTE_TABSTYLE, TABSTYLE_SMALLFONT), new Element(XML_TITLE),

                     SCLXMLUtil.createElement(XML_TGROUP, new Attribute(XML_ATTRIBUTE_COLS, VALUE_3),
                           SCLXMLUtil.createElement(XML_COLSPEC, new Attribute(XML_ATTRIBUTE_COLNUM, VALUE_1),
                                 new Attribute(XML_ATTRIBUTE_COLWIDTH, COLWIDTH_6000)),
                           SCLXMLUtil.createElement(XML_COLSPEC, new Attribute(XML_ATTRIBUTE_COLNUM, VALUE_2),
                                 new Attribute(XML_ATTRIBUTE_COLWIDTH, COLWIDTH_3000)),
                           SCLXMLUtil.createElement(XML_COLSPEC, new Attribute(XML_ATTRIBUTE_COLNUM, VALUE_3),
                                 new Attribute(XML_ATTRIBUTE_COLWIDTH, COLWIDTH_3000)),
                           SCLXMLUtil.createElement(XML_THEAD, SCLXMLUtil.createElement(XML_ROW,
                                 SCLXMLUtil.createElement(XML_ENTRY,
                                       SCLXMLUtil.createElement(XML_PARA, PARA_COMPONENT)),
                                 SCLXMLUtil.createElement(XML_ENTRY, SCLXMLUtil.createElement(XML_PARA, to.getName())),
                                 SCLXMLUtil.createElement(XML_ENTRY,
                                       SCLXMLUtil.createElement(XML_PARA, from.getName())))),
                           new Element(XML_TBODY))));
      }
      // just two columns, because there is nothing to compare
      else
      {
         section = SCLXMLUtil.createElement(XML_SECTION, SCLXMLUtil.createElement(XML_TITLE, getName()),
               SCLXMLUtil.createElement(XML_TABLE, new Attribute(XML_ATTRIBUTE_CONFORMANCE, CONFORMANCE_DIRECTSTART),
                     new Attribute(XML_ATTRIBUTE_PGWIDE, VALUE_1),
                     new Attribute(XML_ATTRIBUTE_TABSTYLE, TABSTYLE_SMALLFONT), new Element(XML_TITLE),

                     SCLXMLUtil.createElement(XML_TGROUP, new Attribute(XML_ATTRIBUTE_COLS, VALUE_2),
                           SCLXMLUtil.createElement(XML_COLSPEC, new Attribute(XML_ATTRIBUTE_COLNUM, VALUE_1),
                                 new Attribute(XML_ATTRIBUTE_COLWIDTH, COLWIDTH_6000)),
                           SCLXMLUtil.createElement(XML_COLSPEC, new Attribute(XML_ATTRIBUTE_COLNUM, VALUE_2),
                                 new Attribute(XML_ATTRIBUTE_COLWIDTH, COLWIDTH_3000)),
                           SCLXMLUtil.createElement(XML_THEAD,
                                 SCLXMLUtil.createElement(XML_ROW,
                                       SCLXMLUtil.createElement(XML_ENTRY,
                                             SCLXMLUtil.createElement(XML_PARA, PARA_COMPONENT)),
                                       SCLXMLUtil.createElement(XML_ENTRY,
                                             SCLXMLUtil.createElement(XML_PARA, to.getName())))),
                           new Element(XML_TBODY))));
      }

      return section;
   }

   /**
    * Return detailed information to be shown in the UI.
    */
   @Override
   public ACLDetailedInformation getDetailedInformation(CCLComponent component, CCLDelivery delivery)
   {
      ACLDetailedInformation detailedInfo = mDetailedInformation.get(component.getName() + delivery.getName());
      if (detailedInfo != null)
      {
         return detailedInfo;
      }

      detailedInfo = new CCLDetailedVersion(getName(), component, delivery);
      mDetailedInformation.put(component.getName() + delivery.getName(), detailedInfo);

      return detailedInfo;
   }

   @Override
   public String getDeliveryInformationName()
   {
      return new CCLDeliveryVersion().getName();
   }

   @Override
   public FileInputStream getImporterImage()
   {
      return null;
   }
}
