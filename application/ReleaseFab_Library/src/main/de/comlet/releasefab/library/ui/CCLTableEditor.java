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
 * @file CCLTableEditor.java
 *
 * @brief GUI table editor.
 */

package de.comlet.releasefab.library.ui;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Class to show information in the UI.
 */
public class CCLTableEditor
{
   private static final int NUMBER_OF_COLUMNS = 2;
   private static final int HORIZONTAL_SPAN = 2;
   private static final int HEIGHT_HINT = 400;
   private static final int MINIMUM_WIDTH_OF_EDITOR = 50;

   /**
    * The table holds a list of all items associated with the given component in
    * the given delivery.
    */
   private Table mTable;

   /** Editor used to change the content of table cells */
   private TableEditor mEditor;
   private CCLColumnDescription[] mColumnDescriptions;
   private ArrayList<Integer> mEditableColumns;
   private int mCurrentColumn;

   /**
    * Constructor.
    */
   public CCLTableEditor()
   {
      this.mEditableColumns = new ArrayList<>();
   }

   /**
    * Initialize UI.
    */
   public void init(Object obj, CCLColumnDescription[] columnDescriptions, String leftLabel, String rightLabel)
   {
      this.mColumnDescriptions = columnDescriptions.clone();

      Composite composite = (Composite) obj;
      composite.setLayout(new GridLayout(NUMBER_OF_COLUMNS, true));

      // left label (branch)
      Label lblLeft = new Label(composite, SWT.NONE);
      lblLeft.setText(leftLabel);

      // right label (tag)
      Label lblRight = new Label(composite, SWT.NONE);
      lblRight.setText(rightLabel);

      // create table for change requests
      mTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
      GridData data = new GridData();
      data.horizontalSpan = HORIZONTAL_SPAN;
      data.heightHint = HEIGHT_HINT;
      mTable.setLayoutData(data);
      mTable.setHeaderVisible(true);

      // create columns
      int index = 0;
      for (CCLColumnDescription cl : this.mColumnDescriptions)
      {
         TableColumn column = new TableColumn(mTable, SWT.NONE);
         column.setText(cl.getDescription());
         column.setWidth(cl.getWidth());

         if (cl.getEditable())
         {
            this.mEditableColumns.add(index);
         }
         ++index;
      }
   }

   /**
    * Opens an editor with a TextBox that allows the user to change the content
    * of the selected table cell.
    */
   public void enableEditor()
   {
      mEditor = new TableEditor(mTable);
      mEditor.horizontalAlignment = SWT.LEFT;
      mEditor.grabHorizontal = true;
      mEditor.minimumWidth = MINIMUM_WIDTH_OF_EDITOR;

      // Dispose editor when another row has been selected
      mTable.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            // Clean up previous editor control if existent
            disposePreviousEditor();
         }
      });

      final class TableClickAdapter extends MouseAdapter
      {
         @Override
         public void mouseDoubleClick(MouseEvent e)
         {
            // Clean up any previous editor control
            disposePreviousEditor();

            // Identify the selected row
            Point pt = new Point(e.x, e.y);
            TableItem tableItem = mTable.getItem(pt);

            if (null != tableItem)
            {
               for (int i = 0; i < mTable.getColumnCount(); i++)
               {
                  // Check which column has been double-clicked by the user
                  Rectangle rect = tableItem.getBounds(i);
                  if (rect.contains(pt))
                  {
                     if (mEditableColumns.contains(i))
                     {
                        // Create a TextBox and let the user change the
                        // selected text
                        createEditorTextBox(tableItem, i);
                     }

                     break;
                  }
               }
            }
         }
      }

      // Handle double-clicks
      mTable.addMouseListener(new TableClickAdapter());
   }

   /**
    * Generic method for adding table rows.
    */
   public void addRows(Iterable<CCLRowAccessor> source)
   {
      for (CCLRowAccessor row : source)
      {
         int columnIndex = 0;
         TableItem tableItem = new TableItem(mTable, SWT.NONE);
         for (CCLColumnDescription cld : mColumnDescriptions)
         {
            String tagContent = row.get(cld.getXmlTag());
            // Save the content in the corresponding TAGs
            if (null != tagContent)
            {
               tableItem.setText(columnIndex, tagContent);
            }
            // If there is no such TAG, save in delivery
            else
            {
               tableItem.setText(columnIndex, "n/a");
            }
            ++columnIndex;
         }
      }
   }

   /**
    * Save items.
    */
   public void saveItems(Iterable<CCLRowAccessor> sink)
   {
      Iterator<CCLRowAccessor> sinkIt = sink.iterator();
      TableItem[] tableItems = mTable.getItems();
      int index = 0;

      while ((index < tableItems.length) && (sinkIt.hasNext()))
      {
         CCLRowAccessor updater = sinkIt.next();
         for (int columnIndex : this.mEditableColumns)
         {
            String text = tableItems[index].getText(columnIndex);
            updater.update(mColumnDescriptions[columnIndex].getXmlTag(), text);
         }
         index++;
      }
   }

   /**
    * Disposes old editor control if existent.
    */
   private void disposePreviousEditor()
   {
      Control oldEditor = mEditor.getEditor();
      if (null != oldEditor && !oldEditor.isDisposed())
      {
         // Save text to the table cell...
         saveTextToTableCell();

         // ... and close the editor.
         oldEditor.dispose();
      }
   }

   /**
    * Creates a TextBox that allows the user to change the text in the given
    * table item.
    *
    * @param tableItem
    */
   private void createEditorTextBox(final TableItem tableItem, int column)
   {
      // create TextBox
      final Text textbox = new Text(mTable, SWT.NONE);
      textbox.setText(tableItem.getText(column));
      textbox.setData("tableitem", tableItem);
      textbox.selectAll();
      textbox.setFocus();
      textbox.addTraverseListener(new TraverseListener()
      {
         @Override
         public void keyTraversed(TraverseEvent e)
         {
            // When the user hits the return key...
            if (e.detail == SWT.TRAVERSE_RETURN)
            {
               // save text to the table cell...
               saveTextToTableCell();

               // ... and close the editor.
               textbox.dispose();
            }
         }
      });

      // Add a TextBox to the editor in the table cell
      mEditor.setEditor(textbox, tableItem, column);
      this.mCurrentColumn = column;
   }

   /**
    * Gets the text from the editors TextBox and writes it into the
    * correspondent table cell.
    */
   public void saveTextToTableCell()
   {
      if (null != mEditor)
      {
         // Get the TextBox
         Text text = (Text) mEditor.getEditor();

         if (null != text && !text.isDisposed())
         {
            // Save the content of the TextBox to the table cell
            String newText = text.getText().trim();

            TableItem tableItem = (TableItem) text.getData("tableitem");
            tableItem.setText(this.mCurrentColumn, newText);
         }
      }
   }
}
