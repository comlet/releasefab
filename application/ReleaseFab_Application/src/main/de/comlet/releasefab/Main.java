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
 * @file Main.java
 *
 * @brief Main class of the application.
 */

package de.comlet.releasefab;

import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.settings.SCLSettings;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import de.comlet.releasefab.ui.CCLMainWindow;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static de.comlet.releasefab.ECLProgramKey.ADDDELIVERY;
import static de.comlet.releasefab.ECLProgramKey.CLI;
import static de.comlet.releasefab.ECLProgramKey.CONFIG;
import static de.comlet.releasefab.ECLProgramKey.CUSTOMERDOCBOOK;
import static de.comlet.releasefab.ECLProgramKey.DOCBOOK;
import static de.comlet.releasefab.ECLProgramKey.FROM;
import static de.comlet.releasefab.ECLProgramKey.GENERALSETTINGS;
import static de.comlet.releasefab.ECLProgramKey.PW;
import static de.comlet.releasefab.ECLProgramKey.RESULTFILE;
import static de.comlet.releasefab.ECLProgramKey.SOURCE;
import static de.comlet.releasefab.ECLProgramKey.TO;
import static de.comlet.releasefab.ECLProgramKey.USER;

/**
 * Main entrance of the application
 */
public final class Main
{
   /** Initialize logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

   private Main()
   {}

   /**
    * Main method, starts application as CLI or GUI
    *
    * @param args arguments
    */
   public static void main(String[] args)
   {
      CCLProgramArguments arguments = new CCLProgramArguments(args);

      if (arguments.isCli())
      {
         // Start as CLI
         startCommandLineInterface(arguments);
      }
      else
      {
         if (null != arguments.getSource())
         {
            // Set project root
            SCLProject.setProjectRoot(arguments.getSource());
         }

         if (null != arguments.getConfig())
         {
            // Set config root
            SCLProject.setConfigRoot(arguments.getConfig());
         }
         
         if (null != arguments.getGeneralSettings())
         {
            // Set executable root
            SCLProject.setExecutableRoot(arguments.getGeneralSettings());
         }

         // Open main window
         new CCLMainWindow();
      }
   }

   /**
    * Start command line interface.
    *
    * @param arguments command line arguments
    */
   private static void startCommandLineInterface(CCLProgramArguments arguments)
   {
      String infoText = info();
      LOGGER.info(infoText);

      // Write CCLProgramArguments to SCLSettings
      Map<String, String> settings = new HashMap<>();
      settings.put(CCLXMLConstants.XML_ALM_USER_KEY, arguments.getUserName());
      settings.put(CCLXMLConstants.XML_ALM_PASSWORD_KEY, String.valueOf(arguments.getUserPassword()));
      SCLSettings.loadSettingsFromCLI(settings);

      // load source file
      File srcFile = new File(arguments.getSource());
      if (!srcFile.exists())
      {
         LOGGER.error("Source file does not exist!");
         return;
      }

      try
      {
         SCLProject.setProjectRoot(arguments.getSource());
         SCLProject.setConfigRoot(arguments.getConfig());
         SCLProject.setExecutableRoot(arguments.getGeneralSettings());
         SCLProject.getInstance().loadStartupFile();

         if (arguments.isDelivery())
         {
            addDelivery(arguments);
         }
         else
         {
            createDocbook(arguments);
         }
      }
      catch (CCLInternalException |
             ParseException |
             IOException |
             JDOMException |
             RuntimeException e)
      {
         final String logMsg = Main.class.getName() + ": " + e;
         LOGGER.error(logMsg);
      }
   }

   /**
    * Adds a new Delivery to the current file with version information.
    *
    * @param arguments The parsed command line arguments.
    */
   private static void addDelivery(CCLProgramArguments arguments)
   {
      try
      {
         for (String deliveryName : arguments.getDeliveryNames())
         {
            // Check if a delivery with the given name already exists
            if (SCLProject.getInstance().checkDeliveryExists(deliveryName))
            {
               LOGGER.error("A delivery with the name: \"{}\" already exists.", deliveryName);
               return;
            }
            CCLDelivery delivery;

            String integratorName = System.getenv("FULL_USER_NAME");
            // Check if the build is running on jenkins
            if (null == integratorName )
            {
               delivery = new CCLDelivery(deliveryName, "created by a build server");
            }
            else
            {
               delivery = new CCLDelivery(deliveryName, integratorName);
            }
            SCLProject.getInstance().getDeliveries().add(delivery);
            SCLProject.addDeliveries(SCLProject.getComponentRoot(), delivery);
         }
         SCLProject.save(SCLProject.getOpenFileName(), SCLProject.getInstance().getDeliveries());

         LOGGER.info("Successfully created a new delivery!");
      }
      catch (IOException | RuntimeException e)
      {
         final String logMsg = Main.class.getName() + ": " + e;
         LOGGER.error(logMsg);
      }
   }

