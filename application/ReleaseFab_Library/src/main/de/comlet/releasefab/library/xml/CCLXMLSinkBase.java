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
 * @file CCLXMLSinkBase.java
 *
 * @brief Basic XML sink.
 */

package de.comlet.releasefab.library.xml;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Base Class for XML sinks.
 */
public class CCLXMLSinkBase extends Element
{
   /**
    * The {@link #serialVersionUID} is used in the process of Serialization and
    * Deserialization. It allows the Java VM to distinguish between different
    * serialized objects and deserialize them into the correct object.
    */
   private static final long serialVersionUID = -2618285952601041745L;

   /**
    * Constructor.
    */
   public CCLXMLSinkBase()
   {
      super(CCLXMLConstants.XML_CONTENT);
   }

   /**
    * Add items to output.
    */
   public void addItems(Iterable<Element> cursor)
   {
      for (Element entry : cursor)
      {
         this.addContent(entry);
      }
   }

   /**
    * Add element to output (release, latest baseline, ...). Use this to add
    * some content at the beginning of the content section.
    */
   public void addElement(String name, String content)
   {
      Element element = new Element(name);
      element.addContent(content);
      this.addContent(element);
   }

   /**
    * Get XML-Output for debugging purposes.
    */
   @Override
   public String toString()
   {
      XMLOutputter temp = new XMLOutputter(Format.getPrettyFormat());
      return temp.outputString(this);
   }
}
