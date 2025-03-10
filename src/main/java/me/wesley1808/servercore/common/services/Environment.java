package me.wesley1808.servercore.common.services;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.Optional;

public class Environment {
    public static final String VERSION = getVersion();
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    public static final boolean CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    public static final boolean SPARK = FabricLoader.getInstance().isModLoaded("spark");

    private static String getVersion() {
        Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer("servercore");
        return optional.map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("Unknown");
    }
}
