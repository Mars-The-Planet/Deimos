package com.mars.deimos.mixin;

import com.mars.deimos.CommonClass;
import com.mars.deimos.ServerClass;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public class onPlayerConnectingConfigPhase {
    @Shadow @Final
    Connection connection;

    @Inject(at = @At("TAIL"), method = "handleLoginAcknowledgement")
    public void Event(ServerboundLoginAcknowledgedPacket packet, CallbackInfo ci) {
        ServerClass.playerJoined(connection);
    }

}
