package com.github.nullforge;

import com.github.nullcore.NullCore;

public class NullForge extends NullCore {
    public static NullForge INSTANCE;
    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        registerPlugin();
        start("com.github.nullforge.Main");
    }

    @Override
    public void onDisable() {
        stop("com.github.nullforge.Main");
    }
}
