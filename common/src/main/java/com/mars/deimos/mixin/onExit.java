package com.mars.deimos.mixin;

import com.mars.deimos.Constants;
import com.mars.deimos.config.DeimosConfig;
import com.mars.deimos.config.DeimosConfigScreenClass;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class onExit {
    @Inject(at = @At("HEAD"), method = "stop")
    public void stop(CallbackInfo ci) {
        // Constants.LOG.info("Saving configs.");
        DeimosConfigScreenClass.applyWhenQuitting.forEach((field, value) -> {
            try {
                field.set(null, value);
            } catch (IllegalAccessException e) {
                Constants.LOG.error("Failed in saving {}", field.getName());
            }
        });
        DeimosConfigScreenClass.applyWhenQuitting.clear();
        DeimosConfig.configClass.forEach((modid, config) -> {
            DeimosConfig.write(modid);
        });
    }
}
