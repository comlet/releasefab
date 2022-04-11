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
 * @file ACLDocBookSinkBase.java
 *
 * @brief Abstract class of basic Docbook sink.
 */

package de.comlet.releasefab.library.xml;

import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Base class for generating XML in Docbook-Format.
 */
public abstract class ACLDocBookSinkBase
{
   private CCLColumnSpec[] mColumnSpecs;
   private Element mTbody;
   private Element mElement;
   private String mComponentName;

   /**
    * Constructor.
    */
   public ACLDocBookSinkBase(Element element, String componentName)
   {
      this.mElement = element;
      this.mComponentName = componentName;
   }

   /**
    * Set column specifications and initialize data.
    */
   protected void init(CCLColumnSpec[] columnSpecs)
   {
      this.mColumnSpecs = columnSpecs.clone();
      init(this.mElement, this.mComponentName);
   }

   /**
    * Initialize table, section, ...
    */
   private void init(Element element, String componentName)
   {
      Element section = null;
      Element tbody = null;
      Element table = null;

      // Check if there is already a section and tbody for tasks of this
      // component
      List<Element> sections = element.getChildren(CCLXMLConstants.XML_SECTION);
      for (Element s : sections)
      {
         if (!componentName.equals(s.getChildText(CCLXMLConstants.XML_TITLE)))
         {
            continue;
         }

         // Use the already existing section
         section = s;
         table = section.getChild(CCLXMLConstants.XML_TABLE);
         if (null != table)
         {
            Element tgroup = table.getChild(CCLXMLConstants.XML_TGROUP);
            if (null != tgroup)
            {
               tbody = tgroup.getChild(CCLXMLConstants.XML_TBODY);
               break;
            }
         }
      }

      // Create title if no section exists
      if (null == section)
      {
         section = new Element(CCLXMLConstants.XML_SECTION);
         section.addContent(SCLXMLUtil.createElement(CCLXMLConstants.XML_TITLE, componentName));

         // Add section to main element
         element.addContent(section);
      }

      // Create table header, add later if task-list is non-empty
      if (null == table)
      {
         table = createTable();
      }

      if (null == tbody)
      {
         // Create a new XML-Element
         tbody = new Element(CCLXMLConstants.XML_TBODY);

         // adding new tbody element to section
         Element tgroup = table.getChild(CCLXMLConstants.XML_TGROUP);
         if (null != tgroup)
         {
            tgroup.addContent(tbody);
         }
      }

      this.mTbody = tbody;

      if (null == table.getParent())
      {
         section.addContent(table);
      }
   }

   /**
    * Add table rows to output.
    */
   public void addItems(Iterable<Element> cursor)
   {
      for (Element entry : cursor)
      {
         this.mTbody.addContent(entry);
      }
   }

   /**
    * Create a table using data from the array containing the column
    * specifications.
    */
   protected Element createTable()
   {
      Element table = new Element(CCLXMLConstants.XML_TABLE);
      table.setAttribute(new Attribute(CCLXMLConstants.XML_ATTRIBUTE_CONFORMANCE, "directstart"));
      table.setAttribute(new Attribute(CCLXMLConstants.XML_ATTRIBUTE_PGWIDE, "1"));
      table.setAttribute(new Attribute(CCLXMLConstants.XML_ATTRIBUTE_TABSTYLE, "smallfont"));

      table.setContent(new Element(CCLXMLConstants.XML_TITLE));

      Element group = new Element(CCLXMLConstants.XML_TGROUP);

      group.setAttribute(new Attribute(CCLXMLConstants.XML_ATTRIBUTE_COLS, String.valueOf(mColumnSpecs.length))); // no
                                                                                                                  // of
                                                                                                                  // columns

      // Set column widths
      for (int i = 0; i < mColumnSpecs.length; ++i)
      {
         Element colspec = new Element(CCLXMLConstants.XML_COLSPEC);
         colspec.setAttribute(new Attribute(CCLXMLConstants.XML_ATTRIBUTE_COLNUM, String.valueOf(i + 1)));
         colspec.setAttribute(
               new Attribute(CCLXMLConstants.XML_ATTRIBUTE_COLWIDTH, String.valueOf(mColumnSpecs[i].getWidth()) + "*"));

         group.addContent(colspec);
      }

      table.addContent(group);

      Element thead = new Element(CCLXMLConstants.XML_THEAD);
      Element row = new Element(CCLXMLConstants.XML_ROW);
      thead.addContent(row);

      // Set column titles
      for (int k = 0; k < mColumnSpecs.length; ++k)
      {
         Element entry = new Element(CCLXMLConstants.XML_ENTRY);
         Element para = new Element(CCLXMLConstants.XML_PARA);
         para.setText(mColumnSpecs[k].getName());
         entry.addContent(para);
         row.addContent(entry);
      }

      group.addContent(thead);

      return table;
   }

   /**
    * Provide XML-Output as String for debugging purposes.
    */
   @Override
   public String toString()
   {
      XMLOutputter temp = new XMLOutputter(Format.getPrettyFormat());
      return temp.outputString(this.mElement);
   }
}
