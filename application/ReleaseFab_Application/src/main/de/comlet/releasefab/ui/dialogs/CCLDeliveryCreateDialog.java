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
 * @file CCLDeliveryCreateDialog.java
 *
 * @brief Create delivery dialog.
 */

package de.comlet.releasefab.ui.dialogs;

import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.SCLProjectHelper;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import java.text.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Allows the user to create a new delivery.
 */
public class CCLDeliveryCreateDialog extends Dialog
{
   private static final int NUMBER_OF_COLUMNS = 2;
   private static final int HORIZONTAL_SPAN = 2;
   
   private Shell mShell;
   private Text mTextBoxDeliveryName;
   private Text mTextBoxDeliveryDate;
   private Text mTextBoxIntegrator;

   /** Created delivery */
   private CCLDelivery mDelivery;

   /**
    * Allows the user to create a new delivery.
    * 
    * @param parent
    */
   public CCLDeliveryCreateDialog(Shell parent)
   {
      this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
   }

   /**
    * Allows the user to create a new delivery.
    * 
    * @param parent
    * @param style
    */
   public CCLDeliveryCreateDialog(Shell parent, int style)
   {
      super(parent, style);
      mShell = new Shell(Display.getCurrent());
      mShell.setImage(new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
      mDelivery = new CCLDelivery();
   }

   /**
    * Returns the newly created delivery.
    * 
    * @return created delivery
    */
   public CCLDelivery open()
   {
      // Create the dialog window
      Shell shell = new Shell(getParent(), getStyle());
      shell.setText(getText());

      createContent(mShell);

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

      return mDelivery;
   }

   /**
    * Creates the contents of the dialog window.
    * 
    * @param shell
    */
   private void createContent(Shell shell)
   {
      shell.setLayout(new GridLayout(NUMBER_OF_COLUMNS, true));
      shell.addTraverseListener(new CCLReturnSubmitListener());

      GridData gridDataTextBox = new GridData(GridData.HORIZONTAL_ALIGN_FILL);

      // label & TextBox for delivery name
      Label lblDeliveryName = new Label(shell, SWT.NONE);
      lblDeliveryName.setText("Delivery name:");
      mTextBoxDeliveryName = new Text(shell, SWT.SINGLE | SWT.BORDER);
      mTextBoxDeliveryName.setText(mDelivery.getName());
      mTextBoxDeliveryName.setLayoutData(gridDataTextBox);
      mTextBoxDeliveryName.selectAll();
      mTextBoxDeliveryName.setFocus();

      // label & TextBox for delivery date
      Label lblDeliveryDate = new Label(shell, SWT.NONE);
      lblDeliveryDate.setText("Delivery date:");
      mTextBoxDeliveryDate = new Text(shell, SWT.SINGLE | SWT.BORDER);
      mTextBoxDeliveryDate.setText(SCLProjectHelper.getDateFormatter().format(mDelivery.getCreated()));

      // label & TextBox for integrator name
      Label lblIntegrator = new Label(shell, SWT.NONE);
      lblIntegrator.setText("Integrator name:");
      mTextBoxIntegrator = new Text(shell, SWT.SINGLE | SWT.BORDER);
      mTextBoxIntegrator.setText(mDelivery.getIntegrator());
      mTextBoxIntegrator.setLayoutData(gridDataTextBox);

      // button group
      GridData gridDataButtonComposite = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
      gridDataButtonComposite.horizontalAlignment = SWT.RIGHT;
      gridDataButtonComposite.horizontalSpan = HORIZONTAL_SPAN;

      Composite buttonComposite = new Composite(shell, SWT.NONE);
      buttonComposite.setLayout(new GridLayout(NUMBER_OF_COLUMNS, false));
      buttonComposite.setLayoutData(gridDataButtonComposite);

      Button btnOK = new Button(buttonComposite, SWT.PUSH);
      btnOK.setText("   OK   ");
      btnOK.addMouseListener(new CCLOKButtonAdapter());

      Button btnCancel = new Button(buttonComposite, SWT.PUSH);
      btnCancel.setText("Cancel");
      btnCancel.addMouseListener(new CCLCancelButtonAdapter());
   }

   /**
    * Saves the values from the form and closes the dialog.
    */
   private void submitForm()
   {
      try
      {
         mDelivery.setCreated(SCLProjectHelper.getDateFormatter().parse(mTextBoxDeliveryDate.getText().trim()));
         mDelivery.setName(mTextBoxDeliveryName.getText().trim());
         mDelivery.setIntegrator(mTextBoxIntegrator.getText().trim());

         mShell.close();
      }
      catch (ParseException ex)
      {
         MessageBox errorMessageBox = new MessageBox(mShell, SWT.ICON_ERROR | SWT.OK);
         errorMessageBox.setMessage("Invalid date format. It has to match the following pattern:\n" +
               SCLProjectHelper.getDateFormatter().toPattern());
         errorMessageBox.setText("Error");
         errorMessageBox.open();
      }
   }

   /**
    * Listener that allows the user to submit the form by hitting the return
    * key.
    */
   private class CCLReturnSubmitListener implements TraverseListener
   {
      @Override
      public void keyTraversed(TraverseEvent e)
      {
         // when the user hits the return key
         if (e.detail == SWT.TRAVERSE_RETURN)
         {
            submitForm();
         }
      }
   }

   /**
    * MouseAdapter for the OK button. Closes the dialog window.
    */
   private class CCLOKButtonAdapter extends MouseAdapter
   {
      @Override
      public void mouseUp(MouseEvent e)
      {
         submitForm();
      }
   }

   /**
    * MouseAdapter for the Cancel button. Closes the dialog window.
    */
   private class CCLCancelButtonAdapter extends MouseAdapter
   {
      @Override
      public void mouseUp(MouseEvent e)
      {
         mDelivery = null;

         mShell.close();
      }
   }
}
