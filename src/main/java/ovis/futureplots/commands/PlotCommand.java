/*
 * Copyright 2024 tim03we, Ovis Development
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
 */

package ovis.futureplots.commands;

import org.cloudburstmc.protocol.bedrock.data.command.CommandData;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.*;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.sub.*;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.components.util.language.manager.LanguageManager;

import java.util.*;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class PlotCommand extends Command {

    private FuturePlots plugin;
    private Set<SubCommand> subCommands;

    public PlotCommand(FuturePlots plugin) {
        super(FuturePlots.getCmdConfig().getString("plot.name"), FuturePlots.getCmdConfig().getString("plot.description"), FuturePlots.getCmdConfig().getString("plot.usage"));
        setAliases(FuturePlots.getCmdConfig().getStringList("plot.alias").toArray(new String[0]));
        this.plugin = plugin;
        this.subCommands = new LinkedHashSet<>();

        this.subCommands.add(new AddCommand(this.plugin));
        this.subCommands.add(new AutoCommand(this.plugin));
        this.subCommands.add(new BorderCommand(this.plugin));
        this.subCommands.add(new ClaimCommand(this.plugin));
        this.subCommands.add(new ClearCommand(this.plugin));
        this.subCommands.add(new DeleteHomeCommand(this.plugin));
        this.subCommands.add(new DenyCommand(this.plugin));
        this.subCommands.add(new ResetCommand(this.plugin));
        this.subCommands.add(new GenerateCommand(this.plugin));
        this.subCommands.add(new HelpCommand(this.plugin));
        this.subCommands.add(new HomeCommand(this.plugin));
        this.subCommands.add(new HomesCommand(this.plugin));
        this.subCommands.add(new InfoCommand(this.plugin));
        this.subCommands.add(new KickCommand(this.plugin));
        this.subCommands.add(new MergeCommand(this.plugin));
        this.subCommands.add(new MiddleCommand(this.plugin));
        this.subCommands.add(new RegenAllRoadsCommand(this.plugin));
        this.subCommands.add(new RegenRoadCommand(this.plugin));
        this.subCommands.add(new ReloadCommand(this.plugin));
        this.subCommands.add(new RemoveCommand(this.plugin));
        this.subCommands.add(new SetHomeCommand(this.plugin));
        this.subCommands.add(new SetOwnerCommand(this.plugin));
        this.subCommands.add(new SetRoadsCommand(this.plugin));
        this.subCommands.add(new FlagCommand(this.plugin));
        this.subCommands.add(new TeleportCommand(this.plugin));
        this.subCommands.add(new TrustCommand(this.plugin));
        this.subCommands.add(new UndenyCommand(this.plugin));
        this.subCommands.add(new UnlinkCommand(this.plugin));
        this.subCommands.add(new VersionCommand(this.plugin));
        this.subCommands.add(new WallCommand(this.plugin));
        this.subCommands.add(new WarpCommand(this.plugin));

        this.commandParameters.clear();

        for (SubCommand subCommand : subCommands) {
            final Set<CommandParameter> parameterSet = subCommand.getParameters();
            final HashMap<String, CommandParameter[]> subMap = subCommand.getSubParameters();

            for (String alias : subCommand.getAliases()) {
                CommandParameter aliasParam = CommandParameter.newEnum("subcommand", false, new CommandEnum("PlotSubcommand" + alias, alias));

                if (subMap.isEmpty()) {
                    final CommandParameter[] parameters = new CommandParameter[parameterSet.size() + 1];
                    parameters[0] = aliasParam;

                    if (!parameterSet.isEmpty()) {
                        int i = 1;
                        for (CommandParameter parameter : parameterSet) {
                            parameters[i++] = parameter;
                        }
                    }
                    this.commandParameters.put(alias, parameters);
                } else {
                    subMap.forEach((key, subParams) -> {
                        final CommandParameter[] parameters = new CommandParameter[subParams.length + 1];
                        parameters[0] = aliasParam;
                        System.arraycopy(subParams, 0, parameters, 1, subParams.length);
                        this.commandParameters.put(alias + key, parameters);
                    });
                }
            }
        }

    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(!testPermission(sender)) {
            return false;
        }
        final String subName = args.length > 0 ? args[0] : "";
        args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];


        for(SubCommand subCommand : this.subCommands) {
            if(subCommand.getAliases().contains(subName)) {
                if(!(sender instanceof Player player)) {
                    if(subCommand.isPlayerOnly()) {
                        sender.sendMessage("§cCommand available only for players!");
                        return true;
                    }
                } else {
                    if(!subCommand.hasPermission(player)) {
                        LanguageManager languageManager = new LanguageManager(player.getClientChainData().getLanguageCode());
                        sender.sendMessage(languageManager.message(player.getUniqueId(), TranslationKey.NO_PERMS, subCommand.getPermissions().toArray()[0]));
                        return true;
                    }
                }
                subCommand.execute(sender, s, args);
            }
        }
        return false;
    }

    @Override
    public CommandDataVersions generateCustomCommandData(Player player) {
        final CommandDataVersions versions = super.generateCustomCommandData(player);
        final NukkitCommandData commandData = versions.versions.get(0);

        final Map<String, CommandOverload> overloads = new HashMap<>(commandData.overloads);
        for (Map.Entry<String, CommandOverload> entry : overloads.entrySet()) {
            for (SubCommand subCommand : this.subCommands) {
                if (subCommand.getAliases().contains(entry.getKey())) {
                    if (!subCommand.hasPermission(player)) {
                        commandData.overloads.remove(entry.getKey());
                    }
                    break;
                }
            }
        }

        return versions;
    }
}
