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
 * @file CCLBasicImportTab.java
 *
 * @brief Basic tab for plugin.
 */

package de.comlet.releasefab.ui;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.api.plugin.ACLAssignmentStrategy;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLParameter;
import de.comlet.releasefab.ui.dialogs.CCLPreviewDialog;
import de.comlet.releasefab.ui.dialogs.CCLProgressDialog;
import de.comlet.releasefab.ui.dialogs.CCLProgressDialog.ACLBackgroundWorker;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.jdom2.Element;

/**
 * Tab containing controls that allow us to set the assignment strategy of an
 * import strategy and it's necessary parameters. The first column of this tab
 * shows the component tree.
 */
public class CCLBasicImportTab extends ACLBasicTab implements PropertyChangeListener
{
   /** Position of the assignment column */
   private static final int ASSIGNMENT_COLUMN = 1;

   /** Position of the column for the test button */
   private static final int TESTBUTTON_COLUMN = 2;

   /**
    * Position of the column for the first parameter (if there are any
    * parameters)
    */
   private static final int PARAM1_COLUMN = 3;

   /** define width values for columns
    *
    */
   private static final int COLUMNWIDTH40 = 40;
   private static final int COLUMNWIDTH180 = 180;

   /** define height values for editor
    *
    */
   private static final int MINIMUMHEIGHT10 = 10;
   private static final int MINIMUMHEIGHT105 = 105;

   /**
    * Import strategy for which we want to define the assignment strategy
    */
   private ACLImportStrategy mImportStrategy;

   /** Maximum number of required parameters */
   private int mMaxParameters;

   /**
    * Creates an initial import tab that is updated whenever something in the
    * component tree changes, e.g. when a new component is added.
    *
    * @param tabItem parent tab item
    * @param importStrategy
    */
   public CCLBasicImportTab(TabItem tabItem, ACLImportStrategy importStrategy)
   {
      super(tabItem);

      mImportStrategy = importStrategy;

      // create content
      createContent();
   }

   /**
    * update view whenever something in the component tree changes, e.g. when a
    * new component is added
    */
   @Override
   public void registerObservers()
   {
      SCLProject.getComponentRoot().addPropertyChangeListener(this);
   }

   @Override
   public void unregisterObservers()
   {
      SCLProject.getComponentRoot().removePropertyChangeListener(this);
   }

   /**
    * Creates columns for the test button, the assignment strategy and each of
    * it's parameters.
    */
   @Override
   protected void createCustomColumns()
   {
      // create columns
      createTreeColumn(mTree, "Strategy", COLUMNWIDTH180);
      createTreeColumn(mTree, "Test", COLUMNWIDTH40);

      // get maximum number of required parameters
      for (ACLAssignmentStrategy assignmentStrategy : mImportStrategy.getAssignmentStrategies())
      {
         if (assignmentStrategy.getNrOfParameters() > mMaxParameters)
         {
            mMaxParameters = assignmentStrategy.getNrOfParameters();
         }
      }

      // add a column for each of the parameters
      for (int i = 1; i <= mMaxParameters; i++)
      {
         createTreeColumn(mTree, "Parameter " + i, COLUMNWIDTH180);
      }
   }

   /**
    * Creates a tree item for the given component as a child of the given
    * parent.
    *
    * @param parentTree parent tree or tree item
    * @param component
    * @return
    */
   @Override
   protected TreeItem createTreeItem(Object parentTree, CCLComponent component)
   {
      // add new TreeItem to parent tree or tree item
      TreeItem treeItem = super.createTreeItem(parentTree, component);

      // assignment strategy
      if (null != component.getAssignmentStrategy(mImportStrategy.getName()))
      {
         treeItem.setText(ASSIGNMENT_COLUMN, component.getAssignmentStrategy(mImportStrategy.getName()).getName());
         CCombo assignmentStrategy = createComboBox(treeItem);
         assignmentStrategy.addFocusListener(new CCLBasicImportFocusAdapter(treeItem, ASSIGNMENT_COLUMN));
         addControl(treeItem, ASSIGNMENT_COLUMN, assignmentStrategy);
      }
      
      // test button
      Button btnTest = new Button(mTree, SWT.PUSH);
      btnTest.setText("T");
      btnTest.addMouseListener(new CCLBasicImportMouseAdapter(component));
      addControl(treeItem, TESTBUTTON_COLUMN, btnTest);

      // parameters
      for (int i = 0; i < mMaxParameters; i++)
      {
         treeItem.setText(PARAM1_COLUMN + i, component.getParameters(mImportStrategy.getName()).get(i).getValue());
         String paramStr = treeItem.getText(PARAM1_COLUMN + i);
         Text parameter = new Text(mTree, SWT.MULTI | SWT.WRAP);
         parameter.setText(paramStr);
         highlightTextfield(parameter);
         parameter.addFocusListener(new CCLBasicImportFocusAdapter(treeItem, PARAM1_COLUMN + i));

         // add listeners to expand the text field when it's text is too long
         // for only one line and collapse it when it loses focus
         Listener textfieldListener = new CCLTextfieldExpandListener();
         parameter.addListener(SWT.KeyDown, textfieldListener);
         parameter.addListener(SWT.KeyUp, textfieldListener);
         parameter.addListener(SWT.FocusIn, textfieldListener);
         parameter.addListener(SWT.FocusOut, textfieldListener);

         addControl(treeItem, PARAM1_COLUMN + i, parameter);
      }

      return treeItem;
   }

