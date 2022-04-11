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
 * @file CCLXMLConstants.java
 *
 * @brief Global XML constants.
 */

package de.comlet.releasefab.library.xml;

/**
 * Common constants used for working with XML-Files.
 */
public final class CCLXMLConstants
{
   public static final String XML_ATTRIBUTE_COLNUM = "colnum";
   public static final String XML_ATTRIBUTE_COLS = "cols";
   public static final String XML_ATTRIBUTE_COLWIDTH = "colwidth";
   public static final String XML_ATTRIBUTE_CONFORMANCE = "conformance";
   public static final String XML_ATTRIBUTE_NAME = "name";
   public static final String XML_ATTRIBUTE_PGWIDE = "pgwide";
   public static final String XML_ATTRIBUTE_RELEVANT = "relevant";
   public static final String XML_ATTRIBUTE_REQUEST_TYPE = "request_type";
   public static final String XML_ATTRIBUTE_REQUEST_TYPE_OTHER = "Other";
   public static final String XML_ATTRIBUTE_ROLE = "role";
   public static final String XML_ATTRIBUTE_TABSTYLE = "tabstyle";
   public static final String XML_ATTRIBUTE_TYPE = "type";
   public static final String XML_ATTRIBUTE_CONTENT_TYPE = "contentType";
   public static final String XML_ATTRIBUTE_KEY_TYPE = "keyType";
   public static final String XML_ATTRIBUTE_VALUE_TYPE = "valueType";
   public static final String XML_ALM_USER_KEY = "ALM_USER";
   public static final String XML_ALM_PASSWORD_KEY = "ALM_PASSWORD";

   public static final String XML_ASSIGNER = "assigner";
   public static final String XML_COLSPEC = "colspec";
   public static final String XML_COMPONENT = "component";
   public static final String XML_COMPONENTS = "components";
   public static final String XML_CONFIGURATION = "configuration";
   public static final String XML_CONTENT = "content";
   public static final String XML_DELIVERY_INFORMATION = "deliveryInformation";
   public static final String XML_EMPHASIS = "emphasis";
   public static final String XML_ENTRY = "entry";
   public static final String XML_ERROR = "error";
   public static final String XML_ID = "id";
   public static final String XML_IMPORTER = "importer";
   public static final String XML_ISSUE = "issue";
   public static final String XML_ITEM = "item";
   public static final String XML_LITERALLAYOUT = "literallayout";
   public static final String XML_PARA = "para";
   public static final String XML_RELEASE = "release";
   public static final String XML_REQUEST = "request";
   public static final String XML_REQUEST_TYPE = "request_type";
   public static final String XML_ROW = "row";
   public static final String XML_SECTION = "section";
   public static final String XML_SETTING = "setting";
   public static final String XML_STATUS = "status";
   public static final String XML_STRING = "string";

   // Docbook tags
   public static final String XML_SUBTITLE = "subtitle";
   public static final String XML_TGROUP = "tgroup";
   public static final String XML_THEAD = "thead";
   public static final String XML_TITLE = "title";
   public static final String XML_TABLE = "table";
   public static final String XML_TBODY = "tbody";

   public static final String XML_MAP_KEY = "key";
   public static final String XML_MAP_VALUE = "value";

   // Settings tags
   public static final String XML_TUPLE_FIRST = "first";
   public static final String XML_TUPLE_SECOND = "second";
   public static final String XML_ALM_WINDOWS_CERTIFICATE_PATH = "ALM_CERTIFICATE_PATH_WINDOWS";
   public static final String XML_ALM_UNIX_CERTIFICATE_PATH = "ALM_CERTIFICATE_PATH_LINUX";
   public static final String XML_ALM_SERVER = "ALM_SERVER";
   public static final String XML_COMMIT_TEMPLATE = "COMMIT_TEMPLATE";
   public static final String XML_ROOT_FORMAT = "XML_ROOT_FORMAT";
   public static final String XML_INCLUDE_MERGE_COMMITS = "INCLUDE_MERGE_COMMITS";

   private CCLXMLConstants()
   {
   }
}
