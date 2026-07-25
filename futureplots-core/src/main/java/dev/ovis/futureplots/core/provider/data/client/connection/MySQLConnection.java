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

package dev.ovis.futureplots.core.provider.data.client.connection;

import dev.ovis.futureplots.core.provider.data.client.clientdetails.ClientDetails;
import dev.ovis.futureplots.core.provider.data.client.clientdetails.MySQLDetails;
import dev.ovis.futureplots.core.provider.data.client.components.DDocument;
import dev.ovis.futureplots.core.provider.data.client.components.sql.SQLClient;
import dev.ovis.futureplots.core.provider.data.client.components.sql.SQLColumn;

import java.sql.DriverManager;
import java.util.Set;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class MySQLConnection extends Connection {

    private java.sql.Connection connection;
    private SQLClient sqlClient;

    @Override
    public void connect(ClientDetails clientDetails) throws Exception {
        MySQLDetails details = (MySQLDetails) clientDetails;
        this.connection = DriverManager.getConnection("jdbc:mysql://" + details.getHost() + ":" + details.getPort() + "/" + details.getDatabase() + "?autoReconnect=true&useGmtMillisForDatetimes=true&serverTimezone=GMT", details.getUser(), details.getPassword());
        this.sqlClient = new SQLClient(this.connection);
    }

    @Override
    public void createCollection(String name, SQLColumn columns) {
        sqlClient.createTable(name, columns);
    }

    @Override
    public void insert(String collection, DDocument values) {
        sqlClient.insert(collection, values);
    }

    @Override
    public void delete(String collection, String key, Object value) {
        this.delete(collection, new DDocument(key, value));
    }

    @Override
    public void delete(String collection, DDocument search) {
        sqlClient.delete(collection, search);
    }

    @Override
    public void update(String collection, String searchKey, Object searchValue, String updateKey, String updateValue) {
        this.update(collection, new DDocument(searchKey, searchValue), new DDocument(updateKey, updateValue));
    }

    @Override
    public void update(String collection, String searchKey, Object searchValue, DDocument updates) {
        sqlClient.update(collection, searchKey, searchValue, updates);
    }

    @Override
    public void update(String collection, DDocument search, DDocument updates) {
        this.update(collection, search.first().getKey(), search.first().getValue(), updates);
    }

    @Override
    public Set<DDocument> find(String collection) {
        return this.find(collection, null);
    }

    @Override
    public Set<DDocument> find(String collection, String key, Object value) {
        return this.find(collection, new DDocument(key, value));
    }

    @Override
    public Set<DDocument> find(String collection, DDocument search) {
        return sqlClient.find(collection, search);
    }

    @Override
    public void disconnect() throws Exception {
        if(this.connection == null) {
            return;
        }
        this.connection.close();
    }

}
