package logisticspipes.routing;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.asm.te.ITileEntityChangeListener;
import logisticspipes.asm.te.LPTileEntityObject;
import logisticspipes.interfaces.IRoutingDebugAdapter;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.PathFinder;
import logisticspipes.ticks.LPTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.utils.OneList;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import oshi.util.tuples.Quartet;

public class ServerRouter implements IRouter, Comparable<ServerRouter> {

  private static final int REFRESH_TIME = 20;

  protected static final ReentrantReadWriteLock SharedLSADatabaseLock = new ReentrantReadWriteLock();
  protected static final Lock SharedLSADatabasereadLock = ServerRouter.SharedLSADatabaseLock.readLock();
  protected static final Lock SharedLSADatabasewriteLock = ServerRouter.SharedLSADatabaseLock.writeLock();
  protected static int[] lastLSAVersion = new int[0];
  protected static LSA[] SharedLSADatabase = new LSA[0];

  // things with specific interests -- providers (including crafters)
  private static final ConcurrentHashMap<Item, TreeSet<ServerRouter>> globalSpecificInterests = new ConcurrentHashMap<>();

  // things potentially interested in every item (chassi with generic sinks)
  private static TreeSet<ServerRouter> genericInterests = new TreeSet<>();
  private static final Lock genericInterestsWLock = new ReentrantLock();

  // things this pipe is interested in (either providing or sinking)
  private TreeSet<Item> interests = new TreeSet<>();
  private final Lock interestsRWLock = new ReentrantLock();

  static int iterated = 0;// used pseudp-random to spread items over the tick range
  private static int maxLSAUpdateIndex = 0;
  private static int firstFreeId = 1;
  private static final BitSet simpleIdUsedSet = new BitSet();

  public final UUID id;
  protected final LSA myLsa;
  protected final ReentrantReadWriteLock routingTableUpdateLock = new ReentrantReadWriteLock();
  protected final Lock routingTableUpdateWriteLock = routingTableUpdateLock.writeLock();
  protected final int simpleID;
  @Getter
  private final int xCoord, yCoord, zCoord;
  // these are maps, not hashMaps because they are unmodifiable Collections to avoid concurrentModification exceptions.
  private Map<CoreRoutedPipe, ExitRoute> adjacent = new HashMap<>();
  private Map<ServerRouter, ExitRoute> adjacentRouter = new HashMap<>();
  private Map<ServerRouter, ExitRoute> adjacentRouter_Old = new HashMap<>();
  @Nullable
  private List<Tuple<ILogisticsPowerProvider, List<IFilter>>> powerAdjacent = new ArrayList<>();
  @Nullable
  private List<Tuple<ISubSystemPowerProvider, List<IFilter>>> subSystemPowerAdjacent = new ArrayList<>();
  private boolean[] sideDisconnected = new boolean[6];

  private EnumSet<Direction> routedExits = EnumSet.noneOf(Direction.class);
  private final Level level;
  private final ResourceKey<Level> dimension;
  @Nullable
  private WeakReference<CoreRoutedPipe> myPipeCache = null;

  /**
   * Map of router -> orientation for all known destinations
   **/
  private List<List<ExitRoute>> routeTable = Collections.unmodifiableList(new ArrayList<>());
  private List<ExitRoute> routeCosts = Collections.unmodifiableList(new ArrayList<>());
  private final LinkedList<Tuple<Integer, IRouterQueuedTask>> queue = new LinkedList<>();
  private int connectionNeedsChecking;
  private final List<DoubleCoordinates> causedBy = new LinkedList<>();
  public List<Tuple<ILogisticsPowerProvider, List<IFilter>>> LPPowerTable = Collections.unmodifiableList(new ArrayList<>());
  public List<Tuple<ISubSystemPowerProvider, List<IFilter>>> SubSystemPowerTable = Collections.unmodifiableList(new ArrayList<>());
  private int LSAVersion = 0;
  int ticksUntilNextInventoryCheck = 0;

  private EnumMap<Direction, Integer> subPowerExits = new EnumMap<>(Direction.class);
  private final ITileEntityChangeListener localChangeListener = new ITileEntityChangeListener() {

    @Override
    public void pipeRemoved(DoubleCoordinates pos) {
      if (connectionNeedsChecking == 0) {
        connectionNeedsChecking = 1;
      }
      if (LogisticsPipes.isDebug()) {
        causedBy.add(pos);
      }
    }

    @Override
    public void pipeAdded(DoubleCoordinates pos, Direction side) {
      if (connectionNeedsChecking == 0) {
        connectionNeedsChecking = 1;
      }
      if (LogisticsPipes.isDebug()) {
        causedBy.add(pos);
      }
    }
  };

  private Set<List<ITileEntityChangeListener>> listenedPipes = new HashSet<>();
  private Set<LPTileEntityObject> oldTouchedPipes = new HashSet<>();


  public ServerRouter(@Nullable UUID globalID, Level level,
      int xCoord, int yCoord, int zCoord) {
    this.id = globalID != null ? globalID : UUID.randomUUID();
    this.level = level;
    this.dimension = level.dimension();
    this.xCoord = xCoord;
    this.yCoord = yCoord;
    this.zCoord = zCoord;
    this.clearPipeCache();
    this.myLsa = new LSA();
    this.myLsa.neighboursWithMetric = new HashMap<>();
    this.myLsa.power = new ArrayList<>();
    ServerRouter.SharedLSADatabasewriteLock.lock(); // any time after we claim the SimpleID, the database could be accessed at that index
    this.simpleID = ServerRouter.claimSimpleID();
    if (ServerRouter.SharedLSADatabase.length <= simpleID) {
      int newlength = ((int) (simpleID * 1.5)) + 1;
      LSA[] new_SharedLSADatabase = new LSA[newlength];
      System.arraycopy(ServerRouter.SharedLSADatabase, 0, new_SharedLSADatabase, 0, ServerRouter.SharedLSADatabase.length);
      ServerRouter.SharedLSADatabase = new_SharedLSADatabase;
      int[] new_lastLSAVersion = new int[newlength];
      System.arraycopy(ServerRouter.lastLSAVersion, 0, new_lastLSAVersion, 0, ServerRouter.lastLSAVersion.length);
      ServerRouter.lastLSAVersion = new_lastLSAVersion;
    }
    ServerRouter.lastLSAVersion[simpleID] = 0;
    ServerRouter.SharedLSADatabase[simpleID] = this.myLsa; // make non-structural change (threadsafe)
    ServerRouter.SharedLSADatabasewriteLock.unlock();
  }

