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
 * @file CCLCredentialsHelper.java
 *
 * @brief Test credentials helper.
 */

package de.comlet.releasefab.test.util;

import de.comlet.releasefab.library.xml.CCLXMLConstants;
import java.io.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.fail;

public final class CCLCredentialsHelper
{
   /**
    * Message asking for the URL of the ALM server.
    */
   private static final String ALM_SERVER_MESSAGE = "Enter the URL of the ALM server: ";

   /**
    * Message asking for the Unix certificate path.
    */
   private static final String CERTIFICATE_UNIX_MESSAGE = "Enter the path to the certificate for your ALM service on unix-based machines: ";

   /**
    * Message asking for the Windows certificate path.
    */
   private static final String CERTIFICATE_WINDOWS_MESSAGE = "Enter the path to the certificate for your ALM service on windows machines: ";

   /** Init logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLCredentialsHelper.class);

   /**
    * The message displayed on the command line to ask the user for a valid
    * ALM user name.
    */
   private static final String ENTER_ALM_USER_NAME_MESSAGE = "Please enter a valid ALM Service user name: ";

   /**
    * The message displayed on the command line to ask the user for a the
    * corresponding ALM password.
    */
   private static final String ENTER_ALM_PASSWORD_MESSAGE = "Please enter the password for the ALM Service user name \"";
   
   /**
    * The key for the "Setting" which contains the ALM Service API Key as its
    * value. Must match the key used by the application itself in
    * all plugin classes for ALM services..
    */
   private static final String ALM_API_KEY = "ALM_API_KEY";

   /**
    * Empty, private default constructor to restrict the instantiation of this
    * helper class.
    */
   private CCLCredentialsHelper()
   {
   }

   /**
    * Gets the ALM Service login credentials either from program arguments or -
    * if they were not passed - from the command line.
    * 
    * @return Map with two items: {@link #ALM_USER_KEY} mapped to "actual
    * username" and {@link #ALM_PASSWORD_KEY} mapped to "actual password". If
    * there were no arguments passed or the arguments are empty, the method will
    * let the current test fail and return null. Optionally certificate paths 
    * can be passed for windows and unix based systems.
    */
   public static Map<String, String> getCredentials()
   {
      // Try to get Credentials from program arguments first
      Map<String, String> credentialsMap = getCredentialsFromProgramArguments();

      boolean credentialsNotInProgramArguments = (null == credentialsMap.get(CCLXMLConstants.XML_ALM_USER_KEY) || null == credentialsMap.get(CCLXMLConstants.XML_ALM_PASSWORD_KEY) || null == credentialsMap.get(CCLXMLConstants.XML_ALM_SERVER));
      boolean certificateNotInProgramArguments = (null == credentialsMap.get(CCLXMLConstants.XML_ALM_UNIX_CERTIFICATE_PATH) && null == credentialsMap.get(CCLXMLConstants.XML_ALM_WINDOWS_CERTIFICATE_PATH));
      
      if (credentialsNotInProgramArguments && certificateNotInProgramArguments)
      {
         return getUserInput();
      }
      else
      {
         // Else return credentials from program arguments
         return credentialsMap;
      }
   }

   /**
    * Delegates the retrieval of the credentials to the method 
    * reading from the command line. Certificate information is 
    * optional.
    * 
    * @return Map with two items: {@link #ALM_USER_KEY} mapped to "actual
    * username" and {@link #ALM_PASSWORD_KEY} mapped to "actual password". If
    * there were no arguments passed or the arguments are empty, the method will
    * let the current test fail and return null. Optionally certificate paths 
    * can be passed for windows and unix based systems.
    */
   private static Map<String, String> getUserInput()
   {
      Map<String, String> credentialsMap;
      // If they are not passed as program arguments -> get from command line
      credentialsMap = getCredentialsFromCommandLine();

      // Check if an actual value was passed
      boolean necessaryInformationMissing = credentialsMap.get(CCLXMLConstants.XML_ALM_USER_KEY).isEmpty() || credentialsMap.get(CCLXMLConstants.XML_ALM_PASSWORD_KEY).isEmpty() || credentialsMap.get(CCLXMLConstants.XML_ALM_SERVER).isEmpty();
      boolean optionalInformationMissing = credentialsMap.get(CCLXMLConstants.XML_ALM_UNIX_CERTIFICATE_PATH).isEmpty() && credentialsMap.get(CCLXMLConstants.XML_ALM_WINDOWS_CERTIFICATE_PATH).isEmpty();
      
      if (necessaryInformationMissing)
      {
         fail("There were no valid login Credentials passed for the ALM Service!\nThey can be passed by program arguments or entered on the command line.");
         return null;
      }
      else if (optionalInformationMissing)
      {
         LOGGER.info("There was no certificate path passed. Tests might fail.");
         return credentialsMap;
      }
      else
      {
         return credentialsMap;
      }
   }

