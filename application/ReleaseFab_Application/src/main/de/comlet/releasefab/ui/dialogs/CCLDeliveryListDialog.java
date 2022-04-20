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
 * @file CCLDeliveryListDialog.java
 *
 * @brief Dialog with list of deliveries.
 */

package de.comlet.releasefab.ui.dialogs;

import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Asks the user to choose deliveries from a list.
 */
public class CCLDeliveryListDialog extends Dialog
{
   private static final int NUMBER_OF_COLUMNS = 2;
   private static final int HORIZONTAL_SPAN = 2;
   private static final int VERTICAL_SPAN = 1;
   private static final int COLUMN_WIDTH_NAME = 100;
   private static final int COLUMN_WIDTH_CREATED = 200;
   private static final int COLUMN_WIDTH_INTEGRATOR = 100;
   private static final int INDEX_ZERO = 0;
   private static final int INDEX_ONE = 1;
   private static final int INDEX_TWO = 2;  
   
   private Shell mShell;
   private Table mDeliveryTableWidget;
   private Button mIsForCustomerCheckBox;

   /** Deliveries to choose from */
   private Collection<CCLDelivery> mDeliveries;

   /** Selected deliveries */
   private Set<CCLDelivery> mSelectedDeliveries = new TreeSet<>();

   /** Value of the checkbox {@link #mIsForCustomerCheckBox} */
   private boolean mIsForCustomer;

   /**
    * Ask the user to choose deliveries from a list.
    * 
    * @param parent
    * @param deliveries deliveries to choose from
    */
   public CCLDeliveryListDialog(Shell parent, Collection<CCLDelivery> deliveries)
   {
      this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, deliveries);
   }

   /**
    * Ask the user to choose deliveries from a list.
    * 
    * @param parent
    * @param style
    * @param deliveries deliveries to choose from
    */
   public CCLDeliveryListDialog(Shell parent, int style, Collection<CCLDelivery> deliveries)
   {
      super(parent, style);
      mShell = new Shell(Display.getCurrent());
      mShell.setImage(new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
      mDeliveries = deliveries;
   }

   /**
    * Returns the selected deliveries
    * 
    * @return selected deliveries
    */
   public Set<CCLDelivery> open()
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

      return mSelectedDeliveries;
   }

   /**
    * Displays the table of deliveries to choose from with information like 
    * their name, date of creation and integrator.
    * 
    * @param shell
    */
   private void createContent(Shell shell)
   {
      shell.setLayout(new GridLayout(NUMBER_OF_COLUMNS, true));

      mDeliveryTableWidget = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
      GridData data = new GridData(GridData.FILL, GridData.BEGINNING, true, false, HORIZONTAL_SPAN, VERTICAL_SPAN);
      mDeliveryTableWidget.setLayoutData(data);

      TableColumn columnName = new TableColumn(mDeliveryTableWidget, SWT.CENTER);
      columnName.setText("Name");
      columnName.setWidth(COLUMN_WIDTH_NAME);

      TableColumn columnCreated = new TableColumn(mDeliveryTableWidget, SWT.CENTER);
      columnCreated.setText("Created");
      columnCreated.setWidth(COLUMN_WIDTH_CREATED);

      TableColumn columnIntegrator = new TableColumn(mDeliveryTableWidget, SWT.CENTER);
      columnIntegrator.setText("Integrator");
      columnIntegrator.setWidth(COLUMN_WIDTH_INTEGRATOR);

      for (CCLDelivery delivery : mDeliveries)
      {
         TableItem tableItem = new TableItem(mDeliveryTableWidget, SWT.NONE);
         tableItem.setText(INDEX_ZERO, delivery.getName());
         tableItem.setText(INDEX_ONE, delivery.getCreated().toString());
         tableItem.setText(INDEX_TWO, delivery.getIntegrator());
      }

      mIsForCustomerCheckBox = new Button(shell, SWT.CHECK);
      mIsForCustomerCheckBox.setText("Export for customer");
      
      // button group
      GridData gridDataButtonComposite = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
      gridDataButtonComposite.horizontalAlignment = SWT.RIGHT;
      
      Composite buttonComposite = new Composite(shell, SWT.NONE);
      buttonComposite.setLayout(new GridLayout(NUMBER_OF_COLUMNS, false));
      buttonComposite.setLayoutData(gridDataButtonComposite);

      Button btnOK = new Button(buttonComposite, SWT.PUSH);
      btnOK.setText("Export");
      btnOK.addMouseListener(new CCLOKButtonAdapter());

      Button btnCancel = new Button(buttonComposite, SWT.PUSH);
      btnCancel.setText("Cancel");
      btnCancel.addMouseListener(new CCLCancelButtonAdapter());
   }

   /**
    * Did the user check the checkbox?
    * 
    * @return
    */
   public boolean isForCustomer()
   {
      return mIsForCustomer;
   }

   /**
    * MouseAdapter for the OK button. Closes the dialog window.
    */
   private class CCLOKButtonAdapter extends MouseAdapter
   {
      @Override
      public void mouseUp(MouseEvent e)
      {
         int[] selectedIndices = mDeliveryTableWidget.getSelectionIndices();
         Arrays.sort(selectedIndices);

         mSelectedDeliveries = new TreeSet<>();

         int i = 0;
         for (CCLDelivery delivery : mDeliveries)
         {
            if (0 <= Arrays.binarySearch(selectedIndices, i))
            {
               mSelectedDeliveries.add(delivery);
            }

            i++;
         }

         mIsForCustomer = mIsForCustomerCheckBox.getSelection();

         mShell.close();
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
         mShell.close();
      }
   }
}
