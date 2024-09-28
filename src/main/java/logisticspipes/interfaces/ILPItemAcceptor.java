package logisticspipes.interfaces;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public interface ILPItemAcceptor {

  <T extends CoreUnroutedPipe> boolean accept(LogisticsGenericPipeBlockEntity<T> pipe, Direction from, ItemStack stack);
}
