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
import org.powernukkitx.command.data.CommandEnum;
import org.powernukkitx.command.data.CommandParameter;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;

import java.util.Locale;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class UnlinkCommand extends SubCommand {

    private final FuturePlots plugin;

    public UnlinkCommand(FuturePlots plugin) {
        super(plugin, "unlink");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.unlink");
        this.addParameter(CommandParameter.newEnum("type", new CommandEnum("plot unlink type", "all", "neighbors")));
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        final String type = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "neighbors";

        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        final Plot plot;
        if(plotManager == null || (plot = plotManager.getMergedPlot(player.getFloorX(), player.getFloorZ())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT));
            return false;
        }

        if(!player.hasPermission("plot.command.admin.unlink") && !plot.isOwner(player.getUniqueId())) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_OWNER));
            return false;
        }

        if(plot.hasNoMerges()) {
            player.sendMessage(this.translate(player, TranslationKey.UNLINK_FAILURE));
            return false;
        }

        switch(type) {
            case "all" -> plotManager.unlinkPlotFromAll(plot);
            case "neighbors" -> plotManager.unlinkPlotFromNeighbors(plot);
            default -> {
                player.sendMessage(this.translate(player, TranslationKey.UNLINK_FAILURE_UNKNOWN_TYPE));
                return false;
            }
        }

        plotManager.savePlot(plot);
        player.sendMessage(this.translate(player, TranslationKey.UNLINK_SUCCESS));
        return true;
    }

}
