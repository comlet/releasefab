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
 * @file CCLVisitorPatternTestHelper.java
 *
 * @brief Test visitor pattern helper.
 */

package de.comlet.releasefab.test.util;

import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLTreeVisitor;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import java.util.List;
import org.jdom2.Element;

/**
 * Wrapper class for multiple Visitor Definitions. These Visitor Definitions
 * must be identical to the ones used in SCLProject in the addDocbookSection
 * method. They are declared within the method and can therefore not be used
 * from the tests.
 * 
 * @author JaFernau
 *
 */
public class CCLVisitorPatternTestHelper
{
   /**
    * Declares whether or not the export is for a customer to the
    * Visitor.
    */
   private Boolean mForCustomer;

   /**
    * The XML-Element that contains the template to be filled with content.
    */
   private Element mSection;

   /**
    * The name of the importer under test.
    */
   private String mImporterName;

   /**
    * The name of the oldest delivery under test - lower bound.
    */
   private CCLDelivery mOldestDelivery;

   /**
    * Standard constructor.
    * 
    * @param forCustomer Provides whether or not the export is for a customer
    * @param section The XML-Element that contains the template to be filled
    * with content
    * @param importerName The name of the importer under test
    * @param oldestDeliveryName The name of the oldest delivery under test -
    * lower bound
    */
   public CCLVisitorPatternTestHelper(Boolean forCustomer, Element section, String importerName,
         CCLDelivery oldestDelivery)
   {
      mForCustomer = forCustomer;
      mSection = section;
      mImporterName = importerName;
      mOldestDelivery = oldestDelivery;
   }

   // visitor definition
   public class VisitorEmptySection extends ACLTreeVisitor<Boolean, CCLDelivery>
   {

      @Override
      public Boolean doIt(CCLComponent component, CCLDelivery delivery)
      {
         // ignore non customer relevant information in check
         // true means it is empty
         if (null != mForCustomer && mForCustomer.booleanValue() && !component.getIsCustomerRelevant())
         {
            return true;
         }

         // check if this components delivery information is empty
         ACLDeliveryInformation info = component.getDeliveryInformation(delivery.getName() + mImporterName);
         return info.isInfoNullOrEmpty();
      }

      @Override
      public Boolean visit(CCLComponent component, CCLDelivery target, boolean quickReturn)
      {
         boolean res = true;

         List<CCLComponent> subComponentList = component.getSubComponents();

         for (int i = 0; i < component.getSubComponents().size() && res; i++)
         {
            CCLComponent subComponent = subComponentList.get(i);

            res = doIt(subComponent, target);

            // cancel search on first hit
            if (res && subComponent.hasSubComponents())
            {
               res = subComponent.accept(this, target, true);
            }
         }
         return res;
      }
   } // end of visitor definition

   // visitor definition
   public class VisitorFillSection extends ACLTreeVisitor<Boolean, CCLDelivery>
   {

      @Override
      public Boolean doIt(CCLComponent component, CCLDelivery delivery)
      {
         if (!component.getDeliveryInformation().isEmpty())
         {
            // don't export any information which customer shouldn't // see
            if (null != mForCustomer && mForCustomer.booleanValue() && !component.getIsCustomerRelevant())
            {
               return false;
            }

            ACLDeliveryInformation info = component.getDeliveryInformation(delivery.getName() + mImporterName);

            // we pass an additional delivery (para oldestDelivery)
            // in case we have to compare e.g. Version info
            info.addDocbookSection(mSection, component, mOldestDelivery,
                  mForCustomer);
         }

         return true;
      }
   } // end of visitor definition
}
