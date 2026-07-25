package dev.ovis.futureplots.core;

import dev.ovis.futureplots.core.components.Platform;

public interface IPlotMain {

    Platform getPlatform();

    void disable();

    void registerCommands();

    void registerGenerator();
}
