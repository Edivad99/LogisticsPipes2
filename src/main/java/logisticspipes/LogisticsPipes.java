package logisticspipes;

import logisticspipes.client.ClientManager;
import logisticspipes.world.item.LogisticsPipesCreativeModTabs;
import logisticspipes.world.item.LogisticsPipesItems;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import logisticspipes.world.level.block.entity.LogisticsPipesBlockEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(LogisticsPipes.ID)
public class LogisticsPipes {

  public static final String ID = "logisticspipes";

  public LogisticsPipes(ModContainer modContainer, Dist dist) {

    var modEventBus = modContainer.getEventBus();
    if (dist.isClient()) {
      ClientManager.init(modEventBus);
    }

    LogisticsPipesBlocks.register(modEventBus);
    LogisticsPipesItems.register(modEventBus);
    LogisticsPipesBlockEntityTypes.register(modEventBus);
    LogisticsPipesCreativeModTabs.register(modEventBus);
  }

  public static ResourceLocation rl(String path) {
    return ResourceLocation.fromNamespaceAndPath(ID, path);
  }
}