  /**
   * Called on server shutdown only
   */
  public static void cleanup() {
    ServerRouter.globalSpecificInterests.clear();
    ServerRouter.genericInterests.clear();
    ServerRouter.SharedLSADatabasewriteLock.lock();
    ServerRouter.SharedLSADatabase = new LSA[0];
    ServerRouter.lastLSAVersion = new int[0];
    ServerRouter.SharedLSADatabasewriteLock.unlock();
    ServerRouter.simpleIdUsedSet.clear();
    ServerRouter.firstFreeId = 1;
  }

  private static int claimSimpleID() {
    int idx = ServerRouter.simpleIdUsedSet.nextClearBit(ServerRouter.firstFreeId);
    ServerRouter.firstFreeId = idx + 1;
    ServerRouter.simpleIdUsedSet.set(idx);
    return idx;
  }

  private static int getBiggestSimpleID() {
    return ServerRouter.simpleIdUsedSet.size();
  }

  @Override
  public CoreRoutedPipe getPipe() {
    CoreRoutedPipe crp = this.getCachedPipe();
    if (crp != null) {
      return crp;
    }
    var pos = new BlockPos(this.xCoord, this.yCoord, this.zCoord);
    var blockEntity = this.level.getBlockEntity(pos);
    if (!(blockEntity instanceof LogisticsGenericPipeBlockEntity<?> pipeBlockEntity)) {
      return null;
    }
    if (!(pipeBlockEntity.pipe instanceof CoreRoutedPipe coreRoutedPipe)) {
      return null;
    }
    this.myPipeCache = new WeakReference<>(coreRoutedPipe);
    return coreRoutedPipe;
  }

  @Override
  @Nullable
  public CoreRoutedPipe getCachedPipe() {
    if(this.myPipeCache != null) {
      return this.myPipeCache.get();
    }
    return null;
  }

  @Override
  public UUID getId() {
    return this.id;
  }

  @Override
  public List<Tuple<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider() {
    return this.LPPowerTable;
  }

  @Override
  public List<Tuple<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider() {
    return this.SubSystemPowerTable;
  }

  @Override
  public boolean isSideDisconnected(@Nullable Direction dir) {
    return dir != null && this.sideDisconnected[dir.ordinal()];
  }

  @Override
  public void clearPipeCache() {
    this.myPipeCache = null;
  }

  public static BitSet getRoutersInterestedIn(@Nullable Item item) {
    final BitSet s = new BitSet(ServerRouter.getBiggestSimpleID() + 1);
    for (IRouter r : ServerRouter.genericInterests) {
      s.set(r.getSimpleID());
    }
    if (item != null) {
      Stream.of(item/*, item.getUndamaged(), item.getIgnoringNBT(),
              item.getUndamaged().getIgnoringNBT(), item.getIgnoringData(),
              item.getIgnoringData().getIgnoringNBT()*/)
          .forEach(itemid -> setBitsForItemInterests(s, itemid));
    }
    return s;
  }

  private static void setBitsForItemInterests(final BitSet bitset, final Item itemid) {
    TreeSet<ServerRouter> specifics = ServerRouter.globalSpecificInterests.get(itemid);
    if (specifics != null) {
      for (IRouter r : specifics) {
        bitset.set(r.getSimpleID());
      }
    }
  }

  @Override
  public int getSimpleID() {
    return this.simpleID;
  }

  @Override
  public boolean isInDim(ResourceKey<Level> dimension) {
    return this.dimension == dimension;
  }

  @Override
  public boolean isAt(ResourceKey<Level> dimension, int x, int y, int z) {
    return this.dimension == dimension && this.xCoord == x && this.yCoord == y && this.zCoord == z;
  }

  @Override
  public DoubleCoordinates getLPPosition() {
    return new DoubleCoordinates(this.xCoord, this.yCoord, this.zCoord);
  }

  public void ensureLatestRoutingTable() {
    if (connectionNeedsChecking != 0) {
      boolean blockNeedsUpdate = this.checkAdjacentUpdate();
      if (blockNeedsUpdate) {
        this.updateLsa();
      }
    }
    if (LSAVersion > ServerRouter.lastLSAVersion[simpleID]) {
      CreateRouteTable(LSAVersion);
    }
  }

  public boolean checkAdjacentUpdate() {
    boolean blockNeedsUpdate = recheckAdjacent();
    if (!blockNeedsUpdate) {
      return false;
    }

    CoreRoutedPipe pipe = getPipe();
    if (pipe == null) {
      return true;
    }
    pipe.refreshRender(true);
    return true;
  }

