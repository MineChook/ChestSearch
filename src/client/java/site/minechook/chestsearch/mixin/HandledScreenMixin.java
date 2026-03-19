package site.minechook.chestsearch.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import site.minechook.chestsearch.client.ChestSearchClient;

import java.awt.event.KeyEvent;

@Mixin(value = HandledScreen.class, priority = 101)
public class HandledScreenMixin {

    @Shadow
    protected int backgroundWidth;
    @Shadow
    protected int x;
    @Shadow
    protected int y;

    String lastSearched = "";

    private boolean isChestScreen() {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        return screen instanceof GenericContainerScreen || screen instanceof ShulkerBoxScreen;
    }

    private TextFieldWidget searchField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 100, 15, Text.literal("Search"));

    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        searchField.setMaxLength(20);
        searchField.setDrawsBackground(true);
        searchField.setEditable(true);
        searchField.setVisible(true);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {

        if (!ChestSearchClient.enabled) return;

        if (!isChestScreen()) return;

        assert searchField != null;

        searchField.setX(this.x + this.backgroundWidth - searchField.getWidth() - 4);
        searchField.setY(this.y + searchField.getHeight() - 14);

        searchField.render(context, mouseX, mouseY, deltaTicks);

        if (!lastSearched.equals(searchField.getText())) lastSearched = searchField.getText().toLowerCase();
    }

    @Inject(method = "drawSlot", at = @At("TAIL"), require = 0)
    public void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (searchField == null || searchField.getText().isEmpty()) return;
        ItemStack item = slot.getStack();

        if (item.isEmpty()) return;

        if (item.getName().toString().toLowerCase().contains(lastSearched)) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, ChestSearchClient.color);
        }
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 0)
    public void onKeyPress(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (searchField.isFocused()) {
            if (keyCode == InputUtil.GLFW_KEY_BACKSPACE) {
                if (searchField.getText().isEmpty()) return;
                searchField.setText(searchField.getText().substring(0, searchField.getText().length() - 1));
                cir.setReturnValue(true);
            }
            else if (keyCode == InputUtil.GLFW_KEY_ESCAPE) {
                return;
            }
            else {
                if (keyCode > 90) return;
                searchField.setText(searchField.getText() + KeyEvent.getKeyText(keyCode).toLowerCase());
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
    public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (mouseX > searchField.getX() && mouseX < searchField.getX() + searchField.getWidth() && mouseY > searchField.getY() && mouseY < searchField.getY() + searchField.getHeight()) {
            searchField.setFocused(true);
            cir.setReturnValue(true);
        }
        else searchField.setFocused(false);
    }
}
