/*
 * Copyright 2022 KCodeYT
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
 * Modified 2024 by tim03we, Ovis Development
 */

package ovis.futureplots.components.util;

import org.powernukkitx.block.Block;
import org.powernukkitx.block.BlockState;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@Builder
@Value
public class BlockEntry {

    boolean isDefault;
    String name;
    BlockState blockState;
    String imageType;
    String imageData;
    String permission;

    public static BlockEntry of(Map<?, ?> map) {
        BlockEntryBuilder builder = BlockEntry.builder().
                isDefault(map.get("name").equals("reset_to_default")).
                name((String) map.get("name")).
                imageType((String) map.get("image_type")).
                imageData((String) map.get("image_data")).
                permission((String) map.get("permission"));

        if (!builder.isDefault) {
            Block block = Block.get(builder.name);
            Number data = (Number) map.get("block_data");
            builder.blockState(block.getProperties().getBlockState(data.shortValue()));
        }

        return builder.build();
    }
}
