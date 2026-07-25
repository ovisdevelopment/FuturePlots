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

import java.util.UUID;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class KickCommand extends SubCommand {

    private final FuturePlots plugin;

    public KickCommand(FuturePlots plugin) {
        super(plugin, "kick");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.kick", "plots.perm.basic");
        this.addParameter(CommandParameter.newType("player", CommandParamType.SELECTION));
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

        final String targetName = (args.length > 0 ? args[0] : "").trim();
        final UUID targetId = this.plugin.getUniqueIdByName(targetName, false);
        final Player target = targetId != null ? player.getServer().getPlayer(targetId).orElse(null) : null;

        if(!player.hasPermission("plot.command.admin.kick") && !plot.isOwner(player.getUniqueId())) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_OWNER));
            return false;
        }

        if(targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(this.translate(player, TranslationKey.PLAYER_SELF));
            return false;
        }

        if(targetName.isEmpty() || targetId == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLAYER));
            return false;
        }

        if(target == null) {
            player.sendMessage(this.translate(player, TranslationKey.PLAYER_NOT_ONLINE));
            return false;
        }

        if(target.hasPermission("plot.admin.bypass.kick") || plot.isHelper(targetId) || plot.isOwner(targetId)) {
            player.sendMessage(this.translate(player, TranslationKey.KICK_CANNOT_PERFORM));
            return false;
        }

        final PlotManager targetPlotManager = this.plugin.getPlotManager(target.getLevel());
        final Plot targetPlot;
        if(targetPlotManager == null || (targetPlot = targetPlotManager.getMergedPlot(target.getFloorX(), target.getFloorZ())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.KICK_CANNOT_PERFORM));
            return false;
        }

        if(!targetPlot.getOriginId().equals(plot.getOriginId())) {
            player.sendMessage(this.translate(player, TranslationKey.KICK_CANNOT_PERFORM));
            return false;
        }

        plotManager.teleportPlayerToPlot(target, plot.getBasePlot(), false);
        player.sendMessage(this.translate(player, TranslationKey.KICK_PLAYER_KICKED, target.getName()));
        return true;
    }

}
