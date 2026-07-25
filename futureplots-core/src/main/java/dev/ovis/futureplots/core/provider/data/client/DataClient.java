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

package dev.ovis.futureplots.core.provider.data.client;

import dev.ovis.futureplots.core.provider.data.client.clientdetails.ClientDetails;
import dev.ovis.futureplots.core.provider.data.client.components.DCollection;
import dev.ovis.futureplots.core.provider.data.client.components.enums.ClientType;
import dev.ovis.futureplots.core.provider.data.client.components.sql.SQLColumn;
import dev.ovis.futureplots.core.provider.data.client.connection.Connection;
import dev.ovis.futureplots.core.provider.data.client.connection.MongoDBConnection;
import dev.ovis.futureplots.core.provider.data.client.connection.MySQLConnection;
import dev.ovis.futureplots.core.provider.data.client.connection.SQLiteConnection;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class DataClient {

    private Connection connection;

    @Getter
    private boolean errored = false;

    @Getter
    private Exception exception;

    private Map<String, Connection> connectionMap = new HashMap<>() {{
       put("mongodb", new MongoDBConnection());
       put("mysql", new MySQLConnection());
       put("sqlite", new SQLiteConnection());
    }};

    public DataClient(ClientType type, ClientDetails clientDetails) {
        try {
            this.connection = connectionMap.get(type.toString());
            this.connection.connect(clientDetails);
        } catch (Exception e) {
            this.errored = true;
            this.exception = e;
        }
    }

    /* This function works and is only available for MySQL and SQLite */
    public void createCollection(String collection, SQLColumn columns) {
        this.connection.createCollection(collection, columns);
    }

    public DCollection getCollection(String collection) {
        return this.connection.getCollection(collection);
    }

    public void disconnect() throws Exception {
        this.connection.disconnect();
    }
}
