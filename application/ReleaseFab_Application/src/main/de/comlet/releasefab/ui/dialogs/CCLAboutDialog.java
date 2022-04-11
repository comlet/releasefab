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
 * @file CCLAboutDialog.java
 *
 * @brief Dialog with information about the application.
 */

package de.comlet.releasefab.ui.dialogs;

import de.comlet.releasefab.CCLAssemblyInfo;
import de.comlet.releasefab.SCLPluginLoader;
import de.comlet.releasefab.api.plugin.ACLImportStrategy;
import de.comlet.releasefab.ui.images.ICLResourceAnchor;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog with copyright and version information.
 */
public class CCLAboutDialog extends Dialog
{
   /** Initialize logger for this class */
   private static final Logger LOGGER = LoggerFactory.getLogger(CCLAboutDialog.class);
   
   private Shell mShell;

   /**
    * Dialog with copyright and version information.
    * 
    * @param parent
    */
   public CCLAboutDialog(Shell parent)
   {
      this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
   }

   /**
    * Dialog with copyright and version information.
    * 
    * @param parent
    * @param style
    */
   public CCLAboutDialog(Shell parent, int style)
   {
      super(parent, style);
      mShell = new Shell(Display.getCurrent());
      mShell.setText("About ReleaseFab");
      mShell.setImage(new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.ico")));
   }

   /**
    * Opens a dialog with copyright and version information.
    */
   public void open()
   {
      mShell.setLayout(new GridLayout(1, false));

      GridData layoutData = new GridData(GridData.FILL_BOTH);
      layoutData.verticalAlignment = SWT.CENTER;
      layoutData.horizontalAlignment = SWT.CENTER;
      
      Label lblImage = new Label(mShell, SWT.NONE);
      Image image = new Image(mShell.getDisplay(), ICLResourceAnchor.class.getResourceAsStream("releasefab.png"));
      lblImage.setImage(image);
      lblImage.setLayoutData(layoutData);

      Link linkGeneralLicense = new Link(mShell, SWT.NONE);
      linkGeneralLicense.setText("The ReleaseFab Library and the main application in version " + CCLAssemblyInfo.getVersion() + " are licensed under the <a>Eclipe Public License - v 2.0</a>");
      linkGeneralLicense.addListener(SWT.Selection, new HyperLinkListener("https://www.eclipse.org/legal/epl-2.0/"));
      
      for (ACLImportStrategy importer : SCLPluginLoader.getInstance().getImportStrategiesMap().values())
      {
         Link linkPluginLicense = new Link(mShell, SWT.NONE);
         linkPluginLicense.setText("The " + importer.getName() + " plugin in version " + importer.getVersion() + " is licensed under <a>" + importer.getLicense() + "</a>");
         linkPluginLicense.addListener(SWT.Selection, new HyperLinkListener(importer.getLicenseSource()));
      }

      Label lblCopyright = new Label(mShell, SWT.NONE);
      lblCopyright.setText(CCLAssemblyInfo.getCopyright() + " " + CCLAssemblyInfo.getCompany());

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
    * Private inner class which opens a supplied link in the default browser
    * if supported.
    */
   private final class HyperLinkListener implements Listener
   {
      private final String mUrl; 
      
      public HyperLinkListener(String url)
      {
         this.mUrl = url;
      }

      @Override
      public void handleEvent(Event arg)
      {
         if (!mUrl.isEmpty() && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
         {
            try
            {
               Desktop.getDesktop().browse(new URI(mUrl));
            }
            catch (IOException | URISyntaxException e)
            {
               LOGGER.debug("Exception while opening the source of the license!", e);
            }
         }
      }
   }
}
