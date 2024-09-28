package logisticspipes.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.routing.IChannelConnectionManager;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.channels.ChannelConnection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class RouterManager implements IChannelConnectionManager {

  private final ArrayList<IRouter> routersClient = new ArrayList<>();
  private final ArrayList<ServerRouter> routersServer = new ArrayList<>();
  private final Map<UUID, Integer> uuidMap = new HashMap<>();

  private List<String> authorized = new LinkedList<>();
  private final ArrayList<ChannelConnection> channelConnectedPipes = new ArrayList<>();

  @Nullable
  public IRouter getRouter(Level level, int id) {
    if (id <= 0 || level.isClientSide) {
      return null;
    } else {
      return this.routersServer.get(id);
    }
  }

  @Nullable
  public ServerRouter getServerRouter(int id) {
    if (id <= 0) {
      return null;
    } else {
      return this.routersServer.get(id);
    }
  }

  public int getIDForUUID(@Nullable UUID id) {
    if (id == null) {
      return -1;
    }
    Integer iId = this.uuidMap.get(id);
    if (iId == null) {
      return -1;
    }
    return iId;
  }

  public void removeRouter(Level level, int id) {
    //TODO: isClient without a world is expensive
    if (!level.isClientSide) {
      this.routersServer.set(id, null);
    }
  }

  /**
   * This assumes you know what you are doing. expect exceptions to be thrown
   * if you pass the wrong side.
   *
   * @param id
   * @param side
   *            false for server, true for client.
   * @return is this a router for the side.
   */
  public boolean isRouterUnsafe(int id, boolean side, Level level) {
    if (level.isClientSide) {
      return true;
    } else {
      return this.routersServer.get(id) != null;
    }
  }

  public List<IRouter> getRouters(Level level) {
    return Collections.unmodifiableList(level.isClientSide ? this.routersClient : this.routersServer);
  }

  @Override
  public boolean hasChannelConnection(IRouter router) {
    return this.channelConnectedPipes.stream()
        .filter(con -> con.routers.size() > 1)
        .anyMatch(con -> con.routers.contains(router.getSimpleID()));
  }

  @Override
  public boolean addChannelConnection(Level level, UUID identifier, IRouter router) {
    if (level.isClientSide) {
      return false;
    }
    int routerSimpleID = router.getSimpleID();
    this.channelConnectedPipes.forEach(con -> con.routers.remove(routerSimpleID));
    var channel = channelConnectedPipes.stream()
        .filter(con -> con.identifier.equals(identifier)).findFirst();
    if (channel.isPresent()) {
      channel.get().routers.add(routerSimpleID);
    } else {
      var newChannel = new ChannelConnection();
      channelConnectedPipes.add(newChannel);
      newChannel.identifier = identifier;
      newChannel.routers.add(routerSimpleID);
    }
    return true;
  }

  @Override
  public List<CoreRoutedPipe> getConnectedPipes(Level level, IRouter router) {
    Optional<ChannelConnection> channel = channelConnectedPipes.stream()
        .filter(con -> con.routers.contains(router.getSimpleID()))
        .findFirst();
    return channel.
        map(channelConnection ->
            channelConnection.routers.stream()
                .filter(r -> r != router.getSimpleID())
                .map(id -> this.getRouter(level, id))
                .filter(Objects::nonNull)
                .map(IRouter::getPipe)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        )
        .orElse(Collections.emptyList());
  }

  @Override
  public void removeChannelConnection(Level level, IRouter router) {
    if (level.isClientSide) {
      return;
    }
    var channel = channelConnectedPipes.stream()
        .filter(con -> con.routers.contains(router.getSimpleID()))
        .findFirst();
    channel.ifPresent(chan -> chan.routers.remove(router.getSimpleID()));
    if (channel.filter(chan -> chan.routers.isEmpty()).isPresent()) {
      channelConnectedPipes.remove(channel.get());
    }
  }

  public void serverStopClean() {
    this.channelConnectedPipes.clear();
    this.routersServer.clear();
    this.uuidMap.clear();
    //this.security.clear();
  }

  public void clearClientRouters() {
    synchronized (this.routersClient) {
      this.routersClient.clear();
    }
  }

  public void dimensionUnloaded(ResourceKey<Level> dim) {
    synchronized (this.routersServer) {
      this.routersServer.stream()
          .filter(r -> r != null && r.isInDim(dim)).forEach(r -> {
        r.clearPipeCache();
        r.clearInterests();
      });
    }
  }

  public IRouter getOrCreateRouter(@Nullable UUID identifier, Level level, int x, int y, int z) {
    IRouter r;
    int id = this.getIDForUUID(identifier);
    if (id > 0) {
      getRouter(level, id); //TODO: Why?
    }
    if (level.isClientSide) {
      synchronized (this.routersClient) {
        for (IRouter r2 : this.routersClient) {
          if (r2.isAt(level.dimension(), x, y, z)) {
            return r2;
          }
        }
        r = new ClientRouter(identifier, level, x, y, z);
        this.routersClient.add(r);
      }
    } else {
      synchronized (this.routersServer) {
        for (IRouter r2 : this.routersServer) {
          if (r2 != null && r2.isAt(level.dimension(), x, y, z)) {
            return r2;
          }
        }
        final var serverRouter = new ServerRouter(identifier, level, x, y, z);
        int rId = serverRouter.getSimpleID();
        if (this.routersServer.size() <= rId) {
          this.routersServer.ensureCapacity(rId + 1);
          while (this.routersServer.size() <= rId) {
            this.routersServer.add(null);
          }
        }
        this.routersServer.set(rId, serverRouter);
        this.uuidMap.put(serverRouter.getId(), serverRouter.getSimpleID());
        r = serverRouter;
      }
    }
    return r;
  }
}
