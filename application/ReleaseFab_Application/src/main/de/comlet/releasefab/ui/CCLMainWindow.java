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
 * @file CCLMainWindow.java
 *
 * @brief Main class of the GUI.
 */

package de.comlet.releasefab.ui;

import de.comlet.releasefab.CCLAssemblyInfo;
import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.ui.commands.ACLCommand;
import de.comlet.releasefab.ui.commands.CCLCommandAbout;
import de.comlet.releasefab.ui.commands.CCLCommandDeliveryAdd;
import de.comlet.releasefab.ui.commands.CCLCommandDeliveryRemove;
import de.comlet.releasefab.ui.commands.CCLCommandExit;
import de.comlet.releasefab.ui.commands.CCLCommandExportDelivery;
import de.comlet.releasefab.ui.commands.CCLCommandExportDocbook;
import de.comlet.releasefab.ui.commands.CCLCommandImportDelivery;
import de.comlet.releasefab.ui.commands.CCLCommandNew;
import de.comlet.releasefab.ui.commands.CCLCommandOpen;
import de.comlet.releasefab.ui.commands.CCLCommandOptions;
import de.comlet.releasefab.ui.commands.CCLCommandSave;
import de.comlet.releasefab.ui.commands.CCLCommandSaveAs;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main menu and ToolBar are defined in this class. The rest of the 
 * window consists of a TabFolder. The TabFolder itself contains two 
 * different kinds of tabs. One represents the information of every 
 * component in each delivery. The other one is responsible for the 
 * strategy used to receive the information. The first column always 
 * shows the component tree.
 */
public final class CCLMainWindow
{
   /** Initialize logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLMainWindow.class);

   private Shell mShell;
   private TabFolder mTabFolder;

   private Set<ACLBasicTab> mTabs = new HashSet<>();

   /**
    * Creates the initial main window with the main menu and a TabFolder.
    */
   public CCLMainWindow()
   {
      Display display = new Display();
      mShell = new Shell(display);
      GridLayout layout = new GridLayout(1, false);
      mShell.setLayout(layout);
      mShell.setText(CCLAssemblyInfo.getProductName());
      mShell.setImage(new Image(display, getClass().getResourceAsStream("images/releasefab.ico")));
      mShell.addListener(SWT.Close, new CCLCommandExit());

      try
      {
         Set<String> missingPlugins = SCLProject.getInstance().loadStartupFile();
         
         if (!missingPlugins.isEmpty())
         {
            MessageBox errorMessageBox = new MessageBox(mShell, SWT.ICON_WARNING | SWT.OK);
            StringBuilder sb = new StringBuilder("The following plugins are referenced in the startup file but do not exist in the plugin folder: \n");
            for (String pluginName : missingPlugins)
            {
               sb.append("- " + pluginName + "\n");
            }
            sb.append("\nThe information from these plugins will not be included in any export!");
            errorMessageBox.setMessage(sb.toString());
            errorMessageBox.setText("Missing Plugins");
            errorMessageBox.open();
         }
         
         setWindowTitle(SCLProject.getOpenFileName());
      }
      catch (CCLInternalException | JDOMException | IOException | ParseException e)
      {
         MessageBox errorMessageBox = new MessageBox(mShell, SWT.ICON_ERROR | SWT.OK);
         errorMessageBox.setMessage(null != e.getMessage() ? e.getMessage() : "Unknown");
         errorMessageBox.setText("Couldn't load startup file");
         errorMessageBox.open();
         LOGGER.info("Couldn't load startup file.", e);
      }

      // create main menu
      createMainMenu();

      // create toolbar
      ToolBar toolbar = new ToolBar(mShell, SWT.NONE);
      createToolbarItem(toolbar, new CCLCommandNew(this));
      createToolbarItem(toolbar, new CCLCommandOpen(this));
      createToolbarItem(toolbar, new CCLCommandSave(this));

      // create tab folder in it's own composite and let it grab all the excess
      // space
      Composite composite = new Composite(mShell, SWT.NONE);
      composite.setLayout(new FillLayout());
      GridData gridData = new GridData();
      gridData.grabExcessVerticalSpace = true;
      gridData.grabExcessHorizontalSpace = true;
      gridData.verticalAlignment = GridData.FILL;
      composite.setLayoutData(gridData);

      mTabFolder = new TabFolder(composite, SWT.BOTTOM);
      fillTabFolder();

      mShell.open();
      while (!mShell.isDisposed())
      {
         if (!display.readAndDispatch())
         {
            display.sleep();
         }
      }

      display.dispose();
   }

   /**
    * Gets the Logger.
    *
    * @return The Logger.
    */
   public static Logger getLogger()
   {
      return LOGGER;
   }

   /**
    * Sets the title of the main window.
    *
    * @param title New title of the main window.
    */
   public void setWindowTitle(String title)
   {
      mShell.setText(CCLAssemblyInfo.getProductName() + " " + title);
   }

   /**
    * Refreshes the content of the tabs in the tab folder.
    */
   public void refresh()
   {
      for (ACLBasicTab tab : mTabs)
      {
         tab.propertyChange(null);
      }
   }

   /**
    * Registers the observers of each tab.
    */
   public void startListeningForChanges()
   {
      for (ACLBasicTab tab : mTabs)
      {
         tab.registerObservers();
      }
   }

   /**
    * Unregisters all observers of each tab. This allows us to do things, that
    * take some time, without updating the GUI multiple times.
    */
   public void stopListeningForChanges()
   {
      for (ACLBasicTab tab : mTabs)
      {
         tab.unregisterObservers();
      }
   }

