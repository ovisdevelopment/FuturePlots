package dev.ovis.futureplots.pnx;

import dev.ovis.futureplots.core.FP;
import dev.ovis.futureplots.core.IPlotMain;
import dev.ovis.futureplots.core.components.Platform;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.powernukkitx.plugin.PluginBase;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PNXMain extends PluginBase implements IPlotMain {

    @Getter
    public static PNXMain instance;

    @Getter
    FP core;

    @Override
    public void onEnable() {
        instance = this;

        core = new FP(this);
    }

    @Override
    public Platform getPlatform() {
        return Platform.PNX;
    }

    @Override
    public void disable() {

    }

    @Override
    public void registerCommands() {

    }

    @Override
    public void registerGenerator() {

    }
}
