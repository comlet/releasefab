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
 * @file module-info.java
 *
 * @brief Module descriptor of module releasefab.library.
 */

import de.comlet.releasefab.api.plugin.ACLAssignmentStrategyExt;

module releasefab.library
{
   // JDK dependencies
   requires transitive java.desktop;
   requires transitive java.xml;
   requires transitive java.sql;
   
   // External dependencies
   requires transitive org.jdom2;
   requires transitive logback.classic;
   requires transitive logback.core;
   requires transitive org.slf4j;
   requires transitive org.eclipse.swt;
   
   // Test dependencies
   requires transitive junit;
   requires transitive org.junit.jupiter.api;
   requires transitive org.junit.jupiter.params;
   requires transitive org.xmlunit;
   
   uses ACLAssignmentStrategyExt;
   
   exports de.comlet.releasefab.api.plugin;
   exports de.comlet.releasefab.api.almservice;
   exports de.comlet.releasefab.api.vcsservice;
   exports de.comlet.releasefab.library.exception;
   exports de.comlet.releasefab.library.model;
   exports de.comlet.releasefab.library.xml;
   exports de.comlet.releasefab.library.plugins;
   exports de.comlet.releasefab.library.settings;
   exports de.comlet.releasefab.library.ui;
   exports de.comlet.releasefab.test.util;
}