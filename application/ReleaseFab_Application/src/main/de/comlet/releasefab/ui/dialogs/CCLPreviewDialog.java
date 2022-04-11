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
 * @file CCLPreviewDialog.java
 *
 * @brief Preview dialog.
 */

package de.comlet.releasefab.ui.dialogs;

import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jdom2.Element;

/**
 * Dialog that displays a given report as preview of an importer setting.
 */
public class CCLPreviewDialog extends Dialog
{
   private static final int HEIGHT_HINT = 600;
   private static final int WIDTH_HINT = 800;

   private Shell mShell;

   /**
    * Dialog that displays a given report as a preview of an importer setting.
    *
    * @param parent
    */
   public CCLPreviewDialog(Shell parent)
   {
      this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
   }

   /**
    * Dialog that displays a given report as a preview of an importer setting.
    *
    * @param parent
    * @param style
    */
   public CCLPreviewDialog(Shell parent, int style)
   {
      super(parent, style);

      mShell = new Shell(Display.getCurrent());
      mShell.setImage(new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
      mShell.setText("Result");
   }

   /**
    * Opens a dialog that displays the given report as a preview of an importer
    * setting.
    *
    * @param report
    */
   public void open(Element report)
   {
      mShell.setLayout(new GridLayout(1, false));

      final ScrolledComposite composite = new ScrolledComposite(mShell, SWT.BORDER | SWT.V_SCROLL);
      final Composite parent = new Composite(composite, SWT.NONE);
      parent.setLayout(new FillLayout());

      Text textbox = new Text(parent, SWT.MULTI | SWT.READ_ONLY);

      GridData gridData = new GridData();
      gridData.heightHint = HEIGHT_HINT;
      gridData.widthHint = WIDTH_HINT;
      composite.setLayoutData(gridData);
      composite.setContent(parent);
      composite.setExpandVertical(true);
      composite.setExpandHorizontal(true);
      composite.addControlListener(new ControlAdapter()
      {
         @Override
         public void controlResized(ControlEvent e)
         {
            Rectangle r = composite.getClientArea();
            composite.setMinSize(parent.computeSize(r.width, SWT.DEFAULT));
         }
      });

      if (null != report)
      {
         String str = report.getValue();
         textbox.setText(str);
      }
      else
      {
         textbox.setText("Couldn't get report");
      }

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
}
