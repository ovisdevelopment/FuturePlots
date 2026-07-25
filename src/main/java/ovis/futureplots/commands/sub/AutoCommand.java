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

import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.event.PlotClaimEvent;
import ovis.futureplots.event.PlotPreClaimEvent;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class AutoCommand extends SubCommand {

    private final FuturePlots plugin;

    public AutoCommand(FuturePlots plugin) {
        super(plugin, "auto");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.auto", "plots.perm.basic");
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        if(plotManager == null && this.plugin.getDefaultPlotLevel() == null || plotManager == null && (plotManager = this.plugin.getPlotManager(this.plugin.getDefaultPlotLevel())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_WORLD));
            return false;
        }

        final int ownedPlots = plotManager.getPlotsByOwner(player.getUniqueId()).size();
        if(!player.hasPermission("plot.limit.unlimited")) {
            int maxLimit = -1;
            for(String permission : player.getEffectivePermissions().keySet()) {
                if(permission.startsWith("plot.limit.")) {
                    try {
                        final String limitStr = permission.substring("plot.limit.".length());
                        if(limitStr.isBlank()) continue;
                        final int limit = Integer.parseInt(limitStr);

                        if(limit > maxLimit) maxLimit = limit;
                    } catch(NumberFormatException ignored) {
                    }
                }
            }

            if(maxLimit > 0 && ownedPlots >= maxLimit) {
                player.sendMessage(this.translate(player, TranslationKey.AUTO_FAILURE_TOO_MANY, ownedPlots));
                return false;
            }
        }

        final Plot plot = plotManager.getNextFreePlot();

        if(plot == null) {
            player.sendMessage(this.translate(player, TranslationKey.AUTO_FAILURE));
            return false;
        }

        final PlotPreClaimEvent plotPreClaimEvent = new PlotPreClaimEvent(player, plot, true, true, true);
        this.plugin.getServer().getPluginManager().callEvent(plotPreClaimEvent);

        if(plotPreClaimEvent.isCancelled()) {
            if(plotPreClaimEvent.isShowCancelMessage())
                player.sendMessage(this.translate(player, TranslationKey.AUTO_FAILURE));
            return false;
        }

        plot.setOwner(player.getUniqueId());
        if(plotPreClaimEvent.isBorderChanging())
            plotManager.changeBorder(plot, plotManager.getLevelSettings().getClaimPlotState());
        plotManager.savePlot(plot);

        final PlotClaimEvent plotClaimEvent = new PlotClaimEvent(player, plot, true);
        this.plugin.getServer().getPluginManager().callEvent(plotClaimEvent);

        plotManager.teleportPlayerToPlot(player, plot, false);
        player.sendMessage(this.translate(player, TranslationKey.AUTO_SUCCESS));
        return true;
    }

}
