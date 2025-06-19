package com.mars.deimos.config;

import com.google.common.collect.Lists;
import com.mars.deimos.ClientClass;
import com.mars.deimos.Constants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.mars.deimos.config.DeimosConfig.*;

public class DeimosConfigScreenClass {
    private static final Pattern INTEGER_ONLY = Pattern.compile("(-?[0-9]*)");
    private static final Pattern DECIMAL_ONLY = Pattern.compile("-?(\\d+\\.?\\d*|\\d*\\.?\\d+|\\.)");

    private static final List<EntryInfo> entries = getEntries();
    public static HashMap<Field, Object> applyWhenQuitting = new HashMap<>();

    // @OnlyIn(Dist.CLIENT)
    static void initClient(String modid, Field field, DeimosConfig.EntryInfo info) {
        info.dataType = field.getType();
        DeimosConfig.Entry e = field.getAnnotation(DeimosConfig.Entry.class);
        info.width = e != null ? e.width() : 0;
        info.field = field;
        info.modid = modid;
        if (info.dataType == List.class) {
            Class<?> listType = (Class<?>) ((ParameterizedType) info.field.getGenericType()).getActualTypeArguments()[0];
            try { info.dataType = (Class<?>) listType.getField("TYPE").get(null);
            } catch (NoSuchFieldException | IllegalAccessException ignored) { info.dataType = listType; }
        }

        if (e != null) {
            if (!e.name().isEmpty()) info.name = Component.translatable(e.name());
            if (info.dataType == int.class) textField(info, Integer::parseInt, INTEGER_ONLY, (int) e.min(), (int) e.max(), true);
            else if (info.dataType == float.class) textField(info, Float::parseFloat, DECIMAL_ONLY, (float) e.min(), (float) e.max(), false);
            else if (info.dataType == double.class) textField(info, Double::parseDouble, DECIMAL_ONLY, e.min(), e.max(), false);
            else if (info.dataType == String.class || info.dataType == ResourceLocation.class) textField(info, String::length, null, Math.min(e.min(), 0), Math.max(e.max(), 1), true);
            else if (info.dataType == boolean.class) {
                Function<Object, Component> func = value -> Component.translatable((Boolean) value ? "gui.yes" : "gui.no").withStyle((Boolean) value ? ChatFormatting.GREEN : ChatFormatting.RED);
                info.function = new AbstractMap.SimpleEntry<Button.OnPress, Function<Object, Component>>(button -> {
                    info.setValue(!(Boolean) info.value); button.setMessage(func.apply(info.value));
                }, func);
            } else if (info.dataType.isEnum()) {
                List<?> values = Arrays.asList(field.getType().getEnumConstants());
                Function<Object, Component> func = value -> Component.translatable(modid + ".deimosconfig." + "enum." + info.dataType.getSimpleName() + "." + info.toTemporaryValue());
                info.function = new AbstractMap.SimpleEntry<Button.OnPress, Function<Object, Component>>(button -> {
                    int index = values.indexOf(info.value) + 1;
                    info.value = values.get(index >= values.size() ? 0 : index); button.setMessage(func.apply(info.value));
                }, func);
            }}
        entries.add(info);
    }

    // @OnlyIn(Dist.CLIENT)
    public static class DeimosConfigScreen extends Screen {
        protected DeimosConfigScreen(Screen parent, String modid) {
            super(Component.translatable(modid + ".deimosconfig." + "title"));

            this.parent = parent; this.modid = modid;
            this.translationPrefix = modid + ".deimosconfig.";
            loadValues();

            for (EntryInfo e : entries) {
                if (e.modid.equals(modid)) {
                    String tabId = e.field.isAnnotationPresent(Entry.class) ? e.field.getAnnotation(Entry.class).category() : e.field.getAnnotation(Comment.class).category();
                    String name = translationPrefix + "category." + tabId;
                    if (!I18n.exists(name) && tabId.equals("default"))
                        name = translationPrefix + "title";
                    if (!tabs.containsKey(name)) {
                        Tab tab = new GridLayoutTab(Component.translatable(name));
                        e.tab = tab;
                        tabs.put(name, tab);
                    } else e.tab = tabs.get(name);
                }
            }
            tabNavigation = TabNavigationBar.builder(tabManager, this.width).addTabs((Tab[])this.tabs.values().toArray((Object[])new Tab[0])).build();
            tabNavigation.selectTab(0, false);
            tabNavigation.arrangeElements();
            prevTab = tabManager.getCurrentTab();
        }
        public final String translationPrefix, modid;
        public final Screen parent;
        public DeimosConfigListWidget list;
        public TabManager tabManager = new TabManager(a -> {}, a -> {});
        public Map<String, Tab> tabs = new HashMap<>();
        public Tab prevTab;
        public TabNavigationBar tabNavigation;
        public Button done;
        public double scrollProgress = 0d;
        public HashMap<Field, Object> cancelValueSave = new HashMap<>();