   /**
    * Gets the ALM Service login credentials from program arguments.
    * 
    * @return Map with two items: {@link #ALM_USER_KEY} mapped to "actual
    * username" and {@link #ALM_PASSWORD_KEY} mapped to "actual password". If
    * there were no arguments passed or the arguments are empty, the method will
    * let the current test fail and return null. Optionally certificate paths 
    * can be passed for windows and unix based systems.
    */
   private static Map<String, String> getCredentialsFromProgramArguments()
   {
      Map<String, String> resMap = new HashMap<String, String>();

      String user = System.getProperty(CCLXMLConstants.XML_ALM_USER_KEY);
      String password = System.getProperty(CCLXMLConstants.XML_ALM_PASSWORD_KEY);
      String almServer = System.getProperty(CCLXMLConstants.XML_ALM_SERVER);
      String winCertificate = System.getProperty(CCLXMLConstants.XML_ALM_WINDOWS_CERTIFICATE_PATH);
      String unixCertificate = System.getProperty(CCLXMLConstants.XML_ALM_UNIX_CERTIFICATE_PATH);
      
      boolean passwordOrUserIsNotNull = (null != user && null != password);

      if (passwordOrUserIsNotNull)
      {
         resMap.put(CCLXMLConstants.XML_ALM_USER_KEY, user);
         resMap.put(CCLXMLConstants.XML_ALM_PASSWORD_KEY, password);
         resMap.put(CCLXMLConstants.XML_ALM_SERVER, almServer);
         resMap.put(CCLXMLConstants.XML_ALM_WINDOWS_CERTIFICATE_PATH, winCertificate);
         resMap.put(CCLXMLConstants.XML_ALM_UNIX_CERTIFICATE_PATH, unixCertificate);
      }

      return resMap;
   }

   /**
    * Gets the ALM Service login credentials from the command line by using
    * either a console or the standard input. The standard input is only a
    * workaround for the buggy Eclipse handling of the console.
    * 
    * @return  Map with two items: {@link #ALM_USER_KEY} mapped to "actual
    * username" and {@link #ALM_PASSWORD_KEY} mapped to "actual password". If
    * there were no arguments passed or the arguments are empty, the method will
    * let the current test fail and return null. Optionally certificate paths 
    * can be passed for windows and unix based systems.
    */
   private static Map<String, String> getCredentialsFromCommandLine()
   {
      String user;
      String password;
      String almServer;
      String winCertificate;
      String unixCertificate;
      Console console = System.console();

      if (null != console)
      {
         // When running from the command line or build file, the password is
         // masked
         user = console.readLine(ENTER_ALM_USER_NAME_MESSAGE);
         password = new String(console.readPassword(ENTER_ALM_PASSWORD_MESSAGE + user + "\": "));
         almServer = console.readLine(ALM_SERVER_MESSAGE);
         winCertificate = console.readLine(CERTIFICATE_WINDOWS_MESSAGE);
         unixCertificate = console.readLine(CERTIFICATE_UNIX_MESSAGE);
      }
      else
      {
         // Due to the handling of the console in Eclipse, creating a console
         // will fail. That is why this failsafe exists
         try (Scanner scanner = new Scanner(System.in))
         {
            LOGGER.info(ENTER_ALM_USER_NAME_MESSAGE);
            user = scanner.nextLine();

            LOGGER.info(ENTER_ALM_PASSWORD_MESSAGE + user + "\": ");
            password = scanner.nextLine();
            
            LOGGER.info(ALM_SERVER_MESSAGE);
            almServer = scanner.nextLine();
            
            LOGGER.info(CERTIFICATE_WINDOWS_MESSAGE);
            winCertificate = scanner.nextLine();
            
            LOGGER.info(CERTIFICATE_UNIX_MESSAGE);
            unixCertificate = scanner.nextLine();
         }
         catch (NoSuchElementException | IllegalStateException e)
         {
            // Local run without Credentials entered
            user = " ";
            password = " ";
            almServer = " ";
            winCertificate = " ";
            unixCertificate = " ";
            
            LOGGER.debug("Exception reading test credentials from command line", e);
         }
      }

      Map<String, String> resMap = new HashMap<String, String>();
      resMap.put(CCLXMLConstants.XML_ALM_USER_KEY, user);
      resMap.put(CCLXMLConstants.XML_ALM_PASSWORD_KEY, password);
      resMap.put(CCLXMLConstants.XML_ALM_SERVER, almServer);
      resMap.put(CCLXMLConstants.XML_ALM_WINDOWS_CERTIFICATE_PATH, winCertificate);
      resMap.put(CCLXMLConstants.XML_ALM_UNIX_CERTIFICATE_PATH, unixCertificate);
      
      return resMap;
   }
   
   public static String getApiKeyFromProgramArguments()
   {
      return System.getProperty(ALM_API_KEY);
   }
}
