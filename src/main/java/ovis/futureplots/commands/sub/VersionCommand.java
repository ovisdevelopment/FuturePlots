/*
 * Copyright 2024 by tim03we, Ovis Development
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

package ovis.futureplots.commands.sub;

import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.PluginDescription;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;


/**
 * @author Tim tim03we, Ovis Development (2024)
 */
public class VersionCommand extends SubCommand {

    private final FuturePlots plugin;

    public VersionCommand(FuturePlots plugin) {
        super(plugin, "version", "Detailed information about the FuturePlots plot plugin", "/plot version");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        PluginDescription plugin = this.plugin.getServer().getPluginManager().getPlugin(this.plugin.getName()).getDescription();
        sender.sendMessage("§8----------");
        sender.sendMessage("§7Detailed information about the FuturePlots plot plugin.");
        sender.sendMessage("§6Developer: §c" + String.join(", ", plugin.getAuthors()));
        sender.sendMessage("§6Version: §c" + plugin.getVersion() + (FuturePlots.getUpdateChecker().isUpdateAvailable() ? " §8(§eUpdate available§8)" : ""));
        sender.sendMessage("§6Website: §c" + plugin.getWebsite());
        sender.sendMessage("§8----------");
        return true;
    }

}
