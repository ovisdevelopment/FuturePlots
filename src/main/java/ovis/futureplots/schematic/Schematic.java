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
 */

package ovis.futureplots.schematic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.powernukkitx.Server;
import org.powernukkitx.blockentity.BlockEntity;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.math.BlockVector3;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;
import com.github.luben.zstd.Zstd;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.schematic.format.SchematicSerializer;
import ovis.futureplots.schematic.format.SchematicSerializers;
import ovis.futureplots.components.util.nukkit.Zlib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@Getter
@ToString
@EqualsAndHashCode
public class Schematic {

    private static final byte[] MAGIC = {0x53, 0x43, 0x48, 0x45, 0x4D};

    private final List<SchematicBlock> blockPalette;
    private final Object2IntMap<Vector3> blocks;
    private final Map<BlockVector3, SchematicBlockEntity> blockEntities;

    public Schematic() {
        this.blockPalette = new ArrayList<>();
        this.blocks = new Object2IntArrayMap<>();
        this.blockEntities = new HashMap<>();
    }

    public boolean isEmpty() {
        return this.blockPalette.isEmpty() && this.blocks.isEmpty() && this.blockEntities.isEmpty();
    }

    public void addBlock(Vector3 vector3, SchematicBlock block) {
        int index = this.blockPalette.indexOf(block);
        if (index == -1) {
            this.blockPalette.add(block);
            index = this.blockPalette.size() - 1;
        }

        this.blocks.put(vector3, index);
    }

    public void addBlockEntity(BlockVector3 blockVector3, String type, CompoundTag compoundTag) {
        this.blockEntities.put(blockVector3, new SchematicBlockEntity(type, compoundTag));
    }

    public void buildInChunk(Vector3 start, IChunk IChunk, ShapeType[] shapes, Allowed<ShapeType> allowedShapes, Integer minX, Integer minZ, Integer maxX, Integer maxZ) {
        final int startX = start.getFloorX();
        final int startY = start.getFloorY();
        final int startZ = start.getFloorZ();

        for (Object2IntMap.Entry<Vector3> entry : this.blocks.object2IntEntrySet()) {
            final Vector3 blockVector = entry.getKey();
            final SchematicBlock schematicBlock = this.blockPalette.get(entry.getIntValue());

            final int x = startX + blockVector.getFloorX();
            final int y = startY + blockVector.getFloorY();
            final int z = startZ + blockVector.getFloorZ();

            if (minX != null && (x < minX || x > maxX)) continue;
            if (minZ != null && (z < minZ || z > maxZ)) continue;

            if (IChunk.getX() == x >> 4 && IChunk.getZ() == z >> 4) {
                final int bX = x & 15;
                final int bZ = z & 15;
                final ShapeType shapeType = shapes[(bZ << 4) | bX];
                if (allowedShapes.isDisallowed(shapeType)) continue;

                IChunk.setBlockState(bX, y, bZ, schematicBlock.getLayer0(), 0);
                IChunk.setBlockState(bX, y, bZ, schematicBlock.getLayer1(), 1);
            }
        }

        for (Map.Entry<BlockVector3, SchematicBlockEntity> entry : this.blockEntities.entrySet()) {
            final SchematicBlockEntity blockEntity = entry.getValue();
            final int x = startX + entry.getKey().getX();
            final int y = startY + entry.getKey().getY();
            final int z = startZ + entry.getKey().getZ();

            if (minX != null && (x < minX || x > maxX)) continue;
            if (minZ != null && (z < minZ || z > maxZ)) continue;

            if (IChunk.getX() == x >> 4 && IChunk.getZ() == z >> 4) {
                final int bX = x & 15;
                final int bZ = z & 15;
                final ShapeType shapeType = shapes[(bZ << 4) | bX];
                if (allowedShapes.isDisallowed(shapeType)) continue;

                try {
                    BlockEntity.createBlockEntity(blockEntity.getType(), IChunk, blockEntity.getCompoundTag().
                            putString("id", blockEntity.getType()).
                            putInt("x", x).
                            putInt("y", y).
                            putInt("z", z)
                    );
                } catch (Exception e) {
                    FuturePlots.INSTANCE.getLogger().error("Could not create block entity " + blockEntity.getType() + " in Chunk[" + IChunk.getX() + ", " + IChunk.getZ() + "] at " + x + ":" + y + ":" + z + "!", e);
                }
            }
        }
    }

    public synchronized void init(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            ByteBuf buf = Unpooled.wrappedBuffer(bytes);
            ByteBufInputStream in = new ByteBufInputStream(buf);

            byte[] magicRead = new byte[MAGIC.length];
            in.readFully(magicRead);

            // Legacy (Zlib)
            if (!Arrays.equals(magicRead, MAGIC)) {
                byte[] inflated = Zlib.inflate(bytes);
                ByteBuf legacyBuf = Unpooled.wrappedBuffer(inflated);

                SchematicSerializers.get(1).deserialize(this, legacyBuf);
                Server.getInstance().getScheduler().scheduleDelayedTask(null, () -> this.save(file), 1);
                return;
            }

            // Version
            int version = in.readByte();

            int b1 = in.read();
            int b2 = in.read();
            int b3 = in.read();
            int b4 = in.read();
            int decompressedSize = (b1 & 0xFF)
                    | ((b2 & 0xFF) << 8)
                    | ((b3 & 0xFF) << 16)
                    | ((b4 & 0xFF) << 24);

            byte[] compressed = new byte[buf.readableBytes()];
            in.readFully(compressed);

            byte[] decompressed = Zstd.decompress(compressed, decompressedSize);

            ByteBuf dataBuf = Unpooled.wrappedBuffer(decompressed);
            ByteBufInputStream dataIn = new ByteBufInputStream(dataBuf);

            SchematicSerializers.get(version).deserialize(this, dataBuf);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public synchronized void save(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {

            SchematicSerializer serializer = SchematicSerializers.getLatest();

            ByteBuf contentBuf = Unpooled.buffer();
            ByteBufOutputStream contentOut = new ByteBufOutputStream(contentBuf);

            serializer.serialize(this, contentBuf);

            byte[] raw = new byte[contentBuf.readableBytes()];
            contentBuf.readBytes(raw);

            byte[] compressed = Zstd.compress(raw);

            ByteBuf headerBuf = Unpooled.buffer();
            ByteBufOutputStream headerOut = new ByteBufOutputStream(headerBuf);

            headerOut.write(MAGIC);

            headerOut.writeByte(serializer.version());

            headerOut.write(raw.length & 0xFF);
            headerOut.write((raw.length >>> 8) & 0xFF);
            headerOut.write((raw.length >>> 16) & 0xFF);
            headerOut.write((raw.length >>> 24) & 0xFF);

            headerOut.write(compressed);

            byte[] finalData = new byte[headerBuf.readableBytes()];
            headerBuf.readBytes(finalData);

            fos.write(finalData);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}