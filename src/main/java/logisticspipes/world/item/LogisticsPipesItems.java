package logisticspipes.world.item;

import logisticspipes.LogisticsPipes;
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

  public static final DeferredItem<PipeTransportBasicItem> PIPE_TRANSPORT_BASIC =
      deferredRegister.register("pipe_transport_basic",
          () -> new PipeTransportBasicItem(new BlockItem.Properties()));
}
