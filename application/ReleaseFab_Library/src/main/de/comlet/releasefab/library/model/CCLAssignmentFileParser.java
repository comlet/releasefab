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
 * @file CCLAssignmentFileParser.java
 *
 * @brief File parser assignment strategy.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom2.Element;

/**
 * Implements an assignment strategy to assign text extracted out of a file.
 */
public class CCLAssignmentFileParser extends ACLAssignmentStrategy
{
   private static final int NROFPARAMETERS = 3;
   private static final int FIRSTPARAMETER = 0;
   private static final int SECONDPARAMETER = 1;
   private static final int THIRDPARAMETER = 2;
   private static final String NAME = "File Parser";
   private static final String USAGE_MESSAGE = NAME + ":\n " +
                                               "Job: Extracts value out of a file.\n" +
                                               "Parameter 1: Name of file (incl. path)\n" +
                                               "Parameter 2: Regular Expression according to Java 1.4.2 (http://docs.oracle.com/javase/1.4.2/docs/api/java/util/regex/Pattern.html)\n" +
                                               "Parameter 3: Output format, for example ${1} ${2}";
   
   private static final String ERROR = "error";

   @Override
   public String getName()
   {
      return NAME;
   }

   @Override
   public int getNrOfParameters()
   {
      return NROFPARAMETERS;
   }

   @Override
   public String getUsageInfo()
   {
      return USAGE_MESSAGE;
   }


   /**
    * Checks validity of given filepath parameter. <br>
    * parameters[0] Name of file (incl. path)
    */
   private String validateFilePath(String filename, String projectRoot)
   {
      String error = null;

      if (filename.equals(""))
      {
         error = "Missing Parameter 1";
      }
      else
      {
         String filepath = SCLProjectHelper.getAbsoluteFilePath(filename, projectRoot);
         File file = new File(filepath);

         if (!file.exists())
         {
            error = "File \"" + filename + "\" does not exist!";
         }
      }

      return error;
   }

   /**
    * Extracts value from a file. <br>
    * parameters[0] Name of file (including path) <br>
    * parameters[1] Regular Expression
    * parameters[2] Output format
    */
   @Override
   public Element getData(List<CCLParameter> aParameters, CCLComponent aComponent, CCLDelivery aDelivery, CCLDelivery formerDelivery,
         ACLImportStrategy aImporter, String projectRoot, CCLComponent initialComponent)
   {
      String filename = aParameters.get(FIRSTPARAMETER).getValue().trim();
      String regex = aParameters.get(SECONDPARAMETER).getValue().trim();
      String format = aParameters.get(THIRDPARAMETER).getValue().trim();

      String errorHeader = aComponent + ":" + aImporter + ":" + getName() + ":";

      Element desc = new Element("content");
      String error = validateFilePath(filename, projectRoot);
      if (null != error)
      {
         LOGGER.error("{} {}", errorHeader, error);
         desc.addContent(SCLXMLUtil.createElement(ERROR, error));
      }
      else
      {
         filename = SCLProjectHelper.getAbsoluteFilePath(filename, projectRoot);
         File file = new File(filename);
         String res = format;

         try (Scanner scanner = new Scanner(file, "ISO-8859-1"))
         {
            // get file contents
            StringBuilder stringBuilder = new StringBuilder();

            while (scanner.hasNext())
            {
               stringBuilder.append(scanner.nextLine());
            }

            String str = stringBuilder.toString();

            // try to find matches with the given regex pattern
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);

            if (matcher.find())
            {
               // replace placeholders in the format string with matches
               for (int i = 1; i <= matcher.groupCount(); i++)
               {
                  String match = matcher.group(i);
                  res = res.replace("${" + i + "}", match);
               }
               
               // if there are no groups or format defined, use the first match
               if (0 == matcher.groupCount() || format.isEmpty())
               {
                  res = matcher.group(0);
               }
            }

            desc.addContent(SCLXMLUtil.createElement("string", res));
         }
         catch (FileNotFoundException | RuntimeException e)
         {
            LOGGER.error("{} {}", errorHeader, e.getMessage(), e);
            desc.addContent(SCLXMLUtil.createElement(ERROR, "Fileparser error!"));
         }
      }
      return desc;
   }
   
   /**
    * Overload of method
    * {@link #getData(List, CCLComponent, CCLDelivery, CCLDelivery, ACLImportStrategy, String, CCLComponent)}
    * without an initial component.
    */
   @Override public Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot)
   {
      return getData(parameters, component, delivery, formerDelivery, importer, projectRoot, null);
   }
}
