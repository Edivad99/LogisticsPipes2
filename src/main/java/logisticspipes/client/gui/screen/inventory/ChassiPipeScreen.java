package logisticspipes.client.gui.screen.inventory;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.inventory.ChassiPipeMenu;
import logisticspipes.world.level.block.entity.ChassiPipeBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChassiPipeScreen extends LogisticsPipesMenuScreen<ChassiPipeMenu> {

  private static final ResourceLocation BACKGROUND_TEXTURE =
      LogisticsPipes.rl("textures/gui/container/chassipipe_size2.png");
  private final ChassiPipeBlockEntity blockEntity;

  public ChassiPipeScreen(ChassiPipeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 187);
    this.imageWidth = 195;
    this.blockEntity = menu.getBlockEntity();
  }

  @Override
  public ResourceLocation getWidgetsTexture() {
    return BACKGROUND_TEXTURE;
  }

  @Override
  protected void init() {
    super.init();
  }
}
