package com.mars.deimos.screen;

import com.mars.deimos.config.DeimosConfig;
import com.mars.deimos.config.DeimosConfigScreenClass;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

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
        context.drawCenteredString(this.font, Component.literal("Deimos Config Synchronization"), this.width / 2, 20, 0xff7900);
        context.drawCenteredString(this.font, Component.literal("Client configuration incorrect in following mods:"), this.width / 2, 32, 0xcd0000);
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
