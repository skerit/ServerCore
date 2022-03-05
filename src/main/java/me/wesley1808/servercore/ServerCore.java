package me.wesley1808.servercore;

import com.mojang.brigadier.CommandDispatcher;
import me.wesley1808.servercore.commands.MobcapsCommand;
import me.wesley1808.servercore.commands.ServerCoreCommand;
import me.wesley1808.servercore.commands.StatisticsCommand;
import me.wesley1808.servercore.config.Config;
import me.wesley1808.servercore.utils.TickManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerCore implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("ServerCore");
    private static MinecraftServer server;

    public static Logger getLogger() {
        return LOGGER;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[ServerCore] Initializing...");
        Config.load();
        this.registerEvents();
    }

    private void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onShutdown);
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
    }

    private void onTick(MinecraftServer server) {
        TickManager.update(server);
    }

    private void onServerStarted(MinecraftServer server) {
        ServerCore.server = server;
        TickManager.initValues(server.getPlayerList());
    }

    private void onShutdown(MinecraftServer server) {
        Config.save();
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        ServerCoreCommand.register(dispatcher);
        StatisticsCommand.register(dispatcher);
        MobcapsCommand.register(dispatcher);
    }
}
