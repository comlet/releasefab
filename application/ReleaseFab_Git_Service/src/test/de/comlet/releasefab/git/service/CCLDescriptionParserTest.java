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
 * @file CCLDescriptionParserTest.java
 *
 * @brief Unit test class of {@link #CCLDescriptionParser}.
 */

package de.comlet.releasefab.git.service;

import de.comlet.releasefab.git.classes.CCLGitCommitContainer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CCLDescriptionParserTest
{
   private static final String ITEM_ID = "Item-ID: #{itemID}";
   private static final String API_MODIFICATION = "API-modification Y[]/N[X]";
   private static final String ITEM_ID_KEY = "Item-ID: #";
   private static final String INTERNAL_DOCUMENTATION_KEY = "internal documentation:";
   private static final String REVIEWED_BY_KEY = "reviewed by:";
   private static final String EXTERNAL_DOCUMENTATION_KEY = "external documentation:";
   private static final String TEST_EXTERNAL_DOCUMENTATION = " Test external documentation";
   private static final String TEST_INTERNAL_DOCUMENTATION = " Test internal documentation";
   private static final String TEST_REVIEWER = " TestReviewer";
   private static final String TEST_ITEM_ID = "00001";
   private static final String TEST_SHORT_DESCRIPTION = "Test short description";
   private static final String STANDARD_DELIMITER = "**********";
   
   private static final String COMMIT_TEMPLATE_STANDARD = "{short description}" + STANDARD_DELIMITER + ITEM_ID +
         STANDARD_DELIMITER + "API-modification Y[{yes}]/N[{no}]" + STANDARD_DELIMITER + "internal documentation:{internal doc}" +
         STANDARD_DELIMITER + "external documentation:{external doc}" + STANDARD_DELIMITER + "reviewed by:{reviewer}";

   private static final String COMMIT_TEMPLATE_OPTION_ONE = "{short description}" + "," + ITEM_ID + "," +
         "API-modification Y[{yes}]/N[{no}]" + "," + "internal documentation:{internal doc}" + "," +
         "external documentation:{external doc}" + "," + "reviewed by:{reviewer}";

   private static final String COMMIT_TEMPLATE_OPTION_TWO = "{short description}Item-ID: #{itemID}" +
         "API-modification Y[{yes}]/N[{no}]internal documentation:{internal doc}" +
         "external documentation:{external doc}reviewed by:{reviewer}";

   private static final String COMMIT_TEMPLATE_OPTION_THREE = ITEM_ID +
         "API-modification Y[{yes}]/N[{no}]internal documentation:{internal doc}" +
         "external documentation:{external doc}reviewed by:{reviewer},{short description}";

   private static final String COMMIT_TEMPLATE_OPTION_FOUR = "{short description},{itemID},{yes},{no},{internal doc},{external doc},{reviewer}";
   private static final String COMMIT_TEMPLATE_OPTION_FIVE = "{yes},{no},{internal doc},{short description}, {itemID},{external doc},{reviewer}";
   
   /**
    * Standard commit message
    */
   private static final String COMMIT_MESSAGE_STANDARD = TEST_SHORT_DESCRIPTION + STANDARD_DELIMITER + ITEM_ID_KEY + TEST_ITEM_ID + STANDARD_DELIMITER +
         API_MODIFICATION + STANDARD_DELIMITER + INTERNAL_DOCUMENTATION_KEY + TEST_INTERNAL_DOCUMENTATION +
         STANDARD_DELIMITER + EXTERNAL_DOCUMENTATION_KEY + TEST_EXTERNAL_DOCUMENTATION + STANDARD_DELIMITER +
         REVIEWED_BY_KEY + TEST_REVIEWER;

   /**
    * Commit message using "," as a delimiter.
    */
   private static final String COMMIT_MESSAGE_OPTION_ONE = TEST_SHORT_DESCRIPTION + "," + ITEM_ID_KEY + TEST_ITEM_ID + "," + API_MODIFICATION +
         "," + INTERNAL_DOCUMENTATION_KEY + TEST_INTERNAL_DOCUMENTATION + "," +
         EXTERNAL_DOCUMENTATION_KEY + TEST_EXTERNAL_DOCUMENTATION + "," + REVIEWED_BY_KEY + TEST_REVIEWER;
   
   /**
    * Commit message using different delimiters.
    */
   private static final String COMMIT_MESSAGE_OPTION_TWO = TEST_SHORT_DESCRIPTION + ITEM_ID_KEY + TEST_ITEM_ID +
         API_MODIFICATION + INTERNAL_DOCUMENTATION_KEY + TEST_INTERNAL_DOCUMENTATION +
         EXTERNAL_DOCUMENTATION_KEY + TEST_EXTERNAL_DOCUMENTATION + REVIEWED_BY_KEY + TEST_REVIEWER;
   
   /**
    * Commit message NOT starting with value to parse.
    */
   private static final String COMMIT_MESSAGE_OPTION_THREE = ITEM_ID_KEY + TEST_ITEM_ID +
         API_MODIFICATION + INTERNAL_DOCUMENTATION_KEY + TEST_INTERNAL_DOCUMENTATION +
         EXTERNAL_DOCUMENTATION_KEY + TEST_EXTERNAL_DOCUMENTATION + REVIEWED_BY_KEY + TEST_REVIEWER + "," + TEST_SHORT_DESCRIPTION;
   
   /**
    * Minimal commit message.
    */
   private static final String COMMIT_MESSAGE_OPTION_FOUR = TEST_SHORT_DESCRIPTION + "," + TEST_ITEM_ID + ", ,X," + TEST_INTERNAL_DOCUMENTATION + "," + TEST_EXTERNAL_DOCUMENTATION + "," + TEST_REVIEWER;
   
   /**
    * Minimal commit message in different order.
    */
   private static final String COMMIT_MESSAGE_OPTION_FIVE = ",X," + TEST_INTERNAL_DOCUMENTATION + "," + TEST_SHORT_DESCRIPTION + ", " + TEST_ITEM_ID + "," + TEST_EXTERNAL_DOCUMENTATION + "," + TEST_REVIEWER;
   
   /**
    * Minimal commit message in different order with special characters in values.
    */
   private static final String COMMIT_MESSAGE_OPTION_SIX = ",X," + TEST_INTERNAL_DOCUMENTATION + "!�$%&/()=?#+**-" + "," + TEST_SHORT_DESCRIPTION + ", " + TEST_ITEM_ID + "," + TEST_EXTERNAL_DOCUMENTATION + "," + TEST_REVIEWER;

   /**
    * Test parsing of all commit messages provided by {@link #provideParameters()}.
    */
   @ParameterizedTest
   @MethodSource("provideParameters")
   void testParseStandard(String message, String template)
   {
      CCLDescriptionParser parser = new CCLDescriptionParser(template);
      CCLGitCommitContainer result = parser.parse(message);

      assertAll(() -> assertEquals(TEST_SHORT_DESCRIPTION, result.getShortDescription()),
            () -> assertEquals(TEST_ITEM_ID, result.getCommitId()), 
            () -> assertEquals(false, result.getApiModification()),
            () -> assertEquals(TEST_INTERNAL_DOCUMENTATION, result.getInternalDoc()),
            () -> assertEquals(TEST_EXTERNAL_DOCUMENTATION, result.getExternalDoc()),
            () -> assertEquals(TEST_REVIEWER, result.getReviewer()));
   }
   
   /**
    * Provides the parameters for testing different commit template and 
    * message combinations.
    * 
    * @return parameters as a Stream of Arguments.
    */
   private static Stream<Arguments> provideParameters()
   {
      return Stream.of(
              Arguments.of(COMMIT_MESSAGE_STANDARD, COMMIT_TEMPLATE_STANDARD),
              Arguments.of(COMMIT_MESSAGE_OPTION_ONE, COMMIT_TEMPLATE_OPTION_ONE),
              Arguments.of(COMMIT_MESSAGE_OPTION_TWO, COMMIT_TEMPLATE_OPTION_TWO),
              Arguments.of(COMMIT_MESSAGE_OPTION_THREE, COMMIT_TEMPLATE_OPTION_THREE),
              Arguments.of(COMMIT_MESSAGE_OPTION_FOUR, COMMIT_TEMPLATE_OPTION_FOUR),
              Arguments.of(COMMIT_MESSAGE_OPTION_FIVE, COMMIT_TEMPLATE_OPTION_FIVE));
   }

   /**
    * Test parsing of {@link #sCommitMessageSix}.
    */
   @Test
   void testParseOptionSix()
   {
      CCLDescriptionParser parser = new CCLDescriptionParser(COMMIT_TEMPLATE_OPTION_FIVE);
      CCLGitCommitContainer result = parser.parse(COMMIT_MESSAGE_OPTION_SIX);

      assertAll(() -> assertEquals(TEST_SHORT_DESCRIPTION, result.getShortDescription()),
            () -> assertEquals(TEST_ITEM_ID, result.getCommitId()), 
            () -> assertEquals(false, result.getApiModification()),
            () -> assertEquals(TEST_INTERNAL_DOCUMENTATION + "!�$%&/()=?#+**-", result.getInternalDoc()),
            () -> assertEquals(TEST_EXTERNAL_DOCUMENTATION, result.getExternalDoc()),
            () -> assertEquals(TEST_REVIEWER, result.getReviewer()));
   }
}