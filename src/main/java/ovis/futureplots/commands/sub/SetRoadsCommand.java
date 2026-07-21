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

import org.cloudburstmc.nbt.NbtMap;
import org.powernukkitx.Player;
import org.powernukkitx.block.Block;
import org.powernukkitx.block.BlockState;
import org.powernukkitx.blockentity.BlockEntity;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.generator.PlotGenerator;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.schematic.Schematic;
import ovis.futureplots.schematic.SchematicBlock;
import ovis.futureplots.components.util.ChunkVector;
import ovis.futureplots.components.util.ShapeType;
import ovis.futureplots.components.util.async.TaskExecutor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class SetRoadsCommand extends SubCommand {

    private final FuturePlots plugin;

    public SetRoadsCommand(FuturePlots plugin) {
        super(plugin, "setroads");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.setroads");
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        if (plotManager == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_WORLD));
            return false;
        }

        TaskExecutor.executeAsync(() -> {
            final Level level = player.getLevel();
            final PlotGenerator plotGenerator = (PlotGenerator) level.getGenerator();

            final int playerX = player.getFloorX();
            final int playerZ = player.getFloorZ();

            final Schematic schematic = new Schematic();
            final Vector3[] plotArea = plotGenerator.getPlotArea(plotManager, playerX, playerZ);
            final Vector3 startPos = plotArea[0];
            final Vector3 endPos = plotArea[1];
            final Map<ChunkVector, ShapeType[]> chunkShapes = new LinkedHashMap<>();

            for (int x = startPos.getFloorX(); x <= endPos.getFloorX(); x++) {
                for (int z = startPos.getFloorZ(); z <= endPos.getFloorZ(); z++) {
                    final ChunkVector chunkVector = new ChunkVector(x >> 4, z >> 4);

                    for (int y = startPos.getFloorY(); y <= endPos.getFloorY(); y++) {
                        final Vector3 defaultBlockVector = new Vector3(x, y, z);
                        final Vector3 blockVector = new Vector3(x - startPos.getFloorX(), y - startPos.getFloorY(), z - startPos.getFloorZ());

                        final BlockState blockState0 = level.getBlockStateAt(x, y, z, 0);
                        final BlockState blockState1 = level.getBlockStateAt(x, y, z, 1);

                        if (Objects.equals(blockState1.getIdentifier(), Block.AIR)) {
                            final ShapeType[] shapes;
                            if (!chunkShapes.containsKey(chunkVector))
                                chunkShapes.put(chunkVector, shapes = plotManager.getShapes(chunkVector.getX() << 4, chunkVector.getZ() << 4));
                            else shapes = chunkShapes.get(chunkVector);

                            if (plotGenerator.isDefaultBlockStateAt(plotManager, shapes, defaultBlockVector, blockState0))
                                continue;
                        }

                        schematic.addBlock(blockVector, new SchematicBlock(
                                blockState0,
                                blockState1
                        ));

                        final BlockEntity blockEntity = level.getBlockEntity(new Vector3(x, y, z));
                        if (blockEntity != null) {

                            CompoundTag nbt = blockEntity.getNbt();

                            CompoundTag cleaned = nbt.copy()
                                    .remove("x")
                                    .remove("y")
                                    .remove("z");

                            schematic.addBlockEntity(
                                    blockVector.asBlockVector3(),
                                    blockEntity.getSaveId(),
                                    cleaned
                            );
                        }

                    }
                }
            }

            if (schematic.isEmpty()) {
                if (plotManager.getPlotSchematic().getSchematic() != null) {
                    plotManager.getPlotSchematic().remove(plotManager.getPlotSchematicFile());
                    player.sendMessage(this.translate(player, TranslationKey.SETROADS_ROAD_REMOVED));
                    return;
                }

                player.sendMessage(this.translate(player, TranslationKey.SETROADS_NO_ROAD_FOUND));
                return;
            }

            plotManager.getPlotSchematic().init(schematic);
            plotManager.getPlotSchematic().save(plotManager.getPlotSchematicFile());
            player.sendMessage(this.translate(player, TranslationKey.SETROADS_FINISHED));
        });

        player.sendMessage(this.translate(player, TranslationKey.SETROADS_STARTING));
        return true;
    }

}
