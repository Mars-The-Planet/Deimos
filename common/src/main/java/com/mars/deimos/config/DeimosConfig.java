package com.mars.deimos.config;

import com.google.common.collect.Lists;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mars.deimos.ClientClass;
import com.mars.deimos.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;


@SuppressWarnings("unchecked")
public abstract class DeimosConfig {
    private static final Pattern HEXADECIMAL_ONLY = Pattern.compile("(-?[#0-9a-fA-F]*)");

    private static final List<EntryInfo> entries = new ArrayList<>();
    public static List<EntryInfo> getEntries() {
        synchronized (entries) {
            return entries;
        }
    }

    private static final ResourceLocation warningIconResourceLocation = ResourceLocation.fromNamespaceAndPath("deimos", "textures/gui/sprites/icon/warning.png");
    private static final ResourceLocation lockIconResourceLocation = ResourceLocation.fromNamespaceAndPath("deimos", "textures/gui/sprites/icon/lock.png");

    // only gets modified on client

    public static class EntryInfo {
        Field field;
        Class<?> dataType;
        int width, listIndex;
        boolean centered;
        Object defaultValue, value, function;
        String modid, tempValue;   // The value visible in the config screen
        boolean inLimits = true;
        Component name, error;
        @Nullable AbstractWidget actionButton; // color picker button / explorer button
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
            if (this.field.getType() != List.class) return this.value.toString();
            else try { return ((List<?>) this.value).get(this.listIndex).toString(); } catch (Exception ignored) {return "";}
        }
        public <T> void writeList(int index, T value) {
            var list = (List<T>) this.value;
            if (index >= list.size()) list.add(value);
            else list.set(index, value);
        }
    }

    public static final Map<String, Class<? extends DeimosConfig>> configClass = new HashMap<>();

    public static Path getPath(String modid) {
        return Services.PLATFORM.getConfigDirectory().resolve(modid + ".json");
    }

    static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.TRANSIENT)
            .addSerializationExclusionStrategy(new HiddenAnnotationExclusionStrategy())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .setPrettyPrinting().create();

    public static @Nullable Object getDefaultValue(String modid, String entry) {
        synchronized (entries) {
            for (EntryInfo e : entries) {
                if (modid.equals(e.modid) && entry.equals(e.field.getName())) return e.defaultValue;
            }
            return null;
        }
    }

    //TODO: prejmenovat modid na name udelam moznost pro client, server a common
    public static void init(String modid, Class<? extends DeimosConfig> config) {
        Path path = getPath(modid);
        configClass.put(modid, config);
            for (Field field : config.getFields()) {
                EntryInfo info = new EntryInfo();
                if ((field.isAnnotationPresent(Entry.class) || field.isAnnotationPresent(Comment.class)) && !field.isAnnotationPresent(Server.class) && !field.isAnnotationPresent(Hidden.class) && Services.PLATFORM.isClientEnv())
                    DeimosConfigScreenClass.initClient(modid, field, info);

                if (field.isAnnotationPresent(Comment.class))
                    info.centered = field.getAnnotation(Comment.class).centered();
                if (field.isAnnotationPresent(Entry.class))
                    try {
                        info.defaultValue = field.get(null);
                    } catch (IllegalAccessException ignored) {
                    }
            }
            try {
                gson.fromJson(Files.newBufferedReader(path), config);
            } catch (Exception e) {
                write(modid);
            }
        synchronized (entries) {
            for (EntryInfo info : entries) {
                if (info.field.isAnnotationPresent(Entry.class)) try {
                    info.value = info.field.get(null);
                    info.tempValue = info.toTemporaryValue();
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    public static Tooltip getTooltip(EntryInfo info) {
        String key = info.modid + ".deimosconfig."+info.field.getName()+".tooltip";
        return Tooltip.create((info.error != null) ? info.error : (I18n.exists(key) ? Component.translatable(key) : Component.empty()));
    }

    // TODO: Maybe move this into the screen class itself to free up some RAM?
    static void textField(EntryInfo info, Function<String, Number> f, Pattern pattern, double min, double max, boolean cast) {
        boolean isNumber = pattern != null;
        info.function = (BiFunction<EditBox, Button, Predicate<String>>) (t, b) -> s -> {
            s = s.trim();
            if (!(s.isEmpty() || !isNumber || pattern.matcher(s).matches())) return false;

            Number value = 0; boolean inLimits = false; info.error = null;
            if (!(isNumber && s.isEmpty()) && !s.equals("-") && !s.equals(".")) {
                try { value = f.apply(s); } catch(NumberFormatException e){ return false; }
                inLimits = value.doubleValue() >= min && value.doubleValue() <= max ;
                info.error = inLimits ? null : Component.literal(value.doubleValue() < min ?
                        "§cMinimum " + (isNumber? "value" : "length") + (cast? " is " + (int)min : " is " + min) :
                        "§cMaximum " + (isNumber? "value" : "length") + (cast? " is " + (int)max : " is " + max)).withStyle(ChatFormatting.RED);
                t.setTooltip(getTooltip(info));
            }

            info.tempValue = s;
            t.setTextColor(inLimits? 0xFFFFFFFF : 0xFFFF7777);
            info.inLimits = inLimits;
            synchronized (entries) {
                b.active = entries.stream().allMatch(e -> e.inLimits);
            }

            if (inLimits) {
                if (info.dataType == ResourceLocation.class) info.setValue(ResourceLocation.tryParse(s));
                else info.setValue(isNumber ? value : s);
            }

            if (info.field.getAnnotation(Entry.class).isColor()) {
                if (!s.contains("#")) s = '#' + s;
                if (!HEXADECIMAL_ONLY.matcher(s).matches()) return false;
                try {
                    assert info.actionButton != null;
                    info.actionButton.setMessage(Component.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
                } catch (Exception ignored) {}
            }
            return true;
        };
    }
    public static DeimosConfig getClass(String modid) {
        try { return configClass.get(modid).getDeclaredConstructor().newInstance(); } catch (Exception e) {throw new RuntimeException(e);}
    }
    public static void write(String modid) {
        getClass(modid).writeChanges(modid);
    }

    public void writeChanges(String modid) {
        try {
            Path path = getPath(modid);
            if (Files.notExists(path))
                Files.createFile(path);
            Files.write(path, gson.toJson(getClass(modid)).getBytes());
            // Constants.LOG.info("FILE WRITE");
            // Constants.LOG.info("{}", gson.toJson(getClass(modid)));
        } catch (Exception e) {
            // Constants.LOG.info("FAILED TO WRITE");
            // e.printStackTrace();
            // e.fillInStackTrace(); // doessn't do anything???????
            throw new RuntimeException("Failed to write config for " + modid, e);
        }
    }

    public static class ButtonEntry extends ContainerObjectSelectionList.Entry<ButtonEntry> {
        private static final Font textRenderer = Minecraft.getInstance().font;
        private final Component text;
        public final List<AbstractWidget> buttons;
        public final EntryInfo info;
        public boolean centered = false;

        public ButtonEntry(List<AbstractWidget> buttons, Component text, EntryInfo info) {
            this.buttons = buttons;
            this.text = text;
            this.info = info;
            if (info != null) this.centered = info.centered;
        }
        public void render(@NotNull GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            buttons.forEach(b -> {
                b.setY(y);
                b.render(context, mouseX, mouseY, tickDelta);
            });

            if (text != null && (!text.getString().contains("spacer") || !buttons.isEmpty())) {
                int wrappedY = y;
                boolean warningIcon = !ClientClass.changedConfigsWRestart.contains(info.field);
                boolean lockIcon = DeimosConfigScreenClass.judgeField(info.field);
                // Saves a miniscule amount of processing time
                for (Iterator<FormattedCharSequence> textIterator = textRenderer.split(text, (buttons.size() > 1 ? buttons.get(1).getX()-24 : Minecraft.getInstance().getWindow().getGuiScaledWidth() - 24)).iterator(); textIterator.hasNext(); wrappedY += 9) {
                    int positionX = (centered) ? (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - (textRenderer.width(text) / 2)) : 12;
                    context.drawString(textRenderer, textIterator.next(), positionX, wrappedY + 5, 0xFFFFFF);
                    if(!warningIcon) {
                        warningIcon = true;
                        context.blit(warningIconResourceLocation, positionX + textRenderer.width(text) + (!lockIcon ? 18 : 0), y, 16, 16, 0, 0, 16, 16, 16, 16);
                    }
                    if(!lockIcon) {
                        lockIcon = true;
                        context.blit(lockIconResourceLocation, positionX + textRenderer.width(text), y - 3, 20, 20, 0, 0, 20, 20, 20, 20);
                    }
                }
            }
        }
        public @NotNull List<? extends GuiEventListener> children() {return Lists.newArrayList(buttons);}
        public @NotNull List<? extends NarratableEntry> narratables() {return Lists.newArrayList(buttons);}
    }
    public static class MidnightSliderWidget extends AbstractSliderButton {
        private final EntryInfo info; private final Entry e;
        public MidnightSliderWidget(int x, int y, int width, int height, Component text, double value, EntryInfo info) {
            super(x, y, width, height, text, value);
            this.e = info.field.getAnnotation(Entry.class);
            this.info = info;
        }

        @Override
        public void updateMessage() { this.setMessage(Component.literal(info.tempValue)); }

        @Override
        public void applyValue() {
            if (info.dataType == int.class) info.setValue(((Number) (e.min() + value * (e.max() - e.min()))).intValue());
            else if (info.field.getType() == double.class) info.setValue(Math.round((e.min() + value * (e.max() - e.min())) * (double) e.precision()) / (double) e.precision());
            else if (info.field.getType() == float.class) info.setValue(Math.round((e.min() + value * (e.max() - e.min())) * (float) e.precision()) / (float) e.precision());
        }
    }

    /**
     * Entry Annotation<br>
     * - <b>width</b>: The maximum character length of the {@link String}, {@link ResourceLocation} or String/ResourceLocation {@link List<String>} field<br>
     * - <b>min</b>: The minimum value of the <code>int</code>, <code>float</code> or <code>double</code> field<br>
     * - <b>max</b>: The maximum value of the <code>int</code>, <code>float</code> or <code>double</code> field<br>
     * - <b>name</b>: The name of the field in the config screen<br>
     * - <b>selectionMode</b>: The selection mode of the file picker button for {@link String} fields,
     *   -1 for none, {@link JFileChooser#FILES_ONLY} for files, {@link JFileChooser#DIRECTORIES_ONLY} for directories,
     *   {@link JFileChooser#FILES_AND_DIRECTORIES} for both (default: -1). Remember to set the translation key
     *   <code>[modid].deimosconfig.[fieldName].fileChooser.title</code> for the file picker dialog title<br>
     * - <b>fileChooserType</b>: The type of the file picker button for {@link String} fields,
     * can be {@link JFileChooser#OPEN_DIALOG} or {@link JFileChooser#SAVE_DIALOG} (default: {@link JFileChooser#OPEN_DIALOG}).
     * Remember to set the translation key <code>[modid].deimosconfig.[fieldName].fileFilter.description</code> for the file filter description
     * if <code>"*"</code> is not used as file extension<br>
     * - <b>fileExtensions</b>: The file extensions for the file picker button for {@link String} fields (default: <code>{"*"}</code>),
     *  only works if selectionMode is {@link JFileChooser#FILES_ONLY} or {@link JFileChooser#FILES_AND_DIRECTORIES}<br>
     * - <b>isColor</b>: If the field is a hexadecimal color code (default: false)<br>
     * - <b>isSlider</b>: If the field is a slider (default: false)<br>
     * - <b>precision</b>: The precision of the <code>float</code> or <code>double</code> field (default: 100)<br>
     * - <b>category</b>: The category of the field in the config screen (default: "default")<br>
     * */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Entry {
        boolean restartClient() default false;
        boolean forceServerValue() default false;

        int width() default 400;
        double min() default Double.MIN_NORMAL;
        double max() default Double.MAX_VALUE;
        String name() default "";
        int selectionMode() default -1;        // -1 for none, 0 for file, 1 for directory, 2 for both
        int fileChooserType() default JFileChooser.OPEN_DIALOG;
        String[] fileExtensions() default {"*"};
        int idMode() default -1;               // -1 for none, 0 for item, 1 for block
        boolean isColor() default false;
        boolean isSlider() default false;
        int precision() default 100;
        String category() default "default";
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Client {}
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Server {}
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Hidden {}
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Comment {
        boolean centered() default false;
        String category() default "default";
    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> clazz) { return false; }
        public boolean shouldSkipField(FieldAttributes fieldAttributes) { return fieldAttributes.getAnnotation(Entry.class) == null; }
    }
}