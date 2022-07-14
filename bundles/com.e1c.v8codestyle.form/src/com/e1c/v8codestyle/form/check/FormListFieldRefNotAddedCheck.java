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
package com.e1c.v8codestyle.form.check;

import static com._1c.g5.v8.dt.form.model.FormPackage.Literals.FORM_ITEM_CONTAINER;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.event.BmSubEvent;
import com._1c.g5.v8.dt.form.model.AbstractDataPath;
import com._1c.g5.v8.dt.form.model.ColumnGroupExtInfo;
import com._1c.g5.v8.dt.form.model.DynamicListExtInfo;
import com._1c.g5.v8.dt.form.model.Form;
import com._1c.g5.v8.dt.form.model.FormAttribute;
import com._1c.g5.v8.dt.form.model.FormField;
import com._1c.g5.v8.dt.form.model.FormGroup;
import com._1c.g5.v8.dt.form.model.FormItem;
import com._1c.g5.v8.dt.form.model.FormPackage;
import com._1c.g5.v8.dt.form.model.Table;
import com._1c.g5.v8.dt.metadata.dbview.DbViewDef;
import com._1c.g5.v8.dt.metadata.dbview.DbViewFieldDef;
import com.e1c.g5.v8.dt.check.CheckComplexity;
import com.e1c.g5.v8.dt.check.EIssue;
import com.e1c.g5.v8.dt.check.ICheck;
import com.e1c.g5.v8.dt.check.ICheckDefinition;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.ICheckResultAcceptor;
import com.e1c.g5.v8.dt.check.context.CheckContextCollectingSession;
import com.e1c.g5.v8.dt.check.context.OnModelFeatureChangeContextCollector;
import com.e1c.g5.v8.dt.check.settings.IssueSeverity;
import com.e1c.g5.v8.dt.check.settings.IssueType;

/**
 * Check if the Reference field is added to dynamic list.
 *
 * @author Olga Bozhko
 */
public class FormListFieldRefNotAddedCheck
    implements ICheck
{
    private static final String CHECK_ID = "form-list-field-ref-not-added"; //$NON-NLS-1$
    private static final Set<EClass> TARGET_CONTAINMENT = new HashSet<>(Arrays.asList(FORM_ITEM_CONTAINER));
    private static final List<String> REF_ABSTRACT_DATA_PATH = List.of("Ref", "Ссылка"); //$NON-NLS-1$ //$NON-NLS-2$
    private static final Predicate<? super DbViewFieldDef> FIELD_NAME_CHECK =
        name -> (name.getName() != null) && name.getName().equals(REF_ABSTRACT_DATA_PATH.get(0));

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    public void configureContextCollector(ICheckDefinition definition)
    {
        definition.addCheckedModelObjects(FormPackage.Literals.FORM, false, TARGET_CONTAINMENT);
        definition.addModelFeatureChangeContextCollector(new ObjectCollectionFeatureChangeContextCollector(),
            FORM_ITEM_CONTAINER);
        definition.setTitle(Messages.FormListFieldRefNotAddedCheck_title);
        definition.setDescription(Messages.FormListFieldRefNotAddedCheck_description);
        definition.setComplexity(CheckComplexity.NORMAL);
        definition.setDefaultSeverity(IssueSeverity.MINOR);
        definition.setIssueType(IssueType.UI_STYLE);
    }

    @Override
    public void check(Object object, ICheckResultAcceptor resultAcceptor, ICheckParameters params,
        IProgressMonitor progressMonitor)
    {
        if (object instanceof Table && ((Table)object).getDataPath() != null)
        {

            Table table = (Table)object;
            if (checkTable(table) && !pathCheck(table.getItems()))
            {
                EIssue issue =
                    new EIssue(Messages.FormListFieldRefNotAddedCheck_The_Ref_field_is_not_added_to_dynamic_list, null);
                resultAcceptor.addIssue(table, issue);
            }

        }

    }

    private static boolean pathCheck(EList<FormItem> items)
    {
        if (!items.isEmpty())
        {
            for (int i = 0; i < items.size(); i++)
            {
                if (items.get(i) instanceof FormField && !items.get(i)
                    .eContents()
                    .stream()
                    .filter(AbstractDataPath.class::isInstance)
                    .filter(item -> itemPathCheck((AbstractDataPath)item))
                    .collect(Collectors.toList())
                    .isEmpty())
                {
                    return true;
                }
                if (items.get(i) instanceof FormGroup && pathCheck(((FormGroup)items.get(i)).getItems()))
                {
                    return true;

                }
            }
        }
        return false;
    }

    private static boolean itemPathCheck(AbstractDataPath path)
    {
        EList<String> segments = path.getSegments();
        if (segments.isEmpty())
        {
            return false;
        }

        String lastSegment = segments.get(segments.size() - 1);
        return lastSegment.equals(REF_ABSTRACT_DATA_PATH.get(0)) || lastSegment.equals(REF_ABSTRACT_DATA_PATH.get(1));
    }

    private static boolean checkTable(Table table)
    {
        if (table != null && table.getDataPath() != null)
        {
            FormAttribute formAttribute = (FormAttribute)table.bmGetTopObject()
                .eContents()
                .stream()
                .filter(FormAttribute.class::isInstance)
                .findAny()
                .orElse(null);
            if (formAttribute != null)
            {
                DbViewDef dbViewDef = ((DynamicListExtInfo)formAttribute.getExtInfo()).getMainTable();
                if (dbViewDef != null && !dbViewDef.eIsProxy()
                    && dbViewDef.getFields().stream().anyMatch(FIELD_NAME_CHECK))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class ObjectCollectionFeatureChangeContextCollector
        implements OnModelFeatureChangeContextCollector
    {
        @Override
        public void collectContextOnFeatureChange(IBmObject object, EStructuralFeature feature, BmSubEvent bmEvent,
            CheckContextCollectingSession contextSession)
        {
            Table table = null;
            if (object instanceof Form)
            {
                table = (Table)object.eContents().stream().filter(Table.class::isInstance).findAny().orElse(null);
            }
            if (object instanceof Table)
            {
                table = (Table)object;
            }
            if (object instanceof FormGroup && ((FormGroup)object).getExtInfo() instanceof ColumnGroupExtInfo
                && checkIfTable(((Form)((FormGroup)object).bmGetTopObject()).getItems()))
            {
                table = (Table)((Form)((FormGroup)object).bmGetTopObject()).getItems()
                    .stream()
                    .filter(Table.class::isInstance)
                    .collect(Collectors.toList())
                    .get(0);
            }

            if (checkTable(table))
            {
                contextSession.addModelCheck(table);
            }
        }

        private static boolean checkIfTable(EList<FormItem> formItems)
        {

            return !formItems.stream().noneMatch(Table.class::isInstance);
        }
    }
}
