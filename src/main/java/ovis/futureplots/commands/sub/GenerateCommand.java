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
import org.powernukkitx.command.data.CommandEnum;
import org.powernukkitx.command.data.CommandParameter;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.components.util.language.TranslationKey;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class GenerateCommand extends SubCommand {

    private final FuturePlots plugin;

    public GenerateCommand(FuturePlots plugin) {
        super(plugin, "generate");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.generate", "plots.perm.admin");
        this.addParameter(CommandParameter.newType("level", CommandParamType.ARG));
        this.addParameter(CommandParameter.newEnum("default", new CommandEnum("should be default level?", "true", "false")));
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        final String levelName = args.length > 0 ? args[0] : "";
        final boolean defaultLevel = args.length > 1 && Utils.parseBoolean(args[1]);

        if(levelName.trim().isEmpty()) {
            player.sendMessage(this.translate(player, TranslationKey.NO_WORLD));
            return false;
        }

        if (this.plugin.getServer().isLevelLoaded(levelName)) {
            player.sendMessage(this.translate(player, TranslationKey.GENERATE_FAILURE));
            return false;
        }

        final PlotLevelRegistration levelRegistration = new PlotLevelRegistration(levelName, defaultLevel);
        this.plugin.getLevelRegistrationMap().put(player, levelRegistration);
        player.sendMessage(this.translate(player, TranslationKey.GENERATE_START, levelName));
        player.sendMessage(this.translate(player, TranslationKey.GENERATE_DIMENSION, levelRegistration.getLevelSettings().getDimension()));
        return true;
    }

}
