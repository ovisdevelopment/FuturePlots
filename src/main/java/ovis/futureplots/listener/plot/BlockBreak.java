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
import org.powernukkitx.Player;
import org.powernukkitx.block.Block;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockBreakEvent;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.components.util.language.manager.LanguageManager;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.components.util.Plot;

import java.util.List;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@RequiredArgsConstructor
public class BlockBreak implements Listener {

    private final FuturePlots plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());

        if(plotManager != null && !player.hasPermission("plot.admin.break")) {
            final int x = event.getBlock().getFloorX();
            final int z = event.getBlock().getFloorZ();
            final Plot plot = plotManager.getMergedPlot(x, z);

            if(plot != null) {
                if(!plotManager.hasPermissions(player, plot) && !((List<String>) plot.getFlagValue("break")).contains(block.getId())) {
                    event.setCancelled(true);
                }

                if(FuturePlots.getSettings().isHomeProtectEnabled() && plot.getHomePosition() != null && plot.getHomePosition().distance(event.getBlock()) < FuturePlots.getSettings().getHomeProtectDistance()) {
                    event.setCancelled(true);
                    LanguageManager language = new LanguageManager(player.getClientChainData().getLanguageCode());
                    player.sendMessage(language.message(TranslationKey.TOO_CLOSE_TO_HOME));
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

}
