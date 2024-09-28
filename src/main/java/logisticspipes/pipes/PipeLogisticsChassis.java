package logisticspipes.pipes;

import logisticspipes.modules.ChassisModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public class PipeLogisticsChassis extends CoreRoutedPipe {

  private final ChassisModule module;

  public PipeLogisticsChassis() {
    super();
    this.module = new ChassisModule(2, this);
    this.module.registerHandler(this, this);
  }

  @Override
  public LogisticsModule getLogisticsModule() {
    return this.module;
  }

  @Override
  public ItemSendMode getItemSendMode() {
    return ItemSendMode.NORMAL;
  }
}
