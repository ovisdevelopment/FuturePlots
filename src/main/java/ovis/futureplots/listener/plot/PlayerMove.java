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
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerMoveEvent;
import lombok.RequiredArgsConstructor;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.components.util.language.manager.LanguageManager;
import ovis.futureplots.event.PlotEnterEvent;
import ovis.futureplots.event.PlotLeaveEvent;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@RequiredArgsConstructor
public class PlayerMove implements Listener {

    private final FuturePlots plugin;

    @EventHandler
    public void on(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());

        if(plotManager != null && event.getFrom() != null) {
            final Plot plotFrom = plotManager.getMergedPlot(event.getFrom().getFloorX(), event.getFrom().getFloorZ());
            final Plot plotTo = plotManager.getMergedPlot(event.getTo().getFloorX(), event.getTo().getFloorZ());

            if(plotTo != null) {
                if(plotTo.isDenied(player.getUniqueId()) || plotTo.isDenied(Utils.UUID_EVERYONE)) {
                    Player owner = this.plugin.getServer().getPlayer(this.plugin.getCorrectName(plotTo.getOwner()));
                    if (!plotTo.isOwner(player.getUniqueId()) && !plotTo.isTrusted(player.getUniqueId()) && !player.hasPermission("plot.admin.bypass.deny")) {
                        if ((plotTo.isHelper(player.getUniqueId()) && owner == null) || !plotTo.isHelper(player.getUniqueId())) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                if(plotFrom == null) {
                    final PlotEnterEvent plotEnterEvent = new PlotEnterEvent(player, plotTo);
                    this.plugin.getServer().getPluginManager().callEvent(plotEnterEvent);
                    if(plotEnterEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                    if(!(boolean) plotTo.getFlagValue("hide-actionbar") && !(boolean) plotTo.getFlagValue("server-plot")) {
                        LanguageManager language = new LanguageManager(player.getClientChainData().getLanguageCode());
                        if(!plotTo.hasOwner())
                            player.sendActionBar(language.message(TranslationKey.PLOT_POPUP_NO_OWNER, plotTo.getId().toString()));
                        else
                            player.sendActionBar(language.message(TranslationKey.PLOT_POPUP_OWNER, this.plugin.getCorrectName(plotTo.getOwner())));
                    }
                }
            } else if(plotFrom != null) {
                final PlotLeaveEvent plotLeaveEvent = new PlotLeaveEvent(player, plotFrom);
                this.plugin.getServer().getPluginManager().callEvent(plotLeaveEvent);
                if(plotLeaveEvent.isCancelled())
                    event.setCancelled(true);
            }
        }
    }
}
