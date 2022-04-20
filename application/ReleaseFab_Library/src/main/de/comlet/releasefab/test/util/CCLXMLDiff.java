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
 * @file CCLXMLDiff.java
 *
 * @brief Assert that two XML-Elements are equal.
 */

package de.comlet.releasefab.test.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import javax.xml.transform.Source;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DOMDifferenceEngine;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEngine;
import org.xmlunit.diff.ElementSelectors;

/**
 * Provides special Assertions for XML-Documents.
 * 
 * @author JaFernau
 *
 */
public final class CCLXMLDiff
{
   /** Initialize logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLXMLDiff.class);

   /**
    * Error String in case a difference was found.
    */
   private static final String FOUND_A_DIFFERENCE = "Found a difference: ";

   /**
    * Empty, private default constructor to restrict the instantiation of this
    * helper class.
    */
   private CCLXMLDiff()
   {
   }

   /**
    * Asserts that two XML-Documents are identical. Gets both elements from a
    * file with the same name but in two different locations.
    * 
    * @param testInDirectory The directory where the control file is
    * @param testOutDirectory The directory in which the produced output file is
    * @param filename The filename which must be identical between both files
    */
   public static void assertXMLEquals(String testInDirectory, String testOutDirectory, String filename)
   {
      Source control = Input.fromFile(testInDirectory + filename).build();
      Source test = Input.fromFile(testOutDirectory + filename).build();
      DifferenceEngine diff = new DOMDifferenceEngine();
      diff.addDifferenceListener(new ComparisonListener()
      {
         @Override
         public void comparisonPerformed(Comparison comparison, ComparisonResult outcome)
         {
            Assert.fail(FOUND_A_DIFFERENCE + comparison);
         }
      });
      diff.compare(control, test);
   }

   /**
    * Asserts that two XML-Documents are identical. Gets the control element
    * from a file and the element under test directly.
    * 
    * @param testInDirectory The directory where the control file is
    * @param elementUnderTest The produced element to be tested
    * @param filename The filename of the control file
    */
   public static void assertXMLEquals(String testInDirectory, Element elementUnderTest, String filename)
   {
      XMLOutputter outputter = new XMLOutputter();
      Source control = Input.fromFile(testInDirectory + filename).build();
      Source test = Input.from(outputter.outputString(elementUnderTest)).build();
      DifferenceEngine diff = new DOMDifferenceEngine();
      diff.addDifferenceListener(new ComparisonListener()
      {
         @Override
         public void comparisonPerformed(Comparison comparison, ComparisonResult outcome)
         {
            Assert.fail(FOUND_A_DIFFERENCE + comparison);
         }
      });
      diff.compare(control, test);
   }

   /**
    * Asserts that two XML-Documents are identical. Gets the control element
    * from a file and the document under test directly.
    * 
    * @param testInDirectory The directory where the control file is
    * @param documentUnderTest The produced document to be tested
    * @param filename The filename of the control file
    */
   public static void assertXMLEquals(String testInDirectory, Document documentUnderTest, String filename)
   {
      XMLOutputter outputter = new XMLOutputter();
      Source control = Input.fromFile(testInDirectory + filename).build();
      Source test = Input.from(outputter.outputString(documentUnderTest)).build();
      DifferenceEngine diff = new DOMDifferenceEngine();
      diff.addDifferenceListener(new ComparisonListener()
      {
         @Override
         public void comparisonPerformed(Comparison comparison, ComparisonResult outcome)
         {
            Assert.fail(FOUND_A_DIFFERENCE + comparison);
         }
      });
      diff.compare(control, test);
   }

   /**
    * Asserts that two XML-Documents are identical. Both elements are passed as
    * JDOM 2 Elements.
    * 
    * @param testInDirectory The directory where the control file is
    * @param elementUnderTest The produced element to be tested
    * @param filename The filename of the control file
    */
   public static void assertXMLEquals(Element element, Element otherElement)
   {
      XMLOutputter outputter = new XMLOutputter();
      Source control = Input.from(outputter.outputString(element)).build();
      Source test = Input.from(outputter.outputString(otherElement)).build();
      DifferenceEngine diff = new DOMDifferenceEngine();
      diff.addDifferenceListener(new ComparisonListener()
      {
         @Override
         public void comparisonPerformed(Comparison comparison, ComparisonResult outcome)
         {
            Assert.fail(FOUND_A_DIFFERENCE + comparison);
         }
      });
      diff.compare(control, test);
   }
   
   /**
    * Asserts that two XML-Documents are identical up to the specified line. Both elements 
    * are passed as file paths.
    * 
    * @param testInDirectory The directory where the control file is
    * @param elementUnderTest The produced element to be tested
    * @param filename The filename of the control file
    * @param line The line up to which the testOut file is compared
    * @param toAppend The XML lines to append in order to make the XML valid
    */
   public static void assertXMLEquals(String testInDirectory, String testOutDirectory, String filename, int line, String toAppend)
   {
      StringBuilder builder = new StringBuilder();
      try (LineNumberReader lr = new LineNumberReader(new FileReader(new File(testOutDirectory + filename))))
      {
         String currentLine;
         while (((currentLine = lr.readLine()) != null) && lr.getLineNumber() <= line)
         {
            builder.append(currentLine);
         }
      } catch (IOException e) {
         LOGGER.error("IO Exception", e);
         Assert.fail(FOUND_A_DIFFERENCE + e.getMessage());
      }
	  
	   builder.append(toAppend);
	   String otherElementTrimmed = builder.toString();
      
	   Source control = Input.fromFile(testInDirectory + filename).build();
      Source test = Input.fromString(otherElementTrimmed).build();
      
      Diff diff = DiffBuilder.compare(control).withTest(test).normalizeWhitespace().withNodeMatcher(
    		  new DefaultNodeMatcher(ElementSelectors.byName)).checkForSimilar().build();

      if (diff.hasDifferences())
      {
         Assert.fail(FOUND_A_DIFFERENCE + diff.getDifferences().iterator().next().toString());
      }
   }
}
