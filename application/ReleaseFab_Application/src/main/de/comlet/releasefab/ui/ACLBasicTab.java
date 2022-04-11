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
 * @file ACLBasicTab.java
 *
 * @brief Basic tab for component tree.
 */

package de.comlet.releasefab.ui;

import de.comlet.releasefab.SCLProject;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.ui.commands.ACLCommand;
import de.comlet.releasefab.ui.commands.CCLCommandAddComponentAfter;
import de.comlet.releasefab.ui.commands.CCLCommandAddComponentBefore;
import de.comlet.releasefab.ui.commands.CCLCommandAddSubComponent;
import de.comlet.releasefab.ui.commands.CCLCommandComponentRelevant;
import de.comlet.releasefab.ui.commands.CCLCommandRemoveComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Tab which displays the component tree.
 */
public abstract class ACLBasicTab implements PropertyChangeListener
{
   protected static final int COMPONENT_COLUMN = 0;
   
   private static final int COLUMN_WIDTH_COMPONENT = 200;
   
   private static final int MINIMUM_WIDTH_OF_EDITOR = 50;

   /** Parent tab item */
   protected TabItem mTabItem;

   /** Tree for this tab */
   protected Tree mTree;

   /** Context menu for this tab */
   protected Menu mContextMenu;

   /**
    * List of all expanded components. This is necessary to prevent the tree
    * from collapsing whenever it is updated.
    */
   private List<CCLComponent> mExpandedComponents = new ArrayList<>();

   /**
    * Indicates whether or not its allowed to rename components by
    * double-clicking them.
    */
   private boolean mIsComponentRenamingAllowed = true;

   /**
    * Constructor
    * 
    * @param tabItem tab item to which we want to add some content
    */
   public ACLBasicTab(TabItem tabItem)
   {
      mTabItem = tabItem;
   }

