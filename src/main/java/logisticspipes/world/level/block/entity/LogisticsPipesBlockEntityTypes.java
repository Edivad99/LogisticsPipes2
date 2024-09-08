package logisticspipes.world.level.block.entity;

import logisticspipes.LogisticsPipes;
import logisticspipes.world.level.block.LogisticsPipesBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LogisticsPipesBlockEntityTypes {

  private static final DeferredRegister<BlockEntityType<?>> deferredRegister =
      DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LogisticsPipes.ID);

  public static void register(IEventBus modEventBus) {
    deferredRegister.register(modEventBus);
  }

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LogisticsTileGenericPipe>> PIPE_BASIC =
      deferredRegister.register("pipe_basic",
          () -> BlockEntityType.Builder
              .of(LogisticsTileGenericPipe::new, LogisticsPipesBlocks.PIPE_BASIC.get())
              .build(null));

}
