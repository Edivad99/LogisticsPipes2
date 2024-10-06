package logisticspipes.world.level.block.entity;

import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.world.inventory.BasicPipeMenu;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

@Getter
public class BasicPipeBlockEntity extends LogisticsGenericPipeBlockEntity<PipeItemsBasicLogistics> implements MenuProvider {

  public BasicPipeBlockEntity(BlockPos pos, BlockState blockState) {
    super(LogisticsPipesBlockEntityTypes.PIPE_BASIC.get(), pos, blockState, new PipeItemsBasicLogistics());
  }

  @Override
  public Component getDisplayName() {
    //TODO: Change
    return Component.literal("Requested items");
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new BasicPipeMenu(id, inventory, this);
  }

  public void setDefaultRoute(boolean defaultRoute) {
    this.pipe.getLogisticsModule().setDefaultRoute(defaultRoute);
  }
}