   /**
    * Creates a combobox to select the assignment strategy.
    *
    * @param treeItem tree item for which we want to create the combobox
    * @return newly created combobox
    */
   private CCombo createComboBox(TreeItem treeItem)
   {
      CCombo combo = new CCombo(mTree, SWT.READ_ONLY);

      for (ACLAssignmentStrategy assigner : mImportStrategy.getAssignmentStrategies())
      {
         combo.add(assigner.getName());
      }

      String selectedText = treeItem.getText(ASSIGNMENT_COLUMN);
      int selectedItem = combo.indexOf(selectedText);
      combo.select(selectedItem);

      // set usage info tooltip
      setUsageInfoTooltip(combo);

      // update tooltip whenever the user selects an item
      combo.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent event)
         {
            CCombo combo = (CCombo) event.widget;

            setUsageInfoTooltip(combo);
         }
      });

      return combo;
   }

   /**
    * Sets the tooltip of the given combo box to the usage info of the selected
    * assignment strategy.
    *
    * @param combo
    */
   private void setUsageInfoTooltip(CCombo combo)
   {
      String selectedAssigner = combo.getItem(combo.getSelectionIndex());
      ACLAssignmentStrategy selectedAssignmentStrategy = mImportStrategy.getAssignmentStrategy(selectedAssigner);
      combo.setToolTipText(selectedAssignmentStrategy.getUsageInfo());
   }

   /**
    * Adds a control to the tree item in the given column
    *
    * @param treeItem
    * @param control
    * @param column
    */
   private void addControl(TreeItem treeItem, int column, Control control)
   {
      TreeEditor editor = new TreeEditor(mTree);
      editor.horizontalAlignment = SWT.LEFT;
      editor.verticalAlignment = SWT.TOP;
      editor.grabHorizontal = true;

      if (mTree.getColumn(column).getWidth() < editor.minimumWidth)
      {
         mTree.getColumn(column).setWidth(editor.minimumWidth);
      }

      control.setData("treeeditor", editor);

      editor.setEditor(control, treeItem, column);
   }

   /**
    * Expands the specified text field to allow the user to enter several lines
    * of text.
    *
    * @param textfield
    */
   private static void expandTextfield(Text textfield)
   {
      Object data = textfield.getData("treeeditor");

      if (data instanceof TreeEditor)
      {
         TreeEditor editor = (TreeEditor) data;
         editor.minimumHeight = MINIMUMHEIGHT105;
         editor.layout();
      }
   }

   /**
    * Expands the specified text field if the text in this text field is too
    * long for only one line.
    *
    * @param parameter
    */
   private static void expandTextIfTooLong(Text textfield)
   {
      if (1 < textfield.getLineCount())
      {
         expandTextfield(textfield);
      }
   }

   /**
    * Collapses the specified text field.
    *
    * @param textfield
    */
   private static void collapseTextfield(Text textfield)
   {
      Object data = textfield.getData("treeeditor");

      if (data instanceof TreeEditor)
      {
         TreeEditor editor = (TreeEditor) data;
         editor.minimumHeight = MINIMUMHEIGHT10;
         editor.layout();
      }
   }

   /**
    * Highlight textfield if there's some user input.
    *
    * @param textfield
    */
   private static void highlightTextfield(Text textfield)
   {
      if (0 < textfield.getText().trim().length())
      {
         textfield.setBackground(textfield.getDisplay().getSystemColor(SWT.COLOR_GRAY));
      }
   }

   /**
    * FocusAdapter that saves any changes when the control loses focus.
    */
   private class CCLBasicImportFocusAdapter extends FocusAdapter
   {
      private TreeItem mTreeItem;
      private int mColumn;
      private CCLComponent mComponent;

      public CCLBasicImportFocusAdapter(TreeItem treeItem, int column)
      {
         mTreeItem = treeItem;
         mColumn = column;
         mComponent = (CCLComponent) mTreeItem.getData();
      }

      @Override
      public void focusLost(FocusEvent e)
      {
         String text = null;

         if (e.widget instanceof Text)
         {
            Text textBox = (Text) e.widget;
            text = textBox.getText();

            highlightTextfield(textBox);

            List<CCLParameter> parameters = mComponent.getParameters(mImportStrategy.getName());
            parameters.get(mColumn - PARAM1_COLUMN).setValue(text);
            mComponent.setParameters(mImportStrategy.getName(), parameters);
         }
         else if (e.widget instanceof CCombo)
         {
            CCombo comboBox = (CCombo) e.widget;
            text = comboBox.getText();

            ACLAssignmentStrategy assignmentStrategy = mImportStrategy.getAssignmentStrategy(text);
            mComponent.setAssignmentStrategy(mImportStrategy.getName(), assignmentStrategy);
         }

         if (null != text && !mTreeItem.getText(mColumn).equals(text))
         {
            // save changes to the table cell
            mTreeItem.setText(mColumn, text);
            SCLProject.setNeedsSaving(true);
         }
      }
   }

   /**
    * Listener for the parameter TextFields that expand. TextFields 
    * expand when there is more than one line of text and expand when 
    * they loose focus.
    */
   private static class CCLTextfieldExpandListener implements Listener
   {
      @Override
      public void handleEvent(Event e)
      {
         Text textBox = (Text) e.widget;

         switch (e.type)
         {
            case SWT.KeyDown:
            {
               switch (e.character)
               {
                  case SWT.CR:
                     // if the user hits the enter key, expand the text field
                     // if it's not already expanded
                     expandTextfield(textBox);
                     break;

                  case SWT.ESC:
                     // if the user hits the escape key, collapse the text
                     // field
                     collapseTextfield(textBox);
                     break;

                  default:
                     // if the user hits any other key, expand the text field if
                     // the text doesn't fit in one line anymore
                     expandTextIfTooLong(textBox);
               }

               break;
            }

            case SWT.KeyUp:
            {
               if (e.character != SWT.CR && e.character != SWT.ESC)
               {
                  expandTextIfTooLong(textBox);
               }

               break;
            }

            case SWT.FocusOut:
            {
               // collapse the text field when it loses focus
               collapseTextfield(textBox);
               break;
            }

            case SWT.FocusIn:
            {
               // expand the text field when it gains focus and if it's text is
               // too long to fit in only one line
               expandTextIfTooLong(textBox);
            }
         }
      }
   }

   /**
    * MouseAdapter for the test button starting a test and presenting a preview
    * of the output from the selected AssignmentStrategy.
    */
   private class CCLBasicImportMouseAdapter extends MouseAdapter
   {
      private CCLComponent mComponent;

      public CCLBasicImportMouseAdapter(CCLComponent component)
      {
         mComponent = component;
      }

      @Override
      public void mouseDown(MouseEvent event)
      {
         SCLProject.setTestMode(true);

         final ACLAssignmentStrategy strategy = mComponent.getAssignmentStrategy(mImportStrategy.getName());

         final Shell shell = mTree.getShell();
         final CCLProgressDialog progress = new CCLProgressDialog(shell);

         // define background worker
         ACLBackgroundWorker worker = new ACLBackgroundWorker(progress)
         {
            @Override
            public void doWork()
            {
               CCLDelivery emptyDelivery = new CCLDelivery();
               Element report = strategy.getData(mComponent.getParameters(mImportStrategy.getName()), mComponent,
                     emptyDelivery, SCLProject.getFormerDelivery(emptyDelivery), mImportStrategy, SCLProject.getProjectRoot(), SCLProject.getInstance().getInitialComponent());
               setResult(report);
            }
         };

         // start background worker thread and open progress dialog
         progress.open(worker);

         CCLPreviewDialog previewDialog = new CCLPreviewDialog(shell);
         previewDialog.open((Element) worker.getResult());

         SCLProject.setTestMode(false);
      }
   }
}