package logisticspipes.routing.pathfinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.asm.te.ITileEntityChangeListener;
import logisticspipes.asm.te.LPTileEntityObject;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IChannelRoutingConnection;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IPaintPath;
import logisticspipes.routing.LaserData;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.utils.CoordinateUtils;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.utils.OneList;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import oshi.util.tuples.Quartet;

/**
 * Examines all pipe connections and their forks to locate all connected routers
 */
public class PathFinder {

  private final int maxVisited;
  private final int maxLength;
  private final HashSet<DoubleCoordinates> setVisited;
  private final HashMap<DoubleCoordinates, Double> distances;
  @Nullable
  private final IPaintPath pathPainter;
  private double pipesVisited;

  @Nullable
  public List<Tuple<ILogisticsPowerProvider, List<IFilter>>> powerNodes;
  @Nullable
  public List<Tuple<ISubSystemPowerProvider, List<IFilter>>> subPowerProvider;
  public HashMap<CoreRoutedPipe, ExitRoute> result;

  public ITileEntityChangeListener changeListener;
  public Set<List<ITileEntityChangeListener>> listenedPipes = new HashSet<>();
  public Set<LPTileEntityObject> touchedPipes = new HashSet<>();

  public PathFinder(@Nullable IPipeInformationProvider startPipe, int maxVisited, int maxLength,
      ITileEntityChangeListener changeListener) {
    this(maxVisited, maxLength, null);
    if (startPipe == null) {
      result = new HashMap<>();
      return;
    }
    this.changeListener = changeListener;
    result = getConnectedRoutingPipes(startPipe, EnumSet.allOf(PipeRoutingConnectionType.class), null);
  }

  public PathFinder(IPipeInformationProvider startPipe, int maxVisited, int maxLength, Direction side) {
    this(maxVisited, maxLength, null);
    result = getConnectedRoutingPipes(startPipe, EnumSet.allOf(PipeRoutingConnectionType.class), side);
  }

  private PathFinder(int maxVisited, int maxLength, @Nullable IPaintPath pathPainter) {
    this.maxVisited = maxVisited;
    this.maxLength = maxLength;
    setVisited = new HashSet<>();
    distances = new HashMap<>();
    this.pathPainter = pathPainter;
  }

  private void listTileEntity(@Nullable BlockEntity blockEntity) {
    if (changeListener != null && blockEntity instanceof ILPTEInformation information
        && information.getLPTileEntityObject() != null) {
      if (!information.getLPTileEntityObject().changeListeners.contains(changeListener)) {
        information.getLPTileEntityObject().changeListeners.add(changeListener);
      }
      listenedPipes.add(information.getLPTileEntityObject().changeListeners);
      touchedPipes.add(information.getLPTileEntityObject());
    }
  }

