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

package ovis.futureplots.listener.plot;

import lombok.RequiredArgsConstructor;
import org.powernukkitx.block.Block;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockPistonEvent;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.manager.PlotManager;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@RequiredArgsConstructor
public class BlockPiston implements Listener {

    private final FuturePlots plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(BlockPistonEvent event) {
        final Block block = event.getBlock();
        final PlotManager plotManager = this.plugin.getPlotManager(block.getLevel());
        if(plotManager != null) {
            final Plot blockPlot = plotManager.getMergedPlot(block.getFloorX(), block.getFloorZ());

            for(Block movingBlock : event.getBlocks()) {
                final Plot movingBlockPlot = plotManager.getMergedPlot(movingBlock.getFloorX(), movingBlock.getFloorZ());

                if(blockPlot != null && movingBlockPlot == null) {
                    event.setCancelled(true);
                    return;
                }

                if(blockPlot == null && movingBlockPlot != null) {
                    event.setCancelled(true);
                    return;
                }

                final ShapeType[] shapes = plotManager.getShapes(movingBlock.getChunkX() << 4, movingBlock.getChunkZ() << 4);
                if(shapes[((movingBlock.getFloorZ() & 15) << 4) | (movingBlock.getFloorX() & 15)] == ShapeType.WALL) {
                    event.setCancelled(true);
                    return;
                }
            }

            for(Block movingBlock : event.getDestroyedBlocks()) {
                final Plot movingBlockPlot = plotManager.getMergedPlot(movingBlock.getFloorX(), movingBlock.getFloorZ());

                if(blockPlot != null && movingBlockPlot == null) {
                    event.setCancelled(true);
                    return;
                }

                if(blockPlot == null && movingBlockPlot != null) {
                    event.setCancelled(true);
                    return;
                }

                final ShapeType[] shapes = plotManager.getShapes(movingBlock.getChunkX() << 4, movingBlock.getChunkZ() << 4);
                if(shapes[((movingBlock.getFloorZ() & 15) << 4) | (movingBlock.getFloorX() & 15)] == ShapeType.WALL) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
