package com.mooclient.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mooclient.MooClient;
import com.mooclient.module.ModuleManager;
import com.mooclient.module.modules.FpsModule;
import com.mooclient.module.modules.FullbrightModule;
import com.mooclient.module.modules.ToggleSprintModule;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles persistent config save/load for Moo Client module settings.
 * Config file: .minecraft/config/mooclient.json
 */
public class MooConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mooclient.json");

    /**
     * Save all module settings to disk.
     */
    public static void save() {
        try {
            JsonObject root = new JsonObject();

            // Gamma / Fullbright Module
            JsonObject gamma = new JsonObject();
            gamma.addProperty("enabled", FullbrightModule.isFullbrightActive());
            root.add("gamma", gamma);

            // FPS Module
            JsonObject fps = new JsonObject();
            fps.addProperty("enabled", FpsModule.isFpsEnabled());
            fps.addProperty("style", FpsModule.getStyle().name());
            fps.addProperty("showBackground", FpsModule.isShowBackground());
            fps.addProperty("textShadow", FpsModule.isTextShadow());
            fps.addProperty("showPrefix", FpsModule.isShowPrefix());
            fps.addProperty("posX", FpsModule.posX);
            fps.addProperty("posY", FpsModule.posY);
            root.add("fps", fps);

            // Sprint Module
            JsonObject sprint = new JsonObject();
            sprint.addProperty("enabled", ToggleSprintModule.isSprintEnabled());
            sprint.addProperty("style", ToggleSprintModule.getStyle().name());
            sprint.addProperty("showBackground", ToggleSprintModule.isShowBackground());
            sprint.addProperty("textShadow", ToggleSprintModule.isTextShadow());
            sprint.addProperty("keyCode", ToggleSprintModule.getKeyCode());
            sprint.addProperty("keyName", ToggleSprintModule.getKeyName());
            sprint.addProperty("posX", ToggleSprintModule.posX);
            sprint.addProperty("posY", ToggleSprintModule.posY);
            root.add("sprint", sprint);

            // Freelook Module
            JsonObject freelook = new JsonObject();
            freelook.addProperty("enabled", com.mooclient.module.modules.FreelookModule.isFreelookEnabled());
            freelook.addProperty("mode", com.mooclient.module.modules.FreelookModule.getMode().name());
            freelook.addProperty("invertPitch", com.mooclient.module.modules.FreelookModule.isInvertPitch());
            freelook.addProperty("keyCode", com.mooclient.module.modules.FreelookModule.getKeyCode());
            freelook.addProperty("keyName", com.mooclient.module.modules.FreelookModule.getKeyName());
            root.add("freelook", freelook);

            // Potion Effects Module
            JsonObject potions = new JsonObject();
            potions.addProperty("enabled", com.mooclient.module.modules.PotionEffectsModule.isModuleEnabled());
            potions.addProperty("style", com.mooclient.module.modules.PotionEffectsModule.getStyle().name());
            potions.addProperty("showBackground", com.mooclient.module.modules.PotionEffectsModule.isShowBackground());
            potions.addProperty("textShadow", com.mooclient.module.modules.PotionEffectsModule.isTextShadow());
            potions.addProperty("posX", com.mooclient.module.modules.PotionEffectsModule.posX);
            potions.addProperty("posY", com.mooclient.module.modules.PotionEffectsModule.posY);
            root.add("potions", potions);

            // Nametags Module
            JsonObject nametags = new JsonObject();
            nametags.addProperty("enabled", com.mooclient.module.modules.NametagsModule.isNametagsEnabled());
            nametags.addProperty("showPing", com.mooclient.module.modules.NametagsModule.isShowPing());
            nametags.addProperty("removeBackground", com.mooclient.module.modules.NametagsModule.isRemoveBackground());
            nametags.addProperty("textShadow", com.mooclient.module.modules.NametagsModule.isTextShadow());
            root.add("nametags", nametags);

            // Zoom Module
            JsonObject zoom = new JsonObject();
            zoom.addProperty("enabled", com.mooclient.module.modules.ZoomModule.isZoomEnabled());
            zoom.addProperty("factor", com.mooclient.module.modules.ZoomModule.getFactor().name());
            zoom.addProperty("mode", com.mooclient.module.modules.ZoomModule.getMode().name());
            zoom.addProperty("smoothZoom", com.mooclient.module.modules.ZoomModule.isSmoothZoom());
            zoom.addProperty("keyCode", com.mooclient.module.modules.ZoomModule.getKeyCode());
            zoom.addProperty("keyName", com.mooclient.module.modules.ZoomModule.getKeyName());
            zoom.addProperty("isMouseButton", com.mooclient.module.modules.ZoomModule.isMouseButton());
            root.add("zoom", zoom);

            Files.writeString(CONFIG_PATH, GSON.toJson(root));
            MooClient.LOGGER.info("Saved config to {}", CONFIG_PATH);
        } catch (IOException e) {
            MooClient.LOGGER.error("Failed to save config", e);
        }
    }

    /**
     * Load all module settings from disk.
     */
    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            MooClient.LOGGER.info("No config file found, using defaults.");
            return;
        }

        try {
            String json = Files.readString(CONFIG_PATH);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            // Gamma Module
            if (root.has("gamma")) {
                JsonObject gamma = root.getAsJsonObject("gamma");
                if (gamma.has("enabled")) {
                    boolean state = gamma.get("enabled").getAsBoolean();
                    FullbrightModule.setFullbrightActive(state);
                    ModuleManager.getInstance().getModule("Gamma").ifPresent(m -> m.setEnabled(state));
                }
            }

            // FPS Module
            if (root.has("fps")) {
                JsonObject fps = root.getAsJsonObject("fps");
                if (fps.has("enabled")) {
                    boolean state = fps.get("enabled").getAsBoolean();
                    FpsModule.setFpsEnabled(state);
                    ModuleManager.getInstance().getModule("FPS").ifPresent(m -> m.setEnabled(state));
                }
                if (fps.has("style")) {
                    try {
                        FpsModule.setStyle(FpsModule.FpsStyle.valueOf(fps.get("style").getAsString()));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (fps.has("showBackground")) FpsModule.setShowBackground(fps.get("showBackground").getAsBoolean());
                if (fps.has("textShadow")) FpsModule.setTextShadow(fps.get("textShadow").getAsBoolean());
                if (fps.has("showPrefix")) FpsModule.setShowPrefix(fps.get("showPrefix").getAsBoolean());
                if (fps.has("posX")) FpsModule.posX = fps.get("posX").getAsInt();
                if (fps.has("posY")) FpsModule.posY = fps.get("posY").getAsInt();
            }

            // Sprint Module
            if (root.has("sprint")) {
                JsonObject sprint = root.getAsJsonObject("sprint");
                if (sprint.has("enabled")) {
                    boolean state = sprint.get("enabled").getAsBoolean();
                    ToggleSprintModule.setSprintEnabled(state);
                    ModuleManager.getInstance().getModule("Sprint").ifPresent(m -> m.setEnabled(state));
                }
                if (sprint.has("style")) {
                    try {
                        ToggleSprintModule.setStyle(ToggleSprintModule.SprintStyle.valueOf(sprint.get("style").getAsString()));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (sprint.has("showBackground")) ToggleSprintModule.setShowBackground(sprint.get("showBackground").getAsBoolean());
                if (sprint.has("textShadow")) ToggleSprintModule.setTextShadow(sprint.get("textShadow").getAsBoolean());
                if (sprint.has("keyCode") && sprint.has("keyName")) {
                    ToggleSprintModule.setKeybind(sprint.get("keyCode").getAsInt(), sprint.get("keyName").getAsString());
                }
                if (sprint.has("posX")) ToggleSprintModule.posX = sprint.get("posX").getAsInt();
                if (sprint.has("posY")) ToggleSprintModule.posY = sprint.get("posY").getAsInt();
            }

            // Freelook Module
            if (root.has("freelook")) {
                JsonObject freelook = root.getAsJsonObject("freelook");
                if (freelook.has("enabled")) {
                    boolean state = freelook.get("enabled").getAsBoolean();
                    com.mooclient.module.modules.FreelookModule.setFreelookEnabled(state);
                    ModuleManager.getInstance().getModule("Freelook").ifPresent(m -> m.setEnabled(state));
                }
                if (freelook.has("mode")) {
                    try {
                        com.mooclient.module.modules.FreelookModule.setMode(com.mooclient.module.modules.FreelookModule.ActivationMode.valueOf(freelook.get("mode").getAsString()));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (freelook.has("invertPitch")) {
                    com.mooclient.module.modules.FreelookModule.setInvertPitch(freelook.get("invertPitch").getAsBoolean());
                }
                if (freelook.has("keyCode") && freelook.has("keyName")) {
                    com.mooclient.module.modules.FreelookModule.setKeybind(freelook.get("keyCode").getAsInt(), freelook.get("keyName").getAsString());
                }
            }

            // Potion Effects Module
            if (root.has("potions")) {
                JsonObject potions = root.getAsJsonObject("potions");
                if (potions.has("enabled")) {
                    boolean state = potions.get("enabled").getAsBoolean();
                    com.mooclient.module.modules.PotionEffectsModule.setModuleEnabled(state);
                    ModuleManager.getInstance().getModule("Potion Effects").ifPresent(m -> m.setEnabled(state));
                }
                if (potions.has("style")) {
                    try {
                        com.mooclient.module.modules.PotionEffectsModule.setStyle(com.mooclient.module.modules.PotionEffectsModule.PotionStyle.valueOf(potions.get("style").getAsString()));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (potions.has("showBackground")) {
                    com.mooclient.module.modules.PotionEffectsModule.setShowBackground(potions.get("showBackground").getAsBoolean());
                }
                if (potions.has("textShadow")) {
                    com.mooclient.module.modules.PotionEffectsModule.setTextShadow(potions.get("textShadow").getAsBoolean());
                }
                if (potions.has("posX")) com.mooclient.module.modules.PotionEffectsModule.posX = potions.get("posX").getAsInt();
                if (potions.has("posY")) com.mooclient.module.modules.PotionEffectsModule.posY = potions.get("posY").getAsInt();
            }

            // Nametags Module
            if (root.has("nametags")) {
                JsonObject nametags = root.getAsJsonObject("nametags");
                if (nametags.has("enabled")) {
                    boolean state = nametags.get("enabled").getAsBoolean();
                    com.mooclient.module.modules.NametagsModule.setNametagsEnabled(state);
                    ModuleManager.getInstance().getModule("Nametags").ifPresent(m -> m.setEnabled(state));
                }
                if (nametags.has("showPing")) {
                    com.mooclient.module.modules.NametagsModule.setShowPing(nametags.get("showPing").getAsBoolean());
                }
                if (nametags.has("removeBackground")) {
                    com.mooclient.module.modules.NametagsModule.setRemoveBackground(nametags.get("removeBackground").getAsBoolean());
                }
                if (nametags.has("textShadow")) {
                    com.mooclient.module.modules.NametagsModule.setTextShadow(nametags.get("textShadow").getAsBoolean());
                }
            }

            // Zoom Module
            if (root.has("zoom")) {
                JsonObject zoom = root.getAsJsonObject("zoom");
                if (zoom.has("enabled")) {
                    boolean state = zoom.get("enabled").getAsBoolean();
                    com.mooclient.module.modules.ZoomModule.setZoomEnabled(state);
                    ModuleManager.getInstance().getModule("Zoom").ifPresent(m -> m.setEnabled(state));
                }
                if (zoom.has("factor")) {
                    try {
                        com.mooclient.module.modules.ZoomModule.setFactor(com.mooclient.module.modules.ZoomModule.ZoomFactor.valueOf(zoom.get("factor").getAsString()));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (zoom.has("mode")) {
                    try {
                        com.mooclient.module.modules.ZoomModule.setMode(com.mooclient.module.modules.ZoomModule.ActivationMode.valueOf(zoom.get("mode").getAsString()));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (zoom.has("smoothZoom")) {
                    com.mooclient.module.modules.ZoomModule.setSmoothZoom(zoom.get("smoothZoom").getAsBoolean());
                }
                if (zoom.has("keyCode") && zoom.has("keyName")) {
                    boolean isMouse = zoom.has("isMouseButton") && zoom.get("isMouseButton").getAsBoolean();
                    com.mooclient.module.modules.ZoomModule.setKeybind(zoom.get("keyCode").getAsInt(), zoom.get("keyName").getAsString(), isMouse);
                }
            }

            MooClient.LOGGER.info("Loaded config from {}", CONFIG_PATH);
        } catch (Exception e) {
            MooClient.LOGGER.error("Failed to load config", e);
        }
    }
}
