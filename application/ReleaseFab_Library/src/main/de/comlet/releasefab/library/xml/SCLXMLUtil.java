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
 * @file SCLXMLUtil.java
 *
 * @brief XML utility.
 */

package de.comlet.releasefab.library.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

/**
 * Utility class for handling XML-Files in a convenient way.
 */
public final class SCLXMLUtil
{
   private SCLXMLUtil()
   {
   }

   /**
    * Loads the XML document with the given file name.
    *
    * @param fileName
    * @return
    * @throws IOException
    * @throws JDOMException
    */
   public static Document loadDocument(String fileName) throws JDOMException, IOException
   {
      return loadDocument(new File(fileName));
   }

   /**
    * Loads the given XML-Document.
    *
    * @param file
    * @return
    * @throws IOException
    * @throws JDOMException
    */
   public static Document loadDocument(File file) throws JDOMException, IOException
   {
      SAXBuilder sax = new SAXBuilder();

      // remove indentation
      sax.setIgnoringElementContentWhitespace(true);
      sax.setIgnoringBoundaryWhitespace(true);

      return sax.build(file);
   }

   /**
    * Saves the given XML document to the specified file. Creates the file and
    * its parent directories if they do not already exist.
    *
    * @param fileName
    * @param doc
    * @throws IOException
    */
   public static void saveDocument(String fileName, Document doc) throws IOException
   {
      saveDocument(new File(fileName), doc);
   }

   /**
    * Saves the given XML-Document to the specified file. Creates the file and
    * its parent directories if they do not already exist.
    *
    * @param file
    * @param doc
    * @throws IOException
    */
   public static void saveDocument(File file, Document doc) throws IOException
   {
      // if the given file does not exist
      if (!file.exists())
      {
         // check if the parent directories of this file exist
         String directory = file.getAbsolutePath();
         directory = directory.substring(0, directory.lastIndexOf(File.separator));
         File dir = new File(directory);

         if (!dir.exists())
         {
            // create them if they don't already exist
            dir.mkdirs();
         }
      }

      // save the XML document
      XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat().setLineSeparator(LineSeparator.NL));
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      xout.output(doc, fileOutputStream);
      fileOutputStream.close();
   }

   /**
    * Creates a new XML-Element.
    *
    * @param name name of the new XML element
    * @param objects Strings will be added to the new XML element as child
    * elements.<br>
    * XML elements will be added to the new XML element as child elements.<br>
    * Attributes of this XML element
    * @return
    */
   public static Element createElement(String name, Object... objects)
   {
      Element element = new Element(name);

      for (Object obj : objects)
      {
         if (null != obj)
         {
            if (obj instanceof String)
            {
               element.addContent((String) obj);
            }
            else if (obj instanceof Content)
            {
               element.addContent((Content) obj);
            }
            else if (obj instanceof Attribute)
            {
               element.setAttribute((Attribute) obj);
            }
         }
      }

      return element;
   }

   /**
    * Creates a new XML-Element from the given name and content. Adds the newly
    * created element to the passed parent XML-Element.
    */
   public static void addElement(Element parent, String name, String content)
   {
      Element element = new Element(name);
      element.addContent(content);
      parent.addContent(element);
   }
}
