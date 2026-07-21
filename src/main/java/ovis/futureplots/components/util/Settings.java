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

package ovis.futureplots.components.util;

import org.powernukkitx.utils.Config;
import lombok.Getter;
import ovis.futureplots.FuturePlots;

import java.util.List;

/**
 * @author Tim tim03we, Ovis Development (2024)
 */
@Getter
public class Settings {

    private boolean defaultLangEnabled;
    private String defaultLanguage;
    private String dataProvider;
    private String economyProvider;
    private boolean economyEnabled;
    private List<String> economyWorlds;
    private boolean debugEnabled;
    private boolean metricsEnabled;
    private String sqliteDatabase;
    private String mongodbUri;
    private String mongoDbDatabase;
    private String mySqlHost;
    private String mySqlPort;
    private String mySqlDatabase;
    private String mySqlUser;
    private String mySqlPassword;

    private boolean teleportOnClaim;
    private boolean homeProtectEnabled;
    private int homeProtectDistance;
    private boolean updateCheckerEnabled;
    private boolean checkForStableUpdates;

    public void init() {
        Config config = FuturePlots.getInstance().getConfig();

        defaultLangEnabled = !config.getBoolean("enable_player_language");
        defaultLanguage = config.getString("default_language");
        dataProvider = config.getString("data-provider");
        debugEnabled = config.getBoolean("debug");
        metricsEnabled = config.getBoolean("metrics");
        economyProvider = config.getString("economy.provider");
        economyEnabled = config.getBoolean("economy.enable");
        economyWorlds = config.getStringList("economy.worlds");

        sqliteDatabase = config.getString("sqlite.database");
        mongodbUri = config.getString("mongodb.uri");
        mongoDbDatabase = config.getString("mongodb.database");
        mySqlHost = config.getString("mysql.host");
        mySqlPort = config.getString("mysql.port");
        mySqlDatabase = config.getString("mysql.database");
        mySqlUser = config.getString("mysql.user");
        mySqlPassword = config.getString("mysql.password");

        teleportOnClaim = config.getBoolean("teleport-on-claim");
        homeProtectEnabled = config.getBoolean("home-protection.enabled");
        homeProtectDistance = config.getInt("home-protection.distance");
        updateCheckerEnabled = config.getBoolean("update-checker.enabled", true);
        checkForStableUpdates = !config.getString("update-checker.mode", "release").equals("release");
    }
}
