package com.mars.deimos;

import com.mars.deimos.config.DeimosConfig;
import com.mars.deimos.packets.SyncPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.function.Consumer;

public record SyncTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(Constants.PACKET_IDENTIFIER);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new SyncPacket(DeimosConfig.configClass));
        this.listener().finishCurrentTask(this.type());
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