        // Real Time config update //
        @Override
        public void tick() {
            super.tick();
            if (prevTab != null && prevTab != tabManager.getCurrentTab()) {
                prevTab = tabManager.getCurrentTab();
                this.list.clear();
                fillList();
                list.setScrollAmount(0);
            }
            scrollProgress = list.getScrollAmount();
            updateEntries();
            updateButtons();
        }
        public void updateButtons() {
            if (this.list != null) {
                for (ButtonEntry entry : this.list.children()) {
                    if (entry.buttons != null && entry.buttons.size() > 1) {
                        if (entry.buttons.get(0) instanceof AbstractWidget widget)
                            if (widget.isFocused() || widget.isHovered()) widget.setTooltip(getTooltip(entry.info));
                        if (entry.buttons.get(1) instanceof Button button)
                            button.active = !Objects.equals(entry.info.value.toString(), entry.info.defaultValue.toString()) && judgeField(entry.info.field);
                    }
                }
            }
        }
        public void loadValues() {
            try {
                Map<Field, Object> gameMemory = new HashMap<>();
                for (Field f : configClass.get(modid).getFields()) {
                    gameMemory.put(f, f.get(null));
                }
                try {
                    gson.fromJson(Files.newBufferedReader(getPath(modid)), configClass.get(modid));
                } catch (Exception e) {
                    write(modid);
                }

                for (Field field : configClass.get(modid).getFields()) {
                    Object fileValue = field.get(null);
                    Object memoryValue = gameMemory.get(field);
                    // Constants.LOG.info("{} - {} / {}", field.getName(), fileValue, memoryValue);
                    if (!fileValue.equals(memoryValue)) {
                        Constants.LOG.info("Error loading \"{}\" from \"{}\". Data in game's memory is different from data in file. Continuing with value from game's memory. {} (Memory) | {} (File)", field.getName(), modid, memoryValue, fileValue);
                        field.set(null, memoryValue);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            for (EntryInfo info : entries) {
                if (info.field.isAnnotationPresent(Entry.class))
                    try {
                        info.value = info.field.getAnnotation(Entry.class).restartClient() && applyWhenQuitting.containsKey(info.field) ? applyWhenQuitting.get(info.field) : info.field.get(null);
                        cancelValueSave.put(info.field, info.value);
                        info.tempValue = info.toTemporaryValue();
                    } catch (IllegalAccessException ignored) {}
            }
        }
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.tabNavigation.keyPressed(keyCode)) return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        @Override
        public void onClose() {
            applyWhenQuitting.putAll(cancelValueSave);
            loadValues();
            cleanup();
            Objects.requireNonNull(this.minecraft).setScreen(parent);
        }
        private void cleanup() {
            entries.forEach(info -> {
                info.error = null; info.value = null; info.tempValue = null; info.actionButton = null; info.listIndex = 0; info.tab = null; info.inLimits = true;
            });
        }
        @Override
        public void init() {
            super.init();
            tabNavigation.setWidth(this.width);
            tabNavigation.arrangeElements();
            if (tabs.size() > 1) this.addRenderableWidget(tabNavigation);

            this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(this.width / 2 - 154, this.height - 26, 150, 20).build());
            done = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
                updateEntries();
                write(modid);
                cleanup();
                Objects.requireNonNull(this.minecraft).setScreen(parent);
            }).bounds(this.width / 2 + 4, this.height - 26, 150, 20).build());

