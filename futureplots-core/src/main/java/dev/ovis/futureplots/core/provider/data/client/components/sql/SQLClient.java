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

package dev.ovis.futureplots.core.provider.data.client.components.sql;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.ovis.futureplots.core.provider.data.client.components.DDocument;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class SQLClient {

    private final Connection connection;

    public SQLClient(Connection connection) {
        this.connection = connection;
    }

    public void createTable(String name, SQLColumn columns) {
        try {
            StringBuilder columnsStringBuilder = new StringBuilder();

            for (String type : columns.get()) {
                columnsStringBuilder.append(type).append(", ");
            }

            String columnsString = columnsStringBuilder.toString();
            columnsString = columnsString.substring(0, columnsString.length() - 2);
            String statement = "CREATE TABLE IF NOT EXISTS `" + name + "`(" + columnsString + ");";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void insert(String collection, DDocument values) {
        try {
            StringBuilder valueNamesBuilder = new StringBuilder("(");
            StringBuilder valueDataBuilder = new StringBuilder("(");

            for (Map.Entry<String, Object> insert : values.getAll().entrySet()) {

                Object data = insert.getValue();

                if (data instanceof List || data instanceof Map) {
                    data = new Gson().toJson(data);
                }

                if (data instanceof String) data = "'" + data + "'";

                valueNamesBuilder.append(insert.getKey()).append(", ");
                valueDataBuilder.append(data).append(", ");
            }

            String valueNames = valueNamesBuilder.substring(0, valueNamesBuilder.length() - 2);
            valueNames = valueNames + ")";

            String valueData = valueDataBuilder.substring(0, valueDataBuilder.length() - 2);
            valueData = valueData + ")";


            String statementString = "INSERT INTO `" + collection + "` " + valueNames + " VALUES " + valueData + ";";

            PreparedStatement statement = connection.prepareStatement(statementString);
            statement.executeUpdate();
            statement.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void delete(String collection, DDocument search) {
        String key = search.first().getKey();
        Object value = search.first().getValue();
        try {
            if (value instanceof String) value = "'" + value + "'";
            String statement = "DELETE FROM `" + collection + "` WHERE " + key + " = " + value + ";";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.executeUpdate();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void update(String collection, String searchKey, Object searchValue, DDocument updates) {
        try {

            Object valueSearch = searchValue;
            if (valueSearch instanceof String) valueSearch = "'" + valueSearch + "'";

            StringBuilder updateBuilder = new StringBuilder();

            for (Map.Entry<String, Object> update : updates.getAll().entrySet()) {

                Object data = update.getValue();

                if (data instanceof List || data instanceof Map) {
                    data = new Gson().toJson(data);
                }

                if (data instanceof String) data = "'" + data + "'";

                updateBuilder.append(update.getKey()).append(" = ").append(data).append(", ");
            }

            String update = updateBuilder.substring(0, updateBuilder.length() - 2);
            String statement = "UPDATE `" + collection + "` SET " + update + " WHERE " + searchKey + " = " + valueSearch + ";";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public Set<DDocument> find(String collection, DDocument search) {
        String key = null;
        Object value = null;
        if(search != null) {
            key = search.first().getKey();
            value = search.first().getValue();
        }
        try {
            if (value instanceof String) value = "'" + value + "'";
            String statement = "SELECT * FROM `" + collection + "`" + (search != null ? " WHERE " + key + " = " + value : "") + ";";

            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData meta = resultSet.getMetaData();

            Set<DDocument> set = new HashSet<>();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();

                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    final String name = meta.getColumnName(i);
                    final Object obj = resultSet.getObject(i);
                    if (obj instanceof String) {
                        String str = obj.toString();
                        if (isValidJson(str)) {
                            Object object = new Gson().fromJson(str, Object.class);
                            map.put(name, object);
                        } else map.put(name, resultSet.getObject(i));
                    } else {
                        map.put(name, resultSet.getObject(i));
                    }
                }

                set.add(new DDocument(map));
            }

            preparedStatement.close();
            return set;

        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    private boolean isValidJson(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            return true; // Parsing erfolgreich
        } catch (JsonSyntaxException e) {
            return false; // Ungültiges JSON
        }
    }
}
