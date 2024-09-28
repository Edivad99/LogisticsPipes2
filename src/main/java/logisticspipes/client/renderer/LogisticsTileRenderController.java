package logisticspipes.client.renderer;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;

public class LogisticsTileRenderController<T extends CoreUnroutedPipe> {

  private final LogisticsGenericPipeBlockEntity<T> blockEntity;

  public LogisticsTileRenderController(LogisticsGenericPipeBlockEntity<T> blockEntity) {
    this.blockEntity = blockEntity;
  }

  public void sendInit() {

  }

  public void onUpdate() {

  }
}
