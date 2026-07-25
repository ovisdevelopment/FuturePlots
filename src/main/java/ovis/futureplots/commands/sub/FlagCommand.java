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
import org.powernukkitx.block.Block;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandEnum;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.registry.Registries;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.components.util.flags.Flag;
import ovis.futureplots.components.util.flags.FlagRegistry;
import ovis.futureplots.components.util.flags.FlagType;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tim tim03we, Ovis Development (2024)
 */
public class FlagCommand extends SubCommand {

    private final FuturePlots plugin;

    public FlagCommand(FuturePlots plugin) {
        super(plugin, "flag");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.flag", "plots.perm.basic");

        this.addSubParameter("_set",
                new CommandParameter[]{
                        CommandParameter.newEnum("set", new String[]{"set"}),
                        CommandParameter.newEnum("flag", false, new CommandEnum("set", getAsArray(FlagType.STRING, FlagType.BOOLEAN, FlagType.INTEGER, FlagType.DOUBLE))),
                        CommandParameter.newType("value", CommandParamType.ARG)
                }
        );
        this.addSubParameter("_add",
                new CommandParameter[]{
                        CommandParameter.newEnum("add", new String[]{"add"}),
                        CommandParameter.newEnum("flag", false, new CommandEnum("add", getAsArray(FlagType.BLOCK_TYPE_LIST))),
                        CommandParameter.newType("value", CommandParamType.ARG)
                }
        );
        this.addSubParameter("_remove",
                new CommandParameter[]{
                        CommandParameter.newEnum("remove", new String[]{"remove"}),
                        CommandParameter.newEnum("flag", false, new CommandEnum("remove", getAsArray(null))),
                        CommandParameter.newType("value", true, CommandParamType.ARG)
                }
        );
        this.addSubParameter("_list",
                new CommandParameter[]{
                        CommandParameter.newEnum("list", new String[]{"list"})

                }
        );
        /* TODO
        this.addSubParameter("_info",
                new CommandParameter[]{
                        CommandParameter.newEnum("flag", new String[]{"flag"}),
                        CommandParameter.newEnum("info", new String[]{"info"}),
                        CommandParameter.newEnum("flag", false, new CommandEnum("flag", getAsArray(null)))

                }
        );
         */
    }

