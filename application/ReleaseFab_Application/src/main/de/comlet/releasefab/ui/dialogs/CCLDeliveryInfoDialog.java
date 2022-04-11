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
 * @file CCLDeliveryInfoDialog.java
 *
 * @brief Delivery information dialog.
 */

package de.comlet.releasefab.ui.dialogs;

import de.comlet.releasefab.SCLPluginLoader;
import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLObservableCollection;
import de.comlet.releasefab.ui.CCLMainWindow;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import java.text.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.slf4j.Logger;

/**
 * Shows information about a component in a delivery.
 */
public class CCLDeliveryInfoDialog extends Dialog
{
   private static final int BUTTON_GROUP_GRID_COUNT = 2;
   private static final Logger LOGGER = CCLMainWindow.getLogger();

   private Shell mShell;

   /** Selected component */
   private CCLComponent mComponent;

   /** Selected delivery */
   private CCLDelivery mDelivery;

   /**
    * Shows information about a component in a delivery.
    *
    * @param parent
    * @param component
    * @param delivery
    */
   public CCLDeliveryInfoDialog(Shell parent, CCLComponent component, CCLDelivery delivery)
   {
      this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, component, delivery);
   }

   /**
    * Shows information about a component in a delivery.
    *
    * @param parent
    * @param style
    * @param component
    * @param delivery
    */
   public CCLDeliveryInfoDialog(Shell parent, int style, CCLComponent component, CCLDelivery delivery)
   {
      super(parent, style);
      mShell = new Shell(Display.getCurrent());
      mShell.setText("Info Component");
      mShell.setImage(new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
      mComponent = component;
      mDelivery = delivery;
   }

   /**
    * Opens the dialog window.
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
    * Creates a TabFolder and fills it with tabs for each importer. The 
    * importers define how their own tab should look like and what information 
    * should be displayed in it.
    */
   private void createContent()
   {
      mShell.setLayout(new GridLayout(1, true));

      TabFolder tabFolder = new TabFolder(mShell, SWT.TOP);

      for (ACLImportStrategy importer : SCLProject.getInstance().getImportStrategiesInViewOrder())
      {
         // create a new tab item for each importer...
         TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
         tabItem.setText(importer.getName());

         Composite composite = new Composite(tabItem.getParent(), SWT.NONE);
         tabItem.setControl(composite);

         // ... and let the importer fill it with information
         importer.getDetailedInformation(mComponent, mDelivery).fillInfoBox(composite);
      }

      // button group
      Composite buttonGroup = new Composite(mShell, SWT.NONE);
      buttonGroup.setLayout(new GridLayout(BUTTON_GROUP_GRID_COUNT, true));

      GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
      gridData.horizontalAlignment = SWT.RIGHT;
      gridData.horizontalSpan = BUTTON_GROUP_GRID_COUNT;

      buttonGroup.setLayoutData(gridData);

      Button btnOk = new Button(buttonGroup, SWT.NONE);
      btnOk.setText("   OK   ");
      btnOk.addSelectionListener(new CCLOkButtonAdapter());

      Button btnCancel = new Button(buttonGroup, SWT.NONE);
      btnCancel.setText("Cancel");
      btnCancel.addSelectionListener(new CCLCancelButtonAdapter());
   }

   /**
    * Lets all importers save their changes and then closes the dialog window.
    */
   private class CCLOkButtonAdapter extends SelectionAdapter
   {
      @Override
      public void widgetSelected(SelectionEvent event)
      {
         CCLObservableCollection<CCLDelivery> deliveries = SCLProject.getInstance().getDeliveries();
         for (ACLImportStrategy importer : SCLPluginLoader.getInstance().getImportStrategiesMap().values())
         {
            try
            {
               importer.getDetailedInformation(mComponent, mDelivery).saveInfoBox(deliveries);
            }
            catch (CCLInternalException | ParseException | RuntimeException e)
            {
               MessageBox errorMessageBox = new MessageBox(mShell, SWT.ICON_ERROR | SWT.OK);
               errorMessageBox.setText("Error");
               errorMessageBox.setMessage("Could not save changes in " + importer.getName() + ".\n" + e.getMessage());
               errorMessageBox.open();
               LOGGER.info("{}: Couldn't save changes", getClass().getCanonicalName(), e);
            }
         }

         mShell.close();
      }
   }

   /**
    * Closes the dialog window without saving any changes.
    */
   private class CCLCancelButtonAdapter extends SelectionAdapter
   {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
         mShell.close();
      }
   }
}
