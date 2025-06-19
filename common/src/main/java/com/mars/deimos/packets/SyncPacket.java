package com.mars.deimos.packets;

import com.mars.deimos.Constants;
import com.mars.deimos.config.DeimosConfig;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SyncPacket implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, CustomPacketPayload> CODEC = StreamCodec.ofMember(
            SyncPacket::write,
            SyncPacket::new);
    public static final CustomPacketPayload.Type<CustomPacketPayload> TYPE = new CustomPacketPayload.Type<>(Constants.PACKET_IDENTIFIER);
    public static final Type<CustomPacketPayload> PACKET_TYPE = new CustomPacketPayload.Type<>(SyncPacket.TYPE.id());

    public static Map<String, BiConsumer<FriendlyByteBuf, Object>> encodeType = new HashMap<>();
    public static Map<String, Function<FriendlyByteBuf, Object>> decodeType = new HashMap<>();


    public Map<String, Map<String, Object>> configClass;
    public SyncPacket(Map<String, Class<? extends DeimosConfig>> originalConfigClass) {
        this.configClass = new HashMap<>();
        originalConfigClass.forEach((modid, config) -> {
            Map<String, Object> fields = new HashMap<>();
            for(Field field : config.getFields()) {
                if(field.isAnnotationPresent(DeimosConfig.Entry.class) && field.getAnnotation(DeimosConfig.Entry.class).forceServerValue()) {
                    try {
                        fields.put(field.getName(), field.get(null));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            this.configClass.put(modid, fields);
        });
    }

    public SyncPacket(FriendlyByteBuf buf) throws EncoderException {
        Map<String, Map<String, Object>> newConfigClass = new HashMap<>();
        int modAmount = buf.readInt();
        for(int currentMod_i = 0; currentMod_i < modAmount; currentMod_i++) {
            String modid = buf.readUtf();
            int fieldAmount = buf.readInt();
            Map<String, Object> fields = new HashMap<>();

            for(int currentField_i = 0; currentField_i < fieldAmount; currentField_i++) {
                String fieldName = buf.readUtf();
                String fieldType = buf.readUtf();
                if(!decodeType.containsKey(fieldType)) {throw new DecoderException("Mod [%s] used an unregistered field type (%s).".formatted(modid, fieldType));}

                Object fieldValue = decodeType.get(fieldType).apply(buf);
                // DeimosInit.LOGGER.info("[{}] - {} ({}) = {}", modid, fieldName, fieldType, fieldValue);
                // try{DeimosInit.LOGGER.info("Client: {}", DeimosConfig.configClass.get(modid).getField(fieldName).get(null));} catch (Exception ignored) {}

                fields.put(fieldName, fieldValue);
            }
            newConfigClass.put(modid, fields);
        }
        this.configClass = newConfigClass;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void write(CustomPacketPayload customPacketPayload, FriendlyByteBuf buf) {
        SyncPacket sp = (SyncPacket) customPacketPayload;
        buf.writeInt(sp.configClass.size());
        sp.configClass.forEach((modid, config) -> {
            buf.writeUtf(modid);
            int length = config.size();
            buf.writeInt(length);
            config.forEach((fieldName, fieldValue) -> {
                buf.writeUtf(fieldName);
                String fieldType = fieldValue.getClass().toString();
                if(encodeType.containsKey(fieldType)) {
                    buf.writeUtf(fieldType);
                    encodeType.get(fieldType).accept(buf, fieldValue);
                } else {
                    throw new EncoderException("Mod [%s] used an unregistered field type (%s).".formatted(modid, fieldType));
                }
            });
        });
    }
}