   /**
    * Creates a tree
    */
   protected void createContent()
   {
      registerObservers();

      // reset tab item
      if (null != mTabItem.getControl())
      {
         mTabItem.getControl().dispose();
      }

      // create tree
      mTree = new Tree(mTabItem.getParent(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
      mTree.setHeaderVisible(true);
      mTree.setLinesVisible(true);

      // Add listeners for expanding and collapsing components. This is needed
      // to remember which components have been expanded before the last update
      // of the tree and to allow us to restore this state.
      // see: expandPreviouslyExpandedComponents()
      mTree.addListener(SWT.Expand, new CCLComponentExpandListener());
      mTree.addListener(SWT.Collapse, new CCLComponentCollapseListener());

      // add the tree to the tab folder
      mTabItem.setControl(mTree);

      // add context menu to the tree
      mContextMenu = new Menu(mTree.getShell(), SWT.POP_UP);
      mContextMenu.addListener(SWT.Show, new CCLTreeMenuListener(mContextMenu, mTree));
      mTree.setMenu(mContextMenu);

      // create the first column which contains the components
      createTreeColumn(mTree, "Component", COLUMN_WIDTH_COMPONENT);

      // ... then create the custom columns if the subclass defines them
      createCustomColumns();

      // list all components
      for (CCLComponent component : SCLProject.getComponentRootData())
      {
         createComponents(mTree, component);
      }

      // expand all components that have been expanded before the last update of
      // this tree
      expandPreviouslyExpandedComponents(mTree);

      // register events for double clicks on tree items
      registerDoubleClickEvents();
   }

   /**
    * This method allows subclasses of ACLBasicTab to add observers.
    */
   public abstract void registerObservers();

   /**
    * This method allows subclasses of ACLBasicTab to remove their observers.
    */
   public abstract void unregisterObservers();

   /**
    * This method allows subclasses of ACLBasicTab to create additional columns.
    */
   protected abstract void createCustomColumns();

   /**
    * Adds a column with the given width to the tree that is labeled with the
    * given title.
    * 
    * @param tree parent tree or tree item
    * @param title
    * @param width
    */
   protected void createTreeColumn(Tree tree, String title, int width)
   {
      TreeColumn treeColumn = new TreeColumn(tree, SWT.CENTER);
      treeColumn.setText(title);
      treeColumn.setWidth(width);
   }

   /**
    * Recursively adds tree items for each component.
    * 
    * @param parentTree parent tree or tree item
    * @param component component that should be added to the tree
    */
   protected void createComponents(Object parentTree, CCLComponent component)
   {
      // create a tree item for this component
      TreeItem treeItem = createTreeItem(parentTree, component);

      // set font color of component name indicating whether or not
      // this component's information is customer relevant
      if (component.getIsCustomerRelevant())
      {
         treeItem.setForeground(COMPONENT_COLUMN, mTree.getDisplay().getSystemColor(SWT.COLOR_BLUE));
         changeFontStyle(treeItem, COMPONENT_COLUMN, SWT.ITALIC);
      }
      else
      {
         treeItem.setForeground(COMPONENT_COLUMN, mTree.getDisplay().getSystemColor(SWT.COLOR_BLACK));
         changeFontStyle(treeItem, COMPONENT_COLUMN, SWT.NORMAL);
      }

      // repeat with all subcomponents of this component
      for (CCLComponent subComponent : component.getSubComponents())
      {
         createComponents(treeItem, subComponent);
      }
   }

   /**
    * Creates a tree item for the given component as a child of the given
    * parent.
    * 
    * @param parentTree parent tree or tree item
    * @param component
    * @return newly created tree item for the given component
    */
   protected TreeItem createTreeItem(Object parentTree, CCLComponent component)
   {
      // add new TreeItem to parent Tree/TreeItem
      TreeItem treeItem = null;

      if (parentTree instanceof TreeItem)
      {
         treeItem = new TreeItem((TreeItem) parentTree, SWT.NONE);
      }
      else if (parentTree instanceof Tree)
      {
         treeItem = new TreeItem((Tree) parentTree, SWT.NONE);
      }

      if (null != treeItem)
      {
         // component name
         treeItem.setText(COMPONENT_COLUMN, component.getName());
   
         treeItem.setData(component);
      }

      return treeItem;
   }

   /**
    * Expands all tree items that have been expanded before the last update of
    * this tree.
    * 
    * @param parentTree parent tree or tree item
    */
   protected void expandPreviouslyExpandedComponents(Object parentTree)
   {
      TreeItem[] treeItems = null;

      // check if parentTree is a Tree or a TreeItem and get all children
      if (parentTree instanceof Tree)
      {
         treeItems = ((Tree) parentTree).getItems();
      }
      else if (parentTree instanceof TreeItem)
      {
         treeItems = ((TreeItem) parentTree).getItems();
      }

      // expand all tree items that are contained in mExpandedComponents
      for (TreeItem treeItem : treeItems)
      {
         if (mExpandedComponents.contains(treeItem.getData()))
         {
            treeItem.setExpanded(true);
         }

         // repeat with all children of this tree item
         expandPreviouslyExpandedComponents(treeItem);
      }
   }

   /**
    * Registers double click events for the tree items.
    */
   private void registerDoubleClickEvents()
   {
      final TreeEditor editor;

      if (mIsComponentRenamingAllowed)
      {
         editor = new TreeEditor(mTree);
         editor.horizontalAlignment = SWT.LEFT;
         editor.grabHorizontal = true;
         editor.minimumWidth = MINIMUM_WIDTH_OF_EDITOR;
      }
      else
      {
         editor = null;
      }

      // dispose editor when another row has been selected
      mTree.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            // clean up any previous editor control if existent
            disposePreviousEditors(editor);
         }
      });

      final class TreeClickAdapter extends MouseAdapter 
      {
         @Override
         public void mouseDoubleClick(MouseEvent e)
         {
            // clean up any previous editor control
            disposePreviousEditors(editor);

            // identify the selected row
            Point pt = new Point(e.x, e.y);
            TreeItem treeItem = mTree.getItem(pt);

            int column = findClickedColumn(pt, treeItem);

            if (column == COMPONENT_COLUMN)
            {
               // the component column was double-clicked
               // check if component renaming is allowed
               if (mIsComponentRenamingAllowed)
               {
                  // create a TextBox and let the user change the
                  // selected component's name
                  createRenamingTextBox(editor, treeItem);
               }
            }
            else if (column > 0)
            {
               // another column was double-clicked
               onCustomColumnDoubleClick(treeItem, mTree.getColumn(column));
            }
         }
      }
      
