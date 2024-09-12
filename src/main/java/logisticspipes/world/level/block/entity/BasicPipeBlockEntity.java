package logisticspipes.world.level.block.entity;

import logisticspipes.world.inventory.BasicPipeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class BasicPipeBlockEntity extends LogisticsTileGenericPipe implements MenuProvider {

  private final SimpleContainer invPattern = new SimpleContainer(9);

  public BasicPipeBlockEntity(BlockPos pos, BlockState blockState) {
    super(LogisticsPipesBlockEntityTypes.PIPE_BASIC.get(), pos, blockState);
  }

  @Override
  public Component getDisplayName() {
    return this.getBlockState().getBlock().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new BasicPipeMenu(id, inventory, this);
  }

  public Container getInvPattern() {
    return this.invPattern;
  }
}
