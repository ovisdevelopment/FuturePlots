package dev.ovis.futureplots.core;

import dev.ovis.futureplots.core.provider.data.DataProvider;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class FP {

    @Getter
    public static FP instance;

    @Getter
    final IPlotMain iPlotMain;

    @Getter
    final DataProvider dataProvider;



    public FP(IPlotMain plotMain) {
        instance = this;

        iPlotMain = plotMain;
        dataProvider = new DataProvider(this);
    }
}
