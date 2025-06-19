package com.mars.deimos;

import com.mars.deimos.packets.SyncPacket;
import net.minecraft.network.Connection;

public abstract class ServerClass {

    public static ServerClass INSTANCE;
    private static boolean skip = false;

    public void serverinit(boolean ignore) {
        INSTANCE = this;
        if(ignore){
            skip = ignore;
        }

        SyncPacket.encodeType.put(Integer.class.toString(), (buf, value) -> buf.writeInt((Integer) value));
        SyncPacket.encodeType.put(Float.class.toString(), (buf, value) -> buf.writeFloat((Float) value));
        SyncPacket.encodeType.put(Double.class.toString(), (buf, value) -> buf.writeDouble((Double) value));
        SyncPacket.encodeType.put(Long.class.toString(), (buf, value) -> buf.writeLong((Long) value));
        SyncPacket.encodeType.put(Short.class.toString(), (buf, value) -> buf.writeShort((Short) value));
        SyncPacket.encodeType.put(Byte.class.toString(), (buf, value) -> buf.writeByte((Byte) value));
        SyncPacket.encodeType.put(Boolean.class.toString(), (buf, value) -> buf.writeBoolean((Boolean) value));
        SyncPacket.encodeType.put(String.class.toString(), (buf, value) -> buf.writeUtf((String) value));
        SyncPacket.encodeType.put(Character.class.toString(), (buf, value) -> buf.writeChar((Character) value));

    }

    public static void playerJoined(Connection connection) {
        if(!skip) {
            INSTANCE.sendToPlayer(connection);
        }
    }

    public abstract void sendToPlayer(Connection connection);
}
