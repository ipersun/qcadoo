/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.view.internal.components.layout;

import org.json.JSONException;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.internal.ComponentDefinition;
import com.qcadoo.view.internal.components.EmptyContainerState;

public class FlowLayoutPattern extends AbstractLayoutPattern {

    private static final String JS_OBJECT = "QCD.components.containers.layout.FlowLayout";

    private static final String JSP_PATH = "containers/layout/flowLayout.jsp";

    private static final String JS_PATH = "/qcadooView/public/js/crud/qcd/components/containers/layout/flowLayout.js";

    public FlowLayoutPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
    }

    @Override
    protected ComponentState getComponentStateInstance() {
        return new EmptyContainerState();
    }

    @Override
    public String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    public String getJsFilePath() {
        return JS_PATH;
    }

    @Override
    public String getJsObjectName() {
        return JS_OBJECT;
    }

}
