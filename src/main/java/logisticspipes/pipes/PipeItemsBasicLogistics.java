package logisticspipes.pipes;

import java.util.Collection;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeItemsBasicLogistics extends CoreRoutedPipe {

  private final ModuleItemSink module;

  public PipeItemsBasicLogistics() {
    super(new PipeTransportLogistics(true) {
      @Override
      public boolean canPipeConnect(BlockEntity blockEntity, Direction side) {
        if (super.canPipeConnect(blockEntity, side)) {
          return true;
        }
        /*if (blockEntity instanceof LogisticsSecurityTileEntity) {
          var ori = OrientationsUtil.getOrientationOfTilewithTile(container, blockEntity);
          return ori != null && ori != Direction.DOWN && ori != Direction.UP;
        }
        if (blockEntity instanceof LogisticsProgramCompilerTileEntity) {
          var ori = OrientationsUtil.getOrientationOfTilewithTile(container, blockEntity);
          return ori != null && ori != Direction.DOWN;
        }*/
        return false;
      }
    });

    this.module = new ModuleItemSink();
    this.module.registerHandler(this, this);
    this.module.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
  }

  @Override
  public ItemSendMode getItemSendMode() {
    return ItemSendMode.NORMAL;
  }

  @Override
  public ModuleItemSink getLogisticsModule() {
    return this.module;
  }

  @Override
  public void setBlockEntity(LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> blockEntity) {
    super.setBlockEntity(blockEntity);
    this.module.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
  }

  @Override
  public boolean hasGenericInterests() {
    return this.module.isDefaultRoute();
  }

  @Override
  public void collectSpecificInterests(Collection<ItemIdentifier> itemIdentifiers) {
    if (!this.module.isDefaultRoute()) {
      this.module.collectSpecificInterests(itemIdentifiers);
    }
  }
}
