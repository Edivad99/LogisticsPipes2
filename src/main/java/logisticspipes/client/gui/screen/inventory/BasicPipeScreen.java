package logisticspipes.client.gui.screen.inventory;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.to_server.ImportItemSinkMessage;
import logisticspipes.network.to_server.SetDefaultRouteItemSinkMessage;
import logisticspipes.world.inventory.BasicPipeMenu;
import logisticspipes.world.level.block.entity.BasicPipeBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class BasicPipeScreen extends LogisticsPipesMenuScreen<BasicPipeMenu> {

  private static final ResourceLocation BACKGROUND_TEXTURE =
      LogisticsPipes.rl("textures/gui/container/itemsink.png");
  private final ModuleItemSink module;
  private final BasicPipeBlockEntity blockEntity;

  public BasicPipeScreen(BasicPipeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 142);
    this.blockEntity = menu.getBlockEntity();
    this.module = menu.getModule();
  }

  @Override
  public ResourceLocation getWidgetsTexture() {
    return BACKGROUND_TEXTURE;
  }

  @Override
  protected void init() {
    super.init();

    if (this.module.isNearInventory()) {
      this.addRenderableWidget(Button.builder(Component.literal("Import"), (button) -> {
        PacketDistributor.sendToServer(new ImportItemSinkMessage(this.blockEntity.getBlockPos()));
      }).bounds(this.leftPos + 7, this.topPos + 36, 48, 11).build());
    }

    this.addRenderableWidget(
        Button.builder(Component.literal(this.module.isDefaultRoute() ? "Yes" : "No"), (button) -> {
          var isDefaultRoute = !this.module.isDefaultRoute();
          this.blockEntity.setDefaultRoute(isDefaultRoute);
          PacketDistributor.sendToServer(new SetDefaultRouteItemSinkMessage(this.blockEntity.getBlockPos(), isDefaultRoute));
          button.setMessage(Component.literal(isDefaultRoute ? "Yes" : "No"));
        }).bounds(this.leftPos + 117, this.topPos + 36, 48, 11).build());
  }

  @Override
  protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    super.renderLabels(guiGraphics, mouseX, mouseY);
    guiGraphics.drawWordWrap(this.font, Component.literal("Default route:"), 75, 38, 40, 0x404040);
  }
}