      // handle double-clicks on the component column and the rest of the
      // columns
      mTree.addMouseListener(new TreeClickAdapter());
   }

   /**
    * Disposes old editor control if existent.
    * 
    * @param editor
    */
   private void disposePreviousEditors(TreeEditor editor)
   {
      if (editor != null)
      {
         Control oldEditor = editor.getEditor();
         if (null != oldEditor && !oldEditor.isDisposed())
         {
            changeComponentName(editor);
            oldEditor.dispose();
         }
      }
   }

   /**
    * Creates a TextBox that allows the user to rename the component in the
    * given TreeItem.
    * 
    * @param editor
    * @param treeItem
    */
   private void createRenamingTextBox(final TreeEditor editor, TreeItem treeItem)
   {
      Text textbox = new Text(mTree, SWT.NONE);
      textbox.setText(treeItem.getText(COMPONENT_COLUMN));
      textbox.selectAll();
      textbox.setFocus();
      textbox.addTraverseListener(new TraverseListener()
      {
         @Override
         public void keyTraversed(TraverseEvent e)
         {
            if (e.detail == SWT.TRAVERSE_RETURN)
            {
               changeComponentName(editor);
            }
         }
      });

      editor.setEditor(textbox, treeItem, COMPONENT_COLUMN);
   }

   /**
    * Changes the component's name according to user input.
    * 
    * @param editor
    */
   private void changeComponentName(TreeEditor editor)
   {
      Text text = (Text) editor.getEditor();
      String newName = text.getText().trim();

      CCLComponent component = (CCLComponent) editor.getItem().getData();

      // only change component's name, if the new name is different from the old
      // one, because we don't want to inform the observers when nothing changed
      if (!component.getName().equals(newName))
      {
         component.setName(newName);
      }
   }

   /**
    * Returns the number of the column that has been clicked in the given 
    * TreeItem at the given coordinates.
    * 
    * @param point coordinates of the clicked point
    * @param treeItem clicked TreeItem
    * @return number of the clicked column or -1 if column was not found
    */
   private int findClickedColumn(Point point, TreeItem treeItem)
   {
      if (null != point && null != treeItem)
      {
         for (int i = 0; i < mTree.getColumnCount(); i++)
         {
            // check which column has been double-clicked by the user
            Rectangle rect = treeItem.getBounds(i);
            if (rect.contains(point))
            {
               return i;
            }
         }
      }

      return -1;
   }

   /**
    * Allows a subclass to specify a behavior for double click events on one of
    * the custom columns. By default this event is ignored.
    * 
    * @param treeItem clicked item
    * @param treeColumn clicked column
    */
   protected void onCustomColumnDoubleClick(TreeItem treeItem, TreeColumn treeColumn)
   {
   }

   /**
    * Allows a subclass to specify a behavior for right click events on one of
    * the custom columns. By default this event is ignored.
    * 
    * @param treeItem clicked item
    * @param treeColumn clicked column
    */
   protected void onCustomColumnRightClick(TreeItem treeItem, TreeColumn treeColumn)
   {
   }

   /**
    * Is it allowed to double-click components to change their names?
    * 
    * @return
    */
   public boolean isComponentRenamingAllowed()
   {
      return mIsComponentRenamingAllowed;
   }

   /**
    * Enables or disables component renaming by double-click.
    * 
    * @param isComponentRenamingAllowed
    */
   public void allowComponentRenaming(boolean isComponentRenamingAllowed)
   {
      mIsComponentRenamingAllowed = isComponentRenamingAllowed;
   }

   /**
    * Re-creates content on update.
    */
   @Override
   public void propertyChange(PropertyChangeEvent pce)
   {
      // Old observers need to be unregistered first.
      unregisterObservers();
      createContent();
   }

   /**
    * Listener for a tree that is called whenever the user right-clicks on a
    * tree item.
    */
   public class CCLTreeMenuListener implements Listener
   {
      private Menu mComponentMenu;
      private Tree mTree;

      public CCLTreeMenuListener(Menu menu, Tree tree)
      {
         mComponentMenu = menu;
         mTree = tree;
      }

      @Override
      public void handleEvent(Event e)
      {
         resetContextMenu(mComponentMenu);

         // are there any items in the tree?
         if (0 >= mTree.getItemCount())
         {
            // add new root component as a subcomponent of the invisible
            // component root
            MenuItem miAddRootComponent = new MenuItem(mComponentMenu, SWT.PUSH);
            miAddRootComponent.setText("Add root component");
            miAddRootComponent.addSelectionListener(new CCLCommandAddSubComponent(SCLProject.getComponentRoot()));
         }
         else
         {
            // get location of the cursor relative to the tree
            Point mouseLocation = mTree.getDisplay().getCursorLocation();
            Point treeLocation = mTree.toDisplay(0, 0);
            Point pt = new Point(mouseLocation.x - treeLocation.x, mouseLocation.y - treeLocation.y);

            // get clicked row and column
            TreeItem treeItem = mTree.getItem(pt);
            int column = findClickedColumn(pt, treeItem);

            if (column == COMPONENT_COLUMN)
            {
               // create menu for the selected item
               CCLComponent component = (CCLComponent) treeItem.getData();

               // add component before
               createContextMenuItem(mComponentMenu, new CCLCommandAddComponentBefore(component));

               // add subcomponent
               createContextMenuItem(mComponentMenu, new CCLCommandAddSubComponent(component));

               // add component after
               createContextMenuItem(mComponentMenu, new CCLCommandAddComponentAfter(component));

               // separator
               new MenuItem(mComponentMenu, SWT.SEPARATOR);

               // remove component
               createContextMenuItem(mComponentMenu, new CCLCommandRemoveComponent(component));

               // seperator
               new MenuItem(mComponentMenu, SWT.SEPARATOR);

               // customer relevant
               createContextMenuItem(mComponentMenu, new CCLCommandComponentRelevant(component), SWT.CHECK,
                     component.getIsCustomerRelevant());
            }
            else if (column > 0)
            {
               onCustomColumnRightClick(treeItem, mTree.getColumn(column));
            }
         }
      }
   }

   /**
    * Disposes all menu items in the given menu.
    * 
    * @param menu
    */
   protected void resetContextMenu(Menu menu)
   {
      MenuItem[] menuItems = menu.getItems();
      for (int i = 0; i < menuItems.length; i++)
      {
         menuItems[i].dispose();
      }
   }

   /**
    * Creates a context menu item in the given parent menu as SWT.PUSH button.
    * 
    * @param parentMenu
    * @param command
    */
   protected void createContextMenuItem(Menu parentMenu, ACLCommand command)
   {
      createContextMenuItem(parentMenu, command, SWT.PUSH, false);
   }

   /**
    * Creates a context menu item in the given parent menu.
    * 
    * @param parentMenu
    * @param command
    * @param type SWT widget type
    * @param selection If type == SWT.CHECK selection will be used as the value
    *           of the check box.
    */
   protected void createContextMenuItem(Menu parentMenu, ACLCommand command, int type, boolean selection)
   {
      MenuItem menuItem = new MenuItem(parentMenu, type);
      menuItem.setText(command.getTitle());
      menuItem.addSelectionListener(command);

      if (type == SWT.CHECK)
      {
         menuItem.setSelection(selection);
      }
   }

   /**
    * Changes the font style of the item in the given column to the given style.
    * 
    * @param item
    * @param column
    * @param style SWT.BOLD, SWT.ITALIC, SWT.NORMAL
    */
   protected void changeFontStyle(TreeItem item, int column, int style)
   {
      Font font = item.getFont();

      if (null != font)
      {
         Device device = font.getDevice();
         FontData[] fontData = font.getFontData();

         if (null != device && null != fontData && 0 < fontData.length)
         {
            String name = fontData[0].getName();
            int height = fontData[0].getHeight();

            item.setFont(column, new Font(device, name, height, style));
         }
      }
   }

   /**
    * Listener for a tree that is called whenever an expanded tree item is
    * collapsed. Removes the component of that item from the list of expanded
    * components.
    */
   public class CCLComponentCollapseListener implements Listener
   {
      @Override
      public void handleEvent(Event e)
      {
         // get component from tree item
         CCLComponent component = (CCLComponent) e.item.getData();

         // remove component from the list
         mExpandedComponents.remove(component);
      }
   }

   /**
    * Listener for a tree that is called whenever a tree item is expanded. Adds
    * the component of that item to the list of expanded components.
    */
   public class CCLComponentExpandListener implements Listener
   {
      @Override
      public void handleEvent(Event e)
      {
         // get component from tree item
         CCLComponent component = (CCLComponent) e.item.getData();

         // add component to the list
         if (!mExpandedComponents.contains(component))
         {
            mExpandedComponents.add(component);
         }
      }
   }
}