  /**
   * Rechecks the piped connection to all adjacent routers as well as discover
   * new ones.
   */
  private boolean recheckAdjacent() {
    this.connectionNeedsChecking = 0;
    if (LogisticsPipes.isDebug()) {
      this.causedBy.clear();
    }
    if (getPipe() != null) {
      /*
      if (getPipe().getDebug() != null && getPipe().getDebug().debugThisPipe) {
        Info info = StackTraceUtil.addTraceInformation("(" + getPipe().getX() + ", " + getPipe().getY() + ", " + getPipe().getZ() + ")");
        StackTraceUtil.printTrace();
        info.end();
      }
      */
      //getPipe().spawnParticle(Particles.LightRedParticle, 5);
    }

    LPTickHandler.adjChecksDone++;
    boolean adjacentChanged = false;
    CoreRoutedPipe thisPipe = getPipe();
    if (thisPipe == null) {
      return false;
    }
    HashMap<CoreRoutedPipe, ExitRoute> adjacent;
    List<Tuple<ILogisticsPowerProvider, List<IFilter>>> power;
    List<Tuple<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
    PathFinder finder = new PathFinder(thisPipe.container, Configs.LOGISTICS_DETECTION_COUNT,
        Configs.LOGISTICS_DETECTION_LENGTH, localChangeListener);
    power = finder.powerNodes;
    subSystemPower = finder.subPowerProvider;
    adjacent = finder.result;

    Map<Direction, List<CoreRoutedPipe>> pipeDirections = new HashMap<>();

    for (Map.Entry<CoreRoutedPipe, ExitRoute> entry : adjacent.entrySet()) {
      List<CoreRoutedPipe> list = pipeDirections.computeIfAbsent(entry.getValue().exitOrientation, k -> new ArrayList<>());
      list.add(entry.getKey());
    }

    pipeDirections.entrySet().stream()
        .filter(entry -> entry.getValue().size() > Configs.MAX_UNROUTED_CONNECTIONS)
        .forEach(entry -> entry.getValue().forEach(adjacent::remove));

    listenedPipes.stream()
        .filter(list -> !finder.listenedPipes.contains(list))
        .forEach(list -> list.remove(localChangeListener));
    listenedPipes = finder.listenedPipes;

    for (CoreRoutedPipe pipe : adjacent.keySet()) {
      if (pipe.isStillNeedReplace()) {
        return false;
      }
    }

    boolean[] oldSideDisconnected = sideDisconnected;
    sideDisconnected = new boolean[6];
    checkSecurity(adjacent);

    boolean changed = false;

    for (int i = 0; i < 6; i++) {
      changed |= sideDisconnected[i] != oldSideDisconnected[i];
    }
    if (changed) {
      CoreRoutedPipe pipe = getPipe();
      if (pipe != null) {
        var level = pipe.getLevel();
        level.markAndNotifyBlock(pipe.getPos(), level.getChunkAt(pipe.getPos()),
            level.getBlockState(pipe.getPos()), level.getBlockState(pipe.getPos()), 3, 512);
        pipe.refreshConnectionAndRender(false);
      }
      adjacentChanged = true;
    }

    if (this.adjacent.size() != adjacent.size()) {
      adjacentChanged = true;
    }

    for (CoreRoutedPipe pipe : this.adjacent.keySet()) {
      if (!adjacent.containsKey(pipe)) {
        adjacentChanged = true;
        break;
      }
    }
    if (this.powerAdjacent != null) {
      if (power == null) {
        adjacentChanged = true;
      } else {
        for (var provider : this.powerAdjacent) {
          if (!power.contains(provider)) {
            adjacentChanged = true;
            break;
          }
        }
      }
    }
    if (power != null) {
      if (this.powerAdjacent == null) {
        adjacentChanged = true;
      } else {
        for (var provider : power) {
          if (!this.powerAdjacent.contains(provider)) {
            adjacentChanged = true;
            break;
          }
        }
      }
    }
    if (this.subSystemPowerAdjacent != null) {
      if (subSystemPower == null) {
        adjacentChanged = true;
      } else {
        for (var provider : this.subSystemPowerAdjacent) {
          if (!subSystemPower.contains(provider)) {
            adjacentChanged = true;
            break;
          }
        }
      }
    }
    if (subSystemPower != null) {
      if (this.subSystemPowerAdjacent == null) {
        adjacentChanged = true;
      } else {
        for (var provider : subSystemPower) {
          if (!this.subSystemPowerAdjacent.contains(provider)) {
            adjacentChanged = true;
            break;
          }
        }
      }
    }
    for (Map.Entry<CoreRoutedPipe, ExitRoute> pipe : adjacent.entrySet()) {
      ExitRoute oldExit = this.adjacent.get(pipe.getKey());
      if (oldExit == null) {
        adjacentChanged = true;
        break;
      }
      ExitRoute newExit = pipe.getValue();

      if (!newExit.equals(oldExit)) {
        adjacentChanged = true;
        break;
      }
    }

    if (!oldTouchedPipes.equals(finder.touchedPipes)) {
      CacheHolder.clearCache(oldTouchedPipes);
      CacheHolder.clearCache(finder.touchedPipes);
      oldTouchedPipes = finder.touchedPipes;
      BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
      visited.set(getSimpleID());
      act(visited, new FloodClearCache());
    }

    if (adjacentChanged) {
      var adjacentRouter = new HashMap<ServerRouter, ExitRoute>();
      var routedexits = EnumSet.noneOf(Direction.class);
      var subpowerexits = new EnumMap<Direction, Integer>(Direction.class);
      for (Map.Entry<CoreRoutedPipe, ExitRoute> pipe : adjacent.entrySet()) {
        adjacentRouter.put((ServerRouter) pipe.getKey().getRouter(), pipe.getValue());
        var connection = pipe.getValue().connectionDetails;
        var exitOrientation = pipe.getValue().exitOrientation;
        if ((connection.contains(PipeRoutingConnectionType.CAN_ROUTE_TO) ||
            connection.contains(PipeRoutingConnectionType.CAN_REQUEST_FROM) &&
                !routedexits.contains(exitOrientation))) {
          routedexits.add(exitOrientation);
        }
        if (!subpowerexits.containsKey(pipe.getValue().exitOrientation) && connection.contains(PipeRoutingConnectionType.CAN_POWER_SUB_SYSTEM_FROM)) {
          subpowerexits.put(exitOrientation,
              PathFinder.messureDistanceToNextRoutedPipe(getLPPosition(), exitOrientation, pipe.getKey().getLevel()));
        }
      }
      this.adjacent = Collections.unmodifiableMap(adjacent);
      this.adjacentRouter_Old = this.adjacentRouter;
      this.adjacentRouter = Collections.unmodifiableMap(adjacentRouter);
      if (power != null) {
        this.powerAdjacent = Collections.unmodifiableList(power);
      } else {
        this.powerAdjacent = null;
      }
      if (subSystemPower != null) {
        this.subSystemPowerAdjacent = Collections.unmodifiableList(subSystemPower);
      } else {
        this.subSystemPowerAdjacent = null;
      }
      this.routedExits = routedexits;
      this.subPowerExits = subpowerexits;
      SendNewLSA();
    }
    return adjacentChanged;
  }

