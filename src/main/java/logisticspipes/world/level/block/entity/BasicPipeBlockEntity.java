package logisticspipes.world.level.block.entity;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.world.inventory.BasicPipeMenu;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Getter
public class BasicPipeBlockEntity extends LogisticsGenericPipeBlockEntity implements MenuProvider {

  private final ModuleItemSink moduleItemSink = new ModuleItemSink();

  public BasicPipeBlockEntity(BlockPos pos, BlockState blockState) {
    super(LogisticsPipesBlockEntityTypes.PIPE_BASIC.get(), pos, blockState);
    this.moduleItemSink.requestedItemsInventory.addListener(__ -> this.setChanged());
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

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    this.moduleItemSink.deserializeNBT(registries, tag.getCompound("moduleItemSink"));
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put("moduleItemSink", this.moduleItemSink.serializeNBT(registries));
  }

  @Override
  public void setLevel(Level level) {
    super.setLevel(level);
    this.moduleItemSink.registerModule(level, this.worldPosition);
  }

  public void setDefaultRoute(boolean defaultRoute) {
    this.moduleItemSink.defaultRoute = defaultRoute;
    this.setChanged();
  }
}
