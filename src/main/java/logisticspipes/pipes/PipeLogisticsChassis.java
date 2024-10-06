package logisticspipes.pipes;

import javax.annotation.Nullable;
import logisticspipes.fromkotlin.Adjacent;
import logisticspipes.fromkotlin.NoAdjacent;
import logisticspipes.fromkotlin.SingleAdjacent;
import logisticspipes.modules.AsyncExtractorModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.Direction;

public class PipeLogisticsChassis extends CoreRoutedPipe {

  //private final ChassisModule module;
  private final AsyncExtractorModule module;

  public PipeLogisticsChassis() {
    super();
    //this.module = new ChassisModule(2, this);
    this.module = new AsyncExtractorModule();
    this.module.registerHandler(this, this);
    this.module.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
  }

  @Override
  public ItemSendMode getItemSendMode() {
    return ItemSendMode.NORMAL;
  }

  @Override
  public LogisticsModule getLogisticsModule() {
    return this.module;
  }

  @Override
  public void setBlockEntity(LogisticsGenericPipeBlockEntity<? extends CoreUnroutedPipe> blockEntity) {
    super.setBlockEntity(blockEntity);
    this.module.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
  }

  @Override
  protected void updateAdjacentCache() {
    super.updateAdjacentCache();
    final Adjacent adjacent = getAdjacent();
    if (adjacent instanceof SingleAdjacent) {
      this.pointedAdjacentProperty.setValue(adjacent);
    } else {
      final @Nullable Direction oldPointedDirection = this.pointedAdjacentProperty.getDirectionOrNull();
      SingleAdjacent newPointedAdjacent = null;
      if (oldPointedDirection != null) {
        // update pointed adjacent with connection type or reset it
        newPointedAdjacent = adjacent.optionalGet(oldPointedDirection)
            .map(connectionType -> new SingleAdjacent(this, oldPointedDirection, connectionType))
            .orElse(null);
      }
      if (newPointedAdjacent == null) {
        newPointedAdjacent = adjacent.neighbors().entrySet().stream().findAny()
            .map(connectedNeighbor -> new SingleAdjacent(this, connectedNeighbor.getKey().getDirection(), connectedNeighbor.getValue()))
            .orElse(null);
      }
      if (newPointedAdjacent == null) {
        this.pointedAdjacentProperty.setValue(NoAdjacent.INSTANCE);
      } else {
        this.pointedAdjacentProperty.setValue(newPointedAdjacent);
      }
    }
  }

  @Override
  public Direction getPointedOrientation() {
    return this.pointedAdjacentProperty.getDirectionOrNull();
  }

  @Override
  public Adjacent getAvailableAdjacent() {
    return this.pointedAdjacentProperty.getValue();
  }

  //TODO: Remove, here for testing
  private final AdjacentProperty pointedAdjacentProperty = new AdjacentProperty();


  static class AdjacentProperty {
    private Adjacent value;

    public void setValue(Adjacent newPointedAdjacent) {
      value = newPointedAdjacent;
    }

    public Direction getDirectionOrNull() {
      if (value instanceof SingleAdjacent) {
        return ((SingleAdjacent) value).getDir();
      }
      return null;
    }

    public Adjacent getValue() {
      return value;
    }
  }
}
