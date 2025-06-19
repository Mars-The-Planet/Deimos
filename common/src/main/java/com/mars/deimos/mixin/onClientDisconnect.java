package com.mars.deimos.mixin;

import com.mars.deimos.ClientClass;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class onClientDisconnect {
    @Inject(at = @At("TAIL"), method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V")
    public void disconnect(DisconnectionDetails disconnectionInfo, CallbackInfo ci) {
        ClientClass.onClientDisconnect();
    }
}
