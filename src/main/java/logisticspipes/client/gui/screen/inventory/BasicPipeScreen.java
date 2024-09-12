package logisticspipes.client.gui.screen.inventory;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.inventory.BasicPipeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BasicPipeScreen extends LogisticsPipesMenuScreen<BasicPipeMenu> {

  private static final ResourceLocation BACKGROUND_TEXTURE =
      LogisticsPipes.rl("textures/gui/container/itemsink.png");

  public BasicPipeScreen(BasicPipeMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 142);
  }

  @Override
  public ResourceLocation getWidgetsTexture() {
    return BACKGROUND_TEXTURE;
  }
}