   /**
    * Creates and exports a Docbook file.
    *
    * @param arguments The parsed command line arguments.
    */
   private static void createDocbook(CCLProgramArguments arguments)
   {
      try
      {
         int count = SCLProject.getInstance().getDeliveries().size();
         LOGGER.info("Number of deliveries: {}", count);

         if (count == 0)
         {
            LOGGER.info("No deliveries available");
         }
         else
         {
            Set<CCLDelivery> deliveries = createDeliveries(arguments, count);
            SCLProject.exportDocbook(arguments.getOutputFile(), deliveries, arguments.isCustomerDocBook());

            LOGGER.info("Docbook export finished successfully!");
         }
      }
      catch (IOException | RuntimeException e)
      {
         final String logMsg = Main.class.getName() + ": " + e;
         LOGGER.error(logMsg);
      }
   }

   /**
    * Creates a {@link Set} of deliveries to be exported.
    * The {@link Set} is created according to the passed parameters.
    * 
    * @param arguments Program arguments passed as CLI parameter
    * @param count Number of deliveries to be exported
    * @return A {@link Set} of deliveries
    */
   private static Set<CCLDelivery> createDeliveries(CCLProgramArguments arguments, int count)
   {
      Set<CCLDelivery> deliveries;

      CCLDelivery[] deliveryArray = new CCLDelivery[count];
      deliveryArray = SCLProject.getInstance().getDeliveries().toArray(deliveryArray);

      // if there is no "from=" & "to=" mention in parameter list
      // only the newest two deliveries are added to export
      if (arguments.getFrom() == null)
      {
         LOGGER.info("Latest delivery: {}", deliveryArray[0].getName());

         if (count > 1)
         {
            LOGGER.info("Former delivery: {}", deliveryArray[1].getName());
            //export the two latest deliveries
            deliveries = collectDeliveries(deliveryArray, deliveryArray[1], deliveryArray[0]);
         }
         else
         {
            //export newest only
            deliveries = collectDeliveries(deliveryArray, deliveryArray[0], deliveryArray[0]);
         }
      }
      // If there is a "from=" parameter given
      else
      {
         CCLDelivery fromDelivery = SCLProject.getInstance().getDeliveryByName(arguments.getFrom());
         if (null == fromDelivery)
         {
            throw new IllegalArgumentException("The delivery: " + arguments.getFrom() + " does not exist!");
         }
         LOGGER.info("From delivery: {}", fromDelivery.getName());

         // when both ("from=" and "to=") parameters are given
         if (arguments.getTo() != null)
         {
            CCLDelivery toDelivery = SCLProject.getInstance().getDeliveryByName(arguments.getTo());
            if (null == toDelivery)
            {
               throw new IllegalArgumentException("The delivery: " + arguments.getTo() + " does not exist!");
            }

            LOGGER.info("To delivery: {}", toDelivery.getName());

            deliveries = collectDeliveries(deliveryArray, fromDelivery, toDelivery);
         }
         // No "to=" parameter given
         else
         {
            deliveries = collectDeliveries(deliveryArray, fromDelivery, fromDelivery);
         }
      }
      return deliveries;
   }

