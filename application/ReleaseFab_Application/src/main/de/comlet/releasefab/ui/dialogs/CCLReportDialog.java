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
 * @file CCLReportDialog.java
 *
 * @brief Delivery creation report dialog.
 */

package de.comlet.releasefab.ui.dialogs;

import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jdom2.Element;

/**
 * Dialog containing the report of a newly created delivery.
 */
public class CCLReportDialog extends Dialog
{
   private static final int IMPORTER_COLUMN_WIDTH = 100;
   private static final int DELIVERY_COLUMN_WIDTH = 100;
   private static final int COMPONENT_COLUMN_WIDTH = 100;
   private static final int ASSIGNER_COLUMN_WIDTH = 150;
   private static final int ERROR_COLUMN_WIDTH = 300;

   private static final int IMPORTER_COLUMN_INDEX = 0;
   private static final int DELIVERY_COLUMN_INDEX = 1;
   private static final int COMPONENT_COLUMN_INDEX = 2;
   private static final int ASSIGNER_COLUMN_INDEX = 3;
   private static final int ERROR_COLUMN_INDEX = 4;

   private Shell mShell;

   private Element mCreationReport;

   /**
    * Dialog containing the report of a newly created delivery.
    * 
    * @param parent
    */
   public CCLReportDialog(Shell parent, Element creationReport)
   {
      this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, creationReport);
   }

   /**
    * Dialog containing the report of a newly created delivery.
    *
    * @param parent
    * @param style
    */
   public CCLReportDialog(Shell parent, int style, Element creationReport)
   {
      super(parent, style);
      mShell = new Shell(Display.getCurrent());
      mShell.setText("Report");
      mShell.setImage(new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
      mCreationReport = creationReport;
   }

   /**
    * Creates and shows the dialog.
    */
   public void open()
   {
      createContent();

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
    * Defines what content to show within the dialog.
    */
   private void createContent()
   {
      mShell.setLayout(new FillLayout());

      Table table = new Table(mShell, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
      table.setHeaderVisible(true);

      TableColumn tcImporter = new TableColumn(table, SWT.NONE);
      tcImporter.setText("Importer");
      tcImporter.setWidth(IMPORTER_COLUMN_WIDTH);

      TableColumn tcDelivery = new TableColumn(table, SWT.NONE);
      tcDelivery.setText("Delivery");
      tcDelivery.setWidth(DELIVERY_COLUMN_WIDTH);

      TableColumn tcComponent = new TableColumn(table, SWT.NONE);
      tcComponent.setText("Component");
      tcComponent.setWidth(COMPONENT_COLUMN_WIDTH);

      TableColumn tcAssigner = new TableColumn(table, SWT.NONE);
      tcAssigner.setText("Assigner");
      tcAssigner.setWidth(ASSIGNER_COLUMN_WIDTH);

      TableColumn tcError = new TableColumn(table, SWT.NONE);
      tcError.setText("Error");
      tcError.setWidth(ERROR_COLUMN_WIDTH);

      for (Element error : mCreationReport.getChildren("error"))
      {
         TableItem ti = new TableItem(table, SWT.NONE);
         ti.setText(IMPORTER_COLUMN_INDEX, error.getAttributeValue("importer").trim());
         ti.setText(DELIVERY_COLUMN_INDEX, error.getAttributeValue("delivery").trim());
         ti.setText(COMPONENT_COLUMN_INDEX, error.getAttributeValue("component").trim());
         ti.setText(ASSIGNER_COLUMN_INDEX, error.getAttributeValue("assigner").trim());
         ti.setText(ERROR_COLUMN_INDEX, error.getText().trim());
      }
   }
}
