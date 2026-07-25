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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author  Tim tim03we, Ovis Development (2024)
 */
public class DDocument {

    private final Map<String, Object> data = new LinkedHashMap<>();

    public DDocument() {

    }

    public DDocument(Map<String, Object> map) {
        this.data.putAll(map);
    }

    public DDocument(String key, Object value) {
        this.data.put(key, value);
    }

    public Map<String, Object> getAll() {
        return this.data;
    }

    public DDocument append(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public Map.Entry<String, Object> first() {
        return this.data.entrySet().iterator().next();
    }

    public String getString(String key) {
        return (String) this.data.get(key);
    }

    public boolean getBoolean(String key) {
        return (boolean) this.data.get(key);
    }

    public int getInt(String key) {
        return (int) this.data.get(key);
    }

    public float getFloat(String key) {
        return (float) this.data.get(key);
    }

    public long getLong(String key) {
        return ((Number) this.data.get(key)).longValue();
    }

    public List<String> getStringList(String key) {
        return (List<String>) this.data.get(key);
    }

    public Object getObject(String key) {
        return this.data.get(key);
    }
}
