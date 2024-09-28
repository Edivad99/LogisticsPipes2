package logisticspipes.interfaces.routing;

import java.util.List;
import java.util.UUID;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import net.minecraft.world.level.Level;

public interface IChannelConnectionManager {

  boolean hasChannelConnection(IRouter router);

  boolean addChannelConnection(Level level, UUID identifier, IRouter router);

  List<CoreRoutedPipe> getConnectedPipes(Level level, IRouter router);

  void removeChannelConnection(Level level, IRouter router);
}
