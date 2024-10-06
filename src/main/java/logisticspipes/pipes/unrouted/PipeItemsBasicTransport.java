package logisticspipes.pipes.unrouted;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.transport.PipeTransportLogistics;
import net.minecraft.network.FriendlyByteBuf;

public class PipeItemsBasicTransport extends CoreUnroutedPipe {

  public PipeItemsBasicTransport() {
    super(new PipeTransportLogistics(false));
  }

  @Override
  public void writeData(FriendlyByteBuf buf) {
  }

  @Override
  public void readData(FriendlyByteBuf buf) {
  }
}
