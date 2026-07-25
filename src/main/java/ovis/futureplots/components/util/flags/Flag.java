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

package ovis.futureplots.components.util.flags;

import lombok.Getter;

/**
 * @author Tim tim03we, Ovis Development (2024)
 */
@Getter
public class Flag {

    private final String saveName;
    private final Object defaultValue;
    private final FlagType type;
    private final boolean basicFlag;

    public Flag(String saveName, Object defaultValue, FlagType type) {
        this.saveName = saveName;
        this.defaultValue = defaultValue;
        this.type = type;
        this.basicFlag = true;
    }

    public Flag(String saveName, Object defaultValue, FlagType type, boolean basicFlag) {
        this.saveName = saveName;
        this.defaultValue = defaultValue;
        this.type = type;
        this.basicFlag = basicFlag;
    }

    public Object update(Plot plot, String value) {
        Object finalVal = null;
        switch (type) {
            case BOOLEAN -> finalVal = Boolean.parseBoolean(value);
            case INTEGER -> finalVal = Integer.parseInt(value);
            case STRING -> finalVal = value;
            default -> {}
        }
        if (finalVal == null) {
            finalVal = defaultValue;
        }
        plot.setFlagValue(saveName, finalVal);
        plot.save();

        return finalVal;
    }

}
