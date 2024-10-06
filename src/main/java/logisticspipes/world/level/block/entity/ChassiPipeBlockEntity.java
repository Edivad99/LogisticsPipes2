package logisticspipes.world.level.block.entity;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.world.inventory.ChassiPipeMenu;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ChassiPipeBlockEntity extends LogisticsGenericPipeBlockEntity<PipeLogisticsChassis> implements MenuProvider {

  @Getter
  private final SimpleContainer container = new SimpleContainer(2);

  public ChassiPipeBlockEntity(BlockPos pos, BlockState blockState) {
    super(LogisticsPipesBlockEntityTypes.CHASSI_MK2.get(), pos, blockState, new PipeLogisticsChassis());
    this.container.addListener(__ -> this.setChanged());
  }

  @Override
  public Component getDisplayName() {
    return Component.empty();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new ChassiPipeMenu(id, inventory, this);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
  }

  @Override
  public void setLevel(Level level) {
    super.setLevel(level);
  }

  @Getter
  public static class ChassiTargetInformation implements IAdditionalTargetInformation {

    private final int moduleSlot;

    public ChassiTargetInformation(int slot) {
      this.moduleSlot = slot;
    }
  }
}
