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
package com.qcadoo.plugin.internal.dependencymanager;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginState;

public class SimplePluginStatusResolver implements PluginStatusResolver {

    public boolean isPluginEnabled(final Plugin plugin) {
        return PluginState.ENABLED.equals(plugin.getState());
    }

    public boolean isPluginDisabled(final Plugin plugin) {
        return PluginState.DISABLED.equals(plugin.getState());
    }

    public boolean isPluginNotInstalled(final Plugin plugin) {
        return PluginState.TEMPORARY.equals(plugin.getState());
    }

}