   /**
    * Collects all deliveries between two given deliveries for the export.
    *
    * @param deliveryArray An array filled with the deliveries.
    * @param from delivery that represents the lower boundary for the export
    * @param to delivery that represents the newest delivery for the export
    * @return collectedDeliveries set of {@link CCLDelivery} deliveries to be exported
    */
   private static Set<CCLDelivery> collectDeliveries(CCLDelivery[] deliveryArray, CCLDelivery fromDelivery, CCLDelivery toDelivery)
   {
      Set<CCLDelivery> collectedDeliveries = new TreeSet<>();

      Iterable<CCLDelivery> iterable = Arrays.asList(deliveryArray);
      Iterator<CCLDelivery> it = iterable.iterator();

      while (it.hasNext())
      {
         CCLDelivery del = it.next();
         if (del.equals(toDelivery))
         {
            collectedDeliveries.add(del);
            break;
         }
      }

      if (!toDelivery.equals(fromDelivery))
      {
         while (it.hasNext())
         {
            CCLDelivery del2 = it.next();
            collectedDeliveries.add(del2);
            if (del2.equals(fromDelivery))
            {
               break;
            }
         }
      }
      return collectedDeliveries;
   }

   /**
    * Format startup information
    *
    * @return startup information
    */
   static String info()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(CCLAssemblyInfo.getProductName());
      sb.append("\n");
      sb.append("Version: ");
      sb.append(CCLAssemblyInfo.getVersion());
      sb.append("\n");
      sb.append(CCLAssemblyInfo.getCopyright());
      sb.append(" ");
      sb.append(CCLAssemblyInfo.getCompany());
      sb.append("\n");

      return sb.toString();
   }

   /**
    * Create and return a manual for the CLI.
    *
    * @return usage information
    */
   @SuppressWarnings("all") // Suppress sonar warning: 'The String literal " [" appears 7 times in this file; the first occurrence is on line 347'
                            // Improves the readability.
   static String usage()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("\n");
      sb.append("Usage: releasefab.bat " + CLI + " " + SOURCE + "=<path_to_project_root> " + PW + "=password [" + USER + "=username] [" + ADDDELIVERY + "=filename] [" + DOCBOOK + " " + RESULTFILE + "=filename [" + FROM + "=old_delivery] [" + TO + "=new_delivery]]\n");
      sb.append("   "  + SOURCE + "=<path_to_project_root>" + "\tFolder containing project to be documented\n");
      sb.append("   " + PW + "=password"                    + "\t\t\t\tpassword for ALM System\n");
      sb.append("   " + USER + "=username"                  + "\t\t\tusername for ALM System and creating delivery\n");
      sb.append("   [" + CONFIG + "=<path_to_config>]"      + "\t\tXML-File containing releasefab config\n");
      sb.append("   [" + GENERALSETTINGS + "=<path_to_settings>]"      + "\tXML-File containing releasefab settings\n");
      sb.append("   [" + ADDDELIVERY + "=name]"             + "\t\t\tcreate new delivery with given name\n");
      sb.append("   [" + DOCBOOK + "]"                      + "\t\t\t\tcreates release information in docbook format\n");
      sb.append("   [" + CUSTOMERDOCBOOK + "]"              + "\t\t\tcreates release information in docbook format for customer\n");
      sb.append("   [" + RESULTFILE + "=filename]"          + "\t\tsets the file to output to (also with path before file)\n");
      sb.append("   [" + FROM + "=from_delivery]"           + "\t\t\tsets the delivery to export from\n");
      sb.append("   [" + TO + "=to_delivery]"               + "\t\t\tsets the delivery to export up to\n");
      sb.append("\n");
      sb.append("Example 1: releasefab.bat -cli source=c:\\git\\reference pw=XYZ delivery_name=d1\n");
      sb.append("           Creates new delivery with name d1\n");
      sb.append("\n");
      sb.append("Example 2: releasefab.bat -cli source=. pw=XYZ user=ccuser -docbook resultfile=Releasenotes.xml\n");
      sb.append("           Exports release info of last deliverie compared to previous one into file Releasenotes.xml\n");
      sb.append("\n");
      sb.append("Example 3: releasefab.bat -cli source=. pw=XYZ -docbook resultfile=export_docbook.xml from=d1 to=d2\n");
      sb.append("           Exports release information of delivery d1 and d2 to export_docbook.xml.\n");
      sb.append("           If there are deliveries in between data could be merged.\n");
      sb.append("\n");
      sb.append("Example 4: releasefab.bat -cli source=. pw=XYZ -docbook resultfile=export_docbook.xml from=d1\n");
      sb.append("           Exports release information of delivery d1 into export_docbook.xml.\n");

      return sb.toString();
   }
}
