/*
 * Copyright 2025 tim03we, Ovis Development
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

package ovis.futureplots.components.provider.economy;

import lombok.Getter;
import lombok.Setter;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.components.provider.economy.client.EconomyClient;
import ovis.futureplots.components.provider.economy.client.enums.ClientType;

import java.util.*;

/**
 * @author  Tim tim03we, Ovis Development (2025)
 */
public final class EconomyProvider {

    @Setter
    @Getter
    private EconomyClient economyClient;

    private final FuturePlots plugin;

    public EconomyProvider(FuturePlots plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        Settings settings = FuturePlots.getSettings();
        this.plugin.getLogger().info("Try to establish a connection with the economy provider " + settings.getEconomyProvider() + ".");
        if(economyClient == null) {
            switch (settings.getEconomyProvider().toLowerCase(Locale.ROOT)) {
                case "llamaeconomy":
                    if(this.plugin.getServer().getPluginManager().getPlugin("LlamaEconomy") == null) {
                        this.plugin.getLogger().error("§cThe specified economy provider could not be activated. The plugin was not found.");
                        this.plugin.getServer().getPluginManager().disablePlugin(FuturePlots.getInstance());
                        return false;
                    }
                    economyClient = new EconomyClient(ClientType.LLAMAECONOMY);
                    break;
                default:
                    this.plugin.getLogger().error("§4Please specify a valid provider: LlamaEconomy");
                    this.plugin.getServer().getPluginManager().disablePlugin(FuturePlots.getInstance());
                    return false;
            }
        } else {
            this.plugin.getServer().getLogger().warning("A economy provider was set by another plugin. The configured economy provider in the config.yml has been disabled.");
        }
        this.plugin.getLogger().info("Connection to the economy provider " + settings.getEconomyProvider() + " was successfully.");
        return true;
    }
}
