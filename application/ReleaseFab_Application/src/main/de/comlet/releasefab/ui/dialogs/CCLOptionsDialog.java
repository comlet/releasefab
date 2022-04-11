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
 * @file CCLOptionsDialog.java
 *
 * @brief Options dialog.
 */

package de.comlet.releasefab.ui.dialogs;

import de.comlet.releasefab.SCLPluginLoader;
import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.api.plugin.ICLPluginSetting;
import de.comlet.releasefab.api.plugin.ICLPluginWithSettings;
import de.comlet.releasefab.library.exception.CCLInternalException;
import de.comlet.releasefab.library.model.CCLTuple;
import de.comlet.releasefab.library.settings.SCLSettings;
import de.comlet.releasefab.library.settings.SCLSettings.ECLSettingsType;
import de.comlet.releasefab.ui.CCLMainWindow;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

/**
 * Dialog that allows the user to edit project and user specific settings.
 */
public class CCLOptionsDialog extends Dialog
{
   private static final int GRIDLAYOUT_ONECOLUM = 1;
   private static final int GRIDLAYOUT_TWOCOLUMS = 2;
   private static final int GRIDDATA_HORIZONTALSPAN = 2;
   private static final int GRIDDATA_VERTICALSPAN = 2;
   
   private static final String ERROR = "Error";
   private static final String EXPORT_ORDER = "EXPORT_ORDER";
   private static final String VIEW_ORDER = "VIEW_ORDER";
   private static final Logger LOGGER = CCLMainWindow.getLogger();

   private Shell mShell;

   /** List of all TextBoxes for project specific settings. */
   private List<Text> mProjectTextboxes = new ArrayList<>();

   /** List of all TextBoxes for user specific settings. */
   private List<Text> mUserTextboxes = new ArrayList<>();

   /** List of all TextBoxes for volatile settins (passwords) */
   private List<Text> mVolatileTextboxes = new ArrayList<>();

   /** Tab for the plugin settings. */
   private List<TabItem> mTiPluginSettings = new ArrayList<>();

   /** Table for export order */
   private Map<String, Table> mSectionTable = new HashMap<>();

   /**
    * Boolean indicating whether plugins have been added.
    */
   private boolean mPluginsAdded;

   /**
    * Boolean indicating whether plugins have been removed.
    */
   private boolean mPluginsRemoved;

   /** Boolean indicating whether the plugin settings tab has been selected. */
   private boolean mHasPluginTabSelected;

   /**
    * Dialog that allows the user to edit project and user specific settings.
    *
    * @param parent
    */
   public CCLOptionsDialog(Shell parent)
   {
      this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
   }

