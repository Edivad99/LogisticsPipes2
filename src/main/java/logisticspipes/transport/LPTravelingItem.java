package logisticspipes.transport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.order.IDistanceTracker;
import logisticspipes.utils.CoordinateUtils;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.utils.EnumFacingUtil;
import logisticspipes.utils.SlidingWindowBitSet;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class LPTravelingItem {

  public static final Map<Integer, WeakReference<LPTravelingItemServer>> SERVER_LIST = new HashMap<>();
  public static final Map<Integer, WeakReference<LPTravelingItemClient>> CLIENT_LIST = new HashMap<>();
  public static final List<Tuple<Integer, Object>> FORCE_KEEP = new ArrayList<>();
  public static final SlidingWindowBitSet CLIENT_SIDE_KNOWN_IDS = new SlidingWindowBitSet(20);

  private static int nextFreeId = 0;
  @Getter
  protected int id;
  @Getter
  @Setter
  protected float speed = 0.01F;
  public int lastTicked = 0;
  @Getter
  @Setter
  @Nullable
  protected BlockEntity container;
  @Getter
  @Setter
  protected float position = 0;
  @Getter
  protected float yaw = 0;
  @Nullable
  public Direction input = null;
  @Nullable
  public Direction output = null;
  public final EnumSet<Direction> blacklist = EnumSet.noneOf(Direction.class);

  public LPTravelingItem() {
    this.id = getNextId();
  }

  public LPTravelingItem(int id) {
    this.id = id;
  }

  public LPTravelingItem(int id, float position,
      @Nullable Direction input, @Nullable Direction output, float yaw) {
    this.id = id;
    this.position = position;
    this.input = input;
    this.output = output;
    this.yaw = yaw;
  }

  protected int getNextId() {
    return ++LPTravelingItem.nextFreeId;
  }

  public void setYaw(float yaw) {
    this.yaw = yaw % 360;
  }

  @Nullable
  public abstract ItemStack getItemIdentifierStack();

  public boolean isCorrupted() {
    return this.getItemIdentifierStack() == null || this.getItemIdentifierStack().isEmpty();
  }

  public int getAge() {
    return 0;
  }

  public void addAge() {
  }

  public float getHoverStart() {
    return 0;
  }

  public abstract LPTravelingItem renderCopy();

  public static final class LPTravelingItemClient extends LPTravelingItem {

    @Setter
    private ItemStack item;
    private int age;
    private float hoverStart = (float) (Math.random() * Math.PI * 2.0D); //TODO: Useless?

    public LPTravelingItemClient(int id, float position,
        @Nullable Direction input, @Nullable Direction output, float yaw) {
      super(id, position, input, output, yaw);
    }

    public LPTravelingItemClient(int id, ItemStack item) {
      super(id);
      this.item = item;
    }

    @Override
    public ItemStack getItemIdentifierStack() {
      return this.item;
    }

    public void updateInformation(Direction input, Direction output, float speed, float position, float yaw) {
      this.input = input;
      this.output = output;
      this.speed = speed;
      this.position = position;
      this.yaw = yaw;
    }

    @Override
    public int getAge() {
      return 0;//age;
    }

    @Override
    public void addAge() {
      this.age++;
    }

    @Override
    public float getHoverStart() {
      return 0;//hoverStart;
    }

    @Override
    public LPTravelingItem renderCopy() {
      var copy = new LPTravelingItemClient(id, position, input, output, yaw);
      copy.speed = speed;
      copy.hoverStart = hoverStart;
      copy.item = item.copy();
      copy.age = age;
      copy.container = container;
      return copy;
    }
  }

  @Getter
  @Setter
  public static final class LPTravelingItemServer extends LPTravelingItem implements IRoutedItem {

    private ItemRoutingInformation info;

    public LPTravelingItemServer(ItemStack stack) {
      super();
      this.info = new ItemRoutingInformation();
      this.info.setItem(stack);
    }

    public LPTravelingItemServer(ItemRoutingInformation info) {
      super();
      this.info = info;
    }

    public LPTravelingItemServer(HolderLookup.Provider provider, CompoundTag tag) {
      super();
      this.info = new ItemRoutingInformation();
      this.readFromNBT(provider, tag);
    }

    @Override
    public ItemStack getItemIdentifierStack() {
      return this.info.getItem();
    }

    public void setInformation(ItemRoutingInformation info) {
      this.info = info;
    }

    @Override
    public void readFromNBT(HolderLookup.Provider provider, CompoundTag tag) {
      setPosition(tag.getFloat("position"));
      setSpeed(tag.getFloat("speed"));
      if (tag.contains("input")) {
        this.input = EnumFacingUtil.getOrientation(tag.getInt("input"));
      } else {
        this.input = null;
      }
      if (tag.contains("output")) {
        this.output = EnumFacingUtil.getOrientation(tag.getInt("output"));
      } else {
        this.output = null;
      }
      this.info.readFromNBT(provider, tag);
    }

    @Override
    public void writeToNBT(HolderLookup.Provider provider, CompoundTag data) {
      data.putFloat("position", getPosition());
      data.putFloat("speed", getSpeed());
      if (input != null) {
        data.putInt("input", input.ordinal());
      }
      if (output != null) {
        data.putInt("output", output.ordinal());
      }
      this.info.writeToNBT(provider, data);
    }

    public void newId() {
      this.id = this.getNextId();
    }

    @Override
    public LPTravelingItem renderCopy() {
      throw new UnsupportedOperationException();
    }

    @Nullable
    public ItemEntity toEntityItem() {
      Level level = this.container.getLevel();
      if (!level.isClientSide) {
        if (getItemIdentifierStack().isEmpty()) {
          return null;
        }

        /*if (getItemIdentifierStack().getItem() instanceof LogisticsFluidContainer) {
          itemWasLost();
          return null;
        }*/

        Direction exitdirection = output;
        if (exitdirection == null) {
          exitdirection = input;
        }

        var position = new DoubleCoordinates(container)
            .add(new DoubleCoordinates(0.5, 0.375, 0.5));

        switch (exitdirection) {
          case DOWN:
            CoordinateUtils.add(position, exitdirection, 0.5);
            break;
          case UP:
            CoordinateUtils.add(position, exitdirection, 0.75);
            break;
          case NORTH:
          case SOUTH:
          case WEST:
          case EAST:
            CoordinateUtils.add(position, exitdirection, 0.625);
            break;
          case null, default:
            break;
        }

        DoubleCoordinates motion = new DoubleCoordinates(0, 0, 0);
        CoordinateUtils.add(motion, exitdirection, getSpeed() * 2.0);

        var itemEntity = new ItemEntity(level, position.getXCoord(), position.getYCoord(),
            position.getZCoord(), getItemIdentifierStack());

        //entityitem.lifespan = 1200;
        //entityitem.delayBeforeCanPickup = 10;

        //uniformly distributed in -0.005 .. 0.01 to increase bias toward smaller values
        float f3 = level.random.nextFloat() * 0.015F - 0.005F;
        itemEntity.setDeltaMovement(
            level.random.nextGaussian() * f3 + motion.getXCoord(),
            level.random.nextGaussian() * f3 + motion.getYCoord(),
            level.random.nextGaussian() * f3 + motion.getZCoord());
        itemWasLost();

        return itemEntity;
      } else {
        return null;
      }
    }

    @Override
    public void clearDestination() {
      if (info.destinationint >= 0) {
        itemWasLost();
        info.jamlist.add(info.destinationint);
      }
      //keep buffercounter and jamlist
      info.destinationint = -1;
      info.destinationUUID = null;
      info._doNotBuffer = false;
      info.arrived = false;
      info._transportMode = TransportMode.UNKNOWN;
      info.targetInfo = null;
    }

    public void itemWasLost() {
      if (container != null) {
        if (this.container.getLevel().isClientSide) {
          return;
        }
      }
      IRouter destinationRouter = SimpleServiceLocator.routerManager
          .getRouter(this.container.getLevel(), info.destinationint);
      if (destinationRouter != null) {
        var pipe = destinationRouter.getPipe();
        if (pipe != null) {
          pipe.notifyOfReroute(info);
          if (pipe instanceof IRequireReliableTransport transport) {
            transport.itemLost(info.getItem(), info.targetInfo);
          }
          /*if (pipe instanceof IRequireReliableFluidTransport requireReliableTransport) {
            if (info.getItem().getItem().isFluidContainer()) {
              FluidIdentifierStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(info.getItem());
              requireReliableTransport.liquidLost(liquid.getFluid(), liquid.getAmount());
            }
          }*/
        }
      }
    }

    @Override
    public int getDestination() {
      return this.info.destinationint;
    }

    @Override
    public void setDestination(int destination) {
      this.info.destinationint = destination;
      final Level level = container != null ? container.getLevel() : null;
      if (!level.isClientSide) {
        IRouter router = SimpleServiceLocator.routerManager.getServerRouter(destination);
        if (router != null) {
          this.info.destinationUUID = router.getId();
        } else {
          this.info.destinationUUID = null;
        }
      } else {
        this.info.destinationUUID = null;
      }
    }

    @Override
    public void setDoNotBuffer(boolean isBuffered) {
      this.info._doNotBuffer = isBuffered;
    }

    @Override
    public boolean getDoNotBuffer() {
      return this.info._doNotBuffer;
    }

    @Override
    public void setArrived(boolean flag) {
      this.info.arrived = flag;
    }

    @Override
    public boolean getArrived() {
      return this.info.arrived;
    }

    @Override
    public void split(int itemsToTake, Direction orientation) {
      /*if (getItemIdentifierStack().getItem().isFluidContainer()) {
        throw new UnsupportedOperationException("Can't split up a FluidContainer");
      }*/
      ItemStack stackToKeep = this.getItemIdentifierStack();
      ItemStack stackToSend = stackToKeep.copy();
      stackToKeep.setCount(itemsToTake);
      stackToSend.setCount(stackToSend.getCount() - itemsToTake);

      this.newId();

      LPTravelingItemServer newItem = new LPTravelingItemServer(stackToSend);
      newItem.setSpeed(this.getSpeed());
      newItem.setTransportMode(this.getTransportMode());

      newItem.setDestination(this.getDestination());
      newItem.clearDestination();

      if (this.container instanceof LogisticsGenericPipeBlockEntity<?> genericPipeBlockEntity) {
        genericPipeBlockEntity.pipe.getTransport().injectItem(newItem, orientation);
      }
    }

    @Override
    public void setTransportMode(TransportMode transportMode) {
      this.info._transportMode = transportMode;
    }

    @Override
    public TransportMode getTransportMode() {
      return this.info._transportMode;
    }

    @Override
    public void addToJamList(IRouter router) {
      this.info.jamlist.add(router.getSimpleID());
    }

    @Override
    public List<Integer> getJamList() {
      return this.info.jamlist;
    }

    @Override
    public int getBufferCounter() {
      return this.info.bufferCounter;
    }

    @Override
    public void setBufferCounter(int counter) {
      this.info.bufferCounter = counter;
    }

    @Override
    public UUID getDestinationUUID() {
      return this.info.destinationUUID;
    }

    @Override
    public void checkIDFromUUID() {
      IRouter router = SimpleServiceLocator.routerManager
          .getRouter(this.container.getLevel(), this.info.destinationint);
      if (router == null || info.destinationUUID != router.getId()) {
        this.info.destinationint = SimpleServiceLocator.routerManager.getIDForUUID(info.destinationUUID);
      }
    }

    @Override
    public void setDistanceTracker(IDistanceTracker tracker) {
      this.info.tracker = tracker;
    }

    @Override
    public IDistanceTracker getDistanceTracker() {
      return this.info.tracker;
    }

    public void resetDelay() {
      this.info.resetDelay();
    }

    @Override
    public void setAdditionalTargetInformation(IAdditionalTargetInformation targetInfo) {
      this.info.targetInfo = targetInfo;
    }

    @Override
    public IAdditionalTargetInformation getAdditionalTargetInformation() {
      return this.info.targetInfo;
    }
  }
}
