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
 * @file CCLDeliveryVersion.java
 *
 * @brief Version detailed information.
 */

package de.comlet.releasefab.version;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLDetailedInformation;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLObservableCollection;
import de.comlet.releasefab.library.model.SCLProjectHelper;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import java.text.ParseException;
import java.util.Date;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Detailed delivery information about the version of a component.
 */
public class CCLDetailedVersion extends ACLDetailedInformation
{
   private static final String STRING_CHILD = "string";
   private static final int COMPOSITE_GRID_LAYOUT_ROWS = 2;
   private static final int DEFAULT_WIDTH_HINT = 200;

   private Text mTxtDeliveryDate;
   private Text mTxtIntegrator;
   private Text mTxtComponentVersion;

   /**
    * Detailed delivery information about the version of a component.
    *
    * @param importerName
    * @param component
    * @param delivery
    */
   public CCLDetailedVersion(String importerName, CCLComponent component, CCLDelivery delivery)
   {
      super(importerName, component, delivery);
   }

   /**
    * Initialize user interface and provide detailed information as a table.
    */
   @Override
   public void fillInfoBox(Object obj)
   {
      Composite composite = (Composite) obj;
      composite.setLayout(new GridLayout(COMPOSITE_GRID_LAYOUT_ROWS, false));

      // set width for all text fields
      GridData gridData = new GridData();
      gridData.widthHint = DEFAULT_WIDTH_HINT;

      // delivery name
      Label lblDeliveryName = new Label(composite, SWT.NONE);
      lblDeliveryName.setText("Delivery name:");

      Text txtDeliveryName = new Text(composite, SWT.READ_ONLY);
      txtDeliveryName.setText(mDelivery.getName());

      // date of delivery creation
      Label lblDeliveryDate = new Label(composite, SWT.NONE);
      lblDeliveryDate.setText("Delivery date:");

      mTxtDeliveryDate = new Text(composite, SWT.BORDER);
      mTxtDeliveryDate.setText(SCLProjectHelper.getDateFormatter().format(mDelivery.getCreated()));
      mTxtDeliveryDate.setLayoutData(gridData);

      // integrator
      Label lblIntegrator = new Label(composite, SWT.NONE);
      lblIntegrator.setText("Integrator name:");

      mTxtIntegrator = new Text(composite, SWT.BORDER);
      mTxtIntegrator.setText(mDelivery.getIntegrator());
      mTxtIntegrator.setLayoutData(gridData);

      // component name
      Label lblComponentName = new Label(composite, SWT.NONE);
      lblComponentName.setText("Component name:");

      Text txtComponentName = new Text(composite, SWT.READ_ONLY);
      txtComponentName.setText(mComponent.getName());

      // component version
      Label lblComponentVersion = new Label(composite, SWT.NONE);
      lblComponentVersion.setText("Component version:");

      mTxtComponentVersion = new Text(composite, SWT.BORDER);
      ACLDeliveryInformation info = mComponent.getDeliveryInformation(mDelivery.getName() + mImporterName);
      String componentVersion = info.getInformation().getChildText(STRING_CHILD);
      componentVersion = (null != componentVersion) ? componentVersion : "";
      mTxtComponentVersion.setText(componentVersion);
      mTxtComponentVersion.setLayoutData(gridData);
   }

   /**
    * Store modifications.
    */
   @Override
   public void saveInfoBox(CCLObservableCollection<CCLDelivery> deliveries) throws CCLInternalException, ParseException
   {
      // date of delivery creation
      String strDeliveryDate = mTxtDeliveryDate.getText().trim();
      Date deliveryDate;

      deliveryDate = SCLProjectHelper.getDateFormatter().parse(strDeliveryDate);

      mDelivery.setCreated(deliveryDate);

      // integrator name
      String integrator = mTxtIntegrator.getText().trim();
      mDelivery.setIntegrator(integrator);

      // component version
      String componentVersion = mTxtComponentVersion.getText().trim();

      ACLDeliveryInformation info = mComponent.getDeliveryInformation(mDelivery.getName() + mImporterName);
      String oldComponentVersion = info.getInformation().getChildText(STRING_CHILD);

      // save only if there are changes, because we don't want to inform the
      // observers if there are no changes
      if (!componentVersion.equals(oldComponentVersion))
      {
         info.getInformation().setContent(SCLXMLUtil.createElement(STRING_CHILD, componentVersion));

         SCLProjectHelper.markDeliveryInformationIfNew(deliveries, mComponent, mDelivery, mImporterName, info);

         mComponent.setDeliveryInformation(mDelivery.getName() + mImporterName, info);
      }
   }
}
