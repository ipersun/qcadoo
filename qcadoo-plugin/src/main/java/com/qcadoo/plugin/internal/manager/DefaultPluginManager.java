/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 0.4.0
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

package com.qcadoo.plugin.internal.manager;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.plugin.api.PluginServerManager;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.PluginException;
import com.qcadoo.plugin.internal.api.PluginArtifact;
import com.qcadoo.plugin.internal.api.PluginDao;
import com.qcadoo.plugin.internal.api.PluginDependencyManager;
import com.qcadoo.plugin.internal.api.PluginDescriptorParser;
import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;
import com.qcadoo.plugin.internal.api.PluginFileManager;
import com.qcadoo.plugin.internal.api.PluginOperationResult;
import com.qcadoo.plugin.internal.dependencymanager.PluginDependencyResult;

@Service
public final class DefaultPluginManager implements PluginManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginManager.class);

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private PluginDao pluginDao;

    @Autowired
    private PluginFileManager pluginFileManager;

    private PluginServerManager pluginServerManager;

    @Autowired
    private PluginDependencyManager pluginDependencyManager;

    @Autowired
    private PluginDescriptorParser pluginDescriptorParser;

    @Autowired
    private PluginDescriptorResolver pluginDescriptorResolver;

    @Override
    public PluginOperationResult enablePlugin(final String... keys) {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (String key : keys) {
            Plugin plugin = pluginAccessor.getPlugin(key);

            if (!plugin.hasState(PluginState.ENABLED)) {
                plugins.add(plugin);
            }
        }

        if (plugins.isEmpty()) {
            return PluginOperationResult.success();
        }

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToEnable(plugins);

        if (!pluginDependencyResult.isDependenciesSatisfied()) {
            if (!pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                return PluginOperationResult.unsatisfiedDependencies(pluginDependencyResult);
            }

            if (!pluginDependencyResult.getDependenciesToEnable().isEmpty()) {
                return PluginOperationResult.dependenciesToEnable(pluginDependencyResult);
            }
        }

        boolean shouldRestart = false;

        List<String> fileNames = new ArrayList<String>();
        for (Plugin plugin : plugins) {
            if (plugin.hasState(PluginState.TEMPORARY)) {
                fileNames.add(plugin.getFilename());
            }
        }
        if (!fileNames.isEmpty()) {
            if (!pluginFileManager.installPlugin(fileNames.toArray(new String[fileNames.size()]))) {
                return PluginOperationResult.cannotInstallPlugin();
            }
            shouldRestart = true;
        }

        plugins = pluginDependencyManager.sortPluginsInDependencyOrder(plugins);
        for (Plugin plugin : plugins) {
            if (plugin.hasState(PluginState.TEMPORARY)) {
                plugin.changeStateTo(PluginState.ENABLING);
            } else {
                plugin.changeStateTo(PluginState.ENABLED);
            }
            pluginDao.save(plugin);
            pluginAccessor.savePlugin(plugin);
        }

        if (shouldRestart) {
            return PluginOperationResult.successWithRestart();
        } else {
            return PluginOperationResult.success();
        }

    }

    @Override
    public PluginOperationResult disablePlugin(final String... keys) {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (String key : keys) {
            Plugin plugin = pluginAccessor.getPlugin(key);

            if (plugin.isSystemPlugin()) {
                return PluginOperationResult.systemPluginDisabling();
            }

            if (plugin.hasState(PluginState.ENABLED)) {
                plugins.add(plugin);
            }
        }

        if (plugins.isEmpty()) {
            return PluginOperationResult.success();
        }

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToDisable(plugins);

        if (!pluginDependencyResult.isDependenciesSatisfied() && !pluginDependencyResult.getDependenciesToDisable().isEmpty()) {
            return PluginOperationResult.dependenciesToDisable(pluginDependencyResult);
        }

        plugins = pluginDependencyManager.sortPluginsInDependencyOrder(plugins);
        Collections.reverse(plugins);
        for (Plugin plugin : plugins) {
            plugin.changeStateTo(PluginState.DISABLED);
            pluginDao.save(plugin);
            pluginAccessor.savePlugin(plugin);
        }

        return PluginOperationResult.success();
    }

    @Override
    public PluginOperationResult uninstallPlugin(final String... keys) {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (String key : keys) {
            Plugin plugin = pluginAccessor.getPlugin(key);

            if (plugin.isSystemPlugin()) {
                return PluginOperationResult.systemPluginUninstalling();
            }

            plugins.add(plugin);
        }

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToUninstall(plugins);

        if (!pluginDependencyResult.isDependenciesSatisfied() && !pluginDependencyResult.getDependenciesToUninstall().isEmpty()) {
            return PluginOperationResult.dependenciesToUninstall(pluginDependencyResult);
        }

        boolean shouldRestart = false;

        List<String> fileNames = new ArrayList<String>();
        for (Plugin plugin : plugins) {
            if (!plugin.hasState(PluginState.TEMPORARY)) {
                shouldRestart = true;
            }
            fileNames.add(plugin.getFilename());
        }

        pluginFileManager.uninstallPlugin(fileNames.toArray(new String[fileNames.size()]));

        plugins = pluginDependencyManager.sortPluginsInDependencyOrder(plugins);
        Collections.reverse(plugins);
        for (Plugin plugin : plugins) {
            if (plugin.hasState(PluginState.ENABLED)) {
                plugin.changeStateTo(PluginState.DISABLED);
            }
            pluginDao.delete(plugin);
            pluginAccessor.removePlugin(plugin);
        }

        if (shouldRestart) {
            return PluginOperationResult.successWithRestart();
        } else {
            return PluginOperationResult.success();
        }
    }

    @Override
    public PluginOperationResult installPlugin(final PluginArtifact pluginArtifact) {
        File pluginFile = null;
        try {
            pluginFile = pluginFileManager.uploadPlugin(pluginArtifact);
        } catch (PluginException e) {
            return PluginOperationResult.cannotUploadPlugin();
        }
        Plugin plugin = null;
        try {
            Resource descriptor = pluginDescriptorResolver.getDescriptor(pluginFile);
            plugin = pluginDescriptorParser.parse(descriptor);
        } catch (PluginException e) {
            pluginFileManager.uninstallPlugin(pluginFile.getName());
            return PluginOperationResult.corruptedPlugin();
        }

        pluginFileManager.renamePlugin(pluginFile.getName(), plugin.getFilename());

        if (plugin.isSystemPlugin()) {
            pluginFileManager.uninstallPlugin(plugin.getFilename());
            return PluginOperationResult.systemPluginUpdating();
        }

        boolean shouldRestart = false;

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToEnable(newArrayList(plugin));

        Plugin existingPlugin = pluginAccessor.getPlugin(plugin.getIdentifier());
        if (existingPlugin == null) {
            plugin.changeStateTo(PluginState.TEMPORARY);
            pluginDao.save(plugin);
            pluginAccessor.savePlugin(plugin);

            if (!pluginDependencyResult.isDependenciesSatisfied()
                    && !pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                return PluginOperationResult.successWithMissingDependencies(pluginDependencyResult);
            } else {
                return PluginOperationResult.success();
            }
        } else {
            if (existingPlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                pluginFileManager.uninstallPlugin(plugin.getFilename());
                return PluginOperationResult.cannotDowngradePlugin();
            }
            if (existingPlugin.hasState(PluginState.TEMPORARY)) {
                if (!pluginDependencyResult.isDependenciesSatisfied()
                        && !pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                    pluginFileManager.uninstallPlugin(existingPlugin.getFilename());
                    plugin.changeStateTo(existingPlugin.getState());
                    pluginDao.save(plugin);
                    pluginAccessor.savePlugin(plugin);
                    return PluginOperationResult.successWithMissingDependencies(pluginDependencyResult);
                }
                plugin.changeStateTo(existingPlugin.getState());
            } else if (existingPlugin.hasState(PluginState.DISABLED)) {
                if (!pluginDependencyResult.isDependenciesSatisfied()
                        && !pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                    pluginFileManager.uninstallPlugin(plugin.getFilename());
                    return PluginOperationResult.unsatisfiedDependencies(pluginDependencyResult);
                }
                if (!pluginFileManager.installPlugin(plugin.getFilename())) {
                    pluginFileManager.uninstallPlugin(plugin.getFilename());
                    return PluginOperationResult.cannotInstallPlugin();
                }
                shouldRestart = true;
                plugin.changeStateTo(existingPlugin.getState());
            } else if (existingPlugin.hasState(PluginState.ENABLED)) {
                if (!pluginDependencyResult.isDependenciesSatisfied()) {
                    if (!pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                        pluginFileManager.uninstallPlugin(plugin.getFilename());
                        return PluginOperationResult.unsatisfiedDependencies(pluginDependencyResult);
                    }

                    if (!pluginDependencyResult.getDependenciesToEnable().isEmpty()) {
                        pluginFileManager.uninstallPlugin(plugin.getFilename());
                        return PluginOperationResult.dependenciesToEnable(pluginDependencyResult);
                    }
                }
                if (!pluginFileManager.installPlugin(plugin.getFilename())) {
                    pluginFileManager.uninstallPlugin(plugin.getFilename());
                    return PluginOperationResult.cannotInstallPlugin();
                }
                shouldRestart = true;
                PluginDependencyResult installPluginDependencyResult = pluginDependencyManager.getDependenciesToUpdate(
                        existingPlugin, plugin);

                if (!installPluginDependencyResult.getDependenciesToDisableUnsatisfiedAfterUpdate().isEmpty()) {
                    pluginFileManager.uninstallPlugin(plugin.getFilename());
                    return PluginOperationResult.unsatisfiedDependenciesAfterUpdate(installPluginDependencyResult);
                }

                List<Plugin> dependencyPlugins = new ArrayList<Plugin>();
                for (PluginDependencyInformation pluginDependencyInformation : installPluginDependencyResult
                        .getDependenciesToDisable()) {
                    dependencyPlugins.add(pluginAccessor.getPlugin(pluginDependencyInformation.getDependencyPluginIdentifier()));
                }
                dependencyPlugins = pluginDependencyManager.sortPluginsInDependencyOrder(dependencyPlugins);
                Collections.reverse(dependencyPlugins);
                for (Plugin dependencyPlugin : dependencyPlugins) {
                    dependencyPlugin.changeStateTo(PluginState.DISABLED);
                }

                existingPlugin.changeStateTo(PluginState.DISABLED);
                plugin.changeStateTo(PluginState.ENABLING);

                Collections.reverse(dependencyPlugins);
                for (Plugin dependencyPlugin : dependencyPlugins) {
                    dependencyPlugin.changeStateTo(PluginState.ENABLING);
                    pluginDao.save(dependencyPlugin);
                }
            }
            pluginFileManager.uninstallPlugin(existingPlugin.getFilename());
            pluginDao.save(plugin);
            pluginAccessor.savePlugin(plugin);
            if (shouldRestart) {
                return PluginOperationResult.successWithRestart();
            } else {
                return PluginOperationResult.success();
            }
        }
    }

    void setPluginAccessor(final PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;

    }

    void setPluginDao(final PluginDao pluginDao) {
        this.pluginDao = pluginDao;

    }

    void setPluginFileManager(final PluginFileManager pluginFileManager) {
        this.pluginFileManager = pluginFileManager;
    }

    void setPluginServerManager(final PluginServerManager pluginServerManager) {
        this.pluginServerManager = pluginServerManager;
    }

    void setPluginDependencyManager(final PluginDependencyManager pluginDependencyManager) {
        this.pluginDependencyManager = pluginDependencyManager;
    }

    void setPluginDescriptorParser(final PluginDescriptorParser pluginDescriptorParser) {
        this.pluginDescriptorParser = pluginDescriptorParser;
    }

    void setPluginDescriptorResolver(final PluginDescriptorResolver pluginDescriptorResolver) {
        this.pluginDescriptorResolver = pluginDescriptorResolver;
    }

}