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

import lombok.Getter;

import java.util.LinkedList;
import java.util.Locale;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class SQLColumn {

    private LinkedList<String> columns = new LinkedList<>();

    public SQLColumn() {}

    public SQLColumn(String name, Type type) {
        columns.add(name + " " + type.toString());
    }

    public SQLColumn(String name, Type type, String index) {
        columns.add(name + " " + type.toString() + " " + index);
    }

    public SQLColumn(String name, Type type, int size) {
        columns.add(name + " " + type.toString() + "(" + size + ")");
    }

    public SQLColumn(String name, Type type, int size, String index) {
        columns.add(name + " " + type.toString() + "(" + size + ")" + " " + index);
    }

    public SQLColumn(String name, Type type, int sizeFrom, int sizeTo) {
        columns.add(name + " " + type.toString() + "(" + sizeFrom + "," + sizeTo + ")");
    }

    public SQLColumn append(String name, Type type) {
        columns.add(name + " " + type.toString());
        return this;
    }

    public SQLColumn append(String name, Type type, String index) {
        columns.add(name + " " + type.toString() + " " + index);
        return this;
    }

    public SQLColumn append(String name, Type type, int size) {
        columns.add(name + " " + type.toString() + "(" + size + ")");
        return this;
    }

    public SQLColumn append(String name, Type type, int size, String index) {
        columns.add(name + " " + type.toString() + "(" + size + ")" + " " + index);
        return this;
    }

    public SQLColumn append(String name, Type type, int sizeFrom, int sizeTo) {
        columns.add(name + " " + type.toString() + "(" + sizeFrom + "," + sizeTo + ")");
        return this;
    }

    public LinkedList<String> get() {
        return this.columns;
    }


    public static enum Type {
        INT,
        TINYINT,
        SMALLINT,
        MEDIUMINT,
        BIGINT,
        LONG,
        FLOAT,
        DOUBLE,
        DECIMAL,
        DATE,
        DATETIME,
        TIMESTAMP,
        TIME,
        YEAR,
        CHAR,
        VARCHAR,
        BLOB,
        TEXT,
        TINYBLOB,
        TINYTEXT,
        MEDIUMBLOB,
        MEDIUMTEXT,
        LONGBLOB,
        LONGTEXT,
        ENUM;

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ROOT);
        }
    }

    public static enum Index {

        NOT_NULL("NOT NULL"),
        UNIQUE("UNIQUE"),
        PRIMARY_KEY("PRIMARY KEY"),
        FOREIGN_KEY("FOREIGN KEY"),
        DEFAULT("DEFAULT");

        @Getter
        private String value;

        Index(String value) {
            this.value = value;
        }

        public Index append(Index index, Object defaultValue) {
            System.out.println(index.value);
            value = value + " " + index.value + (defaultValue != null ? " " + defaultValue : "");
            return this;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

}
