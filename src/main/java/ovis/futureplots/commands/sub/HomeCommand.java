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

import java.util.List;
import java.util.UUID;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class HomeCommand extends SubCommand {

    private final FuturePlots plugin;

    public HomeCommand(FuturePlots plugin) {
        super(plugin, "home");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.home", "plots.perm.basic");
        this.addParameter(CommandParameter.newType("id", true, CommandParamType.INT));
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        if(plotManager == null && this.plugin.getDefaultPlotLevel() == null || plotManager == null && (plotManager = this.plugin.getPlotManager(this.plugin.getDefaultPlotLevel())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_WORLD));
            return false;
        }

        if(args.length == 0) {
            final List<Plot> plots = plotManager.getPlotsByOwner(player.getUniqueId());

            if(plots.isEmpty()) {
                player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE_OWN));
                return false;
            }

            player.sendMessage(this.translate(player, TranslationKey.HOME_SUCCESS_OWN));
            plotManager.teleportPlayerToPlot(player, plots.get(0));
            return false;
        }

        if(args.length == 1) {
            try {
                final int plotId = Integer.parseInt(args[0]) - 1;
                final List<Plot> plots = plotManager.getPlotsByOwner(player.getUniqueId());

                if(plotId < 0 || plotId >= plots.size()) {
                    player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE_OWN_ID, plotId + 1));
                    return false;
                }

                player.sendMessage(this.translate(player, TranslationKey.HOME_SUCCESS_OWN));
                plotManager.teleportPlayerToPlot(player, plots.get(plotId));
                return false;
            } catch (NumberFormatException e) {
                final String targetName = this.plugin.findPlayerName(args[0]);
                final UUID targetId = this.plugin.getUniqueIdByName(targetName, false);

                if(targetName.isEmpty() || targetId == null) {
                    player.sendMessage(this.translate(player, TranslationKey.PLAYER_NOT_ONLINE, targetName));
                    return false;
                }

                final List<Plot> plots = plotManager.getPlotsByOwner(targetId);
                if(plots.isEmpty()) {
                    player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE, this.plugin.getCorrectName(targetId)));
                    return false;
                }

                final Plot plot = plots.get(0);
                final boolean canPerform = (!plot.isDenied(player.getUniqueId()) && !plot.isDenied(Utils.UUID_EVERYONE)) || player.hasPermission("plot.admin.bypass.deny");
                if(canPerform) {
                    player.sendMessage(this.translate(player, TranslationKey.HOME_SUCCESS, this.plugin.getCorrectName(targetId)));
                    plotManager.teleportPlayerToPlot(player, plots.get(0));
                    return false;
                }

                player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE_DENIED));
                return false;
            }
        }

        int plotId = Utils.parseInteger(args[1]) - 1;
        if(plotId < 0) plotId = 0;

        final String targetName = this.plugin.findPlayerName(args[0]);
        final UUID targetId = this.plugin.getUniqueIdByName(targetName, false);

        if(targetName.isEmpty() || targetId == null) {
            player.sendMessage(this.translate(player, TranslationKey.PLAYER_NOT_ONLINE, targetName));
            return false;
        }

        final List<Plot> plots = plotManager.getPlotsByOwner(targetId);
        if(plots.isEmpty()) {
            if(targetId.equals(player.getUniqueId()))
                player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE_OWN));
            else
                player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE, this.plugin.getCorrectName(targetId)));
            return false;
        }

        if(plotId >= plots.size()) {
            if(targetId.equals(player.getUniqueId()))
                player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE_OWN_ID, plotId + 1));
            else
                player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE_ID, this.plugin.getCorrectName(targetId), plotId + 1));
            return false;
        }

        final Plot plot = plots.get(plotId);
        final boolean canPerform = (!plot.isDenied(player.getUniqueId()) && !plot.isDenied(Utils.UUID_EVERYONE)) || player.hasPermission("plot.admin.bypass.deny") || plot.isTrusted(player.getUniqueId()) || (plot.isHelper(player.getUniqueId()) && this.plugin.getServer().getPlayer(targetName) != null);
        if(targetId.equals(player.getUniqueId())) {
            player.sendMessage(this.translate(player, TranslationKey.HOME_SUCCESS_OWN));
            plotManager.teleportPlayerToPlot(player, plots.get(plotId));
        } else if(canPerform) {
            player.sendMessage(this.translate(player, TranslationKey.HOME_SUCCESS, this.plugin.getCorrectName(targetId)));
            plotManager.teleportPlayerToPlot(player, plots.get(plotId));
        } else
            player.sendMessage(this.translate(player, TranslationKey.HOME_FAILURE_DENIED));
        return true;
    }

}
