package com.mooclient.mixin;

import net.minecraft.client.util.Icons;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Mixin to explicitly apply the Moo Client cow logo icon to the GLFW window and taskbar.
 */
@Mixin(Window.class)
public class WindowMixin {

    @Shadow @Final private long handle;

    @Inject(method = "setIcon", at = @At("HEAD"), cancellable = true)
    private void mooClient$setCustomCowIcon(ResourcePack resourcePack, Icons icons, CallbackInfo ci) {
        ci.cancel();
        try {
            String[] iconPaths = new String[] {
                "/assets/minecraft/icons/icon_16x16.png",
                "/assets/minecraft/icons/icon_32x32.png",
                "/assets/minecraft/icons/icon_48x48.png",
                "/assets/minecraft/icons/icon_128x128.png",
                "/assets/minecraft/icons/icon_256x256.png"
            };

            List<ByteBuffer> pixelBuffers = new ArrayList<>();
            List<GLFWImage> images = new ArrayList<>();

            for (String iconPath : iconPaths) {
                try (InputStream is = getClass().getResourceAsStream(iconPath)) {
                    if (is == null) continue;
                    byte[] bytes = is.readAllBytes();
                    ByteBuffer rawBuffer = MemoryUtil.memAlloc(bytes.length);
                    rawBuffer.put(bytes).flip();

                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer w = stack.mallocInt(1);
                        IntBuffer h = stack.mallocInt(1);
                        IntBuffer channels = stack.mallocInt(1);

                        ByteBuffer pixels = STBImage.stbi_load_from_memory(rawBuffer, w, h, channels, 4);
                        MemoryUtil.memFree(rawBuffer);

                        if (pixels != null) {
                            pixelBuffers.add(pixels);
                            GLFWImage img = GLFWImage.malloc();
                            img.set(w.get(0), h.get(0), pixels);
                            images.add(img);
                        }
                    }
                }
            }

            if (!images.isEmpty()) {
                GLFWImage.Buffer buffer = GLFWImage.malloc(images.size());
                for (int i = 0; i < images.size(); i++) {
                    buffer.put(i, images.get(i));
                }
                GLFW.glfwSetWindowIcon(this.handle, buffer);
                buffer.free();

                for (GLFWImage img : images) {
                    img.free();
                }
                for (ByteBuffer b : pixelBuffers) {
                    STBImage.stbi_image_free(b);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
