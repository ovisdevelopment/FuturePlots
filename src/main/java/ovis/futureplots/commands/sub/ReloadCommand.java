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

import org.powernukkitx.command.CommandSender;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.components.util.language.TranslationKey;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class ReloadCommand extends SubCommand {

    private final FuturePlots plugin;

    public ReloadCommand(FuturePlots plugin) {
        super(plugin, "reload");
        this.plugin = plugin;
        this.identify();
        this.setPermissions("plots.reload");
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        FuturePlots.getLanguageProvider().init();

        sender.sendMessage(this.translate(sender, TranslationKey.RELOAD_SUCCESS));
        return true;
    }

}