   /**
    * Dialog that allows the user to edit project and user specific settings.
    *
    * @param parent
    * @param style
    */
   public CCLOptionsDialog(Shell parent, int style)
   {
      super(parent, style);

      mShell = new Shell(Display.getCurrent());
      mShell.setImage(new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
      mShell.setText("Options");
   }

   /**
    * Opens the dialog window.
    */
   public void open()
   {
      checkPlugins();

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
    * Compares loaded plugins to plugins configured in project settings.
    * Sets flags if plugins have been added or removed.
    */
   private void checkPlugins()
   {
      List<String> pluginsConfigured = new ArrayList<>(SCLSettings.getStringList(EXPORT_ORDER, EnumSet.of(ECLSettingsType.PROJECT)));
      List<ACLImportStrategy> pluginsLoaded = new ArrayList<ACLImportStrategy>(SCLPluginLoader.getInstance().getImportStrategiesMap().values());

      for (ACLImportStrategy strat : pluginsLoaded)
      {
         String name = strat.getName().replace(' ', '_').toUpperCase();
         boolean success = pluginsConfigured.remove(name);

         // a loaded plugin was not found in the configuration, changes must
         // be saved
         if (!success)
         {
            mPluginsAdded = true;
         }
      }

      // a configured plugin is not loaded, changes to the configuration must
      // be saved
      if (!pluginsConfigured.isEmpty())
      {
         mPluginsRemoved = true;
      }
   }

   /**
    * Fills the dialog with content.
    */
   private void createContent()
   {
      mShell.setLayout(new GridLayout(GRIDLAYOUT_ONECOLUM, true));

      TabFolder mTabFolder = new TabFolder(mShell, SWT.TOP);
      mTabFolder.addSelectionListener(new CCLTabSelectionAdapter());

      // project settings
      createProjectSettings(mTabFolder);

      // user settings
      createUserSettings(mTabFolder);

      // plugin export settings
      mTiPluginSettings.add(createPluginListSettings(mTabFolder,
                                                     "Export settings",
                                                     "Docbook",
                                                     EXPORT_ORDER));

      // plugin view settings
      mTiPluginSettings.add(createPluginListSettings(mTabFolder,
                                                     "View settings",
                                                     "Order",
                                                     VIEW_ORDER));

      // button group
      Composite buttonGroup = new Composite(mShell, SWT.NONE);
      buttonGroup.setLayout(new GridLayout(GRIDLAYOUT_TWOCOLUMS, true));

      GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
      gridData.horizontalAlignment = SWT.RIGHT;
      gridData.horizontalSpan = GRIDDATA_HORIZONTALSPAN;

      buttonGroup.setLayoutData(gridData);

      Button btnOk = new Button(buttonGroup, SWT.NONE);
      btnOk.setText("   OK   ");
      btnOk.addSelectionListener(new CCLOkButtonAdapter());

      Button btnCancel = new Button(buttonGroup, SWT.NONE);
      btnCancel.setText("Cancel");
      btnCancel.addSelectionListener(new CCLCancelButtonAdapter());

   }

   private void createWidgetForSetting(Group group, ICLPluginSetting setting, ECLSettingsType settingsType)
   {
      LOGGER.trace(int.class.getName());
      if (setting.getContentClass().equals(String.class))
      {
         if (setting.getSettingType().contains(ECLSettingsType.VOLATILE))
         {
            createPasswordTextbox(group, setting.getLabel() + ": ", setting.getName());
         }
         else
         {
            createSettingTextbox(group, setting.getLabel() + ": ", setting.getName(), settingsType, String.class.cast(setting.getDefaultValue()));
         }
      }
   }

   /**
    * Creates a group for each plugin that relies on settings and fills them with
    * the required settings for these plugins.
    *
    * @param parent
    * @param settingsType
    *           CCLSettingsType.PROJECT or CCLSettingsType.USER
    */
   private void createPluginSettings(Composite parent, ECLSettingsType settingsType)
   {
      for (ICLPluginWithSettings pluginUsingSettings : SCLPluginLoader.getInstance().getPluginsWithSettings())
      {
         pluginUsingSettings.addPluginSettings(SCLProject.getProjectSettingsPath());
         Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
         group.setText(pluginUsingSettings.getName());
         group.setLayout(new GridLayout(GRIDLAYOUT_TWOCOLUMS, false));
         group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

         List<ICLPluginSetting> settingsList = null;

         if (ECLSettingsType.PROJECT == settingsType)
         {
            settingsList = pluginUsingSettings.getRequiredProjectSettings();
         }
         else if (ECLSettingsType.USER == settingsType)
         {
            settingsList = pluginUsingSettings.getRequiredUserSettings();
         }

         for (ICLPluginSetting setting : settingsList)
         {
            createWidgetForSetting(group, setting, settingsType);
         }
      }
   }

   /**
    * Creates a TextBox with label.
    *
    * @param parent
    * @param labelText
    * @param settingName
    * @param settingsType
    */
   private void createSettingTextbox(Composite parent, String labelText, String settingName, ECLSettingsType settingsType, String defaultValue)
   {
      Label label = new Label(parent, SWT.NONE);
      label.setText(labelText);

      Text textbox = new Text(parent, SWT.BORDER);

      String value = SCLSettings.get(settingName, EnumSet.of(settingsType));
      if (null == value)
      {
         value = defaultValue;
      }

      textbox.setText(value);
      textbox.setData(settingName);
      textbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      if (ECLSettingsType.PROJECT == settingsType)
      {
         mProjectTextboxes.add(textbox);
      }
      else if (ECLSettingsType.USER == settingsType)
      {
         mUserTextboxes.add(textbox);
      }
   }

   /**
    * Create project specific settings in a TabFolder.
    */
   private void createProjectSettings(TabFolder parent)
   {
      TabItem mTiProjectSettings = new TabItem(parent, SWT.NONE);
      mTiProjectSettings.setText("Project settings");

      Composite projectSettingsComposite = new Composite(parent, SWT.NONE);
      projectSettingsComposite.setLayout(new GridLayout(GRIDLAYOUT_ONECOLUM, false));

      mTiProjectSettings.setControl(projectSettingsComposite);

      // show project specific settings for all version control systems
      createPluginSettings(projectSettingsComposite, ECLSettingsType.PROJECT);

      // show other project specific settings
      Group projectSettings = new Group(projectSettingsComposite, SWT.SHADOW_ETCHED_IN);
      projectSettings.setText("Default");
      projectSettings.setLayout(new GridLayout(GRIDLAYOUT_TWOCOLUMS, false));
      projectSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      createSettingTextbox(projectSettings, "Startup file: ", "STARTUP_FILE", ECLSettingsType.PROJECT, "");
   }

   /**
    * Create user specific settings in a TabFolder.
    */
   private void createUserSettings(TabFolder parent)
   {
      TabItem mTiUserSettings = new TabItem(parent, SWT.NONE);
      mTiUserSettings.setText("User settings");

      Composite userSettingsComposite = new Composite(parent, SWT.NONE);
      userSettingsComposite.setLayout(new GridLayout(GRIDLAYOUT_ONECOLUM, true));

      mTiUserSettings.setControl(userSettingsComposite);

      // show user specific settings for all version control systems
      createPluginSettings(userSettingsComposite, ECLSettingsType.USER);

      // show other user specific settings
      Group userSettings = new Group(userSettingsComposite, SWT.SHADOW_ETCHED_IN);
      userSettings.setText("Logger");
      userSettings.setLayout(new GridLayout(GRIDLAYOUT_TWOCOLUMS, false));
      userSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      createSettingTextbox(userSettings, "Logger path: ", "LOGGER_PATH", ECLSettingsType.USER, "./log.txt");
   }

   /**
    * Create plugin specific settings in a TabFolder.
    */
   private TabItem createPluginListSettings(TabFolder parent,
                                            String title,
                                            String pluginGroupName,
                                            String pluginOrder)
   {
      Map<String, Boolean> enabledMap = SCLProject.getInstance().getEnabledStatesInOrderList(pluginOrder);
      TabItem created = new TabItem(parent, SWT.NONE);
      created.setText(title + (mPluginsAdded || mPluginsRemoved ? "*" : ""));

      Composite pluginSettingsComposite = new Composite(parent, SWT.NONE);
      pluginSettingsComposite.setLayout(new GridLayout(GRIDLAYOUT_ONECOLUM, false));

      created.setControl(pluginSettingsComposite);

      Group pluginSettings = new Group(pluginSettingsComposite, SWT.SHADOW_ETCHED_IN);
      pluginSettings.setText(pluginGroupName);
      pluginSettings.setLayout(new GridLayout(GRIDLAYOUT_TWOCOLUMS, false));
      pluginSettings.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

      Table sectionTable = new Table(pluginSettings, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);
      sectionTable.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
      sectionTable.addSelectionListener(new CCLTableSelectionAdapter());

      TableColumn columnName = new TableColumn(sectionTable, SWT.CENTER);
      columnName.setText("Name");
      for (ACLImportStrategy plugin : SCLProject.getInstance().getImportStrategiesInOrder(pluginOrder))
      {
         TableItem tableItem = new TableItem(sectionTable, SWT.NONE);
         tableItem.setText(plugin.getName());
         tableItem.setData(plugin);
         Boolean enabled = enabledMap.get((plugin.getName().replace(' ', '_')).toUpperCase());
         tableItem.setChecked((enabled == null) || enabled.booleanValue());
      }
      mSectionTable.put(pluginOrder, sectionTable);

      columnName.pack();

      // order button group
      Composite orderButtonComposite = new Composite(pluginSettings, SWT.NONE);
      orderButtonComposite.setLayout(new GridLayout(GRIDLAYOUT_ONECOLUM, false));
      orderButtonComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));

      Button upButton = new Button(orderButtonComposite, SWT.PUSH);
      upButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
      upButton.setText("  Up  ");
      upButton.setEnabled(false);
      upButton.addMouseListener(new CCLUpButtonAdapter());

      Button downButton = new Button(orderButtonComposite, SWT.PUSH);
      downButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
      downButton.setText("Down");
      downButton.setEnabled(false);
      downButton.addMouseListener(new CCLDownButtonAdapter());

      if (mPluginsAdded)
      {
         Label changesMessage = new Label(pluginSettings, SWT.NONE);
         changesMessage.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false, GRIDDATA_HORIZONTALSPAN, GRIDDATA_VERTICALSPAN));
         changesMessage.setText("New plugin(s) have been added.");