  void updateLsa() {
    //now increment LSA version in the network
    BitSet visited = new BitSet(ServerRouter.getBiggestSimpleID());
    for (ServerRouter r : this.adjacentRouter_Old.keySet()) {
      r.act(visited, new FlagForLSAUpdate());
    }
    this.adjacentRouter_Old = new HashMap<>();
    this.act(visited, new FlagForLSAUpdate());
  }


  @Override
  public void update(boolean doFullRefresh, CoreRoutedPipe pipe) {
    if (connectionNeedsChecking == 2) {
      ensureChangeListenerAttachedToPipe(pipe);

      //final Info info = StackTraceUtil.addTraceInformation(causedBy::toString);
      boolean blockNeedsUpdate = checkAdjacentUpdate();
      if (blockNeedsUpdate) {
        updateLsa();
      }
      //info.end();

      ensureChangeListenerAttachedToPipe(pipe);
    }
    if (connectionNeedsChecking == 1) {
      connectionNeedsChecking = 2;
    }
    handleQueuedTasks(pipe);
    updateInterests();
    if (doFullRefresh) {
      ensureChangeListenerAttachedToPipe(pipe);

      boolean blockNeedsUpdate = checkAdjacentUpdate();
      if (blockNeedsUpdate) {
        //updateAdjacentAndLsa();
        updateLsa();
      }

      ensureChangeListenerAttachedToPipe(pipe);
      lazyUpdateRoutingTable();
    } else if (Configs.MULTI_THREAD_NUMBER > 0) {
      lazyUpdateRoutingTable();
    }
  }

  private void ensureChangeListenerAttachedToPipe(CoreRoutedPipe pipe) {
    if (pipe.container instanceof ILPTEInformation information
        && information.getLPTileEntityObject() != null) {
      if (!information.getLPTileEntityObject().changeListeners.contains(localChangeListener)) {
        information.getLPTileEntityObject().changeListeners.add(localChangeListener);
      }
    }
  }

  private void handleQueuedTasks(CoreRoutedPipe pipe) {
    while (!queue.isEmpty()) {
      var element = queue.poll();
      if (element.getA() > MainProxy.getGlobalTick()) {
        element.getB().call(pipe, this);
      }
    }
  }

  public void updateInterests() {
    if (--ticksUntilNextInventoryCheck > 0) {
      return;
    }
    ticksUntilNextInventoryCheck = ServerRouter.REFRESH_TIME;
    if (ServerRouter.iterated++ % simpleID == 0) {
      ticksUntilNextInventoryCheck++; // randomly wait 1 extra tick - just so that every router doesn't tick at the same time
    }
    if (ServerRouter.iterated >= ServerRouter.getBiggestSimpleID()) {
      ServerRouter.iterated = 0;
    }
    CoreRoutedPipe pipe = getPipe();
    if (pipe == null) {
      return;
    }
    if (pipe.hasGenericInterests()) {
      declareGenericInterest();
    } else {
      removeGenericInterest();
    }
    TreeSet<Item> newInterests = new TreeSet<>();
    pipe.collectSpecificInterests(newInterests);

    interestsRWLock.lock();
    try {
      if (newInterests.size() == interests.size() && newInterests.containsAll(interests)) {
        // interests are up-to-date
        return;
      }

      interests.stream().filter(itemid -> !newInterests.contains(itemid)).forEach(this::removeGlobalInterest);
      newInterests.stream().filter(itemid -> !interests.contains(itemid)).forEach(this::addGlobalInterest);
      interests = newInterests;
    } finally {
      interestsRWLock.unlock();
    }
  }

  /**
   * @param hasBeenProcessed a BitSet flagging which nodes have already been acted on.
   * The router should set the bit for its own id.
   */
  public void act(BitSet hasBeenProcessed, Action actor) {
    if (hasBeenProcessed.get(simpleID)) {
      return;
    }
    hasBeenProcessed.set(simpleID);
    if (!actor.isInteresting(this)) {
      return;
    }

    actor.doTo(this);
    for (ServerRouter r : this.adjacentRouter.keySet()) {
      r.act(hasBeenProcessed, actor);
    }
  }

  private void lazyUpdateRoutingTable() {
    if (LSAVersion > ServerRouter.lastLSAVersion[simpleID]) {
      if (Configs.MULTI_THREAD_NUMBER > 0) {
        RoutingTableUpdateThread.add(new UpdateRouterRunnable(this));
      } else {
        CreateRouteTable(LSAVersion);
      }
    }
  }


  @Override
  public List<List<ExitRoute>> getRouteTable() {
    ensureLatestRoutingTable();
    return this.routeTable;
  }

  @Override
  public boolean isRoutedExit(Direction connection) {
    return this.routedExits.contains(connection);
  }

  @Override
  public boolean isSubPoweredExit(Direction connection) {
    return this.subPowerExits.containsKey(connection);
  }

