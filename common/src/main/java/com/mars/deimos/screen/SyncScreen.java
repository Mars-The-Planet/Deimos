package com.mars.deimos.screen;

import com.mars.deimos.config.DeimosConfig;
import com.mars.deimos.config.DeimosConfigScreenClass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


public class SyncScreen extends Screen {
    private final List<String> configMismatches;
    private final Button backButton;
    private final Button applyButton;
    private final Button leftButton;
    private final Button rightButton;
    private int currentPage = 1;
    private final int topListOffset = 60;
    private final ResourceLocation blackPixel = ResourceLocation.fromNamespaceAndPath("deimos", "textures/gui/sprites/icon/transparent_black_pixel.png");


    private int getItemsPerPage() {
        return (this.height - topListOffset - 70) / 15;
    }

    private int getPages() {
        return (int) Math.ceil((double) this.configMismatches.size() / getItemsPerPage());
    }

    public SyncScreen(List<String> configMismatches, Map<String, Map<Field, Object>> changesWRestart) {
        super(Component.literal("SyncScreen"));
        this.configMismatches = configMismatches;
        assert this.minecraft != null;
        this.backButton = Button.builder(Component.literal("Back"), (ButtonWidget) -> this.minecraft.setScreen(new JoinMultiplayerScreen(new TitleScreen()))).width(100).build();

        this.applyButton = Button.builder(Component.literal("Apply and exit"), (ButtonWidget) -> {
            changesWRestart.forEach((modid, config) -> {
                config.forEach((field, o) -> {
                    try {
                        field.set(null, o);
                        DeimosConfigScreenClass.applyWhenQuitting.remove(field);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
                DeimosConfig.write(modid);
            });
            this.minecraft.stop();
        }).width(100).build();

        this.leftButton = Button.builder(Component.literal("<"), (ButtonWidget) -> {
            this.currentPage = Math.max(1, this.currentPage - 1);
        }).width(20).build();

        this.rightButton = Button.builder(Component.literal(">"), (ButtonWidget) -> {
            this.currentPage = Math.min(getPages(), this.currentPage + 1);
        }).width(20).build();

        this.addRenderableWidget(this.backButton);
        this.addRenderableWidget(this.applyButton);
        this.addRenderableWidget(this.leftButton);
        this.addRenderableWidget(this.rightButton);
    }


    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        repositionElements();
        super.render(context, mouseX, mouseY, delta);
        // context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("deimos", "textures/gui/sprites/icon/deimos.png"), 0, 0, 0, 0, 1600, 900, 1600, 900);
        context.drawCenteredString(this.font, Component.literal("Deimos Config Synchronization"), this.width / 2, 20, 0xff7900);
        context.drawCenteredString(this.font, Component.literal("Client configuration incorrect in following mods:"), this.width / 2, 32, 0xcd0000);

        // context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("deimos", "textures/gui/sprites/icon/black_pixel.png"), this.width / 8, topListOffset-5, 0, 0, 3 * this.width / 4, getItemsPerPage()*15 + 10, 1, 1);
        // context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("deimos", "textures/gui/sprites/icon/white_pixel.png"), this.width/2 - (this.width-(this.width/2 - 100 - 50)*2)/2, topListOffset-10, 0, 0, this.width-(this.width/2 - 100 - 50)*2, 1, 1, 1);
        // context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("deimos", "textures/gui/sprites/icon/white_pixel.png"), this.width/2 - (this.width-(this.width/2 - 100 - 50)*2)/2, topListOffset + getItemsPerPage()*15 + 5, 0, 0, this.width-(this.width/2 - 100 - 50)*2, 1, 1, 1);

        // context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("deimos", "textures/gui/sprites/icon/white_pixel.png"), this.width/2 - this.width/16, topListOffset-5, 0, 0, this.width/8, 1, 1, 1);
        // context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("deimos", "textures/gui/sprites/icon/white_pixel.png"), this.width/2  - this.width/16, topListOffset + getItemsPerPage()*15 + 5, 0, 0, this.width/8, 1, 1, 1);

        // int x = this.width / 8;
        // int y = topListOffset-10;
        // int width = 3 * this.width / 4;
        // int height = getItemsPerPage()*15 + 15;

        // context.blit(blackPixel, x, y, width, height, 0, 0, 1, 1, width, height);

        context.fillGradient(this.width / 8, topListOffset-10, this.width / 8 + 3 * this.width / 4, topListOffset-10 + getItemsPerPage()*15 + 15, 0x80000000, 0x80000000);

        int pageSize = getItemsPerPage();
        context.drawCenteredString(this.font, Component.literal("%s/%s".formatted(this.currentPage, getPages())), this.width / 2, this.height - 55, 16777215);

        for(int i = 0; i < this.configMismatches.size(); i++) {
            if(pageSize*(this.currentPage-1) < i+1 && i+1 <= pageSize*this.currentPage) {
                context.drawCenteredString(this.font, Component.literal(this.configMismatches.get(i)), this.width / 2, topListOffset + 15 * (i - pageSize*(this.currentPage-1)), 16777215);
            }
        }
        this.leftButton.active = this.currentPage > 1;
        this.rightButton.active = this.currentPage < getPages();
    }

    @Override
    protected void repositionElements() {
        this.backButton.setY(this.height - 30);
        this.backButton.setX(this.width/2 - 100 - 50);

        this.applyButton.setY(this.height - 30);
        this.applyButton.setX(this.width/2 + 50);

        this.leftButton.setY(this.height - 60);
        this.leftButton.setX(this.width/2 - 20 - 50);

        this.rightButton.setY(this.height - 60);
        this.rightButton.setX(this.width/2 + 50);

    }
}
