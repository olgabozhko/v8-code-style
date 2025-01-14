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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.validation.marker.IExtraInfoKeys;
import com._1c.g5.v8.dt.validation.marker.Marker;
import com.e1c.g5.v8.dt.check.settings.CheckUid;
import com.e1c.g5.v8.dt.check.settings.ICheckSettings;
import com.e1c.v8codestyle.bsl.check.SelfReferenceCheck;
import com.e1c.v8codestyle.internal.bsl.BslPlugin;

/**
 * The test for class {@link SelfReferenceCheck}.
 *
 * @author Maxim Galios
 *
 */
public class SelfReferenceCheckTest
    extends AbstractSingleModuleTestBase
{

    private static final String PROJECT_NAME = "SelfReferenceCheck";

    private static final String COMMON_MODULE_FILE_NAME = "/src/CommonModules/MyCommonModule/Module.bsl";

    private static final String FORM_MODULE_FILE_NAME = "/src/CommonForms/MyForm/Module.bsl";

    private static final String OBJECT_MODULE_FILE_NAME = "/src/Catalogs/Products/ObjectModule.bsl";

    public SelfReferenceCheckTest()
    {
        super(SelfReferenceCheck.class);
    }

    @Override
    protected String getTestConfigurationName()
    {
        return PROJECT_NAME;
    }

    /**
     * Test ThisPbject/ЭтотОбъект references presence in common module
     *
     * @throws Exception
     */
    @Test
    public void testCommonModule() throws Exception
    {
        List<Marker> markers = getMarkers(COMMON_MODULE_FILE_NAME);
        assertEquals(4, markers.size());

        assertEquals("6", markers.get(0).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("6", markers.get(1).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("10", markers.get(2).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("10", markers.get(3).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
    }

    /**
     * Test ThisPbject/ЭтотОбъект references presence in form module
     *
     * @throws Exception
     */
    @Test
    public void testFormModule() throws Exception
    {
        List<Marker> markers = getMarkers(FORM_MODULE_FILE_NAME);
        assertEquals(3, markers.size());

        assertEquals("11", markers.get(0).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("12", markers.get(1).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("13", markers.get(2).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));

        IDtProject dtProject = getProject();
        IProject project = dtProject.getWorkspaceProject();

        ICheckSettings settings = checkRepository.getSettings(new CheckUid(getCheckId(), BslPlugin.PLUGIN_ID), project);
        settings.getParameters()
            .get(SelfReferenceCheck.PARAMETER_CHECK_ONLY_EXISTING_FORM_PROPERTIES)
            .setValue(Boolean.toString(false));
        checkRepository.applyChanges(Collections.singleton(settings), project);
        waitForDD(dtProject);

        List<Marker> markersAfterSettingsChange = getMarkers(FORM_MODULE_FILE_NAME);
        assertEquals(5, markersAfterSettingsChange.size());

        assertEquals("11",
            markersAfterSettingsChange.get(0).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("11",
            markersAfterSettingsChange.get(1).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("12",
            markersAfterSettingsChange.get(2).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("12",
            markersAfterSettingsChange.get(3).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("13",
            markersAfterSettingsChange.get(4).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
    }

    /**
     * Test ThisPbject/ЭтотОбъект references presence in object module
     *
     * @throws Exception
     */
    @Test
    public void testObjectModule() throws Exception
    {
        List<Marker> markers = getMarkers(OBJECT_MODULE_FILE_NAME);
        assertEquals(4, markers.size());

        assertEquals("8", markers.get(0).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("8", markers.get(1).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("9", markers.get(2).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
        assertEquals("9", markers.get(3).getExtraInfo().get(IExtraInfoKeys.TEXT_EXTRA_INFO_LINE_KEY));
    }

    private List<Marker> getMarkers(String moduleFileName)
    {
        String moduleId = Path.ROOT.append(getTestConfigurationName()).append(moduleFileName).toString();
        List<Marker> markers = List.of(markerManager.getMarkers(getProject().getWorkspaceProject(), moduleId));

        String chekcId = getCheckId();

        assertNotNull(chekcId);
        return markers.stream()
            .filter(marker -> chekcId.equals(getCheckIdFromMarker(marker, getProject())))
            .collect(Collectors.toList());
    }
}
