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

package dev.ovis.futureplots.core.provider.data;

import dev.ovis.futureplots.core.FP;
import dev.ovis.futureplots.core.provider.data.client.DataClient;
import dev.ovis.futureplots.core.provider.data.client.clientdetails.MongoDBDetails;
import dev.ovis.futureplots.core.provider.data.client.clientdetails.MySQLDetails;
import dev.ovis.futureplots.core.provider.data.client.clientdetails.SQLiteDetails;
import dev.ovis.futureplots.core.provider.data.client.components.DCollection;
import dev.ovis.futureplots.core.provider.data.client.components.DDocument;
import dev.ovis.futureplots.core.provider.data.client.components.enums.ClientType;
import dev.ovis.futureplots.core.provider.data.client.components.sql.SQLColumn;
import lombok.Getter;
import lombok.Setter;
import org.powernukkitx.math.BlockVector3;
import ovis.futureplots.components.util.Plot;
import ovis.futureplots.components.util.PlotId;
import ovis.futureplots.components.util.Settings;
import ovis.futureplots.manager.PlotManager;

import java.sql.Connection;
import java.util.*;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public final class DataProvider {

    @Setter
    @Getter
    private DataClient dataClient;

    private final FP plugin;

    public DataProvider(FP plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        Settings settings = FuturePlots.getSettings();
        this.plugin.getLogger().info("Try to establish a connection with the provider " + settings.getDataProvider() + ".");
        if(dataClient == null) {
            switch (settings.getDataProvider().toLowerCase(Locale.ROOT)) {
                case "mysql":
                    dataClient = new DataClient(ClientType.MYSQL, new MySQLDetails(
                            settings.getMySqlHost(),
                            settings.getMySqlPort(),
                            settings.getMySqlUser(),
                            settings.getMySqlPassword(),
                            settings.getMySqlDatabase()

                    ));
                    break;
                case "mongodb":
                    dataClient = new DataClient(ClientType.MONGODB, new MongoDBDetails(
                            settings.getMongodbUri(),
                            settings.getMongoDbDatabase()
                    ));
                    break;
                case "sqlite":
                    dataClient = new DataClient(ClientType.SQLITE, new SQLiteDetails(
                            this.plugin.getDataFolder() + "/plots.db"
                    ));
                    break;
                case "yaml": // TODO:

                    break;
                default:
                    this.plugin.getLogger().error("§4Please specify a valid provider: MySql, MongoDB, SQLite");
                    this.plugin.getServer().getPluginManager().disablePlugin(FuturePlots.getInstance());
                    return false;
            }
        } else {
            this.plugin.getServer().getLogger().warning("A data provider was set by another plugin. The configured data provider in the config.yml has been disabled.");
        }
        if(dataClient.isErrored()) {
            this.plugin.getLogger().error("§4An error occurred when trying to establish a connection with the selected provider " + settings.getDataProvider() + ".");
            if(settings.isDebugEnabled()) {
                dataClient.getException().printStackTrace();
            }
            this.plugin.getServer().getPluginManager().disablePlugin(FuturePlots.getInstance());
            return false;
        }
        this.plugin.getLogger().info("Connection to the provider " + settings.getDataProvider() + " has been established.");
        return true;
    }

    public void executeActions(List<DatabaseAction> actions) {
        try (final Connection connection = null) {
            for (DatabaseAction action : actions)
                action.execute(connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createPlotsTable(String levelName) {
        dataClient.createCollection(levelName,
                new SQLColumn()
                        .append("id", SQLColumn.Type.BIGINT, "NOT NULL PRIMARY KEY")
                        .append("x", SQLColumn.Type.INT, "NOT NULL")
                        .append("z", SQLColumn.Type.INT, "NOT NULL")
                        .append("owner", SQLColumn.Type.VARCHAR, 36)
                        .append("trusted", SQLColumn.Type.TEXT)
                        .append("helpers", SQLColumn.Type.TEXT)
                        .append("denied", SQLColumn.Type.TEXT)
                        .append("config", SQLColumn.Type.TEXT)
                        .append("flags", SQLColumn.Type.TEXT)
                        .append("merged", SQLColumn.Type.TINYINT, "NOT NULL DEFAULT '0'")
                        .append("home_position", SQLColumn.Type.VARCHAR, 100)
                        .append("origin", SQLColumn.Type.BIGINT)
        );
    }


    public List<Plot> getPlots(PlotManager manager) {
        DCollection collection = dataClient.getCollection(manager.getLevelName());

        final List<Plot> plots = new ArrayList<>();
        for (DDocument document : collection.find()) {
            final PlotId plotId = PlotId.fromLong(document.getLong("id"));
            final String owner = document.getString("owner");
            List<String> trusted = document.getStringList("trusted");
            List<String> helpers = document.getStringList("helpers");
            List<String> denied = document.getStringList("denied");
            Map<String, Object> config = (Map<String, Object>) document.getObject("config");
            Map<String, Object> flags = (Map<String, Object>) document.getObject("flags");
            final int mergedByte = (int) document.getObject("merged");
            final String homePosition = document.getString("home_position");
            final PlotId origin = PlotId.fromLong(document.getLong("origin"));

            final boolean[] mergedDirections = new boolean[4];
            for (int i = 0; i < 4; i++)
                mergedDirections[i] = (mergedByte & (1 << i)) != 0;

            final BlockVector3 homePositionVector;
            if (homePosition != null) {
                final String[] split = homePosition.split(";");
                homePositionVector = new BlockVector3(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            } else
                homePositionVector = null;

            plots.add(Plot.builder(manager, plotId).
                    owner(owner == null ? null : UUID.fromString(owner)).
                    helpers(helpers.stream().map(UUID::fromString).toList()).
                    trusted(trusted.stream().map(UUID::fromString).toList()).
                    deniedPlayers(denied.stream().map(UUID::fromString).toList()).
                    config(config).
                    flags(flags).
                    mergedDirections(mergedDirections).
                    homePosition(homePositionVector).
                    originId(origin).
                    build());
        }


        return plots;
    }

    public DatabaseAction updatePlot(Plot plot) {
        return connection -> {

            final String levelName = plot.getManager().getLevel().getFolderName();

            DCollection collection = dataClient.getCollection(levelName);

            Set<DDocument> document = collection.find("id", plot.getId().asLong());
            if(!document.iterator().hasNext()) { // TODO
                final BlockVector3 homePosition = plot.getHomePosition();
                int mergedByte = 0;
                for (int i = 0; i < 4; i++) if (plot.isMerged(i)) mergedByte |= 1 << i;
                collection.insert(
                        new DDocument()
                                .append("id", plot.getId().asLong())
                                .append("x", plot.getId().getX())
                                .append("z", plot.getId().getZ())
                                .append("owner", plot.getOwner() == null ? null : plot.getOwner().toString())
                                .append("trusted", plot.getTrusted().stream().map(UUID::toString).toList())
                                .append("helpers", plot.getHelpers().stream().map(UUID::toString).toList())
                                .append("denied", plot.getDeniedPlayers().stream().map(UUID::toString).toList())
                                .append("config", plot.getConfig())
                                .append("flags", plot.getFlags())
                                .append("merged", mergedByte)
                                .append("home_position", homePosition != null ? homePosition.getX() + ";" + homePosition.getY() + ";" + homePosition.getZ() : null)
                                .append("origin", plot.getOriginId().asLong())
                );
            } else {
                if (!plot.hasOwner() && plot.isDefault()) {
                    collection.delete(
                            new DDocument("id", plot.getId().asLong())
                    );
                    return;
                }
                int mergedByte = 0;
                for (int i = 0; i < 4; i++) if (plot.isMerged(i)) mergedByte |= 1 << i;

                final BlockVector3 homePosition = plot.getHomePosition();

                collection.update(
                        new DDocument("id", plot.getId().asLong()),

                        new DDocument()
                                .append("owner", plot.getOwner() == null ? null : plot.getOwner().toString())
                                .append("trusted", plot.getTrusted().stream().map(UUID::toString).toList())
                                .append("helpers", plot.getHelpers().stream().map(UUID::toString).toList())
                                .append("denied", plot.getDeniedPlayers().stream().map(UUID::toString).toList())
                                .append("config", plot.getConfig())
                                .append("flags", plot.getFlags())
                                .append("merged", mergedByte)
                                .append("home_position", homePosition != null ? homePosition.getX() + ";" + homePosition.getY() + ";" + homePosition.getZ() : null)
                                .append("origin", plot.getOriginId().asLong())
                );
            }
        };
    }

    public DatabaseAction insertPlot(String levelName, PlotId plotId, String owner, List<String> trusted, List<String> helpers, List<String> denied, Map<String, Object> config, Map<String, Object> flags, boolean[] mergedDirections, BlockVector3 homePosition, PlotId originId) {
        return connection -> {
            int mergedByte = 0;
            for (int i = 0; i < 4; i++) if (mergedDirections[i]) mergedByte |= 1 << i;

            DCollection collection = dataClient.getCollection(levelName);
            collection.insert(
                    new DDocument()
                            .append("id", plotId.asLong())
                            .append("x", plotId.getX())
                            .append("z", plotId.getZ())
                            .append("owner", owner)
                            .append("trusted", trusted)
                            .append("helpers", helpers)
                            .append("denied", denied)
                            .append("config", config)
                            .append("flags", flags)
                            .append("merged", mergedByte)
                            .append("home_position", homePosition != null ? homePosition.getX() + ";" + homePosition.getY() + ";" + homePosition.getZ() : null)
                            .append("origin", originId.asLong())
            );
        };
    }

    public DatabaseAction deletePlot(Plot plot) {

        return connection -> {
            final String levelName = plot.getManager().getLevel().getFolderName();
            DCollection collection = dataClient.getCollection(levelName);

            collection.delete(new DDocument("id", plot.getId().asLong()));
        };
    }

    public void createPlayersTable() {
        dataClient.createCollection("plot_players",
                new SQLColumn()
                        .append("id", SQLColumn.Type.VARCHAR, 36, "NOT NULL UNIQUE PRIMARY KEY")
                        .append("name", SQLColumn.Type.VARCHAR, 256, "NOT NULL")
        );
    }

    public Map<UUID, String> getPlayers() {
        DCollection collection = dataClient.getCollection("plot_players");
        final Map<UUID, String> names = new HashMap<>();
        for (DDocument document : collection.find()) {
            names.put(UUID.fromString(document.getString("id")), document.getString("name"));
        }
        return names;
    }

    public DatabaseAction addPlayer(UUID uniqueId, String name) {
        return connection -> {
            DCollection collection = dataClient.getCollection("plot_players");
            collection.insert(
                    new DDocument()
                            .append("id", uniqueId.toString())
                            .append("name", name)
            );
        };
    }

    public interface DatabaseAction {
        void execute(Connection connection) throws Exception;
    }

}
