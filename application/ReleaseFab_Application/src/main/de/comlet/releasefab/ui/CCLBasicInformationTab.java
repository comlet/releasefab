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
 * @file CCLBasicInformationTab.java
 *
 * @brief Basic tab for imported information.
 */

package de.comlet.releasefab.ui;

import de.comlet.releasefab.SCLPluginLoader;
import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.api.plugin.ACLImportStrategy.PresentationType;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.ui.commands.ACLCommand;
import de.comlet.releasefab.ui.dialogs.CCLDeliveryInfoDialog;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Tab which displays information imported by a specific strategy defined in
 * CCLBasicImportTab. The first column of this tab shows the component tree.
 */
public class CCLBasicInformationTab extends ACLBasicTab
{
   private static final int COLUMN_WIDTH_DELIVERY = 200;
   
   private static final int NON_TRANSPARENT_ALPHA_VALUE = 255;
   
   /**
    * Map of all images that have already been combined.
    */
   private Map<Integer, Image> mCombinedIcons = new HashMap<Integer, Image>();

   /**
    * Creates an initial information tab that is updated whenever a new delivery
    * is added or an existing delivery is removed. It's also updated whenever
    * something in the component tree changes, e.g. when a new component is
    * added.
    *
    * @param tabItem parent TabItem
    */
   public CCLBasicInformationTab(TabItem tabItem)
   {
      super(tabItem);

      // create content
      createContent();
   }

   /**
    * Update view whenever a new delivery is added or an existing delivery is
    * removed.<br>
    * Update view whenever something in the component tree changes, e.g. when a
    * new component is added.
    */
   @Override
   public void registerObservers()
   {
      SCLProject.getInstance().getDeliveries().addPropertyChangeListener(this);
      SCLProject.getComponentRoot().addPropertyChangeListener(this);
   }

   /**
    * Stop updating view when changes occur.
    */
   @Override
   public void unregisterObservers()
   {
      SCLProject.getInstance().getDeliveries().removePropertyChangeListener(this);
      SCLProject.getComponentRoot().removePropertyChangeListener(this);
   }

   /**
    * Creates a column for each delivery.
    */
   @Override
   protected void createCustomColumns()
   {
      Collection<CCLDelivery> deliveries = SCLProject.getInstance().getDeliveries();
      for (final CCLDelivery delivery : deliveries)
      {
         createTreeColumn(mTree, delivery.getName(), COLUMN_WIDTH_DELIVERY);
      }
   }

   /**
    * Opens an information dialog with detailed information about the selected component
    * in the selected delivery.
    * 
    * @param treeItem the selected TreeItem
    * @param treeColumn the selected TreeColumn
    */
   @Override
   protected void onCustomColumnDoubleClick(TreeItem treeItem, TreeColumn treeColumn)
   {
      CCLDelivery delivery = SCLProject.getInstance().getDeliveryByName(treeColumn.getText());
      CCLComponent component = (CCLComponent) treeItem.getData();

      CCLDeliveryInfoDialog infoDialog = new CCLDeliveryInfoDialog(mTree.getShell(), component, delivery);
      infoDialog.open();
   }

