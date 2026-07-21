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
import org.powernukkitx.Server;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.math.BlockVector3;
import org.powernukkitx.math.Vector3;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.components.util.Plot;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class MiddleCommand extends SubCommand {

    private final FuturePlots plugin;

    public MiddleCommand(FuturePlots plugin) {
        super(plugin, "middle");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.middle", "plots.perm.basic");
        this.addParameter(CommandParameter.newType("player", CommandParamType.SELECTION));
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        if(plotManager == null && this.plugin.getDefaultPlotLevel() == null || plotManager == null && (plotManager = this.plugin.getPlotManager(this.plugin.getDefaultPlotLevel())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_WORLD));
            return false;
        }

        final Plot plot;
        if(plotManager == null || (plot = plotManager.getMergedPlot(player.getFloorX(), player.getFloorZ())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT));
            return false;
        }

        player.teleport(plotManager.getMiddle(plot));
        return true;
    }

}