            this.list = new DeimosConfigListWidget(this.minecraft, this.width, this.height - 57, 24, 25);
            this.addWidget(this.list); fillList();
            if (tabs.size() > 1) list.renderHeaderSeparator = false;
        }
        public void fillList() {
            for (EntryInfo info : entries) {
                if (info.modid.equals(modid) && (info.tab == null || info.tab == tabManager.getCurrentTab())) {
                    Component name = Objects.requireNonNullElseGet(info.name, () -> Component.translatable(translationPrefix + info.field.getName()));
                    SpriteIconButton resetButton = SpriteIconButton.builder(Component.translatable("controls.reset"), (button -> {
                        info.value = info.defaultValue;
                        info.listIndex = 0;
                        info.tempValue = info.toTemporaryValue();
                        list.clear();
                        fillList();
                    }), true).sprite(ResourceLocation.fromNamespaceAndPath("deimos","icon/reset"), 12, 12).size(20, 20).build();
                    resetButton.setPosition(width - 205 + 150 + 25, 0);

                    if (info.function != null) {
                        AbstractWidget widget;
                        Entry e = info.field.getAnnotation(Entry.class);

                        if (info.function instanceof Map.Entry) { // Enums & booleans
                            @SuppressWarnings("unchecked")
                            var values = (Map.Entry<Button.OnPress, Function<Object, Component>>) info.function;
                            if (info.dataType.isEnum())
                                values.setValue(value -> Component.translatable(translationPrefix + "enum." + info.field.getType().getSimpleName() + "." + info.value.toString()));
                            widget = Button.builder(values.getValue().apply(info.value), values.getKey()).bounds(width - 185, 0, 150, 20).tooltip(getTooltip(info)).build();
                        }
                        else if (e.isSlider())
                            widget = new MidnightSliderWidget(width - 185, 0, 150, 20, Component.literal(info.tempValue), (Double.parseDouble(info.tempValue) - e.min()) / (e.max() - e.min()), info);
                        else widget = new EditBox(this.font, width - 185, 0, 150, 20, Component.empty());

                        if (widget instanceof EditBox textField) {
                            textField.setMaxLength(info.width);
                            textField.setValue(info.tempValue);
                            @SuppressWarnings("unchecked")
                            Predicate<String> processor = ((BiFunction<EditBox, Button, Predicate<String>>) info.function).apply(textField, done);
                            textField.setFilter(processor);
                        }
                        widget.setTooltip(getTooltip(info));
                        boolean shouldBeActive = judgeField(info.field);
                        widget.active = shouldBeActive;

                        Button cycleButton = null;
                        if (info.field.getType() == List.class) {
                            cycleButton = Button.builder(Component.literal(String.valueOf(info.listIndex)).withStyle(ChatFormatting.GOLD), (button -> {
                                var values = (List<?>) info.value;
                                values.remove("");
                                info.listIndex = info.listIndex + 1;
                                if (info.listIndex > values.size()) info.listIndex = 0;
                                info.tempValue = info.toTemporaryValue();
                                if (info.listIndex == values.size()) info.tempValue = "";
                                list.clear(); fillList();
                            })).bounds(width - 185, 0, 20, 20).build();
                            cycleButton.active = shouldBeActive;
                        }
                        if (e.isColor()) {
                            Button colorButton = Button.builder(Component.literal("⬛"),
                                    button -> new Thread(() -> {
                                        Color newColor = JColorChooser.showDialog(null, Component.translatable("DeimosConfig.colorChooser.title").getString(), Color.decode(!Objects.equals(info.tempValue, "") ? info.tempValue : "#FFFFFF"));
                                        if (newColor != null) {
                                            info.setValue("#" + Integer.toHexString(newColor.getRGB()).substring(2));
                                            list.clear(); fillList();
                                        }
                                    }).start()
                            ).bounds(width - 185, 0, 20, 20).build();
                            try { colorButton.setMessage(Component.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                            } catch (Exception ignored) {}
                            info.actionButton = colorButton;
                            colorButton.active = shouldBeActive;
                        } else if (e.selectionMode() > -1) {
                            Button explorerButton = SpriteIconButton.builder(Component.literal(""),
                                    button -> new Thread(() -> {
                                        JFileChooser fileChooser = new JFileChooser();
                                        fileChooser.setFileSelectionMode(e.selectionMode()); fileChooser.setDialogType(e.fileChooserType());
                                        fileChooser.setDialogTitle(Component.translatable(translationPrefix + info.field.getName() + ".fileChooser").getString());
                                        if ((e.selectionMode() == JFileChooser.FILES_ONLY || e.selectionMode() == JFileChooser.FILES_AND_DIRECTORIES) && Arrays.stream(e.fileExtensions()).noneMatch("*"::equals))
                                            fileChooser.setFileFilter(new FileNameExtensionFilter(
                                                    Component.translatable(translationPrefix + info.field.getName() + ".fileFilter").getString(), e.fileExtensions()));
                                        if (fileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                                            info.setValue(fileChooser.getSelectedFile().getAbsolutePath());
                                            list.clear(); fillList();
                                        }
                                    }).start(), true
                            ).sprite(ResourceLocation.fromNamespaceAndPath("deimos", "icon/explorer"), 12, 12).size(20, 20).build();
                            explorerButton.setPosition(width - 185, 0);
                            info.actionButton = explorerButton;
                            explorerButton.active = shouldBeActive;
                        }
                        List<AbstractWidget> widgets = Lists.newArrayList(widget, resetButton);
                        if (info.actionButton != null) {
                            widget.setWidth(widget.getWidth() - 22); widget.setX(widget.getX() + 22);
                            widgets.add(info.actionButton);
                        } if (cycleButton != null) {
                            if (info.actionButton != null) info.actionButton.setX(info.actionButton.getX() + 22);
                            widget.setWidth(widget.getWidth() - 22); widget.setX(widget.getX() + 22);
                            widgets.add(cycleButton);
                        }
                        this.list.addButton(widgets, name, info);
                    } else this.list.addButton(List.of(), name, info);
                } list.setScrollAmount(scrollProgress);
                updateButtons();
            }
        }
        @Override
        public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
            super.render(context,mouseX,mouseY,delta);
            this.list.render(context, mouseX, mouseY, delta);
            // context.drawTexture(RenderLayer::getGuiTextured, ResourceLocation.fromNamespaceAndPath("deimos", "textures/gui/sprites/icon/warning.png"), mouseX,mouseY, 0,0,20,20, 20, 20);
            if (tabs.size() < 2) context.drawCenteredString(this.font, title, width / 2, 10, 0xFFFFFF);

            if (this.list != null) {
                for (ButtonEntry entry : this.list.children()) {
                    if (entry.buttons != null && entry.buttons.size() > 1) {
                        if (entry.buttons.getFirst() instanceof AbstractWidget widget) {
                            int idMode = entry.info.field.getAnnotation(Entry.class).idMode();

                            if (idMode != -1) context.renderItem(idMode == 0 ? BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(entry.info.tempValue)).getDefaultInstance() : BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(entry.info.tempValue)).asItem().getDefaultInstance(), widget.getX() + widget.getWidth() - 18, widget.getY() + 2);
                        }
                    }
                }
            }
        }
    }

    // @OnlyIn(Dist.CLIENT)
    public static class DeimosConfigListWidget extends ContainerObjectSelectionList<ButtonEntry> {
        boolean renderHeaderSeparator = true;
        public DeimosConfigListWidget(Minecraft client, int width, int height, int y, int itemHeight) { super(client, width, height, y, itemHeight); }
        @Override public int getScrollbarPosition() { return this.width -7; }

        @Override
        protected void renderListSeparators(@NotNull GuiGraphics context) {
            if (renderHeaderSeparator) super.renderListSeparators(context);
            else { RenderSystem.enableBlend();
                context.blit((this.minecraft.level == null) ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR, getX(), getBottom(), 0.0F, 0.0F, getWidth(), 2, 32, 2);
                RenderSystem.disableBlend(); }
        }
        public void addButton(List<AbstractWidget> buttons, Component text, EntryInfo info) { this.addEntry(new ButtonEntry(buttons, text, info)); }
        public void clear() { this.clearEntries(); }
        @Override public int getRowWidth() { return 10000; }
    }


    // @OnlyIn(Dist.CLIENT)
    public static Screen getScreen(Screen parent, String modid) {
        return new DeimosConfigScreen(parent, modid);
    }

    public static boolean judgeField(Field field) {
        return !(field.getAnnotation(DeimosConfig.Entry.class).forceServerValue() && (Minecraft.getInstance().player instanceof LocalPlayer));
    }

    private static void updateEntries() {
        for (EntryInfo info : DeimosConfigScreenClass.entries) try {
            if(info.field.getAnnotation(Entry.class).restartClient()) {
                if(!info.field.get(null).equals(info.value)) {
                    ClientClass.changedConfigsWRestart.add(info.field);
                    // Constants.LOG.info("Needs restart. {} {}", info.value, info.field.get(null));
                } else {
                    ClientClass.changedConfigsWRestart.remove(info.field);
                    // Constants.LOG.info("Restart no longer needed. {} {}", info.value, info.field.get(null));
                }
                applyWhenQuitting.put(info.field, info.value);
            } else {
                info.field.set(null, info.value);
            }
        } catch (IllegalAccessException ignored) {}
    }
}
