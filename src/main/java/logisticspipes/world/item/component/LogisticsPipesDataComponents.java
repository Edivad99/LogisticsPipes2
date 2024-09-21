package logisticspipes.world.item.component;

import com.mojang.serialization.Codec;
import logisticspipes.LogisticsPipes;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LogisticsPipesDataComponents {

  private static final DeferredRegister.DataComponents deferredRegister =
      DeferredRegister.createDataComponents(LogisticsPipes.ID);

  public static void register(IEventBus modEventBus) {
    deferredRegister.register(modEventBus);
  }

  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DEFAULT_ROUTE =
      deferredRegister.registerComponentType("default_route", builder ->
          builder
              .persistent(Codec.BOOL)
              .networkSynchronized(ByteBufCodecs.BOOL));
}
