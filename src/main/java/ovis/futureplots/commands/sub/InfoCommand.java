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
import ovis.futureplots.components.util.flags.FlagRegistry;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.components.util.Plot;

import java.util.List;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class InfoCommand extends SubCommand {

    private final FuturePlots plugin;

    public InfoCommand(FuturePlots plugin) {
        super(plugin, "info");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.info", "plots.perm.basic");
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

        if(!plot.hasOwner() && !player.hasPermission("plot.command.admin.info")) {
            player.sendMessage(this.translate(player, TranslationKey.INFO_FAILURE));
            return false;
        }

        final String plotOwner = this.plugin.getCorrectName(plot.getOwner());

        final StringBuilder trusted = new StringBuilder();
        plot.getTrusted().forEach(trust -> trusted.append(this.translate(player, TranslationKey.INFO_LIST, this.plugin.getCorrectName(trust))));

        final StringBuilder helpers = new StringBuilder();
        plot.getHelpers().forEach(helper -> helpers.append(this.translate(player, TranslationKey.INFO_LIST, this.plugin.getCorrectName(helper))));

        final StringBuilder denied = new StringBuilder();
        plot.getDeniedPlayers().forEach(deniedPlayer -> denied.append(this.translate(player, TranslationKey.INFO_LIST, this.plugin.getCorrectName(deniedPlayer))));

        StringBuilder flags = new StringBuilder();
        for (String flagKey : plot.getFlags().keySet()) {
            Object flagValue = plot.getFlagValue(flagKey);
            if(!FlagRegistry.isDefaultValue(flagKey, flagValue)) {

                if(flagValue instanceof List<?>) {
                    StringBuilder listBuilder = new StringBuilder();
                    for (String listValue : (List<String>) flagValue) {
                        listBuilder.append(listValue).append(", ");
                    }
                    String listBuilderString = listBuilder.substring(0, listBuilder.length() - 2);
                    flags.append(this.translate(player, TranslationKey.INFO_FLAGS_VALUE, flagKey, listBuilderString));
                } else {
                    flags.append(this.translate(player, TranslationKey.INFO_FLAGS_VALUE, flagKey, flagValue));
                }
            }
        }
        String flagsBuilderString = flags.length() >= 2 ? flags.substring(0, flags.length() - 2) : this.translate(player, TranslationKey.DEACTIVATED);

        player.sendMessage(this.translate(player, TranslationKey.INFO_TITLE));

        player.sendMessage(this.translate(player, TranslationKey.INFO_ID, plot.getOriginId()));
        player.sendMessage(this.translate(player, TranslationKey.INFO_OWNER, plotOwner));
        player.sendMessage(this.translate(player, TranslationKey.INFO_TRUSTED, (trusted.length() >= 2 ? trusted.substring(0, trusted.length() - 2) : "§c-----")));
        player.sendMessage(this.translate(player, TranslationKey.INFO_HELPERS, (helpers.length() >= 2 ? helpers.substring(0, helpers.length() - 2) : "§c-----")));
        player.sendMessage(this.translate(player, TranslationKey.INFO_DENIED, (denied.length() >= 2 ? denied.substring(0, denied.length() - 2) : "§c-----")));
        player.sendMessage(this.translate(player, TranslationKey.INFO_FLAGS, flagsBuilderString));

        player.sendMessage(this.translate(player, TranslationKey.INFO_END));
        return true;
    }

}
