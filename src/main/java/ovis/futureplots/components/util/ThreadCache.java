package ovis.futureplots.components.util;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.powernukkitx.utils.IterableThreadLocal;

public class ThreadCache {

    public static final IterableThreadLocal<FastByteArrayOutputStream> fbaos = new IterableThreadLocal<>() {
        public FastByteArrayOutputStream init() {
            return new FastByteArrayOutputStream(1024);
        }
    };

    public static void clean() {
        fbaos.clean();
    }
}