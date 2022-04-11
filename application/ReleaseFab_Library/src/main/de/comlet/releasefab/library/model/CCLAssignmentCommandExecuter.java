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
 * @file CCLAssignmentCommandExecuter.java
 *
 * @brief Command Executor assignment strategy.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom2.Element;

/**
 * Implementation of an assignment strategy to assign a value delivered by an
 * external program.
 */
public class CCLAssignmentCommandExecuter extends ACLAssignmentStrategy
{
   private static final String NAME = "Command Executer";
   
   /**
    * Number of parameters for command executor
    */
   private static final int NROFPARAMETERS = 4;
   private static final int FIRSTPARAMETER = 0;
   private static final int SECONDPARAMETER = 1;
   private static final int THIRDPARAMETER = 2;
   private static final int FOURTHPARAMETER = 3;

   private static final String USAGE_MESSAGE = "Command Executer:\n " + 
         "Job: Assigns a value delivered by an external program.\n" + 
         "Parameter 1: Name of the program (execution call /incl. path)\n" + 
         "Parameter 2: Parameters for the program \n" + 
         "Parameter 3: Regular Expression \n" + 
         "Parameter 4: Output format, for example ${1} ${2}";

   private static final String ELEMENT_ERROR = "error";

   /**
    * Timeout in milliseconds [ms] for executor
    */
   private static final int EXECUTOR_TIMEOUT = 10000;

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
    * Assigns a value delivered by an external program. parameters[0] Name of
    * the program. parameters[1] Parameters for the program. parameters[2] Regular
    * Expression.
    */
   @Override
   public Element getData(List<CCLParameter> parameters, CCLComponent component, CCLDelivery delivery,
         CCLDelivery formerDelivery, ACLImportStrategy importer, String projectRoot, CCLComponent initialComponent)
   {
      String command = parameters.get(FIRSTPARAMETER).getValue();

      String errorHeader = component + ":" + importer + ":" + getName() + ":";

      Element desc = new Element("content");
      if (command.equals(""))
      {
         LOGGER.error("{} Missing Parameter 1", errorHeader);
         desc.addContent(SCLXMLUtil.createElement(ELEMENT_ERROR, "Missing Parameter 1"));
         return desc;
      }

      String arguments = parameters.get(SECONDPARAMETER).getValue();
      String regex = parameters.get(THIRDPARAMETER).getValue();
      String format = parameters.get(FOURTHPARAMETER).getValue();

      try
      {
         String commandForExecutor = "\"" + command + "\" " + arguments;
         CCLCommandExecuter executer = new CCLCommandExecuter(commandForExecutor);
         int returnValue = executer.execute(EXECUTOR_TIMEOUT);

         String res = executer.getOutputString();

         if (0 == returnValue)
         {
            // Some programs return the output on stderr output, for example
            // java.exe
            if (res.equals(""))
            {
               res = executer.getErrorString();
            }
         }
         else
         {
            LOGGER.warn("Command executor for command \"{}\" failed with error message: \n{}###\nend of error message",
                  commandForExecutor, executer.getErrorString());
            desc.addContent(SCLXMLUtil.createElement(ELEMENT_ERROR, executer.getErrorString()));
            return desc;
         }

         // This part is for filtering data with a regular expression
         if (!regex.equals(""))
         {
            res = filterWithRegex(res, regex, format);
         }

         desc.addContent(SCLXMLUtil.createElement("string", res));

         return desc;
      }
      catch (RuntimeException e)
      {
         LOGGER.error("{} {}", errorHeader, e.getMessage(), e);
         desc.addContent(SCLXMLUtil.createElement(ELEMENT_ERROR, e.getMessage()));
         return desc;
      }
   }

   public String filterWithRegex(String text, String regex, String format)
   {
      String result = format;
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(text);

      if (matcher.find())
      {
         // replace placeholders in the format string with matches
         for (int i = 1; i <= matcher.groupCount(); i++)
         {
            String match = matcher.group(i);
            result = result.replace("${" + i + "}", match);
         }
         
         // if there are no groups or format defined, use the first match
         if (0 == matcher.groupCount() || format.isEmpty())
         {
            result = matcher.group(0);
         }
      }

      return result;
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
