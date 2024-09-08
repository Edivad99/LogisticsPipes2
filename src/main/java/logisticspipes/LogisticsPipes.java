package logisticspipes;

import logisticspipes.client.ClientManager;
import logisticspipes.datagen.LogisticsPipesBlockStateProvider;
import logisticspipes.world.item.LogisticsPipesCreativeModTabs;
import logisticspipes.world.item.LogisticsPipesItems;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import logisticspipes.world.level.block.entity.LogisticsPipesBlockEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(LogisticsPipes.ID)
public class LogisticsPipes {

  public static final String ID = "logisticspipes";

  public LogisticsPipes(ModContainer modContainer, Dist dist) {

    var modEventBus = modContainer.getEventBus();
    modEventBus.addListener(this::handleGatherData);

    if (dist.isClient()) {
      ClientManager.init(modEventBus);
    }

    LogisticsPipesBlocks.register(modEventBus);
    LogisticsPipesItems.register(modEventBus);
    LogisticsPipesBlockEntityTypes.register(modEventBus);
    LogisticsPipesCreativeModTabs.register(modEventBus);
  }

  private void handleGatherData(GatherDataEvent event) {
    var generator = event.getGenerator();
    var packOutput = generator.getPackOutput();
    var lookupProvider = event.getLookupProvider();
    var fileHelper = event.getExistingFileHelper();

    generator.addProvider(event.includeClient(), new LogisticsPipesBlockStateProvider(packOutput, fileHelper));
  }


  public static ResourceLocation rl(String path) {
    return ResourceLocation.fromNamespaceAndPath(ID, path);
  }
}
