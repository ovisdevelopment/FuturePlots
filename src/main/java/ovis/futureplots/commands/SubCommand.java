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

package ovis.futureplots.commands;

import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.utils.Config;
import lombok.Getter;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.components.util.language.manager.LanguageManager;
import ovis.futureplots.components.util.language.TranslationKey;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
@Getter
public abstract class SubCommand {

    public String key = "";

    private String name;
    private String description;
    private String usage;
    private FuturePlots plugin;
    private Set<String> aliases;
    private Set<String> permissions;

    private boolean playerOnly = false;
    private boolean enabled;

    private final Set<CommandParameter> parameters;
    private final HashMap<String, CommandParameter[]> subParameters;

    public SubCommand(FuturePlots plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.description = null;
        this.usage = null;
        this.permissions = null;

        this.aliases = new LinkedHashSet<>();
        this.aliases.add(this.name);
        this.parameters = new LinkedHashSet<>();
        this.subParameters = new HashMap<>();
    }

    public SubCommand(FuturePlots plugin, String name, @Nullable String description) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.usage = null;
        this.permissions = null;

        this.aliases = new LinkedHashSet<>();
        this.aliases.add(this.name);
        this.parameters = new LinkedHashSet<>();
        this.subParameters = new HashMap<>();
    }


    public SubCommand(FuturePlots plugin, String name, @Nullable String description, @Nullable String usage) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permissions = null;

        this.aliases = new LinkedHashSet<>();
        this.aliases.add(this.name);
        this.parameters = new LinkedHashSet<>();
        this.subParameters = new HashMap<>();
    }

    public boolean execute(CommandSender sender, String command, String[] args) {
        return true;
    }

    public void identify() {
        Config config = FuturePlots.getCmdConfig();
        String prefix = "plot." + this.name;
        if(config.getBoolean(prefix + ".enable")) {
            String name = config.getString( prefix + ".name");
            String description = config.getString( prefix + ".description");
            String usage = config.getString( prefix + ".usage");
            Set<String> aliases = new LinkedHashSet<>(config.getStringList(prefix + ".alias"));
            boolean enabled = config.getBoolean( prefix + ".enable");

            this.aliases.clear();
            this.name = name;
            this.description = description;
            this.usage = usage;
            this.aliases = aliases;
            this.aliases.add(this.name);
            this.enabled = enabled;
        }
    }

    public void playerOnly() {
        this.playerOnly = true;
    }

    public void addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public void addParameter(CommandParameter parameter) {
        this.parameters.add(parameter);
    }

    public void addSubParameter(String name, CommandParameter[] parameter) {
        this.subParameters.put(name, parameter);
    }

    public void setPermissions(String... permissions) {
        this.permissions = new LinkedHashSet<>(Arrays.asList(permissions));
    }

    public boolean hasPermission(Player player) {
        return this.permissions == null || this.permissions.stream().anyMatch(player::hasPermission);
    }

    protected String translate(CommandSender sender, TranslationKey key, Object... replacements) {
        return translate(sender, key.getKey(), replacements);
    }

    protected String translate(CommandSender sender, String key, Object... replacements) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            return new LanguageManager(player.getClientChainData().getLanguageCode()).message(player.getUniqueId(), key, replacements);
        }
        return new LanguageManager(FuturePlots.getSettings().getDefaultLanguage()).message(null, key, replacements);
    }
}
