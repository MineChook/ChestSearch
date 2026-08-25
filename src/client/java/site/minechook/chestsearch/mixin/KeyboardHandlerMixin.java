package site.minechook.chestsearch.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.minechook.chestsearch.client.SearchFieldScreen;

@Mixin(value = KeyboardHandler.class, priority = 2000)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void suppressKeybindsWhileSearching(final long handle, final int action, final KeyEvent event, final CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.gui.screen();
        if (handle != minecraft.getWindow().handle()
                || !(screen instanceof SearchFieldScreen searchScreen)
                || !searchScreen.chestsearch$isSearchFocused()) return;

        if (action != GLFW.GLFW_RELEASE) {
            screen.afterKeyboardAction();
            searchScreen.chestsearch$handleSearchKey(event);
        }

        KeyMapping.set(InputConstants.getKey(event), false);
        ci.cancel();
    }
}
