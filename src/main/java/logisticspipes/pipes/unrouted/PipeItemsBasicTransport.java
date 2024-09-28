package logisticspipes.pipes.unrouted;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.transport.PipeTransportLogistics;

public class PipeItemsBasicTransport extends CoreUnroutedPipe {

  public PipeItemsBasicTransport() {
    super(new PipeTransportLogistics(false));
  }
}
