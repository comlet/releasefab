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
 * @file CCLDetailedImportantInformation.java
 *
 * @brief Detailed information of Important Information.
 */

package de.comlet.releasefab.importantinformation;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLDetailedInformation;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLObservableCollection;
import de.comlet.releasefab.library.model.SCLProjectHelper;
import de.comlet.releasefab.library.xml.SCLXMLUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * User interface. List all important information of a component and allow
 * editing.
 */
public class CCLDetailedImportantInformation extends ACLDetailedInformation
{
   private static final String CHILD_STRING = "string";
   private Text mTextbox;

   /**
    * Constructor. Initialize information about a component.
    *
    * @param importerName
    * @param component
    * @param delivery
    */
   public CCLDetailedImportantInformation(String importerName, CCLComponent component, CCLDelivery delivery)
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
      composite.setLayout(new FillLayout());

      ACLDeliveryInformation deliveryInfo = mComponent.getDeliveryInformation(mDelivery.getName() + mImporterName);
      String value = deliveryInfo.getInformation().getChildText(CHILD_STRING);
      value = (null != value) ? value : "";

      mTextbox = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP);
      mTextbox.setText(value);
   }

   /**
    * Store modifications.
    */
   @Override
   public void saveInfoBox(CCLObservableCollection<CCLDelivery> deliveries) throws CCLInternalException
   {
      String text = mTextbox.getText();

      ACLDeliveryInformation deliveryInfo = mComponent.getDeliveryInformation(mDelivery.getName() + mImporterName);
      String oldText = deliveryInfo.getInformation().getChildText(CHILD_STRING);

      // save only if there are changes, because we don't want to inform the
      // observers if there are no changes
      if (!text.equals(oldText))
      {
         deliveryInfo.getInformation().setContent(SCLXMLUtil.createElement(CHILD_STRING, text));

         SCLProjectHelper.markDeliveryInformationIfNew(deliveries, mComponent, mDelivery, mImporterName, deliveryInfo);

         mComponent.setDeliveryInformation(mDelivery.getName() + mImporterName, deliveryInfo);
      }
   }
}