  private HashMap<CoreRoutedPipe, ExitRoute> getConnectedRoutingPipes(IPipeInformationProvider startPipe, EnumSet<PipeRoutingConnectionType> connectionFlags, @Nullable Direction side) {
    HashMap<CoreRoutedPipe, ExitRoute> foundPipes = new HashMap<>();

    final int setVisitedSize = setVisited.size();

    boolean root = setVisitedSize == 0;

    //Reset visited count at top level
    if (setVisitedSize == 1) {
      pipesVisited = 0;
    }

    //Break recursion if we have visited a set number of pipes, to prevent client hang if pipes are weirdly configured
    pipesVisited += startPipe.getDistanceWeight() > 0 ? startPipe.getDistanceWeight() : 1;
    if (pipesVisited > maxVisited) {
      return foundPipes;
    }

    //Break recursion after certain amount of nodes visited
    if (setVisitedSize > maxLength * 10) {
      return foundPipes;
    }

    //Break recursion after certain length of nodes visited
    //Maximize to 1 so we don't stop at routes with resistor pipes
    //Check size of setVisited first to speed up the process, so we don't sum the distances all the time
    if (setVisitedSize > maxLength && distances.values().stream().mapToDouble(i -> Math.max(Math.min(i, 1), 0)).sum() > maxLength) {
      return foundPipes;
    }

    if (!startPipe.isRouterInitialized()) {
      return foundPipes;
    }

    //Break recursion if we end up on a routing pipe, unless its the first one. Will break if matches the first call
    if (startPipe.isRoutingPipe() && setVisitedSize != 0) {
      CoreRoutedPipe rp = startPipe.getRoutingPipe();
      if (rp.isStillNeedReplace()) {
        return foundPipes;
      }
      double size = 0;
      for (Double dis : distances.values()) {
        size += dis;
      }

      if (!rp.getUpgradeManager().hasPowerPassUpgrade()) {
        connectionFlags.remove(PipeRoutingConnectionType.CAN_POWER_SUB_SYSTEM_FROM);
      }

      foundPipes.put(rp, new ExitRoute(null, rp.getRouter(), null, side.getOpposite(), Math.max(1, size), connectionFlags, distances.size()));

      return foundPipes;
    }

    //Visited is checked after, so we can reach the same target twice to allow to keep the shortest path
    setVisited.add(new DoubleCoordinates(startPipe));
    distances.put(new DoubleCoordinates(startPipe), startPipe.getDistance() * startPipe.getDistanceWeight());

    // first check specialPipeConnections (tesseracts, teleports, other connectors)
    var pipez = SimpleServiceLocator.specialpipeconnection.getConnectedPipes(startPipe, connectionFlags, side);
    for (var specialConnection : pipez) {
      if (setVisited.contains(new DoubleCoordinates(specialConnection.getConnectedPipe()))) {
        //Don't go where we have been before
        continue;
      }
      distances.put(new DoubleCoordinates(startPipe).center(), specialConnection.getDistance());
      var result = getConnectedRoutingPipes(specialConnection.getConnectedPipe(), specialConnection.getConnectionFlags(), specialConnection.getInsertOrientation());
      distances.remove(new DoubleCoordinates(startPipe).center());
      for (Map.Entry<CoreRoutedPipe, ExitRoute> pipe : result.entrySet()) {
        pipe.getValue().exitOrientation = specialConnection.getExitOrientation();
        ExitRoute foundPipe = foundPipes.get(pipe.getKey());
        if (foundPipe == null || (pipe.getValue().distanceToDestination < foundPipe.distanceToDestination)) {
          // New path OR 	If new path is better, replace old path
          foundPipes.put(pipe.getKey(), pipe.getValue());
        }
      }
    }

    ArrayDeque<Quartet<BlockEntity, Direction, Integer, Boolean>> connections = new ArrayDeque<>();

    //Recurse in all directions
    for (var direction : Direction.values()) {
      if (root && side != null && !direction.equals(side)) {
        continue;
      }

      // tile may be up to 1 second old, but any neighbour pipe change will cause an immidiate update here, so we know that if it has changed, it isn't a pipe that has done so.
      var tile = startPipe.getNextConnectedBlockEntity(direction);

      if (tile == null) {
        continue;
      }
      if (root && (direction.getAxis() == Direction.Axis.X || direction.getAxis() == Direction.Axis.Z)) {
        if (tile instanceof ILogisticsPowerProvider provider) {
          if (powerNodes == null) {
            powerNodes = new ArrayList<>();
          }
          //If we are a FireWall pipe add our filter to the pipes
          if (startPipe.isFirewallPipe()) {
            powerNodes.add(new Tuple<>(provider, new OneList<>(startPipe.getFirewallFilter())));
          } else {
            powerNodes.add(new Tuple<>(provider, Collections.unmodifiableList(new ArrayList<>(0))));
          }
        } else if (tile instanceof ISubSystemPowerProvider provider) {
          if (subPowerProvider == null) {
            subPowerProvider = new ArrayList<>();
          }
          //If we are a FireWall pipe add our filter to the pipes
          if (startPipe.isFirewallPipe()) {
            subPowerProvider.add(new Tuple<>(provider, new OneList<>(startPipe.getFirewallFilter())));
          } else {
            subPowerProvider.add(new Tuple<>(provider, Collections.unmodifiableList(new ArrayList<>(0))));
          }
        }
      }
      connections.add(new Quartet<>(tile, direction, 0, false));
    }

    while (!connections.isEmpty()) {
      var quartet = connections.pollFirst();
      BlockEntity tile = quartet.getA();
      Direction direction = quartet.getB();
      int resistance = quartet.getC();
      boolean isDirectConnection = quartet.getD();
      EnumSet<PipeRoutingConnectionType> nextConnectionFlags = EnumSet.copyOf(connectionFlags);

      if (root) {
        Collection<BlockEntity> list = SimpleServiceLocator.specialtileconnection.getConnectedPipes(tile);
        if (!list.isEmpty()) {
          connections.addAll(list.stream()
              .map(pipe -> new Quartet<>(pipe, direction, 0, false))
              .toList());
          listTileEntity(tile);
          continue;
        }
        if (!startPipe.getRoutingPipe().getUpgradeManager().hasPowerPassUpgrade()) {
          nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_POWER_SUB_SYSTEM_FROM);
        }
      }

      if (!SimpleServiceLocator.pipeInformationManager.isPipe(tile) && startPipe.isRoutingPipe() && startPipe.getRoutingPipe() instanceof IChannelRoutingConnection && startPipe.canConnect(tile, direction, false)) {
        var level = tile.getLevel();
        var pos = tile.getBlockPos();
        var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite());
        if (cap != null) {
          if (SimpleServiceLocator.connectionManager.hasChannelConnection(startPipe.getRoutingPipe().getRouter())) {
            List<CoreRoutedPipe> connectedPipes = SimpleServiceLocator.connectionManager
                .getConnectedPipes(level, startPipe.getRoutingPipe().getRouter());
            connections.addAll(connectedPipes.stream()
                .map(pipe -> new Quartet<>((BlockEntity) pipe.container, direction, ((IChannelRoutingConnection) startPipe.getRoutingPipe()).getConnectionResistance(), true))
                .toList());
            if (!connectedPipes.isEmpty()) {
              continue;
            }
          }
        }
      }

