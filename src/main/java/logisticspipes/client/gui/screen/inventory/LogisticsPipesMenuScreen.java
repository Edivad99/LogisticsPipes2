package logisticspipes.client.gui.screen.inventory;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.systems.RenderSystem;
import logisticspipes.world.inventory.LogisticsPipesMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public abstract class LogisticsPipesMenuScreen<T extends LogisticsPipesMenu>
    extends AbstractContainerScreen<T> {

  protected final Inventory inventory;

  protected LogisticsPipesMenuScreen(T menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    this.inventory = inventory;
  }

  protected LogisticsPipesMenuScreen(T menu, Inventory inventory, Component title, int imageHeight) {
    super(menu, inventory, title);
    this.inventory = inventory;
    this.imageHeight = imageHeight;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    super.render(guiGraphics, mouseX, mouseY, partialTicks);
    var left = this.leftPos;
    var top = this.topPos;

    RenderSystem.setShaderColor(1, 1, 1, 1);

    /*if (this.menu.getCarried().isEmpty()) {
      for (var renderer : this.widgetRenderers) {
        if (!renderer.widget.hidden) {
          var tooltip = renderer.getTooltip();
          if (tooltip != null && renderer.isMouseOver(mouseX - left, mouseY - top)) {
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
          }
        }
      }

      for (var slot : this.menu.slots) {
        if (slot instanceof RailcraftSlot railcraftSlot && slot.getItem().isEmpty()) {
          var tooltip = railcraftSlot.getTooltip();
          if (tooltip != null && this.isMouseOverSlot(slot, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
          }
        }
      }
    }*/

    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }

  public abstract ResourceLocation getWidgetsTexture();

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
    int x = (this.width - this.getXSize()) / 2;
    int y = (this.height - this.getYSize()) / 2;

    guiGraphics.blit(getWidgetsTexture(), x, y, 0, 0, this.getXSize(), this.getYSize());
  }

  /**
   * Returns if the passed mouse position is over the specified slot.
   */
  private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
    mouseX -= this.leftPos;
    mouseY -= this.topPos;
    return mouseX >= slot.x - 1 && mouseX < slot.x + 16 + 1 && mouseY >= slot.y - 1
        && mouseY < slot.y + 16 + 1;
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX,
      double deltaY) {
    /*Slot slot = this.getSlotUnderMouse();
    if (button == GLFW.GLFW_MOUSE_BUTTON_1 && slot instanceof RailcraftSlot railcraftSlot
        && railcraftSlot.isPhantom())
      return true;*/
    return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
  }
}
