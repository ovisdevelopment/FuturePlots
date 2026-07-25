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

package dev.ovis.futureplots.core.provider.data.client.components;

import dev.ovis.futureplots.core.provider.data.client.connection.Connection;
import lombok.Getter;

import java.util.Set;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class DCollection {

    @Getter
    private String collection;

    private Connection connection;


    public DCollection(Connection connection, String collection) {
        this.connection = connection;
        this.collection = collection;
    }

    public void insert(DDocument document) {
        this.connection.insert(this.collection, document);
    }

    public void update(String searchKey, Object searchValue, String updateKey, String updateValue) {
        this.connection.update(this.collection, searchKey, searchValue, updateKey, updateValue);
    }

    public void update(String searchKey, Object searchValue, DDocument updates) {
        this.connection.update(this.collection, searchKey, searchValue, updates);
    }

    public void update(DDocument search, DDocument value) {
        this.connection.update(this.collection, search, value);
    }

    public void delete(String key, Object value) {
        this.connection.delete(this.collection, key, value);
    }

    public void delete(DDocument search) {
        this.connection.delete(this.collection, search);
    }

    public Set<DDocument> find() {
        return this.connection.find(this.collection, null);
    }

    public Set<DDocument> find(String key, Object value) {
        return this.connection.find(this.collection, key, value);
    }

    public Set<DDocument> find(DDocument search) {
        return this.connection.find(this.collection, search);
    }
}
