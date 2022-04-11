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
 * @file CCLProgramArguments.java
 *
 * @brief Contains the arguments passed on the command line.
 */

package de.comlet.releasefab;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCLProgramArguments
{
   /** Initialize logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLProgramArguments.class);
   
   /**
    * Number of parameters for assignments of values.
    */
   private static final int TWOPARAMS = 2;
   
   /**
    * Escaped double quotes for use in strings.
    */
   private static final String ESCAPEDDBLQUOTES = "\"";
   
   /**
    * Start phrase for CLI message.
    */
   private static final String USETEXT = "Using " + ESCAPEDDBLQUOTES;
   
   /**
    * Whether to start this application as command line tool or as gui application.
    */
   private boolean mCli;
   
   /**
    * The directory containing the file with the version information.
    */
   private String mSource;
   
   /**
    * The directory containing the configuration files.
    */
   private String mConfig;
   
   /**
    * The last delivery to export. If mFrom isn't set this is the only delivery exported.
    */
   private String mTo;
   
   /**
    * The first delivery to export.
    */
   private String mFrom;
   
   /**
    * The file to export the information in docbook format to.
    */
   private String mOutputFile;
   
   /**
    * The names of the deliveries to add to the current file with the version information.
    */
   private List<String> mDeliveryNames;
   
   /**
    * Whether to export a docbook file containing only customer relevant information or not.
    */
   private boolean mIsCustomerDocBook;
   
   /**
    * Whether to export a docbook file or not.
    */
   private boolean mIsDocBook;
   
   /**
    * Whether to add one or more deliveries.
    */
   private boolean mIsDelivery;
   
   /**
    * Stores the given password.
    */
   private char[] mUserPassword;
   
   /**
    * Stores the given username.
    */
   private String mUserName;
   
   /**
    * Local copy of parameter array.
    */
   private String[] mParameterArray;
   
   /**
    * Optional path to settings.xml (Necessary when using jlink image)
    */
   private String mGeneralSettings;
      

   /**
    * Parses the passed program arguments and stores the results in the new object.
    * 
    * @param args The program arguments passed to the {@link Main#main(String[])} method
    */
   public CCLProgramArguments(String[] args)
   {
      for (int i = 0; i < args.length; i++)
      {
         String[] param = args[i].split("=");
         ECLProgramKey key = ECLProgramKey.getEnumFromContent(param[0]);
         if (key != null)
         {
            switchKeyWithoutValue(key, param);
         }
         else
         {
            printError("\n" + ESCAPEDDBLQUOTES + param[0] + ESCAPEDDBLQUOTES + " is not a valide keyword!\n");
            System.exit(0);
         }
      }
      String error = this.validate();
      if (error != null)
      {
         printError("\n" + error + "\n");
         System.exit(0);
      }
   }

   /**
    * Handles the {@link ECLProgramKey} key without a value.
    * If key has an assigned value the pair is passed through to {@link #switchKey(ECLProgramKey, String[])}.
    * 
    * @param key The key associated to parameter[0].
    * @param parameter The program argument split at "=".
    */
   private void switchKeyWithoutValue(ECLProgramKey key, String[] parameter)
   {      
      switch (key)
      {
         case CLI:
         {
            this.mCli = true;
            break;
         }
         case DOCBOOK:
         {
            this.mIsDocBook = true;
            break;
         }
         case CUSTOMERDOCBOOK:
         {
            this.mIsCustomerDocBook = true;
            this.mIsDocBook = true;
            break;
         }
         default:
         {
            checkForParameter(key, parameter);
            switchKey(key, parameter);
         }
      }
   }
   
   /**
    * Handles the {@link String[]} parameter depending on the {@link ECLProgramKey} key.
    * 
    * @param key The key associated to parameter[0].
    * @param parameter The program argument split at "=".
    */
   private void switchKey(ECLProgramKey key, String[] parameter)
   {
      // The following check was introduced due to Sonar warning "Security - Array is stored directly"
      if (parameter == null)
      {
         this.mParameterArray = new String[0];
      }
      else
      {
         this.mParameterArray = Arrays.copyOf(parameter, parameter.length);
      }
      
      switchKeyWithValue(key);
   }

   /**
    * Handles the {@link ECLProgramKey} key with a value.
    * The value for the key is set accordingly.
    * 
    * @param key The key associated to parameter[0].
    */
   private void switchKeyWithValue(ECLProgramKey key)
   {
      switch (key)
      {
         case SOURCE:
         {
            this.mSource = mParameterArray[1];
            break;
         }
         case CONFIG:
         {
            this.mConfig = mParameterArray[1];
            break;
         }
         case ADDDELIVERY:
         {
            this.mIsDelivery = true;
            this.setDeliveryNames(mParameterArray[1]);
            break;
         }
         case RESULTFILE:
         {
            this.mOutputFile = mParameterArray[1];
            break;
         }
         case FROM:
         {
            this.mFrom = mParameterArray[1];
            break;
         }
         case TO:
         {
            this.mTo = mParameterArray[1];
            break;
         }
         case PW:
         {
            this.mUserPassword = mParameterArray[1].toCharArray();
            break;
         }
         case USER:
         {
            this.mUserName = mParameterArray[1];
            break;
         }
         case GENERALSETTINGS:
         {
            this.mGeneralSettings = mParameterArray[1];
            break;
         }
         case HELP:
            // intentional fall through to default.
         default:
         {
            printError("\n");
            System.exit(0);
            break;
         }
      }
   }

   /**
    * Prints an error if the length of {@link String[]} param is shorter than 2.
    * 
    * @param key The command line argument key to use in the error message.
    * @param param
    */
   private void checkForParameter(final ECLProgramKey key, final String[] param)
   {
      if (param.length < TWOPARAMS)
      {
         printError("\n" + ESCAPEDDBLQUOTES + key + ESCAPEDDBLQUOTES + " expects a parameter!\n");
         System.exit(0);
      }
   }

   /**
    * Prints and logs the {@link Main#info()} startup information, the given {@link String} error and the
    * {@link Main#usage()} usage information of this program.
    * 
    * @param error The error that occured.
    */
   private void printError(String error)
   {
      LOGGER.info(Main.info() + error + Main.usage());
   }

   public boolean isCli()
   {
      return mCli;
   }

   public String getSource()
   {
      return mSource;
   }

   public String getConfig()
   {
      return mConfig;
   }

   public String getTo()
   {
      return mTo;
   }

   public String getFrom()
   {
      return mFrom;
   }

   public String getOutputFile()
   {
      return mOutputFile;
   }

   public boolean isCustomerDocBook()
   {
      return mIsCustomerDocBook;
   }

   public boolean isDocBook()
   {
      return mIsDocBook;
   }

   public boolean isDelivery()
   {
      return mIsDelivery;
   }

   public Iterable<String> getDeliveryNames()
   {
      return mDeliveryNames;
   }
   
   public char[] getUserPassword()
   {
      return mUserPassword;
   }

   public String getUserName()
   {
      return mUserName;
   }

   public String getGeneralSettings()
   {
      return mGeneralSettings;
   }

   private void setDeliveryNames(final String deliveryNames)
   {
      String names = deliveryNames.replace("{", "").replace("}", "");
      StringTokenizer tk = new StringTokenizer(names, ",");
      this.mDeliveryNames = new LinkedList<String>();
      while (tk.hasMoreTokens())
      {
         this.mDeliveryNames.add(tk.nextToken());
      }
   }
   
   /**
    * Checks if the required command line arguments have been passed.
    * 
    * @return An error message describing what is wrong with the given command line arguments.
    */
   private String validate()
   {
      StringBuilder sb = new StringBuilder();

      if (mSource == null)
      {
         sb.append("You need to set " + ECLProgramKey.SOURCE + "=SOURCE\n");
      }

      if (mCli)
      {
         validateDelivery(sb);
         validateDocbook(sb);
      }

      String error = sb.toString();
      
      if (!error.isEmpty())
      {
         return "\n\n" + error + "\n";
      }
      else
      {
         return null;
      }
   }

   /**
    * Checks command line argument dependencies for creating a delivery.
    * 
    * @param sb The {@link StringBuilder} to append errors to.
    */
   private void validateDelivery(StringBuilder sb)
   {
      if (mIsDelivery)
      {
         if (mIsCustomerDocBook)
         {
            sb.append("You can either use: " + ECLProgramKey.CUSTOMERDOCBOOK + " or " + ECLProgramKey.ADDDELIVERY + "\n");
         }
         else if (mIsDocBook)
         {
            sb.append("You can either use; " + ECLProgramKey.DOCBOOK + " or " + ECLProgramKey.ADDDELIVERY + "\n");
         }

         if (mDeliveryNames == null)
         {
            sb.append("You chose to add deliveries but didn't provide the names. Please supply " + ESCAPEDDBLQUOTES + ECLProgramKey.ADDDELIVERY + "={[d1],[d2],...}" + ESCAPEDDBLQUOTES + ".");
         }
      }
      else if (!mIsDocBook)
      {
         sb.append("Don't know what to do! Please add parameters!\n");
      }
   }

   /**
    * Checks command line argument dependencies for creating docbook export.
    * 
    * @param sb The {@link StringBuilder} to append errors to.
    */
   private void validateDocbook(StringBuilder sb)
   {
      if (mOutputFile == null)
      {
         final String messagePart = ESCAPEDDBLQUOTES + " requires you to set an output file using " + ESCAPEDDBLQUOTES + ECLProgramKey.RESULTFILE + "=filename" + ESCAPEDDBLQUOTES + " !\n";
         if (mIsCustomerDocBook)
         {
            sb.append(USETEXT + ECLProgramKey.CUSTOMERDOCBOOK + messagePart + "\n");
         }
         else if (mIsDocBook)
         {
            sb.append(USETEXT + ECLProgramKey.DOCBOOK + messagePart + "\n");
         }
      }
      if (mFrom == null && mTo != null)
      {
         sb.append(USETEXT + ECLProgramKey.TO + ESCAPEDDBLQUOTES + " requires you to set " + ESCAPEDDBLQUOTES + ECLProgramKey.FROM + ESCAPEDDBLQUOTES + " !\n");
      }
   }
}
