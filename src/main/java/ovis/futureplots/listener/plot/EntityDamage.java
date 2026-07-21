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
import org.powernukkitx.entity.Entity;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import org.powernukkitx.event.entity.EntityDamageEvent;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.components.util.Plot;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@RequiredArgsConstructor
public class EntityDamage implements Listener {

    private final FuturePlots plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        final PlotManager plotManager = this.plugin.getPlotManager(entity.getLevel());

        if(event instanceof EntityDamageByEntityEvent)
            return;

        if(plotManager != null) {
            Plot plot = plotManager.getMergedPlot(entity.getFloorX(), entity.getFloorZ());
            if(plot != null) {
                if(!((boolean) plot.getFlagValue("damage")))
                    event.setCancelled(true);
            } else
                event.setCancelled(true);
        }
    }
}