    private String[] getAsArray(FlagType... flagTypes) {
        if(flagTypes == null || flagTypes.length == 0) {
            return FlagRegistry.getFlags().stream().map(Flag::getSaveName).toList().toArray(new String[0]);
        }
        List<String> result = new ArrayList<>();
        for (FlagType flagType : flagTypes) {
            result.addAll(
                    FlagRegistry.getFlags(flagType).stream()
                            .map(Flag::getSaveName)
                            .toList()
            );
        }
        return result.toArray(new String[0]);
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
        final String parameter = args.length > 0 ? args[0] : "";
        final String flagName = args.length > 1 ? args[1] : null;
        final String value = args.length > 2 ? args[2] : null;
        switch (parameter) {
            case "set" -> {
                Flag flag = FlagRegistry.getFlagByName(flagName);
                if(flag == null) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_NOT_EXIST));
                    return false;
                }
                if(!player.hasPermission("plot.flag." + flag.getSaveName()) && (flag.isBasicFlag() && !player.hasPermission("plots.permpack.basicflags"))) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_NO_PERMISSION, "plot.flag." + flag.getSaveName()));
                    return false;
                }
                if(flag.getType() == FlagType.BLOCK_TYPE_LIST) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_NOT_SUPPORT_PARAMETER));
                    return false;
                }
                Object set = flag.update(plot, value);
                player.sendMessage(this.translate(player, TranslationKey.FLAG_SET_CHANGED, flag.getSaveName(), set));
            }
            case "add" -> {
                Flag flag = FlagRegistry.getFlagByName(flagName);
                if(flag == null) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_NOT_EXIST));
                    return false;
                }
                if(!player.hasPermission("plot.flag." + flag.getSaveName()) && (flag.isBasicFlag() && !player.hasPermission("plots.permpack.basicflags"))) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_NO_PERMISSION, "plot.flag." + flag.getSaveName()));
                    return false;
                }
                if(flag.getType() != FlagType.BLOCK_TYPE_LIST) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_NOT_SUPPORT_PARAMETER));
                    return false;
                }
                if(value == null) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_SPECIFY_MATERIAL));
                    return false;
                }
                String blockName = value.toLowerCase();
                final Block block = Registries.BLOCK.get(blockName);
                if(block == null) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_MATERIAL_NOT_EXIST));
                    return false;
                }
                List<String> plotFlags = plot.getFlagValue(flagName) == null ? new ArrayList<>() : (List<String>) plot.getFlagValue(flagName);
                if(plotFlags.contains(blockName)) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_MATERIAL_EXIST));
                    return false;
                }
                plotFlags.add(blockName);
                plot.setFlagValue(flagName, plotFlags);
                player.sendMessage(this.translate(player, TranslationKey.FLAG_MATERIAL_ADDED, blockName, flagName));
            }
            case "remove" -> {
                Flag flag = FlagRegistry.getFlagByName(flagName);
                if(flag == null) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_NOT_EXIST));
                    return false;
                }
                if(!player.hasPermission("plot.flag." + flag.getSaveName()) && (flag.isBasicFlag() && !player.hasPermission("plots.permpack.basicflags"))) {
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_NO_PERMISSION, "plot.flag." + flag.getSaveName()));
                    return false;
                }
                if(args.length > 2) { // Remove Flag Value
                    if(flag.getType() != FlagType.BLOCK_TYPE_LIST) {
                        player.sendMessage(this.translate(player, TranslationKey.FLAG_NOT_SUPPORT_PARAMETER));
                        return false;
                    }
                    List<String> valueList = (List<String>) plot.getFlagValue(flagName);
                    if(value == null) {
                        player.sendMessage(this.translate(player, TranslationKey.FLAG_VALUE_NULL));
                        return false;
                    }
                    if(!valueList.contains(value)) {
                        player.sendMessage(this.translate(player, TranslationKey.FLAG_VALUE_NOT_CONTAINS, value));
                        return false;
                    }
                    boolean removed = valueList.remove(value);
                    if(!removed) {
                        player.sendMessage(this.translate(player, TranslationKey.FLAG_VALUE_NOT_REMOVED));
                        return false;
                    }
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_REMOVED_VALUE, flagName));
                } else { // Remove Flag
                    plot.removeFlag(flagName);
                    player.sendMessage(this.translate(player, TranslationKey.FLAG_REMOVED, flagName));
                }
            }
            case "list" -> {
                StringBuilder blockTypeListBuilder = new StringBuilder();
                StringBuilder booleanBuilder = new StringBuilder();
                StringBuilder doubleBuilder = new StringBuilder();
                StringBuilder integerBuilder = new StringBuilder();
                StringBuilder stringBuilder = new StringBuilder();
                for (Flag flag : FlagRegistry.getFlags()) {
                    switch (flag.getType()) {
                        case BLOCK_TYPE_LIST -> blockTypeListBuilder.append(flag.getSaveName()).append(", ");
                        case BOOLEAN -> booleanBuilder.append(flag.getSaveName()).append(", ");
                        case DOUBLE -> doubleBuilder.append(flag.getSaveName()).append(", ");
                        case INTEGER -> integerBuilder.append(flag.getSaveName()).append(", ");
                        case STRING -> stringBuilder.append(flag.getSaveName()).append(", ");
                    }
                }
                String blockTypeListFlags = blockTypeListBuilder.length() >= 2 ? blockTypeListBuilder.substring(0, blockTypeListBuilder.length() - 2) : "§c-----";
                String booleanFlags = booleanBuilder.length() >= 2 ? booleanBuilder.substring(0, booleanBuilder.length() - 2) : "§c-----";
                String doubleFlags = doubleBuilder.length() >= 2 ? doubleBuilder.substring(0, doubleBuilder.length() - 2) : "§c-----";
                String integerFlags = integerBuilder.length() >= 2 ? integerBuilder.substring(0, integerBuilder.length() - 2) : "§c-----";
                String stringFlags = stringBuilder.length() >= 2 ? stringBuilder.substring(0, stringBuilder.length() - 2) : "§c-----";

                player.sendMessage(this.translate(player, TranslationKey.FLAG_LIST_TITLE));
                player.sendMessage(this.translate(player, TranslationKey.FLAG_LIST_TYPE_BLOCKTYPELIST, blockTypeListFlags));
                player.sendMessage(this.translate(player, TranslationKey.FLAG_LIST_TYPE_BOOLEAN, booleanFlags));
                player.sendMessage(this.translate(player, TranslationKey.FLAG_LIST_TYPE_DOUBLE, doubleFlags));
                player.sendMessage(this.translate(player, TranslationKey.FLAG_LIST_TYPE_INTEGER, integerFlags));
                player.sendMessage(this.translate(player, TranslationKey.FLAG_LIST_TYPE_STRING, stringFlags));
            }
            /*case "info" -> {
                TODO
                break;
            }*/
            default -> {
                player.sendMessage("§cUngültiger Parameter.");
            }
        }
        return true;
    }

}
