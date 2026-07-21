/*
 * Copyright 2022 KCodeYT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified 2024 by tim03we, Ovis Development
 */

package ovis.futureplots.commands.sub;

import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;
import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.components.util.Plot;
import ovis.futureplots.components.util.Utils;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class WarpCommand extends SubCommand {

    private final FuturePlots plugin;

    public WarpCommand(FuturePlots plugin) {
        super(plugin, "warp");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.warp", "plots.perm.basic");
        this.addParameter(CommandParameter.newType("id", CommandParamType.ARG));
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        if(plotManager == null && this.plugin.getDefaultPlotLevel() == null || plotManager == null && (plotManager = this.plugin.getPlotManager(this.plugin.getDefaultPlotLevel())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_WORLD));
            return false;
        }

        final String[] plotIds = args.length > 0 ?
                args[0].split(";").length > 1 ? args[0].split(";") :
                        args[0].split(":").length > 1 ? args[0].split(":") :
                                args[0].split(",").length > 1 ? args[0].split(",") :
                                        new String[0] : new String[0];

        final Integer plotX = plotIds.length != 0 ? Utils.parseIntegerWithNull(plotIds[0]) : null;
        final Integer plotZ = plotIds.length != 0 ? Utils.parseIntegerWithNull(plotIds[1]) : null;

        if(plotX == null || plotZ == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_ID));
            return false;
        }

        final Plot plot = plotManager.getPlotById(plotX, plotZ);

        if(!plot.hasOwner() && !player.hasPermission("plot.command.warp.free")) {
            player.sendMessage(this.translate(player, TranslationKey.WARP_FAILURE_FREE));
            return false;
        }

        final boolean isDenied = plot.isDenied(player.getUniqueId()) || plot.isDenied(Utils.UUID_EVERYONE);
        final boolean isOwnerOrHelper = plot.isOwner(player.getUniqueId()) || plot.isHelper(player.getUniqueId()) || plot.isHelper(Utils.UUID_EVERYONE);
        final boolean hasPermission = player.hasPermission("plot.admin.bypass.deny");

        if(isDenied && !isOwnerOrHelper && !hasPermission) {
            player.sendMessage(this.translate(player, TranslationKey.WARP_FAILURE));
             return false;
        }

        plotManager.teleportPlayerToPlot(player, plot);
        player.sendMessage(this.translate(player, TranslationKey.WARP_SUCCESS, (plotX + ";" + plotZ)));
        return true;
    }

}
