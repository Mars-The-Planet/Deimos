package com.mars.deimos;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

    public static final String MOD_ID = "deimos";
    public static final String MOD_NAME = "Deimos";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final ResourceLocation PACKET_IDENTIFIER = ResourceLocation.parse("deimos:sync");
}
