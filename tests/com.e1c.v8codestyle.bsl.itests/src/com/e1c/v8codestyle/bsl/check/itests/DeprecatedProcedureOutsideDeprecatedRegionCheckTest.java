/*******************************************************************************
 * Copyright (C) 2022, 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     1C-Soft LLC - initial API and implementation
 *******************************************************************************/
package com.e1c.v8codestyle.bsl.check.itests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Path;
import org.junit.Test;

import com._1c.g5.v8.dt.validation.marker.IExtraInfoKeys;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.v8codestyle.bsl.check.DeprecatedProcedureOutsideDeprecatedRegionCheck;

/**
 * Tests for {@link DeprecatedProcedureOutsideDeprecatedRegionCheck} check.
 *
 * @author Olga Bozhko
 */
public class DeprecatedProcedureOutsideDeprecatedRegionCheckTest
    extends AbstractSingleModuleTestBase
{
    private static final String MESSAGE =
        "The deprecated procedure (function) {0} should be placed in the Deprecated region of the Public region in a common module area";
    private static final String DEPRECATED_PROCEDURE_NAME = "\"DeprecatedProcedure\"";
    private static final String MANAGER_MODULE_FILE_NAME = "/src/Catalogs/Catalog/ManagerModule.bsl";

    public DeprecatedProcedureOutsideDeprecatedRegionCheckTest()
    {
        super(DeprecatedProcedureOutsideDeprecatedRegionCheck.class);
    }

    @Override
    protected String getTestConfigurationName()
    {
        return "DeprecatedProcedureOutsideDeprecatedRegion";
    }
    /**
     * Test a deprecated method is placed in the Deprecated region of the Public region in a common module area
     *
     * @throws Exception the exception
     */
    @Test
    public void testCompliantDeprecatedMethod() throws Exception
    {
        updateModule(FOLDER_RESOURCE + "deprecated-method-compliant.bsl");

        List<Marker> markers = getModuleMarkers();
        assertTrue(markers.isEmpty());
    }

    /**
     * Test a non-export deprecated method
     *
     * @throws Exception the exception
     */
    @Test
    public void testNonExportDeprecatedMethod() throws Exception
    {
        updateModule(FOLDER_RESOURCE + "deprecated-method-non-export.bsl");

        List<Marker> markers = getModuleMarkers();
        assertTrue(markers.isEmpty());
    }

    /**
     * Test a deprecated method is placed outside the Deprecated region
     *
     * @throws Exception the exception
     */
    @Test
    public void testNonCompliantDeprecatedMethod() throws Exception
    {
        updateModule(FOLDER_RESOURCE + "deprecated-method-non-compliant.bsl");

        List<Marker> markers = getModuleMarkers();
        assertEquals(1, markers.size());
        assertEquals("4", markers.get(0).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals(MessageFormat.format(MESSAGE, DEPRECATED_PROCEDURE_NAME), markers.get(0).getMessage());
    }

    /**
     * Test a deprecated method is placed in Manager module area
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeprecatedMethodInManagerModule() throws Exception
    {
        String moduleId = Path.ROOT.append(getTestConfigurationName()).append(MANAGER_MODULE_FILE_NAME).toString();
        List<Marker> markers = List.of(markerManager.getMarkers(getProject().getWorkspaceProject(), moduleId));

        String checkId = getCheckId();

        assertNotNull(checkId);
        List<Marker> markersToCheck = markers.stream()
            .filter(marker -> checkId.equals(getCheckIdFromMarker(marker, getProject())))
            .collect(Collectors.toList());
        assertEquals(1, markersToCheck.size());
        assertEquals("7", markersToCheck.get(0).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals(MessageFormat.format(MESSAGE, DEPRECATED_PROCEDURE_NAME), markersToCheck.get(0).getMessage());
    }
}