         FontData fontData = changesMessage.getFont().getFontData()[0];
         Font font = new Font(mShell.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.ITALIC));
         changesMessage.setFont(font);
      }

      if (mPluginsRemoved)
      {
         Label changesMessage = new Label(pluginSettings, SWT.NONE);
         changesMessage.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false, GRIDDATA_HORIZONTALSPAN, GRIDDATA_VERTICALSPAN));
         changesMessage.setText("Plugin(s) have been removed.");

         FontData fontData = changesMessage.getFont().getFontData()[0];
         Font font = new Font(mShell.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.ITALIC));
         changesMessage.setFont(font);
      }

      return created;
   }

   /**
    * Create a password text box. The input will be stored as a volatile setting.
    * Passwords will not be stored in a file.
    */
   private void createPasswordTextbox(Composite parent,
                                      String    labelText,
                                      String    settingName)
   {
      Label label = new Label(parent, SWT.NONE);
      label.setText(labelText);

      Text textBox = new Text(parent, SWT.PASSWORD | SWT.BORDER);

      String value = SCLSettings.get(settingName, EnumSet.of(ECLSettingsType.VOLATILE));
      if (null == value)
      {
         value = "";
      }

      textBox.setText(value);
      textBox.setData(settingName);
      textBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      mVolatileTextboxes.add(textBox);
   }

   private void enableButtonsBySelectedIndex(int index, Button upButton, Button downButton, Table sectionTable)
   {
      if (null != upButton)
      {
         upButton.setEnabled(0 != index);
      }

      if (null != downButton)
      {
         downButton.setEnabled(sectionTable != null &&  index != sectionTable.getItemCount() - 1);
      }
   }

   /**
    * Saves all settings to the appropriate files and then closes the dialog
    * window.
    */
   private class CCLOkButtonAdapter extends SelectionAdapter
   {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
         // save project specific settings
         saveChanges(mProjectTextboxes, ECLSettingsType.PROJECT, SCLProject.getProjectSettingsPath());

         // save user specific settings
         saveChanges(mUserTextboxes, ECLSettingsType.USER, SCLProject.getUserSettingsPath());

         // save password settings
         saveVolatileChanges(mVolatileTextboxes);

         // save plugin settings only if the tab has been selected
         if (mHasPluginTabSelected)
         {
            savePluginChanges(EXPORT_ORDER);
            savePluginChanges(VIEW_ORDER);
         }

         mShell.close();
      }

      /**
       * Searches for changed values in the given list of TextBoxes and tries to
       * save changes to the file containing the setting. Informs the user with
       * an error message if something goes wrong.
       *
       * @param textboxes list of TextBoxes to check for changes
       * @param settingsType
       * @param settingsPath path to the correspondent settings file
       */
      private void saveChanges(List<Text> textboxes, ECLSettingsType settingsType, String settingsPath)
      {
         try
         {
            boolean hasChanges = false;

            for (Text textbox : textboxes)
            {
               String settingName = ((String) textbox.getData()).trim();
               String settingValue = textbox.getText().trim();
               String oldValue = SCLSettings.get(settingName, EnumSet.of(settingsType));

               // save the content of the textbox if it's different from the
               // current value
               if (!settingValue.equals(oldValue))
               {
                  SCLSettings.add(settingName, settingValue, EnumSet.of(settingsType));
                  hasChanges = true;
               }
            }

            if (hasChanges)
            {
               SCLSettings.save(settingsPath, settingsType);
            }
         }
         catch (IOException | CCLInternalException | RuntimeException ex)
         {
            MessageBox errorMessageBox = new MessageBox(mShell, SWT.ICON_ERROR | SWT.OK);
            errorMessageBox.setMessage(ex.getMessage());
            errorMessageBox.setText(ERROR);
            errorMessageBox.open();
            LOGGER.info("{}: saveChanges - {}", getClass().getCanonicalName(), ERROR, ex);
         }
      }

      /**
       * Store passwords in settings container, but not in a file.
       * 
       * @param textboxes All TextBoxes containing passwords.
       */
      private void saveVolatileChanges(List<Text> textboxes)
      {
         try
         {
            for (Text textbox : textboxes)
            {
               String settingName = ((String) textbox.getData()).trim();
               String settingValue = textbox.getText().trim();
               SCLSettings.add(settingName, settingValue, EnumSet.of(ECLSettingsType.VOLATILE));
            }
         }
         catch (RuntimeException ex)
         {
            MessageBox errorMessageBox = new MessageBox(mShell, SWT.ICON_ERROR | SWT.OK);
            errorMessageBox.setMessage(ex.getMessage());
            errorMessageBox.setText(ERROR);
            errorMessageBox.open();
            LOGGER.info("{}: saveVolatileChanges - {}", getClass().getCanonicalName(), ERROR, ex);
         }
      }

      /**
       * Searches for changes in the plugin settings and saves them to the
       * plugin settings file. Informs the user with an error message if
       * something goes wrong.
       */
      private void savePluginChanges(String orderSettings)
      {
         try
         {
            boolean hasChanges = false;
            List<CCLTuple<String, Boolean>> importerPositions = SCLSettings.getTupleList(orderSettings, EnumSet.of(ECLSettingsType.PROJECT));
            List<CCLTuple<String, Boolean>> newImporterPositions = new ArrayList<>();
            Table sectionTable = mSectionTable.get(orderSettings);

            for (TableItem item : sectionTable.getItems())
            {
               newImporterPositions.add(new CCLTuple<>(item.getText().replace(' ', '_').toUpperCase(), Boolean.valueOf(item.getChecked())));
            }

            hasChanges = !newImporterPositions.equals(importerPositions);

            if (hasChanges || mPluginsAdded || mPluginsRemoved)
            {
               SCLSettings.addList(orderSettings, newImporterPositions, EnumSet.of(ECLSettingsType.PROJECT));
               SCLSettings.save(SCLProject.getProjectSettingsPath(), ECLSettingsType.PROJECT);
            }
         }
         catch (IOException | CCLInternalException | RuntimeException ex)
         {
            MessageBox errorMessageBox = new MessageBox(mShell, SWT.ICON_ERROR | SWT.OK);
            errorMessageBox.setMessage(ex.getMessage());
            errorMessageBox.setText(ERROR);
            errorMessageBox.open();
            LOGGER.info("{}: savePluginChanges - {}", getClass().getCanonicalName(), ERROR, ex);
         }
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

   private Button findDownButton(Composite composite)
   {
      Button foundButton = null;
      for (Control current : Arrays.asList(composite.getChildren()))
      {
         if (Button.class.isInstance(current) && ((Button) current).getText().equals("Down"))
         {
            foundButton = (Button) current;
         }
         else if (Composite.class.isInstance(current))
         {
            foundButton = findDownButton((Composite) current);
         }
         if (null != foundButton)
         {
            break;
         }
      }
      return foundButton;
   }

   private Button findUpButton(Composite composite)
   {
      Button foundButton = null;
      for (Control current : Arrays.asList(composite.getChildren()))
      {
         if (Button.class.isInstance(current) && ((Button) current).getText().equals("  Up  "))
         {
            foundButton = (Button) current;
         }
         else if (Composite.class.isInstance(current))
         {
            foundButton = findUpButton((Composite) current);
         }
         if (null != foundButton)
         {
            break;
         }
      }
      return foundButton;
   }

   private Table findSectionTable(Composite composite)
   {
      Table foundTable = null;
      for (Control current : Arrays.asList(composite.getChildren()))
      {
         if (Table.class.isInstance(current))
         {
            foundTable = (Table) current;
            break;
         }
      }
      return foundTable;
   }

   /**
    * Selection Adapter for the Section Table. Enables/Disables the Up and Down
    * Buttons depending on the current selection.
    */
   private class CCLTableSelectionAdapter extends SelectionAdapter
   {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
         Composite composite = ((TableItem) e.item).getParent().getParent();
         Table sectionTable = ((TableItem) e.item).getParent();
         enableButtonsBySelectedIndex(sectionTable.indexOf((TableItem) e.item),
                                      findUpButton(composite),
                                      findDownButton(composite),
                                      sectionTable);
      }
   }

   /**
    * MouseAdapter for the Up button. Moves the currently selected item up.
    */
   private class CCLUpButtonAdapter extends MouseAdapter
   {
      @Override
      public void mouseUp(MouseEvent e)
      {
         Composite composite = ((Button) e.getSource()).getParent().getParent();
         Table sectionTable = findSectionTable(composite);
         if (null != sectionTable)
         {
            int index = sectionTable.getSelectionIndex();
            if (index > 0)
            {
               TableItem oldItem = sectionTable.getItem(index);
               String text = oldItem.getText();
               Object data = oldItem.getData();
               boolean checked = oldItem.getChecked();
               oldItem = null;
               sectionTable.remove(index);

               TableItem newItem = new TableItem(sectionTable, SWT.NONE, index - 1);
               newItem.setText(text);
               newItem.setData(data);
               newItem.setChecked(checked);
               sectionTable.select(index - 1);
               enableButtonsBySelectedIndex(index - 1,
                     findUpButton(composite),
                     findDownButton(composite),
                     sectionTable);
            }
         }
      }
   }

   /**
    * MouseAdapter for the Down button. Moves the currently selected item down.
    */
   private class CCLDownButtonAdapter extends MouseAdapter
   {
      @Override
      public void mouseUp(MouseEvent e)
      {
         Composite composite = ((Button) e.getSource()).getParent().getParent();
         Table sectionTable = findSectionTable(composite);
         if (null != sectionTable)
         {
            int index = sectionTable.getSelectionIndex();
            if (index >= 0 && index < sectionTable.getItemCount())
            {
               TableItem oldItem = sectionTable.getItem(index);
               String text = oldItem.getText();
               Object data = oldItem.getData();
               boolean checked = oldItem.getChecked();
               oldItem = null;
               sectionTable.remove(index);

               TableItem newItem = new TableItem(sectionTable, SWT.NONE, index + 1);
               newItem.setText(text);
               newItem.setData(data);
               newItem.setChecked(checked);
               sectionTable.select(index + 1);
               enableButtonsBySelectedIndex(index + 1,
                     findUpButton(composite),
                     findDownButton(composite),
                     sectionTable);
            }
         }
      }
   }

   /**
    * SelectionAdapter for the tab folder hosting the tabs for the different
    * settings.
    */
   private class CCLTabSelectionAdapter extends SelectionAdapter
   {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
         if (!mTiPluginSettings.isEmpty() && (e.item == mTiPluginSettings.get(0) || e.item == mTiPluginSettings.get(1)))
         {
            mHasPluginTabSelected = true;
         }
      }
   }
}
