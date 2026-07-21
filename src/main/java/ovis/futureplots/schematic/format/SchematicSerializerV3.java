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

package ovis.futureplots.schematic.format;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;
import org.powernukkitx.math.BlockVector3;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.registry.Registries;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ovis.futureplots.schematic.Schematic;
import ovis.futureplots.schematic.SchematicBlock;
import ovis.futureplots.schematic.SchematicBlockEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchematicSerializerV3 implements SchematicSerializer {

    public static final SchematicSerializer INSTANCE = new SchematicSerializerV3();

    @Override
    public void serialize(Schematic schematic, ByteBuf buf) {

        // Block Palette
        buf.writeIntLE(schematic.getBlockPalette().size());

        for (SchematicBlock block : schematic.getBlockPalette()) {
            buf.writeIntLE(block.getLayer0().blockStateHash());
            buf.writeIntLE(block.getLayer1().blockStateHash());
        }

        // Blocks
        buf.writeIntLE(schematic.getBlocks().size());

        for (Object2IntMap.Entry<Vector3> entry : schematic.getBlocks().object2IntEntrySet()) {
            Vector3 pos = entry.getKey();
            int paletteIndex = entry.getIntValue();

            buf.writeIntLE(pos.getFloorX());
            buf.writeIntLE(pos.getFloorY());
            buf.writeIntLE(pos.getFloorZ());

            buf.writeIntLE(paletteIndex);
        }

        // Block Entities
        buf.writeIntLE(schematic.getBlockEntities().size());

        for (Map.Entry<BlockVector3, SchematicBlockEntity> entry : schematic.getBlockEntities().entrySet()) {
            BlockVector3 pos = entry.getKey();
            SchematicBlockEntity entity = entry.getValue();

            buf.writeIntLE(pos.getX());
            buf.writeIntLE(pos.getY());
            buf.writeIntLE(pos.getZ());

            writeString(buf, entity.getType());

            byte[] nbtBytes = writeNbt(entity.getCompoundTag().toNetwork());
            buf.writeIntLE(nbtBytes.length);
            buf.writeBytes(nbtBytes);
        }
    }

    @Override
    public void deserialize(Schematic schematic, ByteBuf buf) {

        // Block Palette
        int paletteCount = buf.readIntLE();

        for (int i = 0; i < paletteCount; i++) {
            int layer0 = buf.readIntLE();
            int layer1 = buf.readIntLE();

            schematic.getBlockPalette().add(
                    new SchematicBlock(
                            Registries.BLOCKSTATE.get(layer0),
                            Registries.BLOCKSTATE.get(layer1)
                    )
            );
        }

        // Blocks
        int blockCount = buf.readIntLE();

        for (int i = 0; i < blockCount; i++) {
            int x = buf.readIntLE();
            int y = buf.readIntLE();
            int z = buf.readIntLE();

            int paletteIndex = buf.readIntLE();

            schematic.getBlocks().put(new Vector3(x, y, z), paletteIndex);
        }

        // Block Entities
        int entityCount = buf.readIntLE();

        for (int i = 0; i < entityCount; i++) {
            int x = buf.readIntLE();
            int y = buf.readIntLE();
            int z = buf.readIntLE();

            String type = readString(buf);

            int nbtLength = buf.readIntLE();
            byte[] nbtBytes = new byte[nbtLength];
            buf.readBytes(nbtBytes);

            CompoundTag tag = CompoundTag.fromNetwork(readNbt(nbtBytes));

            schematic.getBlockEntities().put(
                    new BlockVector3(x, y, z),
                    new SchematicBlockEntity(type, tag)
            );
        }
    }

    @Override
    public int version() {
        return 3;
    }

    private void writeString(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes();
        buf.writeIntLE(bytes.length);
        buf.writeBytes(bytes);
    }

    private String readString(ByteBuf buf) {
        int len = buf.readIntLE();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes);
    }

    private byte[] writeNbt(NbtMap tag) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             NBTOutputStream writer = NbtUtils.createWriterLE(out)) {

            writer.writeTag(tag);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private NbtMap readNbt(byte[] bytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return (NbtMap) NbtUtils.createReaderLE(in).readTag();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
