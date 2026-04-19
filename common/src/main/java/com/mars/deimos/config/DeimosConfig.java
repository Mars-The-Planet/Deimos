package com.mars.deimos.config;

import com.google.common.collect.Lists;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mars.deimos.CommonClass;
import com.mars.deimos.platform.Services;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Based on MidnightConfig by TeamDeimosDust and Motschen <a href="https://github.com/TeamMidnightDust/MidnightLib">...</a>
 */
public abstract class DeimosConfig {
    private static final Pattern INTEGER_ONLY = Pattern.compile("(-?[0-9]*)");
    private static final Pattern DECIMAL_ONLY = Pattern.compile("-?(\\d+\\.?\\d*|\\d*\\.?\\d+|\\.)");
    private static final Pattern HEXADECIMAL_ONLY = Pattern.compile("(-?[#0-9a-fA-F]*)");
    private static final List<EntryInfo> entries = new ArrayList<>();
    public static class EntryInfo {
        Field field;
        Class<?> dataType;
        int width;
        int listIndex;
        boolean centered;
        Object defaultValue;
        Object value;
        Object function;
        String modid;
        String tempValue;
        boolean inLimits = true;
        Component name;
        Component error;
        AbstractWidget actionButton;
        Tab tab;

        public void setValue(Object value) {
            if (this.field.getType() != List.class) {
                this.value = value;
                this.tempValue = value.toString();
            } else {
                writeList(this.listIndex, value);
                this.tempValue = toTemporaryValue();
            }
        }

        public String toTemporaryValue() {
            if (this.field.getType() != List.class)
                return this.value.toString();
            try {
                return ((List<?>)this.value).get(this.listIndex).toString();
            } catch (Exception ignored) {
                return "";
            }
        }

        public <T> void writeList(int index, T value) {
            List<T> list = (List<T>)this.value;
            if (index >= list.size()) {
                list.add(value);
            } else {
                list.set(index, value);
            }
        }
    }

