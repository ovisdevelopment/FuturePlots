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

import java.util.UUID;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class SetOwnerCommand extends SubCommand {

    private final FuturePlots plugin;

    public SetOwnerCommand(FuturePlots plugin) {
        super(plugin, "setowner");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.setowner", "plots.perm.basic");
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

        if(!plot.isOwner(player.getUniqueId()) && !player.hasPermission("plot.command.admin.setowner")) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_OWNER));
            return false;
        }

        if(targetName.trim().isEmpty() || targetId == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLAYER));
            return false;
        }

        if(target == null) {
            player.sendMessage(this.translate(player, TranslationKey.PLAYER_NOT_ONLINE));
            return false;
        }

        if(targetName.equalsIgnoreCase(player.getName()) && !player.hasPermission("plot.command.admin.setowner")) {
            player.sendMessage(this.translate(player, TranslationKey.PLAYER_SELF));
            return false;
        }

        final int ownedPlots = plotManager.getPlotsByOwner(targetId).size();
        if(!target.hasPermission("plot.limit.unlimited") && !player.hasPermission("plot.command.admin.setowner")) {
            int maxLimit = -1;
            for(String permission : target.getEffectivePermissions().keySet()) {
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
                player.sendMessage(this.translate(player, TranslationKey.SETOWNER_FAILURE_TOO_MANY));
                return false;
            }
        }

        plot.setOwner(targetId);
        plotManager.savePlot(plot);

        target.sendMessage(this.translate(target, TranslationKey.SETOWNER_SUCCESS_TARGET, plot.getId()));
        player.sendMessage(this.translate(player, TranslationKey.SETOWNER_SUCCESS, this.plugin.getCorrectName(targetId)));
        return true;
    }

}
