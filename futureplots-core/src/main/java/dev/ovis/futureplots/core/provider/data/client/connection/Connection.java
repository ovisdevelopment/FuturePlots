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
import dev.ovis.futureplots.core.provider.data.client.components.DCollection;
import dev.ovis.futureplots.core.provider.data.client.components.DDocument;
import dev.ovis.futureplots.core.provider.data.client.components.sql.SQLColumn;

import java.util.Set;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class Connection {

    public void connect(ClientDetails clientDetails) throws Exception {

    }

    public void createCollection(String name, SQLColumn columns) {

    }

    public void insert(String collection, DDocument document) {

    }

    public void update(String collection, String searchKey, Object searchValue, String updateKey, String updateValue) {

    }

    public void update(String collection, String searchKey, Object searchValue, DDocument updates) {

    }

    public void update(String collection, DDocument search, DDocument value) {

    }

    public void delete(String collection, String key, Object value) {

    }

    public void delete(String collection, DDocument search) {

    }

    public Set<DDocument> find(String collection) {
        return null;
    }

    public Set<DDocument> find(String collection, String key, Object value) {
        return null;
    }

    public Set<DDocument> find(String collection, DDocument search) {
        return null;
    }

    public void disconnect() throws Exception {

    }

    public DCollection getCollection(String collection) {
        return new DCollection(this, collection);
    }
}
