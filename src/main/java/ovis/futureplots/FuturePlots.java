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

package ovis.futureplots;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.DimensionEnum;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.LevelConfig;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.registry.RegisterException;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.Setter;
import ovis.futureplots.commands.PlotCommand;
import ovis.futureplots.components.bstats.Metrics;
import ovis.futureplots.components.provider.data.DataProvider;
import ovis.futureplots.components.provider.economy.EconomyProvider;
import ovis.futureplots.components.util.*;
import ovis.futureplots.components.util.language.manager.LanguageManager;
import ovis.futureplots.components.util.language.provider.LanguageProvider;
import ovis.futureplots.listener.plot.*;
import ovis.futureplots.generator.PlotGenerator;
import ovis.futureplots.generator.PlotStage;
import ovis.futureplots.listener.PlotLevelRegistrationListener;
import ovis.futureplots.manager.PlayerManager;
import ovis.futureplots.manager.PlayerNameFunction;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.components.util.async.TaskExecutor;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class FuturePlots extends PluginBase {

    @Getter
    public static Config cmdConfig;

    public static FuturePlots INSTANCE;

    private Config worldsConfig;

    @Getter
    private static Settings settings;

    @Getter
    private DataProvider dataProvider;

    @Getter
    private EconomyProvider economyProvider;

    @Getter
    private Map<String, PlotManager> plotManagerMap;
    private PlayerManager playerManager;

    @Getter
    private Level defaultPlotLevel;

    @Getter
    private Map<Player, PlotLevelRegistration> levelRegistrationMap;

    private boolean namesLoad;

    @Getter
    @Setter
    private PlayerNameFunction nameFunction;

    private final PlayerNameFunction defaultNameFunction = (name, nameConsumer) -> nameConsumer.accept(name.equalsIgnoreCase("admin") ? "§4Administrator" : name);

    @Getter
    private int plotsPerPage = 5;

    @Getter
    private boolean showCommandParams = true;

    @Getter
    private boolean addOtherCommands = true;

    @Getter
    private final List<BlockEntry> borderEntries = new ArrayList<>();

    @Getter
    private final List<BlockEntry> wallEntries = new ArrayList<>();

    private static FuturePlots instance;

    @Getter
    private static LanguageProvider languageProvider;

    @Getter
    private static UpdateChecker updateChecker;


    public static FuturePlots getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        this.plotManagerMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        INSTANCE = this;
        try {
            Registries.GENERATE_STAGE.register(PlotStage.NAME, PlotStage.class);
            Registries.GENERATOR.register("plot", PlotGenerator.class);
        } catch (RegisterException e) {
            throw new RuntimeException(e);
        }

        this.dataProvider = new DataProvider(this);
        this.economyProvider = new EconomyProvider(this);
    }

    @Override
    public void onEnable() {
        this.worldsConfig = new Config(new File(this.getDataFolder(), "worlds.yml"), Config.YAML);


        saveResource("config.yml");

        checkVersion();

        settings = new Settings();
        settings.init();

        final Config config = this.getConfig();
        boolean providerLoaded = this.dataProvider.init();
        boolean economyProviderLoaded = getSettings().isEconomyEnabled() ? this.economyProvider.init() : false;

        if(providerLoaded) {
            this.plotsPerPage = 5;

            this.borderEntries.addAll(config.getMapList("borders").stream().map(BlockEntry::of).toList());
            this.wallEntries.addAll(config.getMapList("walls").stream().map(BlockEntry::of).toList());

            this.levelRegistrationMap = new HashMap<>();

            final Server server = this.getServer();

            for (Object o : this.worldsConfig.getList("levels", new ArrayList<>())) {
                if (o instanceof final String levelName) {
                    this.dataProvider.createPlotsTable(levelName);

                    final PlotManager plotManager = new PlotManager(this, levelName, true);
                    this.plotManagerMap.put(levelName, plotManager);

                    if (!server.isLevelLoaded(levelName)) {
                        LevelConfig.GeneratorConfig plot = new LevelConfig.GeneratorConfig("plot", ThreadLocalRandom.current().nextLong(), false, LevelConfig.AntiXrayMode.LOW, false,
                                DimensionEnum.OVERWORLD.getDimensionData(), Collections.emptyMap());
                        LevelConfig levelConfig = new LevelConfig("leveldb", false, Map.of(0, plot));
                        levelConfig = levelConfig.generators(Map.of(0, plot));
                        server.generateLevel(levelName, levelConfig);
                    }

                    Level level;
                    if ((level = server.getLevelByName(levelName)) == null) {
                        server.loadLevel(levelName);
                        level = server.getLevelByName(levelName);
                    }

                    if (level == null) {
                        this.plotManagerMap.remove(levelName);
                        continue;
                    }

                    if (this.worldsConfig.getString("default", "").equalsIgnoreCase(levelName))
                        this.defaultPlotLevel = level;
                    plotManager.initLevel(level);
                }
            }

            this.playerManager = new PlayerManager(this);


            server.getScheduler().scheduleDelayedTask(this, this::loadPlayerNames, 1);

            registerListener();
            server.getPluginManager().registerEvents(new PlotLevelRegistrationListener(this), this);

            languageProvider = LanguageManager.init();
            languageProvider.init();
            registerCommands();

            server.getScheduler().scheduleDelayedTask(this, () -> {
                for (PlotManager plotManager : this.plotManagerMap.values()) {
                    plotManager.savePlots();
                }
            }, 6000);

            if(getSettings().isMetricsEnabled()) {
                int pluginId = 16194;
                Metrics metrics = new Metrics(this, pluginId);
                if(metrics.isEnabled()) {
                    getLogger().info("Metrics are enabled. Anonymous usage data about the plugin is being sent to bStats. If you'd like to opt-out, you can disable this in the config.yml file.");
                }
            }

            if(FuturePlots.getSettings().isUpdateCheckerEnabled()) {
                updateChecker = new UpdateChecker(FuturePlots.getSettings().isCheckForStableUpdates());
                updateChecker.check();
            }
        }
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(new BlockBreak(this), this);
        getServer().getPluginManager().registerEvents(new BlockIgnite(this), this);
        getServer().getPluginManager().registerEvents(new BlockPiston(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlace(this), this);
        getServer().getPluginManager().registerEvents(new BlockUpdate(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamage(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntity(this), this);
        getServer().getPluginManager().registerEvents(new EntityExplode(this), this);
        getServer().getPluginManager().registerEvents(new ItemFrameUse(this), this);
        getServer().getPluginManager().registerEvents(new LeavesDecay(this), this);
        getServer().getPluginManager().registerEvents(new LiquidFlow(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBucketEmpty(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBucketFill(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntity(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMove(this), this);
        getServer().getPluginManager().registerEvents(new StructureGrow(this), this);
    }

    private void registerCommands() {
        getServer().getCommandMap().register("plot", new PlotCommand(this));
    }

    private void checkVersion() {
        getLogger().info("Check commands.yml version..");
        cmdConfig = new Config(getDataFolder() + "/commands.yml", Config.YAML);
        if(!cmdConfig.getString("version").equals("1.1.1")) {
            new File(getDataFolder() + "/commands_old.yml").delete();
            if(new File(getDataFolder() + "/commands.yml").renameTo(new File(getDataFolder() + "/commands_old.yml"))) {
                getLogger().critical("The version of the commands file does not match. You will find the old file marked \"commands_old.yml\" in the same directory.");
                saveResource("commands.yml");
                cmdConfig = new Config(getDataFolder() + "/commands.yml", Config.YAML);
            }
        }

        getLogger().info("Check config.yml version..");
        Config config = new Config(getDataFolder() + "/config.yml", Config.YAML);
        if(!config.getString("version").equals("2.0.1")) {
            new File(getDataFolder() + "/config_old_" + config.getString("version") + ".yml").delete();
            if(new File(getDataFolder() + "/config.yml").renameTo(new File(getDataFolder() + "/config_old_" + config.getString("version") + ".yml"))) {
                getLogger().critical("The version of the config file does not match. You will find the old file marked \"" + "config_old_" + config.getString("version") + ".yml" + "\" in the same directory.");
                saveResource("config.yml");
            }
        }
    }

    private void loadPlayerNames() {
        if (!this.namesLoad) {
            this.namesLoad = true;
            this.playerManager.load(this.nameFunction == null ? this.defaultNameFunction : this.nameFunction);
        }
    }

    @Override
    public void onDisable() {
        try {
            this.dataProvider.getDataClient().disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PlotManager getPlotManager(Level level) {
        if (level == null) return null;
        return this.getPlotManager(new File(level.getFolderPath()).getName());
    }

    public PlotManager getPlotManager(String levelName) {
        return this.plotManagerMap.getOrDefault(levelName, null);
    }

    public Level createLevel(String levelName, boolean defaultLevel, PlotLevelSettings levelSettings) {
        if (this.getServer().isLevelLoaded(levelName)) this.getServer().getLevelByName(levelName);

        TaskExecutor.executeAsync(() -> this.dataProvider.createPlotsTable(levelName));

        final PlotManager plotManager = new PlotManager(this, levelName, levelSettings, false);
        this.plotManagerMap.put(levelName, plotManager);

        int dimension = levelSettings.getDimension();
        LevelConfig.GeneratorConfig plot = new LevelConfig.GeneratorConfig("plot", ThreadLocalRandom.current().nextLong(), false, LevelConfig.AntiXrayMode.LOW, false,
                DimensionEnum.getDataFromId(dimension), Collections.emptyMap());
        LevelConfig levelConfig = new LevelConfig("leveldb", false, Map.of(dimension, plot));
        levelConfig = levelConfig.generators(Map.of(dimension, plot));
        this.getServer().generateLevel(levelName, levelConfig);
        final Level level = this.getServer().getLevelByName(levelName);

        if (level == null) return null;

        plotManager.initLevel(level);

        final List<String> levels = this.worldsConfig.get("levels", new ArrayList<>());
        levels.add(levelName);
        this.worldsConfig.set("levels", levels);

        if (defaultLevel) {
            this.worldsConfig.set("default", levelName);
            this.defaultPlotLevel = level;
        }

        this.worldsConfig.save(true);
        return level;
    }

    public void registerPlayer(Player player) {
        final String playerName = player.getName();

        TaskExecutor.executeAsync(() -> {
            if (!this.playerManager.has(player.getUniqueId())) {
                this.dataProvider.executeActions(Collections.singletonList(
                        this.dataProvider.addPlayer(player.getUniqueId(), playerName)
                ));
            }

            (this.nameFunction == null ? this.defaultNameFunction : this.nameFunction)
                    .execute(player.getName(), displayName -> this.playerManager.add(player.getUniqueId(), playerName, displayName));
        });
    }

    public UUID getUniqueIdByName(String playerName) {
        return this.getUniqueIdByName(playerName, true);
    }

    public UUID getUniqueIdByName(String playerName, boolean allowEveryone) {
        final String firstPlayerName = playerName.trim();
        if (allowEveryone && firstPlayerName.equals(Utils.STRING_EVERYONE))
            return Utils.UUID_EVERYONE;

        for (Map.Entry<UUID, String> entry : this.playerManager.getPlayers()) {
            if (entry.getValue().equalsIgnoreCase(firstPlayerName))
                return entry.getKey();
        }

        return null;
    }

    public String getCorrectName(UUID playerId) {
        return playerId == Utils.UUID_EVERYONE ? Utils.STRING_EVERYONE : this.playerManager.get(this.getNameByUniqueId(playerId));
    }

    public String findPlayerName(String playerName) {
        final Collection<String> playerNames = this.playerManager.getPlayerNames();
        for (String name : playerNames) {
            if (name.equalsIgnoreCase(playerName) || name.toLowerCase().startsWith(playerName.toLowerCase()))
                return name;
        }

        return playerName;
    }

    private String getNameByUniqueId(UUID uniqueId) {
        return this.playerManager.get(uniqueId);
    }

}