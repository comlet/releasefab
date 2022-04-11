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
 * @file CCLProgressDialog.java
 *
 * @brief Progress dialog.
 */

package de.comlet.releasefab.ui.dialogs;

import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that shows an animated progress bar.
 */
public class CCLProgressDialog extends Dialog
{
   private static final int CENTER_DIVIDER = 2;
   private static final int PROGRESS_BAR_WIDTH = 200;
   private static final int PROGRESS_BAR_HEIGHT = 32;

   private Shell mShell;

   /**
    * Dialog that shows an animated progress bar.
    *
    * @param parent
    */
   public CCLProgressDialog(Shell parent)
   {
      this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
   }

   /**
    * Dialog that shows an animated progress bar.
    *
    * @param parent
    * @param style
    */
   public CCLProgressDialog(Shell parent, int style)
   {
      super(parent, style);

      mShell = new Shell(Display.getCurrent(), SWT.TITLE);
      mShell.setText("Progress");
      mShell.setImage(new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
      mShell.setLayout(new FillLayout());
   }

   /**
    * Starts a new thread for the given task and opens the dialog that shows an
    * animated progress bar.<br>
    * Blocks until all work is done and then automatically closes the progress
    * dialog.
    */
   public void open(ACLBackgroundWorker backgroundWorker)
   {
      // start worker thread
      new Thread(backgroundWorker).start();

      // create progress bar
      ProgressBar progressBar = new ProgressBar(mShell, SWT.INDETERMINATE);
      progressBar.setSize(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);

      // move the dialog to the center of the screen
      Point size = mShell.computeSize(-1, -1);
      Rectangle screen = mShell.getDisplay().getMonitors()[0].getBounds();
      mShell.setBounds((screen.width - size.x) / CENTER_DIVIDER, (screen.height - size.y) / CENTER_DIVIDER, size.x, size.y);

      mShell.pack();
      mShell.open();
      Display display = getParent().getDisplay();
      while (!mShell.isDisposed())
      {
         if (!display.readAndDispatch())
         {
            display.sleep();
         }
      }
   }

   /**
    * Closes the dialog.
    */
   public void close()
   {
      mShell.dispose();
   }

   /**
    * Background worker that does some work defined in subclasses and then
    * closes the given progress dialog.
    */
   public abstract static class ACLBackgroundWorker implements Runnable
   {
      /** Progress dialog this worker belongs to */
      private CCLProgressDialog mDialog;

      /** Sometimes it's useful to provide some results of the work. */
      private Object mResult;
      private boolean mSucceeded;

      /**
       * Background worker that does some work defined in subclasses and then
       * closes the given progress dialog.
       *
       * @param dialog
       */
      public ACLBackgroundWorker(CCLProgressDialog dialog)
      {
         mDialog = dialog;
      }

      /**
       * This method allows subclasses of ACLBackgroundWorker to define what the
       * background worker should do.
       */
      public abstract void doWork();

      @Override
      public void run()
      {
         doWork();

         // work is done -> close the progress dialog
         Display.getDefault().asyncExec(new Runnable()
         {
            @Override
            public void run()
            {
               mDialog.close();
            }
         });
      }

      public void setResult(Object result)
      {
         mResult = result;
      }

      public Object getResult()
      {
         return mResult;
      }

      public void setSucceeded(boolean succeeded)
      {
         mSucceeded = succeeded;
      }

      public boolean isSucceeded()
      {
         return mSucceeded;
      }
   }
}
