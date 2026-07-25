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
import org.powernukkitx.level.Level;
import org.powernukkitx.level.format.IChunk;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.generator.PlotGenerator;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.components.util.async.TaskExecutor;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class RegenAllRoadsCommand extends SubCommand {

    private final FuturePlots plugin;

    public RegenAllRoadsCommand(FuturePlots plugin) {
        super(plugin, "regenallroads");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.regenallroads");
        this.addParameter(CommandParameter.newType("radius", CommandParamType.INT));
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        if(plotManager == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_WORLD));
            return false;
        }

        final int chunkRadius = Utils.parseInteger(args.length > 0 ? args[0] : "32", 32);
        final Level level = player.getLevel();
        final PlotGenerator plotGenerator = (PlotGenerator) level.getGenerator();

        final int pChunkX = player.getChunkX();
        final int pChunkZ = player.getChunkZ();

        TaskExecutor.executeAsync(() -> {
            for(int chunkX = -chunkRadius; chunkX <= chunkRadius; chunkX++) {
                for(int chunkZ = -chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
                    final IChunk IChunk = level.getChunk(pChunkX + chunkX, pChunkZ + chunkZ, false);
                    if (IChunk != null) plotGenerator.regenerateChunk(plotManager, IChunk);
                }
            }

            player.sendMessage(this.translate(player, TranslationKey.REGENALLROADS_FINISHED));
        });

        player.sendMessage(this.translate(player, TranslationKey.REGENALLROADS_START));
        return true;
    }

}
