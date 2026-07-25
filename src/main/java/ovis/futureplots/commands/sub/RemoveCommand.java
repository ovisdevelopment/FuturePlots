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
public class RemoveCommand extends SubCommand {

    private final FuturePlots plugin;

    public RemoveCommand(FuturePlots plugin) {
        super(plugin, "remove");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.remove", "plots.perm.basic");
        this.addParameter(CommandParameter.newType("player", CommandParamType.SELECTION));
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        final Plot plot;
        if(plotManager == null || (plot = plotManager.getMergedPlot(player.getFloorX(), player.getFloorZ())) == null) {
            player.sendMessage(this.translate(player, "no.plot"));
            return false;
        }

        String targetName = (args.length > 0 ? args[0] : "").trim();
        final UUID targetId = this.plugin.getUniqueIdByName(targetName);

        if(targetName.equalsIgnoreCase(player.getName()) && !player.hasPermission("plot.command.admin.remove")) {
            player.sendMessage(this.translate(player, "player.self"));
            return false;
        }

        if(targetName.isEmpty() || targetId == null) {
            player.sendMessage(this.translate(player, "no.player"));
            return false;
        }

        if(!player.hasPermission("plot.command.admin.remove") && !plot.isOwner(player.getUniqueId())) {
            player.sendMessage(this.translate(player, "no.plot.owner"));
            return false;
        }

        targetName = this.plugin.getCorrectName(targetId);
        if(!plot.isHelper(targetId) && !plot.isTrusted(targetId)) {
            player.sendMessage(this.translate(player, TranslationKey.NO_HELPER_TRUSTED, targetName));
            return false;
        }

        if(plot.removeHelper(targetId)) {
            player.sendMessage(this.translate(player, TranslationKey.REMOVED_HELPER, targetName));
        }

        if(plot.removeTrusted(targetId)) {
            player.sendMessage(this.translate(player, TranslationKey.REMOVED_TRUSTED, targetName));
        }
        plotManager.savePlot(plot);
        return true;
    }

}
