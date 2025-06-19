package com.mars.deimos;

import com.mars.deimos.config.DeimosConfig;
import com.mars.deimos.packets.SyncPacket;
import com.mars.deimos.screen.SyncScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;


public class ClientClass {
    public static HashSet<Field> changedConfigsWRestart = new HashSet<>();
    public static Map<Field, Object> undoServerChanges = new HashMap<>();

    public static void clientinit() {
        SyncPacket.decodeType.put(Integer.class.toString(), FriendlyByteBuf::readInt);
        SyncPacket.decodeType.put(Float.class.toString(), FriendlyByteBuf::readFloat);
        SyncPacket.decodeType.put(Double.class.toString(), FriendlyByteBuf::readDouble);
        SyncPacket.decodeType.put(Long.class.toString(), FriendlyByteBuf::readLong);
        SyncPacket.decodeType.put(Short.class.toString(), FriendlyByteBuf::readShort);
        SyncPacket.decodeType.put(Byte.class.toString(), FriendlyByteBuf::readByte);
        SyncPacket.decodeType.put(Boolean.class.toString(), FriendlyByteBuf::readBoolean);
        SyncPacket.decodeType.put(String.class.toString(), FriendlyByteBuf::readUtf);
        SyncPacket.decodeType.put(Character.class.toString(), FriendlyByteBuf::readChar);
    }

    public static void applyServerConfig(SyncPacket sp, Consumer<Component> disconnect) {
        List<String> configMismatches = new ArrayList<>();
        Map<Field, Object> changes = new HashMap<>();
        Map<String, Map<Field, Object>> changesWRestart = new HashMap<>();
        sp.configClass.forEach((modid, config) -> config.forEach((fieldName, fieldValue) -> {
            try {
                Field clientField = DeimosConfig.configClass.get(modid).getField(fieldName);
                Object clientValue = clientField.get(null);
                // Constants.LOG.info("[{}] - {} = {} (S) -> {} (C) | (Equals? {})", modid, fieldName, fieldValue, clientValue, fieldValue.equals(clientValue));

                if(!fieldValue.equals(clientValue) ) {
                    if(clientField.getAnnotation(DeimosConfig.Entry.class).restartClient()) {
                        configMismatches.add("[%s]: \"%s\" = %s. Must be %s.".formatted(modid, fieldName, clientValue, fieldValue));
                        if(!changesWRestart.containsKey(modid)) {
                            changesWRestart.put(modid, new HashMap<>());
                        }
                        changesWRestart.get(modid).put(clientField, fieldValue);
                    } else {
                        changes.put(clientField, fieldValue);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }));
        if(!configMismatches.isEmpty()) {
            // sp.loadScreen(Context, configMismatches, changesWRestart);
            loadConfigMismatchScreen(disconnect, configMismatches, changesWRestart);
        } else {
            changes.forEach(((field, o) -> {
                try {
                    undoServerChanges.put(field, field.get(null));
                    field.set(null, o);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        // Constants.LOG.info("{}", ExampleConfig.retard2);
    }

    public static void loadConfigMismatchScreen(Consumer<Component> disconnect, List<String> configMismatches, Map<String, Map<Field, Object>> changesWRestart) {
        Minecraft client = Minecraft.getInstance();
        disconnect.accept(Component.literal("deimos.sync.error"));
        client.disconnect(new SyncScreen(configMismatches, changesWRestart));

    }

    public static void onClientDisconnect() {
        undoServerChanges.forEach((f, o) -> {
            try {
                f.set(null, o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
