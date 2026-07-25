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


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import dev.ovis.futureplots.core.provider.data.client.clientdetails.ClientDetails;
import dev.ovis.futureplots.core.provider.data.client.clientdetails.MongoDBDetails;
import dev.ovis.futureplots.core.provider.data.client.components.DDocument;
import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class MongoDBConnection extends Connection {

    private MongoClient client;
    private MongoDatabase database;

    @Override
    public void connect(ClientDetails clientDetails) {
        MongoDBDetails details = (MongoDBDetails) clientDetails;

        client = MongoClients.create(details.getUri());
        database = client.getDatabase(details.getDatabase());
    }

    @Override
    public void insert(String collection, DDocument document) {
        this.database.getCollection(collection).insertOne(convertToBson(document));
    }

    @Override
    public void delete(String collection, String key, Object value) {
        this.delete(collection, new DDocument(key, value));
    }

    @Override
    public void delete(String collection, DDocument search) {
        this.database.getCollection(collection).deleteOne(convertToBson(search));
    }

    @Override
    public void update(String collection, String searchKey, Object searchValue, String updateKey, String updateValue) {
        this.update(collection, new DDocument(searchKey, searchValue), new DDocument(updateKey, updateValue));
    }

    @Override
    public void update(String collection, String searchKey, Object searchValue, DDocument updates) {
        this.update(collection, new DDocument(searchKey, searchValue), updates);
    }

    @Override
    public void update(String collection, DDocument search, DDocument updates) {
        this.database.getCollection(collection).updateOne(convertToBson(search), new Document("$set", convertToBson(updates)));
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
        Set<DDocument> set = new HashSet<>();
        FindIterable<Document> documents = search != null ? this.database.getCollection(collection).find(convertToBson(search)) : this.database.getCollection(collection).find();
        for (Document document : documents) {
            set.add(convertFromBson(document));
        }
        return set;
    }

    @Override
    public void disconnect() {
        if(this.client == null) {
            return;
        }
        this.client.close();
    }

    private Document convertToBson(final DDocument document) {
        Document doc = new Document();
        document.getAll().forEach(doc::append);
        return doc;
    }

    private DDocument convertFromBson(final Document document) {
        DDocument doc = new DDocument();
        document.forEach(doc::append);
        return doc;
    }
}
