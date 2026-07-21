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
import ovis.futureplots.components.provider.economy.client.EconomyClient;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.components.util.Plot;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class ClearCommand extends SubCommand {

    private final FuturePlots plugin;

    public ClearCommand(FuturePlots plugin) {
        super(plugin, "clear");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.clear", "plots.perm.basic");
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

        if(!plot.isOwner(player.getUniqueId()) && !player.hasPermission("plot.command.admin.clear")) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_OWNER));
            return false;
        }

        if(FuturePlots.getSettings().isEconomyEnabled() && FuturePlots.getSettings().getEconomyWorlds().contains(plotManager.getLevelName()) && !player.hasPermission("plot.economy.bypass")) {
            EconomyClient economyClient = this.plugin.getEconomyProvider().getEconomyClient();
            double price = plotManager.getLevelSettings().getClearPrice();
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

        if(!plotManager.clearPlot(plot)) {
            player.sendMessage(this.translate(player, TranslationKey.CLEAR_FAILURE));
            return false;
        }

        plotManager.savePlot(plot);
        player.sendMessage(this.translate(player, TranslationKey.CLEAR_SUCCESS));
        return true;
    }

}
