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
 * @file CCLXMLDocBookSink.java
 *
 * @brief XML Docbook sink.
 */

package de.comlet.releasefab.library.xml;

import org.jdom2.Element;

/**
 * Base class for XML Docbook sinks.
 */
public class CCLXMLDocBookSink extends ACLDocBookSinkBase
{
   private static final int NUMBER_OF_COLUMNS = 2;
   private static final int COLUMN_WIDTH_ID = 2_000;
   private static final int COLUMN_WIDTH_DESCRIPTION = 6_000;

   /**
    * Constructor. Setup column data.
    */
   public CCLXMLDocBookSink(Element element, String componentName, String columnNameOne, String columnNameTwo)
   {
      super(element, componentName);
      CCLColumnSpec[] temp = new CCLColumnSpec[NUMBER_OF_COLUMNS];
      temp[0] = new CCLColumnSpec(columnNameOne, COLUMN_WIDTH_ID);
      temp[1] = new CCLColumnSpec(columnNameTwo, COLUMN_WIDTH_DESCRIPTION);
      init(temp);
   }
}