  @Override
  public boolean hasRoute(int id, boolean active, Item item) {
    if (!SimpleServiceLocator.routerManager.isRouterUnsafe(id, false, this.level)) {
      return false;
    }
    this.ensureLatestRoutingTable();
    if (this.getRouteTable().size() <= id) {
      return false;
    }
    List<ExitRoute> source = this.getRouteTable().get(id);
    if (source == null) {
      return false;
    }

    outer:
    for (ExitRoute exit : source) {
      if (exit.containsFlag(PipeRoutingConnectionType.CAN_ROUTE_TO)) {
        for (IFilter filter : exit.filters) {
          if (!active) {
            if (filter.blockRouting() || filter.isBlocked() == filter.isFilteredItem(item)) {
              continue outer;
            }
          } else {
            if ((filter.blockProvider() && filter.blockCrafting()) || filter.isBlocked() == filter.isFilteredItem(item)) {
              continue outer;
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public ExitRoute getExitFor(int id, boolean active, Item type) {
    ensureLatestRoutingTable();
    if (getRouteTable().size() <= id || getRouteTable().get(id) == null) {
      return null;
    }
    outer:
    for (ExitRoute exit : getRouteTable().get(id)) {
      if (exit.containsFlag(PipeRoutingConnectionType.CAN_ROUTE_TO)) {
        for (IFilter filter : exit.filters) {
          if (!active) {
            if (filter.blockRouting() || filter.isBlocked() == filter.isFilteredItem(type)) {
              continue outer;
            }
          } else {
            if ((filter.blockProvider() && filter.blockCrafting()) || filter.isBlocked() == filter.isFilteredItem(type)) {
              continue outer;
            }
          }
        }
        return exit;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private void declareGenericInterest() {
    genericInterestsWLock.lock();
    try {
      final TreeSet<ServerRouter> newGenericInterests = (TreeSet<ServerRouter>) ServerRouter.genericInterests.clone();
      if (newGenericInterests.add(this)) {
        ServerRouter.genericInterests = newGenericInterests;
      }
    } finally {
      genericInterestsWLock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  private void addGlobalInterest(Item itemid) {
    ServerRouter.globalSpecificInterests.compute(itemid, (unused, serverRouters) -> {
      final TreeSet<ServerRouter> newServerRouters = serverRouters == null ? new TreeSet<>() : (TreeSet<ServerRouter>) serverRouters.clone();
      newServerRouters.add(this);
      return newServerRouters;
    });
  }


  private void removeAllInterests() {
    this.removeGenericInterest();

    this.interestsRWLock.lock();
    try {
      this.interests.forEach(this::removeGlobalInterest);
      this.interests.clear();
    } finally {
      this.interestsRWLock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  private void removeGenericInterest() {
    ServerRouter.genericInterestsWLock.lock();
    try {
      final TreeSet<ServerRouter> newGenericInterests = (TreeSet<ServerRouter>) ServerRouter.genericInterests.clone();
      if (newGenericInterests.remove(this)) {
        ServerRouter.genericInterests = newGenericInterests;
      }
    } finally {
      ServerRouter.genericInterestsWLock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  private void removeGlobalInterest(Item item) {
    ServerRouter.globalSpecificInterests.computeIfPresent(item, (unused, serverRouters) -> {
      if (serverRouters.equals(ObjectSets.singleton(this))) {
        return null;
      } else {
        final var newServerRouters = (TreeSet<ServerRouter>) serverRouters.clone();
        newServerRouters.remove(this);
        return newServerRouters;
      }
    });
  }

  @Override
  public int compareTo(ServerRouter o) {
    return simpleID - o.simpleID;
  }

  @Override
  public List<ExitRoute> getDistanceTo(IRouter r) {
    ensureLatestRoutingTable();
    int id = r.getSimpleID();
    if (this.routeTable.size() <= id) {
      return new ArrayList<>(0);
    }
    List<ExitRoute> result = this.routeTable.get(id);
    return result != null ? result : new ArrayList<>(0);
  }

  @Override
  public void clearInterests() {
    this.removeAllInterests();
  }

  private void checkSecurity(HashMap<CoreRoutedPipe, ExitRoute> adjacent) {
    CoreRoutedPipe pipe = getPipe();
    if (pipe == null) {
      return;
    }
    UUID id = pipe.getSecurityID();
    if (id != null) {
      for (Map.Entry<CoreRoutedPipe, ExitRoute> entry : adjacent.entrySet()) {
        if (!entry.getValue().connectionDetails.contains(PipeRoutingConnectionType.CAN_ROUTE_TO) &&
            !entry.getValue().connectionDetails.contains(PipeRoutingConnectionType.CAN_REQUEST_FROM)) {
          continue;
        }
        UUID thatId = entry.getKey().getSecurityID();
        /*if (!(pipe instanceof PipeItemsFirewall)) {
          if (thatId == null) {
            entry.getKey().insetSecurityID(id);
          } else if (!id.equals(thatId)) {
            sideDisconnected[entry.getValue().exitOrientation.ordinal()] = true;
          }
        } else {
          if (!(entry.getKey() instanceof PipeItemsFirewall)) {
            if (thatId != null && !id.equals(thatId)) {
              sideDisconnected[entry.getValue().exitOrientation.ordinal()] = true;
            }
          }
        }*/
      }
      List<CoreRoutedPipe> toRemove = adjacent.entrySet().stream()
          .filter(entry -> sideDisconnected[entry.getValue().exitOrientation.ordinal()])
          .map(Map.Entry::getKey)
          .toList();
      toRemove.forEach(adjacent::remove);
    }
  }

  private void SendNewLSA() {
    HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric = new HashMap<>();
    for (var adjacent : this.adjacentRouter.entrySet()) {
      var value = adjacent.getValue();
      neighboursWithMetric.put(adjacent.getKey(), new Quartet<>(value.distanceToDestination,
          value.connectionDetails, value.filters, value.blockDistance));
    }
    ArrayList<Tuple<ILogisticsPowerProvider, List<IFilter>>> power = null;
    if (this.powerAdjacent != null) {
      power = new ArrayList<>(this.powerAdjacent);
    }
    ArrayList<Tuple<ISubSystemPowerProvider, List<IFilter>>> subSystemPower = null;
    if (this.subSystemPowerAdjacent != null) {
      subSystemPower = new ArrayList<>(this.subSystemPowerAdjacent);
    }
    if (Configs.MULTI_THREAD_NUMBER > 0) {
      RoutingTableUpdateThread.add(new LSARouterRunnable(neighboursWithMetric, power, subSystemPower));
    } else {
      lockAndUpdateLSA(neighboursWithMetric, power, subSystemPower);
    }
  }

  private void lockAndUpdateLSA(
      @Nullable HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric,
      @Nullable ArrayList<Tuple<ILogisticsPowerProvider, List<IFilter>>> power,
      @Nullable ArrayList<Tuple<ISubSystemPowerProvider, List<IFilter>>> subSystemPower) {
    ServerRouter.SharedLSADatabasewriteLock.lock();
    this.myLsa.neighboursWithMetric = neighboursWithMetric;
    this.myLsa.power = power;
    this.myLsa.subSystemPower = subSystemPower;
    ServerRouter.SharedLSADatabasewriteLock.unlock();
  }


  public void CreateRouteTable(int version_to_update_to) {
    CreateRouteTable(version_to_update_to, new DummyRoutingDebugAdapter());
  }

  /**
   * Create a route table from the link state database
   */
  public void CreateRouteTable(int version_to_update_to, IRoutingDebugAdapter debug) {

    if (ServerRouter.lastLSAVersion[simpleID] >= version_to_update_to && !debug.independent()) {
      return; // this update is already done.
    }

    //Dijkstra!

    debug.init();

    int routingTableSize = ServerRouter.getBiggestSimpleID();
    if (routingTableSize == 0) {
      routingTableSize = ServerRouter.SharedLSADatabase.length; // deliberatly ignoring concurrent access, either the old or the version of the size will work, this is just an approximate number.
    }

    /**
     * same info as above, but sorted by distance -- sorting is implicit,
     * because Dijkstra finds the closest routes first.
     **/
    List<ExitRoute> routeCosts = new ArrayList<>(routingTableSize);

    //Add the current Router
    routeCosts.add(new ExitRoute(this, this, null, null, 0, EnumSet.allOf(PipeRoutingConnectionType.class), 0));

    ArrayList<Tuple<ILogisticsPowerProvider, List<IFilter>>> powerTable;
    if (powerAdjacent != null) {
      powerTable = new ArrayList<>(powerAdjacent);
    } else {
      powerTable = new ArrayList<>(5);
    }
    ArrayList<Tuple<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
    if (subSystemPowerAdjacent != null) {
      subSystemPower = new ArrayList<>(subSystemPowerAdjacent);
    } else {
      subSystemPower = new ArrayList<>(5);
    }

    //space and time inefficient, a bitset with 3 bits per node would save a lot but makes the main iteration look like a complete mess
    var closedSet =
        new ArrayList<EnumSet<PipeRoutingConnectionType>>(ServerRouter.getBiggestSimpleID());
    for (int i = 0; i < ServerRouter.getBiggestSimpleID(); i++) {
      closedSet.add(null);
    }

    var filterList =
        new ArrayList<EnumMap<PipeRoutingConnectionType, List<List<IFilter>>>>(ServerRouter.getBiggestSimpleID());
    for (int i = 0; i < ServerRouter.getBiggestSimpleID(); i++) {
      filterList.add(null);
    }

    /** The total cost for the candidate route **/
    PriorityQueue<ExitRoute> candidatesCost = new PriorityQueue<>((int) Math.sqrt(routingTableSize)); // sqrt nodes is a good guess for the total number of candidate nodes at once.

    //Init candidates
    // the shortest way to go to an adjacent item is the adjacent item.
    for (Map.Entry<ServerRouter, ExitRoute> pipe : adjacentRouter.entrySet()) {
      ExitRoute currentE = pipe.getValue();
      IRouter newRouter = pipe.getKey();
      if (newRouter != null) {
        ExitRoute newER = new ExitRoute(newRouter, newRouter, currentE.distanceToDestination, currentE.connectionDetails, currentE.filters, new ArrayList<>(0), currentE.blockDistance);
        candidatesCost.add(newER);
        debug.newCandidate(newER);
      }
    }

    debug.start(candidatesCost, closedSet, filterList);

    ServerRouter.SharedLSADatabasereadLock.lock(); // readlock, not inside the while - too costly to aquire, then release.
    ExitRoute lowestCostNode;
    while ((lowestCostNode = candidatesCost.poll()) != null) {
      if (!lowestCostNode.hasActivePipe()) {
        continue;
      }

      if (debug.isDebug()) {
        ServerRouter.SharedLSADatabasereadLock.unlock();
      }
      debug.nextPipe(lowestCostNode);
      if (debug.isDebug()) {
        ServerRouter.SharedLSADatabasereadLock.lock();
      }

      for (ExitRoute e : candidatesCost) {
        e.debug.isNewlyAddedCandidate = false;
      }

      //if the node does not have any flags not in the closed set, check it
      var lowestCostClosedFlags = closedSet.get(lowestCostNode.destination.getSimpleID());
      if (lowestCostClosedFlags == null) {
        lowestCostClosedFlags = EnumSet.noneOf(PipeRoutingConnectionType.class);
      }
      if (lowestCostClosedFlags.containsAll(lowestCostNode.getFlagsNoCopy())) {
        continue;
      }

      if (debug.isDebug()) {
        EnumSet<PipeRoutingConnectionType> newFlags = lowestCostNode.getFlags();
        newFlags.removeAll(lowestCostClosedFlags);

        debug.newFlagsForPipe(newFlags);
      }

      var filters = filterList.get(lowestCostNode.destination.getSimpleID());

      debug.filterList(filters);

      if (filters != null) {
        boolean containsNewInfo = false;
        for (PipeRoutingConnectionType type : lowestCostNode.getFlagsNoCopy()) {
          if (lowestCostClosedFlags.contains(type)) {
            continue;
          }
          if (!filters.containsKey(type)) {
            containsNewInfo = true;
            break;
          }
          boolean matches = false;
          List<List<IFilter>> list = filters.get(type);
          for (List<IFilter> filter : list) {
            if (lowestCostNode.filters.containsAll(filter)) {
              matches = true;
              break;
            }
          }
          if (!matches) {
            containsNewInfo = true;
            break;
          }
        }
        if (!containsNewInfo) {
          continue;
        }
      }

      //Add new candidates from the newly approved route
      LSA lsa = null;
      if (lowestCostNode.destination.getSimpleID() < ServerRouter.SharedLSADatabase.length) {
        lsa = ServerRouter.SharedLSADatabase[lowestCostNode.destination.getSimpleID()];
      }
      if (lsa == null) {
        lowestCostNode.removeFlags(lowestCostClosedFlags);
        lowestCostClosedFlags.addAll(lowestCostNode.getFlagsNoCopy());
        if (lowestCostNode.containsFlag(PipeRoutingConnectionType.CAN_ROUTE_TO) || lowestCostNode.containsFlag(PipeRoutingConnectionType.CAN_REQUEST_FROM)) {
          routeCosts.add(lowestCostNode);
        }
        closedSet.set(lowestCostNode.destination.getSimpleID(), lowestCostClosedFlags);
        continue;
      }
      if (lowestCostNode.containsFlag(PipeRoutingConnectionType.CAN_POWER_FROM)) {
        if (lsa.power != null && !lsa.power.isEmpty()) {
          for (Tuple<ILogisticsPowerProvider, List<IFilter>> p : lsa.power) {
            var entry = new Tuple<>(p.getA(), p.getB());
            List<IFilter> list = new ArrayList<>();
            list.addAll(p.getB());
            list.addAll(lowestCostNode.filters);
            entry.setB(Collections.unmodifiableList(list));
            if (!powerTable.contains(entry)) {
              powerTable.add(entry);
            }
          }
        }
      }
      if (lowestCostNode.containsFlag(PipeRoutingConnectionType.CAN_POWER_SUB_SYSTEM_FROM)) {
        if (lsa.subSystemPower != null && !lsa.subSystemPower.isEmpty()) {
          for (Tuple<ISubSystemPowerProvider, List<IFilter>> p : lsa.subSystemPower) {
            var entry = new Tuple<>(p.getA(), p.getB());
            List<IFilter> list = new ArrayList<>();
            list.addAll(p.getB());
            list.addAll(lowestCostNode.filters);
            entry.setB(Collections.unmodifiableList(list));
            if (!subSystemPower.contains(entry)) {
              subSystemPower.add(entry);
            }
          }
        }
      }
      for (Map.Entry<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> newCandidate : lsa.neighboursWithMetric.entrySet()) {
        double candidateCost = lowestCostNode.distanceToDestination + newCandidate.getValue().getA();
        int blockDistance = lowestCostNode.blockDistance + newCandidate.getValue().getD();
        EnumSet<PipeRoutingConnectionType> newCT = lowestCostNode.getFlags();
        newCT.retainAll(newCandidate.getValue().getB());
        if (!newCT.isEmpty()) {
          ExitRoute next = new ExitRoute(lowestCostNode.root, newCandidate.getKey(),
              candidateCost, newCT, lowestCostNode.filters, newCandidate.getValue().getC(), blockDistance);
          next.debug.isTraced = lowestCostNode.debug.isTraced;
          candidatesCost.add(next);
          debug.newCandidate(next);
        }
      }

      lowestCostClosedFlags = lowestCostClosedFlags.clone();

      lowestCostNode.removeFlags(lowestCostClosedFlags);
      lowestCostClosedFlags.addAll(lowestCostNode.getFlagsNoCopy());
      if (lowestCostNode.containsFlag(PipeRoutingConnectionType.CAN_ROUTE_TO) || lowestCostNode.containsFlag(PipeRoutingConnectionType.CAN_REQUEST_FROM) || lowestCostNode.containsFlag(PipeRoutingConnectionType.CAN_POWER_SUB_SYSTEM_FROM)) {
        routeCosts.add(lowestCostNode);
      }
      EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> map = filterList.get(lowestCostNode.destination.getSimpleID());
      if (map == null) {
        map = new EnumMap<>(PipeRoutingConnectionType.class);
        filterList.set(lowestCostNode.destination.getSimpleID(), map);
      }
      for (PipeRoutingConnectionType type : lowestCostNode.getFlagsNoCopy()) {
        if (!map.containsKey(type)) {
          map.put(type, new ArrayList<>());
        }
        map.get(type).add(Collections.unmodifiableList(new ArrayList<>(lowestCostNode.filters)));
      }
      if (lowestCostNode.filters.isEmpty()) {
        closedSet.set(lowestCostNode.destination.getSimpleID(), lowestCostClosedFlags);
      }

      if (debug.isDebug()) {
        ServerRouter.SharedLSADatabasereadLock.unlock();
      }
      debug.handledPipe();
      if (debug.isDebug()) {
        ServerRouter.SharedLSADatabasereadLock.lock();
      }
    }
    ServerRouter.SharedLSADatabasereadLock.unlock();

    debug.stepOneDone();

    //Build route table
    var routeTable = new ArrayList<List<ExitRoute>>(ServerRouter.getBiggestSimpleID() + 1);
    while (simpleID >= routeTable.size()) {
      routeTable.add(null);
    }
    routeTable.set(simpleID, new OneList<>(new ExitRoute(this, this, null, null, 0, EnumSet
        .allOf(PipeRoutingConnectionType.class), 0)));

    for (ExitRoute node : routeCosts) {
      IRouter firstHop = node.root;
      ExitRoute hop = adjacentRouter.get(firstHop);
      if (hop == null) {
        continue;
      }
      node.root = this; // replace the root with this, rather than the first hop.
      node.exitOrientation = hop.exitOrientation;
      while (node.destination.getSimpleID() >= routeTable
          .size()) { // the array will not expand, as it is init'd to contain enough elements
        routeTable.add(null);
      }

      List<ExitRoute> current = routeTable.get(node.destination.getSimpleID());
      if (current != null && !current.isEmpty()) {
        List<ExitRoute> list = new ArrayList<>(current);
        list.add(node);
        routeTable.set(node.destination.getSimpleID(), Collections.unmodifiableList(list));
      } else {
        routeTable.set(node.destination.getSimpleID(), new OneList<>(node));
      }
    }
    debug.stepTwoDone();
    if (!debug.independent()) {
      routingTableUpdateWriteLock.lock();
      if (version_to_update_to == LSAVersion) {
        ServerRouter.SharedLSADatabasereadLock.lock();

        if (ServerRouter.lastLSAVersion[simpleID] < version_to_update_to) {
          ServerRouter.lastLSAVersion[simpleID] = version_to_update_to;
          this.LPPowerTable = Collections.unmodifiableList(powerTable);
          this.SubSystemPowerTable = Collections.unmodifiableList(subSystemPower);
          this.routeTable = Collections.unmodifiableList(routeTable);
          this.routeCosts = Collections.unmodifiableList(routeCosts);
        }
        ServerRouter.SharedLSADatabasereadLock.unlock();
      }
      routingTableUpdateWriteLock.unlock();
    }
    if (getCachedPipe() != null) {
      System.out.println("Spawning particles");
      //getCachedPipe().spawnParticle(Particles.LightGreenParticle, 5);
    }

    debug.done();
  }

  public void flagForRoutingUpdate() {
    this.LSAVersion++;
    //if(LogisticsPipes.DEBUG)
    //System.out.println("[LogisticsPipes] targeted for routing update to "+_LSAVersion+" for Node" +  simpleID);
  }


  protected static class LSA {

    @Nullable
    public HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric;
    @Nullable
    public List<Tuple<ILogisticsPowerProvider, List<IFilter>>> power;
    @Nullable
    public ArrayList<Tuple<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;
  }


  public interface Action {
    boolean isInteresting(ServerRouter router);

    void doTo(ServerRouter router);
  }

  /**
   * Floodfill LSA increment and clean up the _prevAdjacentRouter list left by
   * floodCheckAdjacent
   */
  static class FlagForLSAUpdate implements Action {

    public boolean isInteresting(ServerRouter router) {
      return true;
    }

    public void doTo(ServerRouter router) {
      router.flagForRoutingUpdate();
    }
  }

  static class FloodClearCache implements Action {

    public boolean isInteresting(ServerRouter router) {
      return true;
    }

    public void doTo(ServerRouter router) {
      CacheHolder.clearCache(router.oldTouchedPipes);
    }
  }

  private abstract static class RouterRunnable implements Comparable<RouterRunnable>, Runnable {

    public abstract int getPrority();

    public abstract int localCompare(RouterRunnable o);

    @Override
    public int compareTo(RouterRunnable o) {
      if (o.getPrority() == getPrority()) {
        return localCompare(o);
      }
      return o.getPrority() - getPrority();
    }
  }

  private class UpdateRouterRunnable extends RouterRunnable {

    int newVersion;
    boolean run;
    IRouter target;

    UpdateRouterRunnable(IRouter target) {
      run = true;
      newVersion = LSAVersion;
      this.target = target;
    }

    @Override
    public void run() {
      if (!run) {
        return;
      }
      try {
        CoreRoutedPipe p = target.getCachedPipe();
        if (p == null) {
          run = false;
          return;
        }
        //spinlock during the first tick, we can't touch the routing table, untill Update() has been called on every pipe.
        for (int i = 0; i < 10 && p.isStillNeedReplace(); i++) {
          Thread.sleep(10);
        }
        if (p.isStillNeedReplace()) {
          return; // drop the pipe update if it still needs replace after 5 ticks.
        }
        CreateRouteTable(newVersion);
      } catch (Exception e) {
        LogisticsPipes.LOG.error(e.getMessage(), e);
      }
      run = false;
    }

    @Override
    public int getPrority() {
      return 0;
    }

    @Override
    public int localCompare(RouterRunnable o) {
      int c = 0;
      if (((UpdateRouterRunnable) o).newVersion <= 0) {
        c = newVersion - ((UpdateRouterRunnable) o).newVersion; // negative numbers have priority, more negative first
      }
      if (c != 0) {
        return 0;
      }
      c = target.getSimpleID() - ((UpdateRouterRunnable) o).target.getSimpleID(); // do things in order of router id, to minimize router recursion
      if (c != 0) {
        return 0;
      }
      c = ((UpdateRouterRunnable) o).newVersion - newVersion; // higher version first
      return c;
    }
  }

  private class LSARouterRunnable extends RouterRunnable {

    private final int index = ServerRouter.maxLSAUpdateIndex++;
    @Nullable
    HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric;
    @Nullable
    ArrayList<Tuple<ILogisticsPowerProvider, List<IFilter>>> power;
    @Nullable
    ArrayList<Tuple<ISubSystemPowerProvider, List<IFilter>>> subSystemPower;

    LSARouterRunnable(
        @Nullable HashMap<IRouter, Quartet<Double, EnumSet<PipeRoutingConnectionType>, List<IFilter>, Integer>> neighboursWithMetric,
        @Nullable ArrayList<Tuple<ILogisticsPowerProvider, List<IFilter>>> power,
        @Nullable ArrayList<Tuple<ISubSystemPowerProvider, List<IFilter>>> subSystemPower) {
      this.neighboursWithMetric = neighboursWithMetric;
      this.power = power;
      this.subSystemPower = subSystemPower;
    }

    @Override
    public void run() {
      lockAndUpdateLSA(neighboursWithMetric, power, subSystemPower);
    }

    @Override
    public int getPrority() {
      return 1;
    }

    @Override
    public int localCompare(RouterRunnable o) {
      return index - ((LSARouterRunnable) o).index;
    }
  }

}
