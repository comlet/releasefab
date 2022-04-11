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
 * @file CCLCommandExecuter.java
 *
 * @brief Command executer.
 */

package de.comlet.releasefab.library.model;

import de.comlet.releasefab.library.exception.CCLInternalRuntimeException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that allows the execution of commands with a timeout.
 */
public class CCLCommandExecuter
{
   /**
    * Waiting time in milliseconds [ms] for gobbler full buffer.
    */
   private static final int GOBBLERWAITINGTIME = 1000;

   /** Initialize logger for this class. */
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLCommandExecuter.class);

   /** Command to execute. */
   private String mCommand;

   /**
    * Consumer for output streams. This is necessary to avoid deadlocks due to a
    * full buffer.
    */
   private CCLStreamGobbler mOutputGobbler;

   /**
    * Consumer for error streams. This is necessary to avoid deadlocks due to a
    * full buffer.
    */
   private CCLStreamGobbler mErrorGobbler;

   /**
    * Constructor.
    * 
    * @param command command to execute
    */
   public CCLCommandExecuter(String command)
   {
      mCommand = command;
   }

   /**
    * Execute the given command. Execution is aborted if timeout expires.
    * 
    * @param timeout
    * @return
    */
   public int execute(long timeout)
   {
      try
      {
         ProcessBuilder builder = new ProcessBuilder(createCommandList());
         Process process = builder.start();

         // start background worker
         CCLBackgroundWorker worker = new CCLBackgroundWorker(process);
         worker.start();

         // consume error and output streams
         mOutputGobbler = new CCLStreamGobbler(process.getInputStream());
         mErrorGobbler = new CCLStreamGobbler(process.getErrorStream());
         mOutputGobbler.start();
         mErrorGobbler.start();

         return waitForWorker(timeout, worker);
      }
      catch (InterruptedException e)
      {
         throw new CCLInternalRuntimeException(mCommand + " did not complete due to an unexpected interruption.", e);
      }
      catch (IOException e)
      {
         throw new CCLInternalRuntimeException(mCommand + " did not complete due to an unexpected IOException.", e);
      }
   }

   private int waitForWorker(long aTimeout, CCLBackgroundWorker worker) throws InterruptedException
   {
      try
      {
         // wait for the background worker to complete its task
         worker.join(aTimeout);

         // wait for the output and error stream consumers to finish reading
         // their streams
         mOutputGobbler.join(GOBBLERWAITINGTIME);
         mErrorGobbler.join(GOBBLERWAITINGTIME);

         // get exit value
         Integer exitValue = worker.getExitValue();
         if (exitValue != null)
         {
            // the worker thread completed within the timeout period
            return exitValue;
         }

         // background worker timed out
         throw new CCLInternalRuntimeException(
               mCommand + " timed out. This might be because the specified program did not terminate.");
      }
      catch (InterruptedException e)
      {
         worker.interrupt();
         Thread.currentThread().interrupt();
         throw e;
      }
   }

   /**
    * Get the output of the executed command.
    * 
    * @return output of the executed command
    */
   public String getOutputString()
   {
      return mOutputGobbler.getOutput();
   }

   /**
    * Get the error output of the executed command
    * 
    * @return error output of the executed command
    */
   public String getErrorString()
   {
      return mErrorGobbler.getOutput();
   }

   /**
    * Splits a command string into a list of strings. The first string in the
    * list is the program name, the subsequent strings are arguments for this
    * program.
    * 
    * @return command split into list of strings
    */
   private List<String> createCommandList()
   {
      List<String> command = new ArrayList<>();

      int i = 0;
      while (i < mCommand.length())
      {
         int index = -1;
         if (mCommand.charAt(i) == '"')
         {
            // if the current character is a quotation mark, search for the
            // index of the next quotation mark
            index = mCommand.indexOf('"', i + 1);

            // skip the current character
            i = i + 1;
         }
         else
         {
            // search for the index of the next blank
            index = mCommand.indexOf(' ', i + 1);

            // if the current character is a blank skip it
            if (mCommand.charAt(i) == ' ')
            {
               i = i + 1;
            }
         }

         // if there is no next delimiter character set the index to the last
         // index of the string
         if (index < 0)
         {
            index = mCommand.length();
         }

         if (i < mCommand.length())
         {
            String str = mCommand.substring(i, index);
            command.add(str);
         }

         i = index + 1;
      }

      return command;
   }

   /**
    * Background worker that executes the command in a thread. This allows the
    * abortion of the execution if necessary.
    */
   private static class CCLBackgroundWorker extends Thread
   {
      private final Process mProcess;
      private Integer mExitValue;

      public CCLBackgroundWorker(final Process aProcess)
      {
         mProcess = aProcess;
      }

      public Integer getExitValue()
      {
         return mExitValue;
      }

      @Override
      public void run()
      {
         try
         {
            mExitValue = mProcess.waitFor();
         }
         catch (InterruptedException e)
         {
            LOGGER.error(e.getMessage(), e);
         }
      }
   }

   /**
    * StreamGobbler to consume the output and error streams.
    */
   private static class CCLStreamGobbler extends Thread
   {
      private InputStream mInputStream;
      private String mOutput;

      public CCLStreamGobbler(final InputStream aInputStream)
      {
         mInputStream = aInputStream;
      }

      @Override
      public void run()
      {
         try (Scanner scanner = new Scanner(mInputStream, "ISO-8859-1"))
         {
            StringBuilder stringBuilder = new StringBuilder();

            while (scanner.hasNext())
            {
               stringBuilder.append(scanner.nextLine());
               stringBuilder.append("\r\n");
            }

            mOutput = stringBuilder.toString();
         }
      }

      public String getOutput()
      {
         return mOutput;
      }
   }
}
