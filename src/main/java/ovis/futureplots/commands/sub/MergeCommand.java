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
import org.powernukkitx.math.NukkitMath;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.components.provider.economy.client.EconomyClient;
import ovis.futureplots.event.PlotMergeEvent;
import ovis.futureplots.event.PlotPreMergeEvent;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;

import java.util.Set;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class MergeCommand extends SubCommand {

    private final FuturePlots plugin;

    public MergeCommand(FuturePlots plugin) {
        super(plugin, "merge");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.merge");
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        final Plot plot;
        if(plotManager == null || (plot = plotManager.getMergedPlot(player.getFloorX(), player.getFloorZ())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT));
            return false;
        }

        final int dir = (NukkitMath.floorDouble((player.getYaw() * 4 / 360) + 0.5) - 2) & 3;
        final Set<Plot> plotsToMerge = plotManager.calculatePlotsToMerge(plot, dir);

        if(!player.hasPermission("plot.command.admin.merge")) {
            for(Plot plotToMerge : plotsToMerge) {
                if(!plotToMerge.isOwner(player.getUniqueId())) {
                    player.sendMessage(this.translate(player, TranslationKey.MERGE_FAILURE_OWNER));
                    return false;
                }
            }
        }

        if(!player.hasPermission("plot.merge.limit.unlimited")) {
            int maxLimit = -1;
            for(String permission : player.getEffectivePermissions().keySet()) {
                if(permission.startsWith("plot.merge.limit.")) {
                    try {
                        final String limitStr = permission.substring("plot.merge.limit.".length());
                        if(limitStr.isBlank()) continue;
                        final int limit = Integer.parseInt(limitStr);

                        if(limit > maxLimit) maxLimit = limit;
                    } catch(NumberFormatException ignored) {
                    }
                }
            }

            if(maxLimit > 0 && plotsToMerge.size() > maxLimit) {
                player.sendMessage(this.translate(player, TranslationKey.MERGE_FAILURE_TOO_MANY, plotsToMerge.size()));
                return false;
            }
        }

        if(plot.isMerged(dir)) {
            player.sendMessage(this.translate(player, TranslationKey.MERGE_FAILURE_ALREADY_MERGED));
            return false;
        }

        if(FuturePlots.getSettings().isEconomyEnabled() && FuturePlots.getSettings().getEconomyWorlds().contains(plotManager.getLevelName()) && !player.hasPermission("plot.economy.bypass")) {
            EconomyClient economyClient = this.plugin.getEconomyProvider().getEconomyClient();
            double price = plotManager.getLevelSettings().getMergePrice();
            if(price > 0) {
                double playerMoney = economyClient.getAPI().getMoney(player.getName());
                if(economyClient.getAPI().getMoney(player.getName()) < price) {
                    player.sendMessage(this.translate(player, TranslationKey.ECONOMY_NOT_ENOUGH, (price - playerMoney)));
                    return true;
                } else {
                    economyClient.getAPI().reduceMoney(player.getName(), price);
                }
            }
        }

        final PlotPreMergeEvent plotPreMergeEvent = new PlotPreMergeEvent(player, plot, dir, plotsToMerge);
        this.plugin.getServer().getPluginManager().callEvent(plotPreMergeEvent);
        if(plotPreMergeEvent.isCancelled()) return false;

        if(!plotManager.startMerge(plot, plotsToMerge)) {
            player.sendMessage(this.translate(player, TranslationKey.MERGE_FAILURE_NO_PLOTS_FOUND));
            return false;
        }

        final PlotMergeEvent plotMergeEvent = new PlotMergeEvent(player, plot, plotsToMerge);
        this.plugin.getServer().getPluginManager().callEvent(plotMergeEvent);

        player.sendMessage(this.translate(player, TranslationKey.MERGE_SUCCESS));
        return true;
    }

}
