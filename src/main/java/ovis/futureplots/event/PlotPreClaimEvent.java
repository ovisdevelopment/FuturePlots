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

package ovis.futureplots.event;

import org.powernukkitx.Player;
import org.powernukkitx.event.Cancellable;
import org.powernukkitx.event.HandlerList;
import lombok.Getter;
import lombok.Setter;
import ovis.futureplots.components.util.Plot;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@Setter
@Getter
public class PlotPreClaimEvent extends PlotEvent implements Cancellable {

    @Getter
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final boolean auto;
    private boolean borderChanging;
    private boolean showCancelMessage;

    public PlotPreClaimEvent(Player player, Plot plot, boolean auto, boolean borderChanging, boolean showCancelMessage) {
        super(plot);
        this.player = player;
        this.auto = auto;
        this.borderChanging = borderChanging;
        this.showCancelMessage = showCancelMessage;
    }

}
