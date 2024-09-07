package logisticspipes.world.level.block;

import logisticspipes.LogisticsPipes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LogisticsPipesBlocks {

  private static final DeferredRegister.Blocks deferredRegister =
      DeferredRegister.createBlocks(LogisticsPipes.ID);

  public static void register(IEventBus modEventBus) {
    deferredRegister.register(modEventBus);
  }

  public static final DeferredBlock<Block> PIPE_TRANSPORT_BASIC =
      deferredRegister.register("pipe_transport_basic",
          () -> new PipeTransportBasicBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
              .strength(5.0F, 6.0F)));

  public static final DeferredBlock<Block> STONE =
      deferredRegister.register("stone",
          () -> new StoneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
              .strength(5.0F, 6.0F)));

}