    public static final Map<String, Class<? extends DeimosConfig>> configClass = new HashMap<>();

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.TRANSIENT)
            .addSerializationExclusionStrategy(new HiddenAnnotationExclusionStrategy())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .setPrettyPrinting()
            .create();

    @Nullable
    public static Object getDefaultValue(String modid, String entry) {
        for (EntryInfo e : entries) {
            if (modid.equals(e.modid) && entry.equals(e.field.getName()))
                return e.defaultValue;
        }
        return null;
    }

    public static void init(String modid, Class<? extends DeimosConfig> config) {
        Path configPath = CommonClass.PLATFORM.getConfigDirectory().resolve(modid + ".json");
        configClass.put(modid, config);

        synchronized (entries) {
            // 1) Scan & register every @Entry / @Comment field for this mod
            for (Field field : config.getFields()) {
                EntryInfo info = new EntryInfo();
                if ((field.isAnnotationPresent(Entry.class) || field.isAnnotationPresent(Comment.class))
                        && !field.isAnnotationPresent(Server.class)
                        && !field.isAnnotationPresent(Hidden.class)
                        && CommonClass.PLATFORM.isClientEnv()) {
                    initClient(modid, field, info);
                }
                if (field.isAnnotationPresent(Comment.class)) {
                    info.centered = field.getAnnotation(Comment.class).centered();
                }
                if (field.isAnnotationPresent(Entry.class)) {
                    try {
                        info.defaultValue = field.get(null);
                    } catch (IllegalAccessException ignored) {}
                }
            }

            // 2) Ensure config directory & file exist
            try {
                Files.createDirectories(configPath.getParent());
                if (Files.notExists(configPath)) {
                    Files.createFile(configPath);
                    // write defaults immediately if file was missing
                    write(modid);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not create config file for " + modid, e);
            }

            // 3) Read JSON (or rewrite if corrupted)
            try (Reader reader = Files.newBufferedReader(configPath)) {
                gson.fromJson(reader, config);
            } catch (Exception e) {
                write(modid);
            }

            // 4) Populate each EntryInfo.value/tempValue from its field
            for (EntryInfo info : entries) {
                if (info.modid.equals(modid) && info.field.isAnnotationPresent(Entry.class)) {
                    try {
                        info.value = info.field.get(null);
                        info.tempValue = info.toTemporaryValue();
                    } catch (IllegalAccessException ignored) {}
                }
            }
        }
    }

    private static void initClient(String modid, Field field, EntryInfo info) {
        info.dataType = getUnderlyingType(field);
        Entry e = field.<Entry>getAnnotation(Entry.class);
        info.width = (e != null) ? e.width() : 0;
        info.field = field;
        info.modid = modid;
        if (e != null) {
            if (!e.name().isEmpty())
                info.name = (Component)Component.translatable(e.name());
            if (info.dataType == int.class) {
                textField(info, Integer::parseInt, INTEGER_ONLY, (int)e.min(), (int)e.max(), true);
            } else if (info.dataType == float.class) {
                textField(info, Float::parseFloat, DECIMAL_ONLY, (float)e.min(), (float)e.max(), false);
            } else if (info.dataType == double.class) {
                textField(info, Double::parseDouble, DECIMAL_ONLY, e.min(), e.max(), false);
            } else if (info.dataType == String.class || info.dataType == ResourceLocation.class) {
                textField(info, String::length, null, Math.min(e.min(), 0.0D), Math.max(e.max(), 1.0D), true);
            } else if (info.dataType == boolean.class) {
                Function<Object, Component> func = value -> Component.translatable(((Boolean)value).booleanValue() ? "gui.yes" : "gui.no").withStyle(((Boolean)value).booleanValue() ? ChatFormatting.GREEN : ChatFormatting.RED);
                info.function = new AbstractMap.SimpleEntry<Button.OnPress, Function<Object, Component>>(button -> {
                    info.setValue(Boolean.valueOf(!((Boolean)info.value).booleanValue()));
                    button.setMessage(func.apply(info.value));
                }, func);
            } else if (info.dataType.isEnum()) {
                List<?> values = Arrays.asList(field.getType().getEnumConstants());
                Function<Object, Component> func = value -> Component.translatable(modid + ".deimosconfig.enum." + modid + "." + info.dataType.getSimpleName());
                info.function = new AbstractMap.SimpleEntry<Button.OnPress, Function<Object, Component>>(button -> {
                    int index = values.indexOf(info.value) + 1;
                    info.value = values.get((index >= values.size()) ? 0 : index);
                    button.setMessage(func.apply(info.value));
                }, func);
            }
        }
        entries.add(info);
    }

    public static Class<?> getUnderlyingType(Field field) {
        if (field.getType() == List.class) {
            Class<?> listType = (Class)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
            try {
                return (Class)listType.getField("TYPE").get((Object)null);
            } catch (NoSuchFieldException|IllegalAccessException ignored) {
                return listType;
            }
        }
        return field.getType();
    }

    public static Tooltip getTooltip(EntryInfo info) {
        String key = info.modid + ".deimosconfig." + info.modid + ".tooltip";
        return Tooltip.create((info.error != null) ? info.error : (I18n.exists(key) ? (Component)Component.translatable(key) : (Component)Component.empty()));
    }

    private static void textField(EntryInfo info, Function<String, Number> f, Pattern pattern, double min, double max, boolean cast) {
        boolean isNumber = (pattern != null);
        info.function = (BiFunction<EditBox, Button, Predicate<String>>) (t, b) -> s -> {
            s = s.trim();
            if (!(s.isEmpty() || !isNumber || pattern.matcher(s).matches())) return false;

            Number value = 0; boolean inLimits = false; info.error = null;
            if (!(isNumber && s.isEmpty()) && !s.equals("-") && !s.equals(".")) {
                try { value = f.apply(s); } catch(NumberFormatException e){ return false; }
                inLimits = value.doubleValue() >= min && value.doubleValue() <= max;
                info.error = inLimits? null : Component.literal(value.doubleValue() < min ?
                        "§cMinimum " + (isNumber? "value" : "length") + (cast? " is " + (int)min : " is " + min) :
                        "§cMaximum " + (isNumber? "value" : "length") + (cast? " is " + (int)max : " is " + max)).withStyle(ChatFormatting.RED);
                t.setTooltip(getTooltip(info));
            }

            info.tempValue = s;
            t.setTextColor(inLimits? 0xFFFFFFFF : 0xFFFF7777);
            info.inLimits = inLimits;
            b.active = entries.stream().allMatch(e -> e.inLimits);

            if (inLimits) {
                if (info.dataType == ResourceLocation.class) info.setValue(ResourceLocation.tryParse(s));
                else info.setValue(isNumber ? value : s);
            }

            if (info.field.getAnnotation(Entry.class).isColor()) {
                if (!s.contains("#")) s = '#' + s;
                if (!HEXADECIMAL_ONLY.matcher(s).matches()) return false;
                try { info.actionButton.setMessage(Component.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                } catch (Exception ignored) {}
            }
            return true;
        };
    }

    public static DeimosConfig getClass(String modid) {
        try {
            return ((Class<DeimosConfig>)configClass.get(modid)).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(String modid) {
        Path configPath = Services.PLATFORM.getConfigDirectory().resolve(modid + ".json");
        try {
            Files.createDirectories(configPath.getParent());
            if (Files.notExists(configPath)) {
                Files.createFile(configPath);
            }
            String json = gson.toJson(getClass(modid));
            Files.write(configPath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config for " + modid, e);
        }
    }

    public static class DeimosConfigScreen extends Screen {
        public final String translationPrefix;
        public final String modid;
        public final Screen parent;
        public DeimosConfigListWidget list;
        public TabManager tabManager = new TabManager(a -> {}, a -> {});
        public Map<String, Tab> tabs;
        public Tab prevTab;
        public TabNavigationBar tabNavigation;
        public Button done;
        public double scrollProgress;

        public static Screen getScreen(Screen parent, String modid) {
            return new DeimosConfigScreen(parent, modid);
        }

        protected DeimosConfigScreen(Screen parent, String modid) {
            super((Component)Component.translatable(modid + ".deimosconfig.title"));
            this.tabs = new HashMap<>();
            this.scrollProgress = 0.0D;
            this.parent = parent;
            this.modid = modid;
            this.translationPrefix = modid + ".deimosconfig.";
            loadValues();
            for (EntryInfo e : DeimosConfig.entries) {
                if (e.modid.equals(modid)) {
                    String tabId = e.field.isAnnotationPresent((Class) Entry.class) ? ((Entry)e.field.<Entry>getAnnotation(Entry.class)).category() : ((Comment)e.field.<Comment>getAnnotation(Comment.class)).category();
                    String name = this.translationPrefix + "category." + this.translationPrefix;
                    if (!I18n.exists(name) && tabId.equals("default"))
                        name = this.translationPrefix + "title";
                    if (!this.tabs.containsKey(name)) {
                        GridLayoutTab gridLayoutTab = new GridLayoutTab((Component)Component.translatable(name));
                        e.tab = (Tab)gridLayoutTab;
                        this.tabs.put(name, gridLayoutTab);
                        continue;
                    }
                    e.tab = this.tabs.get(name);
                }
            }
            this.tabNavigation = TabNavigationBar.builder(this.tabManager, this.width).addTabs((Tab[])this.tabs.values().toArray((Object[])new Tab[0])).build();
            this.tabNavigation.selectTab(0, false);
            this.tabNavigation.arrangeElements();
            this.prevTab = this.tabManager.getCurrentTab();
        }

        public void tick() {
            super.tick();
            if (this.prevTab != null && this.prevTab != this.tabManager.getCurrentTab()) {
                this.prevTab = this.tabManager.getCurrentTab();
                this.list.clear();
                fillList();
                this.list.setScrollAmount(0.0D);
            }
            this.scrollProgress = this.list.getScrollAmount();
            for (EntryInfo info : DeimosConfig.entries) {
                if (!info.modid.equals(this.modid)) continue;
                if (info.value == null) continue;
                try {
                    info.field.set((Object)null, info.value);
                } catch (IllegalAccessException illegalAccessException) {}
            }
            updateButtons();
        }

        public void updateButtons() {
            if (this.list != null)
                for (ButtonEntry entry : this.list.children()) {
                    if (entry.buttons != null && entry.buttons.size() > 1) {
                        AbstractWidget abstractWidget = (AbstractWidget)entry.buttons.get(0);
                        if (abstractWidget instanceof AbstractWidget) {
                            AbstractWidget widget = abstractWidget;
                            if (widget.isFocused() || widget.isHovered())
                                widget.setTooltip(DeimosConfig.getTooltip(entry.info));
                        }
                        abstractWidget = entry.buttons.get(1);
                        if (abstractWidget instanceof Button) {
                            Button button = (Button)abstractWidget;
                            button.active = !Objects.equals(entry.info.value.toString(), entry.info.defaultValue.toString());
                        }
                    }
                }
        }

        public void loadValues() {
            // compute the path for this.modid.json
            Path configPath = CommonClass.PLATFORM
                    .getConfigDirectory()
                    .resolve(this.modid + ".json");

            // attempt to re-read it; if it fails, rewrite defaults
            try (Reader reader = Files.newBufferedReader(configPath)) {
                DeimosConfig.gson.fromJson(reader,
                        DeimosConfig.configClass.get(this.modid));
            } catch (Exception e) {
                DeimosConfig.write(this.modid);
            }

            // now sync up our EntryInfo objects from the newly loaded fields
            synchronized (DeimosConfig.entries) {
                for (EntryInfo info : DeimosConfig.entries) {
                    if (info.modid.equals(this.modid)
                            && info.field.isAnnotationPresent(Entry.class)) {
                        try {
                            info.value     = info.field.get(null);
                            info.tempValue = info.toTemporaryValue();
                        } catch (IllegalAccessException ignored) {}
                    }
                }
            }
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.tabNavigation.keyPressed(keyCode))
                return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        public void onClose() {
            loadValues();
            cleanup();
            ((Minecraft)Objects.<Minecraft>requireNonNull(this.minecraft)).setScreen(this.parent);
        }

        private void cleanup() {
            DeimosConfig.entries.forEach(info -> {
                info.error = null;
                info.value = null;
                info.tempValue = null;
                info.actionButton = null;
                info.listIndex = 0;
                info.tab = null;
                info.inLimits = true;
            });
        }

        public void init() {
            super.init();
            this.tabNavigation.setWidth(this.width);
            this.tabNavigation.arrangeElements();
            if (this.tabs.size() > 1)
                addRenderableWidget(this.tabNavigation);
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).bounds(this.width / 2 - 154, this.height - 26, 150, 20).build());
            this.done = (Button)addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
                for (EntryInfo info : DeimosConfig.entries) {
                    if (info.modid.equals(this.modid))
                        try {
                            info.field.set(null, info.value);
                        } catch (IllegalAccessException illegalAccessException) {}
                }
                DeimosConfig.write(this.modid);
                cleanup();
                ((Minecraft)Objects.<Minecraft>requireNonNull(this.minecraft)).setScreen(this.parent);
            }).bounds(this.width / 2 + 4, this.height - 26, 150, 20).build());
            this.list = new DeimosConfigListWidget(this.minecraft, this.width, this.height - 57, 24, 25);
            addWidget(this.list);
            fillList();
            if (this.tabs.size() > 1)
                this.list.renderHeaderSeparator = false;
        }

        public void fillList() {
            for (Iterator<EntryInfo> iterator = DeimosConfig.entries.iterator(); iterator.hasNext(); ) {
                EntryInfo info = iterator.next();
                if (info.modid.equals(this.modid) && (info.tab == null || info.tab == this.tabManager.getCurrentTab())) {
                    Component name = Objects.<Component>requireNonNullElseGet(info.name, () -> Component.translatable(this.translationPrefix + info.field.getName()));
                    Button resetButton = Button.builder((Component)Component.literal("R").withStyle(ChatFormatting.RED), button -> {
                        info.value = info.defaultValue;
                        info.listIndex = 0;
                        info.tempValue = info.toTemporaryValue();
                        this.list.clear();
                        fillList();
                    }).size(20, 20).build();
                    resetButton.setPosition(this.width - 205 + 150 + 25, 0);
                    if (info.function != null) {
                        AbstractWidget editBox;
                        Entry e = info.field.<Entry>getAnnotation(Entry.class);
                        if (info.function instanceof Map.Entry) {
                            Map.Entry<Button.OnPress, Function<Object, Component>> values = (Map.Entry<Button.OnPress, Function<Object, Component>>)info.function;
                            if (info.dataType.isEnum())
                                values.setValue(value -> Component.translatable(this.translationPrefix + "enum." + this.translationPrefix + "." + info.field.getType().getSimpleName()));
                            editBox = Button.builder(((Function<Object, Component>)values.getValue()).apply(info.value), values.getKey()).bounds(this.width - 185, 0, 150, 20).tooltip(DeimosConfig.getTooltip(info)).build();
                        } else if (e.isSlider()) {
                            editBox = new DeimosSliderWidget(this.width - 185, 0, 150, 20, Component.nullToEmpty(info.tempValue), (Double.parseDouble(info.tempValue) - e.min()) / (e.max() - e.min()), info);
                        } else {
                            editBox = new EditBox(this.font, this.width - 185, 0, 150, 20, (Component)Component.empty());
                        }
                        if (editBox instanceof EditBox textField) {
                            textField.setMaxLength(info.width);
                            textField.setValue(info.tempValue);
                            Predicate<String> processor = ((BiFunction<EditBox, Button, Predicate<String>>)info.function).apply(textField, this.done);
                            textField.setFilter(processor);
                        }
                        editBox.setTooltip(DeimosConfig.getTooltip(info));
                        Button cycleButton = null;
                        if (info.field.getType() == List.class)
                            cycleButton = Button.builder((Component)Component.literal(String.valueOf(info.listIndex)).withStyle(ChatFormatting.GOLD), button -> {
                                List<?> values = (List)info.value;
                                values.remove("");
                                info.listIndex++;
                                if (info.listIndex > values.size())
                                    info.listIndex = 0;
                                info.tempValue = info.toTemporaryValue();
                                if (info.listIndex == values.size())
                                    info.tempValue = "";
                                this.list.clear();
                                fillList();
                            }).bounds(this.width - 185, 0, 20, 20).build();
                        if (e.isColor()) {
                            Button colorButton = Button.builder(Component.literal("⬛"), (button -> {})).bounds(this.width - 185, 0, 20, 20).build();
                            try {
                                colorButton.setMessage((Component)Component.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                            } catch (Exception exception) {}
                            info.actionButton = (AbstractWidget)colorButton;
                        } else if (e.selectionMode() > -1) {
                            SpriteIconButton spriteIconButton = SpriteIconButton.builder((Component)Component.empty(), button -> {}, true).sprite(ResourceLocation.fromNamespaceAndPath("deimoslib", "icon/explorer"), 12, 12).size(20, 20).build();
                            spriteIconButton.setPosition(this.width - 185, 0);
                            info.actionButton = (AbstractWidget)spriteIconButton;
                        }
                        List<AbstractWidget> widgets = Lists.newArrayList(editBox, resetButton);
                        if (info.actionButton != null) {
                            if (Minecraft.ON_OSX)
                                info.actionButton.active = false;
                            editBox.setWidth(editBox.getWidth() - 22);
                            editBox.setX(editBox.getX() + 22);
                            widgets.add(info.actionButton);
                        }
                        if (cycleButton != null) {
                            if (info.actionButton != null)
                                info.actionButton.setX(info.actionButton.getX() + 22);
                            editBox.setWidth(editBox.getWidth() - 22);
                            editBox.setX(editBox.getX() + 22);
                            widgets.add(cycleButton);
                        }
                        this.list.addButton(widgets, name, info);
                    } else {
                        this.list.addButton(List.of(), name, info);
                    }
                }
                this.list.setScrollAmount(this.scrollProgress);
                updateButtons();
            }
        }

        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            this.list.render(context, mouseX, mouseY, delta);
            if (this.tabs.size() < 2)
                context.drawCenteredString(this.font, this.title, this.width / 2, 10, 16777215);
            if (this.list != null)
                for (ButtonEntry entry : this.list.children()) {
                    if (entry.buttons != null && entry.buttons.size() > 1) {
                        Object object = entry.buttons.getFirst();
                        if (object instanceof AbstractWidget) {
                            AbstractWidget widget = (AbstractWidget)object;
                            int idMode = ((Entry)entry.info.field.<Entry>getAnnotation(Entry.class)).idMode();
                            if (idMode != -1)
                                context.renderItem((idMode == 0) ? ((Item)BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(entry.info.tempValue))).getDefaultInstance() : ((Block)BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(entry.info.tempValue))).asItem().getDefaultInstance(), widget.getX() + widget.getWidth() - 18, widget.getY() + 2);
                        }
                    }
                }
        }
    }

    public static class DeimosConfigListWidget extends ContainerObjectSelectionList<ButtonEntry> {
        public boolean renderHeaderSeparator = true;

        public DeimosConfigListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
        }

        public int getScrollbarPosition() {
            return this.width - 7;
        }

        protected void renderListSeparators(GuiGraphics context) {
            if (this.renderHeaderSeparator) {
                super.renderListSeparators(context);
            } else {
                RenderSystem.enableBlend();
                context.blit((this.minecraft.level == null) ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR, getX(), getBottom(), 0.0F, 0.0F, getWidth(), 2, 32, 2);
                RenderSystem.disableBlend();
            }
        }

        public void addButton(List<AbstractWidget> buttons, Component text, EntryInfo info) {
            addEntry(new ButtonEntry(buttons, text, info));
        }

        public void clear() {
            clearEntries();
        }

        public int getRowWidth() {
            return 10000;
        }
    }

    public static class ButtonEntry extends ContainerObjectSelectionList.Entry<ButtonEntry> {
        private static final Font textRenderer = (Minecraft.getInstance()).font;

        public final Component text;

        public final List<AbstractWidget> buttons;

        public final EntryInfo info;

        public boolean centered = false;

        public ButtonEntry(List<AbstractWidget> buttons, Component text, EntryInfo info) {
            this.buttons = buttons;
            this.text = text;
            this.info = info;
            if (info != null)
                this.centered = info.centered;
        }

        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.buttons.forEach(b -> {
                b.setY(y);
                b.render(context, mouseX, mouseY, tickDelta);
            });
            if (this.text != null && (!this.text.getString().contains("spacer") || !this.buttons.isEmpty())) {
                int wrappedY = y;
                for (Iterator<FormattedCharSequence> textIterator = textRenderer.split((FormattedText)this.text, (this.buttons.size() > 1) ? (((AbstractWidget)this.buttons.get(1)).getX() - 24) : (Minecraft.getInstance().getWindow().getGuiScaledWidth() - 24)).iterator(); textIterator.hasNext(); wrappedY += 9)
                    context.drawString(textRenderer, textIterator.next(), this.centered ? (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - textRenderer.width((FormattedText)this.text) / 2) : 12, wrappedY + 5, 16777215);
            }
        }

        public List<? extends GuiEventListener> children() {
            return Lists.newArrayList(this.buttons);
        }

        public List<? extends NarratableEntry> narratables() {
            return Lists.newArrayList(this.buttons);
        }
    }

    public static class DeimosSliderWidget extends AbstractSliderButton {
        private final EntryInfo info;

        private final Entry e;

        public DeimosSliderWidget(int x, int y, int width, int height, Component text, double value, EntryInfo info) {
            super(x, y, width, height, text, value);
            this.e = info.field.<Entry>getAnnotation(Entry.class);
            this.info = info;
        }

        public void updateMessage() {
            setMessage(Component.nullToEmpty(this.info.tempValue));
        }

        public void applyValue() {
            if (this.info.dataType == int.class) {
                this.info.setValue(Integer.valueOf(Double.valueOf(this.e.min() + this.value * (this.e.max() - this.e.min())).intValue()));
            } else if (this.info.field.getType() == double.class) {
                this.info.setValue(Double.valueOf(Math.round((this.e.min() + this.value * (this.e.max() - this.e.min())) * this.e.precision()) / this.e.precision()));
            } else if (this.info.field.getType() == float.class) {
                this.info.setValue(Float.valueOf((float)Math.round((this.e.min() + this.value * (this.e.max() - this.e.min())) * this.e.precision()) / this.e.precision()));
            }
        }
    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return (fieldAttributes.getAnnotation(Entry.class) == null);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public static @interface Entry {
        int width() default 400;

        double min() default 2.2250738585072014E-308D;

        double max() default 1.7976931348623157E308D;

        String name() default "";

        int selectionMode() default -1;

        int fileChooserType() default 0;

        String[] fileExtensions() default {"*"};

        int idMode() default -1;

        boolean isColor() default false;

        boolean isSlider() default false;

        int precision() default 100;

        String category() default "default";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public static @interface Comment {
        boolean centered() default false;

        String category() default "default";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public static @interface Server {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public static @interface Hidden {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public static @interface Client {}
}