   /**
    * Fills the TabFolder with TabItems for the version info and for each of
    * the import strategies.
    */
   public void fillTabFolder()
   {
      // dispose old tabs
      if (0 < mTabFolder.getItemCount())
      {
         for (TabItem tabItem : mTabFolder.getItems())
         {
            tabItem.dispose();
         }
      }

      // clear list of old tabs
      if (!mTabs.isEmpty())
      {
         mTabs.clear();
      }

      // create version info tab
      TabItem tabItemVersionInfo = new TabItem(mTabFolder, SWT.NONE);
      tabItemVersionInfo.setText("Info: Version");

      Map<String, Boolean> enabledMap = SCLProject.getInstance().getEnabledStatesInOrderList("VIEW_ORDER");

      ACLBasicTab informationTab = new CCLBasicInformationTab(tabItemVersionInfo);
      mTabs.add(informationTab);

      // create a tab for each of the import strategies
      for (ACLImportStrategy strat : SCLProject.getInstance().getImportStrategiesInViewOrder())
      {
         Boolean enabled = enabledMap.get((strat.getName().replace(' ', '_')).toUpperCase());
         if ((enabled == null) || enabled.booleanValue())
         {
            TabItem tabItem = new TabItem(mTabFolder, SWT.NONE);
            tabItem.setText("Import: " + strat.getName());

            ACLBasicTab importTab = new CCLBasicImportTab(tabItem, strat);
            mTabs.add(importTab);
         }
      }
   }

   /**
    * Creates the main menu.
    */
   private void createMainMenu()
   {
      Menu mainMenu = new Menu(mShell, SWT.BAR);
      mShell.setMenuBar(mainMenu);

      // File -> ...
      Menu fileMenu = createSubMenu(mainMenu, "File");
      createMenuItem(fileMenu, new CCLCommandNew(this));
      createMenuItem(fileMenu, new CCLCommandOpen(this));
      createMenuItem(fileMenu, new CCLCommandSave(this));
      createMenuItem(fileMenu, new CCLCommandSaveAs(this));

      new MenuItem(fileMenu, SWT.SEPARATOR);

      // File -> Export (submenu)
      Menu exportMenu = createSubMenu(fileMenu, "Export");
      createMenuItem(exportMenu, new CCLCommandExportDelivery());
      createMenuItem(exportMenu, new CCLCommandExportDocbook());

      // File -> Import (submenu)
      Menu importMenu = createSubMenu(fileMenu, "Import");
      createMenuItem(importMenu, new CCLCommandImportDelivery(this));

      new MenuItem(fileMenu, SWT.SEPARATOR);

      // File -> Exit
      createMenuItem(fileMenu, new CCLCommandExit());

      // Action -> ...
      Menu actionMenu = createSubMenu(mainMenu, "Action");
      createMenuItem(actionMenu, new CCLCommandDeliveryAdd(this));
      createMenuItem(actionMenu, new CCLCommandDeliveryRemove());

      new MenuItem(actionMenu, SWT.SEPARATOR);

      createMenuItem(actionMenu, new CCLCommandOptions());

      // Help -> ...
      Menu helpMenu = createSubMenu(mainMenu, "Help");
      createMenuItem(helpMenu, new CCLCommandAbout());
   }

   /**
    * Adds a submenu with the given title to parentMenu.
    *
    * @param parentMenu
    * @param title
    * @return submenu
    */
   private Menu createSubMenu(Menu parentMenu, String title)
   {
      Menu subMenu = new Menu(mShell, SWT.DROP_DOWN);

      MenuItem subMenuHeader = new MenuItem(parentMenu, SWT.CASCADE);
      subMenuHeader.setText(title);
      subMenuHeader.setMenu(subMenu);

      return subMenu;
   }

   private void createMenuItem(Menu parentMenu, ACLCommand command)
   {
      createMenuItem(parentMenu, command.getTitle(), command.getImagePath(), command);
   }

   /**
    * Adds a menu item with the given title, imagePath 
    * and a selectionListener to parentMenu.
    *
    * @param parentMenu
    * @param title
    * @param imagePath
    * @param selectionListener
    */
   private void createMenuItem(Menu parentMenu, String title, String imagePath, SelectionListener selectionListener)
   {
      MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE);
      menuItem.setText(title);

      if (null != imagePath && !imagePath.isEmpty())
      {
         InputStream imageStream = this.getClass().getResourceAsStream(imagePath);

         if (null != imageStream)
         {
            menuItem.setImage(new Image(mShell.getDisplay(), imageStream));
         }
      }

      if (null != selectionListener)
      {
         menuItem.addSelectionListener(selectionListener);
      }
   }

   /**
    * Adds an item to the given ToolBar.
    *
    * @param parentToolbar
    * @param command
    */
   private void createToolbarItem(ToolBar parentToolbar, ACLCommand command)
   {
      ToolItem toolItem = new ToolItem(parentToolbar, SWT.PUSH);
      toolItem.setToolTipText(command.getTitle());

      if (null != command.getImagePath() && !command.getImagePath().isEmpty())
      {
         InputStream imageStream = this.getClass().getResourceAsStream(command.getImagePath());

         if (null != imageStream)
         {
            toolItem.setImage(new Image(mShell.getDisplay(), imageStream));
         }
      }

      toolItem.addSelectionListener(command);
   }
}
