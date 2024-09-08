package logisticspipes.world.item;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LogisticsPipesItems {

  private static final DeferredRegister.Items deferredRegister =
      DeferredRegister.createItems(LogisticsPipes.ID);

  public static void register(IEventBus modEventBus) {
    deferredRegister.register(modEventBus);
  }

  public static final DeferredItem<BlockItem> PIPE_BASIC =
      deferredRegister.registerSimpleBlockItem(LogisticsPipesBlocks.PIPE_BASIC);
}
