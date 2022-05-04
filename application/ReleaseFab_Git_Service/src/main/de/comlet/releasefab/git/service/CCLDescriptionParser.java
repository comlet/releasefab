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
 * @file CCLCommitFilter.java
 *
 * @brief Parses information from Git commit messages.
 */

package de.comlet.releasefab.git.service;

import de.comlet.releasefab.git.classes.CCLGitCommitContainer;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Parse Git commit message into attributes of {@link #CCLGitCommitContainer}.
 */
public final class CCLDescriptionParser
{
   private static final String SHORT_DESC_KEY = "short description";
   private static final String ITEM_ID_KEY = "itemID";
   private static final String API_YES_KEY = "yes";
   private static final String INTERNAL_DOC_KEY = "internal doc";
   private static final String EXTERNAL_DOC_KEY = "external doc";
   private static final String REVIEWER_KEY = "reviewer";

   private String mCommitTemplate;

   public CCLDescriptionParser(String template)
   {
      mCommitTemplate = template;
   }

   /**
    * Parse Git commit description.
    * There should be 6 entries in the commit template: - short description - Item-ID -
    * API-modification - internal documentation - external documentation -
    * reviewed by
    */
   public CCLGitCommitContainer parse(String inputString)
   {
      // Copy inputs and declare result variables
      String input = inputString;
      String template = mCommitTemplate;
      CCLGitCommitContainer retValue = null;
      boolean apiMod = false;
      Map<String, String> entryMap = new HashMap<String, String>();
      
      // Trim the start of the template and input before the first value
      input = StringUtils.substringAfter(input, StringUtils.substringBefore(template, "{"));
      template = "{" + StringUtils.substringAfter(template, "{");

      // Get delimiter to check that the format of the template is correct
      String delimiter = StringUtils.substringBetween(template, "}", "{");
      while (template.contains("{") && !delimiter.isEmpty())
      {
         // Retrieve key between {} from template
         // Retrieve delimiter between two value fields from template (between } and {)
         // Retrieve the substring before that delimiter from the input as the value
         String key = StringUtils.substringBetween(template, "{", "}");
         delimiter = StringUtils.substringBetween(template, "}", "{");
         String value = StringUtils.substringBefore(input, delimiter);

         // If the YES field of the API modification is marked with an "X"
         // set apiMod to true
         if (key.equals(API_YES_KEY) && !value.isBlank())
         {
            apiMod = true;
         }
         else
         {
            // Else add key and value to the map
            entryMap.put(key, value);
         }

         // Use everything after the delimiter in the next iteration
         // Input and template need to start with a value
         input = StringUtils.substringAfter(input, delimiter);
         template = StringUtils.substringAfter(template, delimiter);
      }

      // No fields are mandatory for a CCLGitCommitContainer
      // All missing values are null
      retValue = new CCLGitCommitContainer("", entryMap.get(ITEM_ID_KEY), 0,
            entryMap.get(SHORT_DESC_KEY), entryMap.get(INTERNAL_DOC_KEY), entryMap.get(EXTERNAL_DOC_KEY),
            entryMap.get(REVIEWER_KEY), apiMod);
      
      return retValue;
   }
}