      if (tile == null) {
        continue;
      }

      IPipeInformationProvider currentPipe = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(tile);

      if (currentPipe != null && currentPipe.isRouterInitialized() && (isDirectConnection || SimpleServiceLocator.pipeInformationManager.canConnect(startPipe, currentPipe, direction, true))) {

        listTileEntity(tile);

        if (currentPipe.isMultiBlock()) {
          currentPipe.getPartsOfPipe().forEach(this::listTileEntity);
        }

        if (setVisited.contains(new DoubleCoordinates(currentPipe))) {
          //Don't go where we have been before
          continue;
        }
        if (side != direction && !root) { //Only straight connections for subsystem power
          nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_POWER_SUB_SYSTEM_FROM);
        }
        if (isDirectConnection) { //ISC doesn't pass power
          nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_POWER_FROM);
          nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_POWER_SUB_SYSTEM_FROM);
        }
        //Iron, obsidean and liquid pipes will separate networks
        if (currentPipe.divideNetwork()) {
          continue;
        }
        if (currentPipe.powerOnly()) {
          nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_ROUTE_TO);
          nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_REQUEST_FROM);
        }
        if (startPipe.isOnewayPipe()) {
          if (startPipe.isOutputClosed(direction)) {
            nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_ROUTE_TO);
          }
        }
        if (currentPipe.isOnewayPipe()) {
          nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_POWER_SUB_SYSTEM_FROM);
          if (currentPipe.isOutputClosed(direction.getOpposite())) {
            nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_REQUEST_FROM);
            nextConnectionFlags.remove(PipeRoutingConnectionType.CAN_POWER_FROM);
          }
        }

        if (nextConnectionFlags.isEmpty()) { //don't bother going somewhere we can't do anything with
          continue;
        }

        int beforeRecurseCount = foundPipes.size();
        HashMap<CoreRoutedPipe, ExitRoute> result = null;
        if (currentPipe instanceof IRouteProvider) {
          List<IRouteProvider.RouteInfo> list = ((IRouteProvider) currentPipe).getConnectedPipes(direction.getOpposite());
          if (list != null) {
            result = new HashMap<>();
            DoubleCoordinates pos = new DoubleCoordinates(currentPipe);
            for (IRouteProvider.RouteInfo info : list) {
              if (info.getPipe() == startPipe) continue;
              if (setVisited.contains(new DoubleCoordinates(info.getPipe()))) {
                //Don't go where we have been before
                continue;
              }
              distances.put(pos, (currentPipe.getDistance() * currentPipe.getDistanceWeight()) + info.getLength());
              result.putAll(getConnectedRoutingPipes(info.getPipe(), nextConnectionFlags, direction));
              distances.remove(pos);
            }
          }
        }
        if (result == null) {
          result = getConnectedRoutingPipes(currentPipe, nextConnectionFlags, direction);
        }
        for (Map.Entry<CoreRoutedPipe, ExitRoute> pipeEntry : result.entrySet()) {
          //Update Result with the direction we took
          pipeEntry.getValue().exitOrientation = direction;
          ExitRoute foundPipe = foundPipes.get(pipeEntry.getKey());
          if (foundPipe == null) {
            // New path
            foundPipes.put(pipeEntry.getKey(), pipeEntry.getValue());
            //Add resistance
            pipeEntry.getValue().distanceToDestination += resistance;
          } else if (pipeEntry.getValue().distanceToDestination + resistance < foundPipe.distanceToDestination) {
            //If new path is better, replace old path, otherwise do nothing
            foundPipes.put(pipeEntry.getKey(), pipeEntry.getValue());
            //Add resistance
            pipeEntry.getValue().distanceToDestination += resistance;
          }
        }
        if (foundPipes.size() > beforeRecurseCount && pathPainter != null) {
          var data = new LaserData(startPipe.getX(), startPipe.getY(), startPipe.getZ(), direction, connectionFlags);
          pathPainter.addLaser(startPipe.getLevel(), data);
        }
      }
    }
    setVisited.remove(new DoubleCoordinates(startPipe));
    distances.remove(new DoubleCoordinates(startPipe));
    if (startPipe.isRoutingPipe()) { // ie, has the recursion returned to the pipe it started from?
      for (ExitRoute e : foundPipes.values()) {
        e.root = (startPipe.getRoutingPipe()).getRouter();
      }
    }
    //If we are a FireWall pipe add our filter to the pipes
    if (startPipe.isFirewallPipe() && root) {
      for (ExitRoute e : foundPipes.values()) {
        e.filters = new OneList<>(startPipe.getFirewallFilter());
      }
    }
    return foundPipes;
  }


  public static int messureDistanceToNextRoutedPipe(DoubleCoordinates lpPosition, Direction exitOrientation, Level level) {
    int dis = 1;
    BlockEntity blockEntity = lpPosition.getBlockEntity(level);
    if (blockEntity instanceof LogisticsGenericPipeBlockEntity<?> pipeBlockEntity) {
      blockEntity = pipeBlockEntity.getNextConnectedBlockEntity(exitOrientation);
    }
    if (blockEntity == null) {
      return 0;
    }
    IPipeInformationProvider info = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(blockEntity);
    while (info != null && !info.isRoutingPipe()) {
      blockEntity = info.getNextConnectedBlockEntity(exitOrientation);
      if (blockEntity == null) {
        info = null;
        continue;
      }
      info = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(blockEntity);
      dis++;
    }
    return dis;
  }

  /**
   * Recurse through all exists of a pipe to find instances of
   * PipeItemsRouting. maxVisited and maxLength are safeguards for recursion
   * runaways.
   *
   * @param startPipe
   *            - The TileEntity to start the search from
   * @param maxVisited
   *            - The maximum number of pipes to visit, regardless of
   *            recursion level
   * @param maxLength
   *            - The maximum recurse depth, i.e. the maximum length pipe that
   *            is supported
   */
  public static HashMap<CoreRoutedPipe, ExitRoute> paintAndGetConnectedRoutingPipes(
      BlockEntity startPipe, Direction startOrientation, int maxVisited, int maxLength,
      IPaintPath pathPainter, EnumSet<PipeRoutingConnectionType> connectionType) {
    IPipeInformationProvider startProvider = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(startPipe);
    if (startProvider == null) {
      return new HashMap<>();
    }
    PathFinder newSearch = new PathFinder(maxVisited, maxLength, pathPainter);
    DoubleCoordinates p = new DoubleCoordinates(startProvider);
    newSearch.setVisited.add(p);
    CoordinateUtils.add(p, startOrientation);
    BlockEntity entity = p.getBlockEntity(startProvider.getLevel());
    IPipeInformationProvider provider = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(entity);
    if (provider == null) {
      return new HashMap<>();
    }
    return newSearch.getConnectedRoutingPipes(provider, connectionType, startOrientation);
  }
}