   /**
    * Opens a context menu that allows the user to open an info dialog with
    * detailed information about the selected component in the selected delivery.
    * 
    * @param treeItem the selected TreeItem
    * @param treeColumn the selected TreeColumn
    */
   @Override
   protected void onCustomColumnRightClick(TreeItem treeItem, TreeColumn treeColumn)
   {
      final CCLDelivery delivery = SCLProject.getInstance().getDeliveryByName(treeColumn.getText());
      final CCLComponent component = (CCLComponent) treeItem.getData();

      resetContextMenu(mContextMenu);

      createContextMenuItem(mContextMenu, new ACLCommand("Show details")
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            CCLDeliveryInfoDialog infoDialog = new CCLDeliveryInfoDialog(mTree.getShell(), component, delivery);
            infoDialog.open();
         }
      });
   }

   /**
    * Creates a tree item for the given component as a child of the given
    * parent.<br>
    * It then adds version information for each delivery to the created tree
    * item and marks new information.
    * 
    * @param parentTree the parent TreeItem
    * @param component the component to be included in the TreeItem
    */
   @Override
   protected TreeItem createTreeItem(Object parentTree, CCLComponent component)
   {
      // add new TreeItem to parent tree or tree item
      TreeItem treeItem = super.createTreeItem(parentTree, component);

      // add version information for each delivery to the created tree item
      int i = 1;
      for (CCLDelivery delivery : SCLProject.getInstance().getDeliveries())
      {
         List<ACLImportStrategy> importerIcons = new ArrayList<ACLImportStrategy>();
         int possibleIconsCount = 0;
         for (ACLImportStrategy importer : SCLPluginLoader.getInstance().getImportStrategiesMap().values())
         {
            // this importer doesn't want to be displayed in an overview
            if (PresentationType.NONE == importer.getPreferredPresentationType())
            {
               continue;
            }

            ACLDeliveryInformation deliveryInfo = component.getDeliveryInformation(delivery.getName() + importer.getName());
            
            if (null != deliveryInfo)
            {
               // this importer wants to display its information as a label in
               // an overview
               if (PresentationType.LABEL == importer.getPreferredPresentationType())
               {
                  displayAsLabel(treeItem, i, deliveryInfo);
               }

               // this importer wants to display an icon in an overview
               // indicating, that there is some new information to be viewed in
               // a detail view
               else if (PresentationType.ICON == importer.getPreferredPresentationType())
               {
                  possibleIconsCount = displayAsIcon(importerIcons, possibleIconsCount, importer, deliveryInfo);
               }
            }
         }

         // load and combine all necessary icons...
         Image icon = loadIcons(importerIcons, possibleIconsCount);

         if (null != icon)
         {
            // ... and put the resulting image into the table cell.
            treeItem.setImage(i, icon);
         }

         i++;
      }

      return treeItem;
   }

   /**
    * Adds the importer to the list importerIcons and increments the icons count.
    * 
    * @param importerIcons
    * @param possibleIconsCount
    * @param importer
    * @param deliveryInfo
    * @return
    */
   private int displayAsIcon(List<ACLImportStrategy> importerIcons, int possibleIconsCount, ACLImportStrategy importer,
         ACLDeliveryInformation deliveryInfo)
   {
      if (deliveryInfo.isNew())
      {
         importerIcons.add(importer);
      }
      
      int newPossibleIconsCount = possibleIconsCount + 1;

      return newPossibleIconsCount;
   }

   /**
    * Displays information from deliveryInfo in the treeItem using the correct font style.
    * 
    * @param treeItem
    * @param i
    * @param deliveryInfo
    */
   private void displayAsLabel(TreeItem treeItem, int i, ACLDeliveryInformation deliveryInfo)
   {
      String info = deliveryInfo.getInformation().getChildText("string");
      info = (null == info) ? "" : info;
      String previousText = treeItem.getText(i);
      treeItem.setText(i, (null != previousText && !previousText.isEmpty()) ? previousText + ", " + info : info);

      if (deliveryInfo.isNew())
      {
         changeFontStyle(treeItem, i, SWT.BOLD);
      }
      else
      {
         changeFontStyle(treeItem, i, SWT.NORMAL);
      }
   }

   /**
    * Loads and combines all necessary icons for the given importers.
    *
    * @param importers list of all importers whose icons should be loaded and
    *           combined into one bigger icon
    * @param possibleIconsCount number of all plugins that prefer to display an
    *           icon. To ensure that all resulting icons are equal in size, this
    *           method adds empty placeholders for icons not included in the
    *           given list of importers. This is important, because SWT resizes
    *           images in table cells to the size of the first rendered image.
    * @return combined icon or null if the list is empty
    */
   private Image loadIcons(List<ACLImportStrategy> importers, int possibleIconsCount)
   {
      if (1 > importers.size())
      {
         return null;
      }

      // Is the current combination a cached one?
      Image combinedIcon = mCombinedIcons.get(importers.hashCode());
      if (null == combinedIcon)
      {
         List<Image> icons = new ArrayList<Image>();

         // load the icons
         for (ACLImportStrategy importer : importers)
         {
            InputStream imageStream = importer.getImporterImage();
            
            if (null != imageStream)
            {
               Image icon = new Image(mTree.getDisplay(), imageStream);

               if (null != icon)
               {
                  icons.add(icon);
               }
            }
         }

         // we need to replace icons that we don't want to display in this
         // cell with an empty placeholder, because SWT resizes images in
         // table cells to the size of the first rendered image, so we
         // want them all to be equal in size
         if (icons.size() < possibleIconsCount)
         {
            InputStream imageStream = getClass().getResourceAsStream("images/empty.png");
            Image placeholder = new Image(mTree.getDisplay(), imageStream);
            for (int j = icons.size(); j < possibleIconsCount; j++)
            {
               icons.add(placeholder);
            }
         }

         // combine all icons into one big icon
         combinedIcon = combineIcons(icons);

         // put the resulting image into the map, so we don't need to do
         // this a second time for the exact same combination of icons
         mCombinedIcons.put(importers.hashCode(), combinedIcon);
      }

      return combinedIcon;
   }

   /**
    * Combines all given icons into one bigger icon growing at the right side of
    * the image.
    *
    * @param icons images that should be combined into one bigger image
    * @return combined image or null if the list is empty
    */
   private Image combineIcons(List<Image> icons)
   {
      if (1 > icons.size())
      {
         return null;
      }

      Image firstIcon = icons.get(0);

      int height = firstIcon.getImageData().height;
      int width = firstIcon.getImageData().width;

      for (int i = 1; i < icons.size(); i++)
      {
         width += icons.get(i).getImageData().width;
      }

      ImageData imageData = new ImageData(width, height, firstIcon.getImageData().depth, firstIcon.getImageData().palette);

      int i = 0;
      for (Image icon : icons)
      {
         int iconWidth = icon.getImageData().width;
         int iconHeight = icon.getImageData().height;

         int[] linePixels = new int[iconWidth];
         byte[] lineAlphas = new byte[iconWidth];

         // iterate over all image lines
         for (int y = 0; y < iconHeight; y++)
         {
            // get pixels and alpha values of the current line
            icon.getImageData().getPixels(0, y, iconWidth, linePixels, 0);
            icon.getImageData().getAlphas(0, y, iconWidth, lineAlphas, 0);

            // iterate over all pixels in the current line
            for (int x = 0; x < linePixels.length; x++)
            {
               // set pixels and alpha values in the destination image
               imageData.setPixel(x + i, y, linePixels[x]);
               imageData.setAlpha(x + i, y, (lineAlphas[x] > -1) ? lineAlphas[x] : NON_TRANSPARENT_ALPHA_VALUE);
            }
         }

         i += iconWidth;
      }

      // create image
      Image image = new Image(firstIcon.getDevice(), imageData);

      return image;
   }
}
