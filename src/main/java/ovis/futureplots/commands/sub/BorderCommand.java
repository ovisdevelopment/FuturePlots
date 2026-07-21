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

package ovis.futureplots.commands.sub;

import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.form.element.simple.ButtonImage;
import org.powernukkitx.form.element.simple.ElementButton;
import org.powernukkitx.form.window.SimpleForm;
import ovis.futureplots.FuturePlots;
import ovis.futureplots.commands.SubCommand;
import ovis.futureplots.components.util.language.TranslationKey;
import ovis.futureplots.manager.PlotManager;
import ovis.futureplots.components.util.BlockEntry;
import ovis.futureplots.components.util.Plot;

import java.util.Locale;


/**
 * @modified Tim tim03we, Ovis Development (2024)
 */
public class BorderCommand extends SubCommand {

    private final FuturePlots plugin;

    public BorderCommand(FuturePlots plugin) {
        super(plugin, "border");
        this.plugin = plugin;
        this.identify();
        this.playerOnly();
        this.setPermissions("plots.border");
    }

    @Override
    public boolean execute(CommandSender sender, String command, String[] args) {
        Player player = (Player) sender;
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        final Plot plot;
        if(plotManager == null || (plot = plotManager.getMergedPlot(player.getFloorX(), player.getFloorZ())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT));
            return false;
        }

        if(!player.hasPermission("plot.command.admin.border") && !plot.isOwner(player.getUniqueId())) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_OWNER));
            return false;
        }

        final SimpleForm window = new SimpleForm(this.translate(player, TranslationKey.BORDER_FORM_TITLE), "");

        for(BlockEntry entry : this.plugin.getBorderEntries()) {
            final String text;
            if(entry.isDefault()) {
                text = this.translate(player, TranslationKey.BORDER_RESET_TO_DEFAULT_BUTTON);
            } else {
                final boolean hasPerm = entry.getPermission() == null || player.hasPermission(entry.getPermission());
                final String permText = this.translate(player, hasPerm ? TranslationKey.BORDER_BUTTON_HAS_PERM : TranslationKey.BORDER_BUTTON_NO_PERM);

                text = this.translate(player, TranslationKey.BORDER_FORM_BUTTON, entry.getName(), permText);
            }

            final String imageType = entry.getImageType();
            final ButtonImage imageData;
            switch(imageType == null ? "" : imageType.toLowerCase(Locale.ROOT)) {
                case "url" -> imageData = new ButtonImage(ButtonImage.Type.URL, entry.getImageData());
                case "path" -> imageData = new ButtonImage(ButtonImage.Type.PATH, entry.getImageData());
                default -> imageData = null;
            }

            ElementButton button = new ElementButton(text);
            if(imageData != null) button = button.image(imageData);

            window.addButton(button, p -> {
                if(entry.getPermission() != null && !player.hasPermission(entry.getPermission())) {
                    player.sendMessage(this.translate(player, TranslationKey.BORDER_NO_PERMS, entry.getName()));
                    return;
                }

                if(entry.isDefault()) {
                    for(Plot mergedPlot : plotManager.getConnectedPlots(plot))
                        plotManager.changeBorder(mergedPlot, (plot.hasOwner() ? plotManager.getLevelSettings().getClaimPlotState() : plotManager.getLevelSettings().getWallPlotState()));
                    player.sendMessage(this.translate(player, TranslationKey.BORDER_RESET_TO_DEFAULT_SUCCESS));
                } else {
                    for(Plot mergedPlot : plotManager.getConnectedPlots(plot))
                        plotManager.changeBorder(mergedPlot, entry.getBlockState());
                    player.sendMessage(this.translate(player, TranslationKey.BORDER_SUCCESS, entry.getName()));
                }
            });
        }

        window.send(player);
        return true;
    }

}
