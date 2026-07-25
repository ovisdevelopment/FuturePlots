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

package ovis.futureplots.listener.plot;

import org.powernukkitx.Player;
import org.powernukkitx.blockentity.BlockEntityItemFrame;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.ItemFrameUseEvent;
import lombok.RequiredArgsConstructor;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.manager.PlotManager;

/**
 * @author Tim tim03we, Ovis Development (2024)
 */
@RequiredArgsConstructor
public class ItemFrameUse implements Listener {

    private final FuturePlots plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(ItemFrameUseEvent event) {
        final Player player = event.getPlayer();
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());

        if(plotManager != null && !player.hasPermission("plot.admin.interact")) {
            final BlockEntityItemFrame blockEntityItemFrame = event.getItemFrame();

            final int x = blockEntityItemFrame.getFloorX();
            final int z = blockEntityItemFrame.getFloorZ();
            final Plot plot = plotManager.getMergedPlot(x, z);

            if(plot != null) {
                if(!plotManager.hasPermissions(player, plot)) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }
}
