package logisticspipes.client.gui.screen.inventory;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.to_server.UpdateModuleItemSinkMessage;
import logisticspipes.world.inventory.ItemSinkModuleMenu;
import logisticspipes.world.item.ItemSinkModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ItemSinkModuleScreen extends LogisticsPipesMenuScreen<ItemSinkModuleMenu> {

  private static final ResourceLocation BACKGROUND_TEXTURE =
      LogisticsPipes.rl("textures/gui/container/itemsink.png");
  private final ItemStack itemstack;
  private final InteractionHand hand;

  public ItemSinkModuleScreen(ItemSinkModuleMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 142);
    this.itemstack = menu.getItemstack();
    this.hand = menu.getModuleInventory().getHand();
  }

  @Override
  protected void init() {
    super.init();

    this.addRenderableWidget(Button.builder(
        Component.literal(ItemSinkModule.getDefaultRoute(this.itemstack) ? "Yes" : "No"), (button) -> {
          var isDefaultRoute = !ItemSinkModule.getDefaultRoute(this.itemstack);
          ItemSinkModule.setDefaultRoute(this.itemstack, isDefaultRoute);
          PacketDistributor.sendToServer(new UpdateModuleItemSinkMessage(this.hand, isDefaultRoute));
          button.setMessage(Component.literal(isDefaultRoute ? "Yes" : "No"));
        }).bounds(this.leftPos + 117, this.topPos + 36, 48, 11).build());
  }

  @Override
  protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    super.renderLabels(guiGraphics, mouseX, mouseY);
    guiGraphics.drawWordWrap(this.font, Component.literal("Default route:"), 75, 38, 40, 0x404040);
  }

  @Override
  public ResourceLocation getWidgetsTexture() {
    return BACKGROUND_TEXTURE;
  }
}
