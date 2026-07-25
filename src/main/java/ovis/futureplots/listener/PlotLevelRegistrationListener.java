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

package ovis.futureplots.listener;

import lombok.RequiredArgsConstructor;
import org.powernukkitx.Player;
import org.powernukkitx.block.BlockAir;
import org.powernukkitx.block.BlockState;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.level.Level;
import org.powernukkitx.registry.Registries;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.components.util.language.manager.LanguageManager;
import ovis.futureplots.components.util.language.TranslationKey;

/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
@RequiredArgsConstructor
public class PlotLevelRegistrationListener implements Listener {

    private final FuturePlots plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(PlayerChatEvent event) {
        final Player player = event.getPlayer();
        LanguageManager language = new LanguageManager(player.getClientChainData().getLanguageCode());
        if (this.plugin.getLevelRegistrationMap().containsKey(player)) {
            event.setCancelled(true);

            final PlotLevelRegistration registration = this.plugin.getLevelRegistrationMap().get(player);

            switch (registration.getState()) {
                case DIMENSION -> {
                    final int dimension;
                    try {
                        dimension = Integer.parseInt(event.getMessage());
                    } catch (NumberFormatException e) {
                        break;
                    }

                    registration.getLevelSettings().setDimension(dimension);
                }
                case PLOT_BIOME -> {
                    final int biome;
                    try {
                        biome = Integer.parseInt(event.getMessage());
                    } catch (NumberFormatException e) {
                        break;
                    }

                    registration.getLevelSettings().setPlotBiome(biome);
                }
                case ROAD_BIOME -> {
                    final int biome;
                    try {
                        biome = Integer.parseInt(event.getMessage());
                    } catch (NumberFormatException e) {
                        break;
                    }
                    registration.getLevelSettings().setRoadBiome(biome);
                }
                case FIRST_LAYER -> {
                    try {
                        final BlockState blockState = Registries.BLOCK.get("minecraft:" + event.getMessage()).getBlockState();
                        if (blockState == BlockAir.STATE) {
                            break;
                        }
                        registration.getLevelSettings().setFirstLayerBlockHash(blockState.blockStateHash());
                    } catch (NumberFormatException ignore) {
                    }
                }
                case MIDDLE_LAYER -> {
                    try {
                        final BlockState blockState = Registries.BLOCK.get("minecraft:" + event.getMessage()).getBlockState();
                        if (blockState == BlockAir.STATE) {
                            break;
                        }
                        registration.getLevelSettings().setMiddleLayerBlockHash(blockState.blockStateHash());
                    } catch (NumberFormatException ignore) {
                    }
                }
                case LAST_LAYER -> {
                    try {
                        final BlockState blockState = Registries.BLOCK.get("minecraft:" + event.getMessage()).getBlockState();
                        if (blockState == BlockAir.STATE) {
                            break;
                        }
                        registration.getLevelSettings().setLastLayerBlockHash(blockState.blockStateHash());
                    } catch (NumberFormatException ignore) {
                    }
                }
                case ROAD -> {
                    try {
                        final BlockState blockState = Registries.BLOCK.get("minecraft:" + event.getMessage()).getBlockState();
                        if (blockState == BlockAir.STATE) {
                            break;
                        }
                        registration.getLevelSettings().setRoadBlockHash(blockState.blockStateHash());
                    } catch (NumberFormatException ignore) {
                    }
                }
                case ROAD_FILLING -> {
                    try {
                        final BlockState blockState = Registries.BLOCK.get("minecraft:" + event.getMessage()).getBlockState();
                        if (blockState == BlockAir.STATE) {
                            break;
                        }
                        registration.getLevelSettings().setRoadFillingBlockHash(blockState.blockStateHash());
                    } catch (NumberFormatException ignore) {
                    }
                }
                case WALL_UNOWNED -> {
                    try {
                        final BlockState blockState = Registries.BLOCK.get("minecraft:" + event.getMessage()).getBlockState();
                        if (blockState == BlockAir.STATE) {
                            break;
                        }
                        registration.getLevelSettings().setWallPlotBlockHash(blockState.blockStateHash());
                    } catch (NumberFormatException ignore) {
                    }
                }
                case WALL_CLAIMED -> {
                    try {
                        final BlockState blockState = Registries.BLOCK.get("minecraft:" + event.getMessage()).getBlockState();
                        if (blockState == BlockAir.STATE) {
                            break;
                        }
                        registration.getLevelSettings().setClaimPlotBlockHash(blockState.blockStateHash());
                    } catch (NumberFormatException ignore) {
                    }
                }
                case WALL_FILLING -> {
                    try {
                        final BlockState blockState = Registries.BLOCK.get("minecraft:" + event.getMessage()).getBlockState();
                        if (blockState == BlockAir.STATE) {
                            break;
                        }
                        registration.getLevelSettings().setWallFillingBlockHash(blockState.blockStateHash());
                    } catch (NumberFormatException ignore) {
                    }
                }
                case PLOT_SIZE -> {
                    final int number;
                    try {
                        number = Integer.parseInt(event.getMessage());
                    } catch (NumberFormatException e) {
                        break;
                    }

                    registration.getLevelSettings().setPlotSize(number);
                }
                case ROAD_SIZE -> {
                    final int number;
                    try {
                        number = Integer.parseInt(event.getMessage());
                    } catch (NumberFormatException e) {
                        break;
                    }

                    registration.getLevelSettings().setRoadSize(number);
                }
                case GROUND_HEIGHT -> {
                    final int number;
                    try {
                        number = Integer.parseInt(event.getMessage());
                    } catch (NumberFormatException e) {
                        break;
                    }

                    registration.getLevelSettings().setGroundHeight(number);
                }
                case WALL_HEIGHT -> {
                    final int number;
                    try {
                        number = Integer.parseInt(event.getMessage());
                    } catch (NumberFormatException e) {
                        break;
                    }

                    registration.getLevelSettings().setWallHeight(number);
                }
            }

            final PlotLevelRegistration.RegistrationState[] states = PlotLevelRegistration.RegistrationState.values();
            final int nextStage = registration.getState().ordinal() + 1;
            if (nextStage >= states.length) {
                this.plugin.getLevelRegistrationMap().remove(player);
                Level level;
                if ((level = this.plugin.createLevel(registration.getLevelName(), registration.isDefaultLevel(), registration.getLevelSettings())) != null) {
                    player.sendMessage(language.message(
                            registration.isDefaultLevel() ? TranslationKey.GENERATE_SUCCESS_DEFAULT : TranslationKey.GENERATE_SUCCESS,
                            registration.getLevelName())
                    );
                    player.teleport(level.getSafeSpawn(level.getSpawnLocation()));
                } else {
                    player.sendMessage(language.message(TranslationKey.GENERATE_FAILURE, registration.getLevelName()));
                }
                return;
            }

            registration.setState(states[nextStage]);
            switch (registration.getState()) {
                case PLOT_BIOME -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_PLOT_BIOME,
                        registration.getLevelSettings().getPlotBiome()
                ));
                case ROAD_BIOME -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_ROAD_BIOME,
                        registration.getLevelSettings().getRoadBiome()));
                case FIRST_LAYER -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_FIRST_LAYER,
                        getIdentifier(registration.getLevelSettings().getFirstLayerState())
                ));
                case MIDDLE_LAYER -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_MIDDLE_LAYER,
                        getIdentifier(registration.getLevelSettings().getMiddleLayerState())
                ));
                case LAST_LAYER -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_LAST_LAYER,
                        getIdentifier(registration.getLevelSettings().getLastLayerState())
                ));
                case ROAD -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_ROAD,
                        getIdentifier(registration.getLevelSettings().getRoadState())
                ));
                case ROAD_FILLING -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_ROAD_FILLING,
                        getIdentifier(registration.getLevelSettings().getRoadFillingState())
                ));
                case WALL_UNOWNED -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_WALL_UNOWNED,
                        getIdentifier(registration.getLevelSettings().getWallPlotState())
                ));
                case WALL_CLAIMED -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_WALL_CLAIMED,
                        getIdentifier(registration.getLevelSettings().getClaimPlotState())
                ));
                case WALL_FILLING -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_WALL_FILLING,
                        getIdentifier(registration.getLevelSettings().getWallFillingState())
                ));
                case PLOT_SIZE -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_PLOT_SIZE,
                        registration.getLevelSettings().getPlotSize()
                ));
                case ROAD_SIZE -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_ROAD_SIZE,
                        registration.getLevelSettings().getRoadSize()
                ));
                case GROUND_HEIGHT -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_GROUND_HEIGHT,
                        registration.getLevelSettings().getGroundHeight()
                ));
                case WALL_HEIGHT -> player.sendMessage(language.message(
                        TranslationKey.GENERATE_WALL_HEIGHT,
                        registration.getLevelSettings().getWallHeight()
                ));
            }
        }
    }

    private String getIdentifier(BlockState blockState) {
        return blockState.getIdentifier().replace("minecraft:", "");
    }

    private boolean validNumber(String str) {
        try {
            int number = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
