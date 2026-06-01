package site.minechook.chestsearch.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import site.minechook.chestsearch.client.ChestSearchClient;
import net.minecraft.client.input.KeyEvent;

import java.util.Objects;

@Mixin(value = AbstractContainerScreen.class, priority = 101)
public class HandledScreenMixin {

    @Shadow
    protected int imageWidth;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;

    @Unique
    EditBox searchField;

    @Unique
    private boolean isChestScreen() {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        return screen instanceof ContainerScreen || screen instanceof ShulkerBoxScreen;
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        searchField = new EditBox(Minecraft.getInstance().font, 100, 15, Component.literal("Search"));
        searchField.setMaxLength(20);
        searchField.setEditable(true);
        searchField.setVisible(true);
    }

    @Inject(method = "extractContents", at = @At("TAIL"))
    public void onRender(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float deltaTicks, CallbackInfo ci) {

        if (!ChestSearchClient.enabled) return;

        if (!isChestScreen()) return;

        searchField.setX(this.leftPos + this.imageWidth - searchField.getWidth() - 4);
        searchField.setY(this.topPos + searchField.getHeight() - 14);

        searchField.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
    }

    @Inject(method = "extractSlot", at = @At("TAIL"))
    public void onExtractSlot(final GuiGraphicsExtractor graphics, final Slot slot, final int mouseX, final int mouseY, CallbackInfo ci) {
        if (searchField.getValue().isEmpty()) return;
        ItemStack item = slot.getItem();

        if (item.isEmpty()) return;

        if (item.getHoverName().getString().toLowerCase().contains(searchField.getValue().toLowerCase())) {
            graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, ChestSearchClient.color);
            return;
        }

        ItemEnchantments enchantments = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> enchantmentHolder : enchantments.keySet()) {
            String enchantName = Enchantment.getFullname(enchantmentHolder, enchantments.getLevel(enchantmentHolder))
                    .getString()
                    .toLowerCase();

            if (enchantName.contains(searchField.getValue().toLowerCase())) {
                graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, ChestSearchClient.color);
                return;
            }
        }

        ItemEnchantments bookEnchantments = item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> enchantmentHolder : bookEnchantments.keySet()) {
            String enchantName = Enchantment.getFullname(enchantmentHolder, bookEnchantments.getLevel(enchantmentHolder))
                    .getString()
                    .toLowerCase();

            if (enchantName.contains(searchField.getValue().toLowerCase())) {
                graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, ChestSearchClient.color);
                return;
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void onKeyPressed(final KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (searchField.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (searchField.getValue().isEmpty()) return;
                searchField.setValue(searchField.getValue().substring(0, searchField.getValue().length() - 1));
                cir.setReturnValue(true);
            }
            else if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                searchField.setFocused(false);
            }
            else if (event.key() == GLFW.GLFW_KEY_SPACE) {
                searchField.setValue(searchField.getValue() + " ");
            }
            else {
                if (event.key() > 90) return;
                searchField.setValue(searchField.getValue() + Objects.requireNonNull(GLFW.glfwGetKeyName(event.key(), event.scancode())).toLowerCase());
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClicked(final MouseButtonEvent event, final boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.x() > searchField.getX() && event.x() < searchField.getX() + searchField.getWidth() && event.y() > searchField.getY() && event.y() < searchField.getY() + searchField.getHeight()) {
            searchField.setFocused(true);
            cir.setReturnValue(true);
        }
        else searchField.setFocused(false);
    }
}
