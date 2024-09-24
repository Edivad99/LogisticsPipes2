package logisticspipes.pipes.basic;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.transport.PipeTransportLogistics;

public abstract class CoreRoutedPipe extends CoreUnroutedPipe {

  public CoreRoutedPipe(PipeTransportLogistics transport) {
    super(transport);
  }

  public CoreRoutedPipe() {
    this(new PipeTransportLogistics(true));
  }

  public abstract LogisticsModule getLogisticsModule();

  @Override
  public boolean isRoutedPipe() {
    return true;
  }

  public boolean hasGenericInterests() {
    return false;
  }
}
