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

import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.projectile.EntityProjectile;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import lombok.RequiredArgsConstructor;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.manager.PlotManager;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@RequiredArgsConstructor
public class EntityDamageByEntity implements Listener {

    private final FuturePlots plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        final PlotManager plotManager = this.plugin.getPlotManager(entity.getLevel());

        if(plotManager != null) {
            final Plot plot = plotManager.getMergedPlot(entity.getFloorX(), entity.getFloorZ());
            damager = damager instanceof EntityProjectile && ((EntityProjectile) damager).shootingEntity != null ? ((EntityProjectile) damager).shootingEntity : damager;

            if(plot != null) {
                if(!((damager instanceof Player && (((Player) damager).hasPermission("plot.admin.damage")) || (entity instanceof Player ? ((boolean) plot.getFlagValue("pvp")) : ((boolean) plot.getFlagValue("pve"))) || (!(entity instanceof Player) && damager instanceof Player && plot.isOwner(damager.getUniqueId())))))
                    event.setCancelled(true);
            } else if(!(damager instanceof Player) || !((Player) damager).hasPermission("plot.admin.damage"))
                event.setCancelled(true);
        }
    }
}
