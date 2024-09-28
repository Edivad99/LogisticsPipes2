package logisticspipes.logisticspipes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.tuples.Triplet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LogisticsManager implements ILogisticsManager {

  @Override
  public IRoutedItem assignDestinationFor(IRoutedItem item, int sourceRouterId, boolean excludeSource) {
    //Assert: only called server side.

    //If we for some reason can't get the router we can't do anything either
    IRouter sourceRouter = SimpleServiceLocator.routerManager.getServerRouter(sourceRouterId);
    if (sourceRouter == null) {
      return item;
    }

    //Wipe current destination
    item.clearDestination();

    final ItemStack itemIdStack = item.getItemIdentifierStack();
    if (itemIdStack == null) {
      return item;
    }
    BitSet routersIndex = ServerRouter.getRoutersInterestedIn(itemIdStack.getItem());
    List<ExitRoute> validDestinations = new ArrayList<>(); // get the routing table
    for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i + 1)) {
      IRouter r = SimpleServiceLocator.routerManager.getServerRouter(i);
      List<ExitRoute> exits = sourceRouter.getDistanceTo(r);
      if (exits != null) {
        validDestinations.addAll(exits.stream()
            .filter(e -> e.containsFlag(PipeRoutingConnectionType.CAN_ROUTE_TO))
            .toList());
      }
    }

    Collections.sort(validDestinations);
    final ItemStack stack = itemIdStack.copy();
    /*if (stack.getItem() instanceof LogisticsFluidContainer) {
      Pair<Integer, FluidSinkReply> bestReply = SimpleServiceLocator.logisticsFluidManager.getBestReply(SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(itemIdStack), sourceRouter, item.getJamList());
      if (bestReply != null) {
        item.setDestination(bestReply.getValue1());
      }
    } else {*/
      Triplet<Integer, SinkReply, List<IFilter>> bestReply =
          getBestReply(stack, itemIdStack.getItem(), sourceRouter, validDestinations, excludeSource, item.getJamList(), null, true);
      if (bestReply.getA() != null && bestReply.getA() != 0) {
        item.setDestination(bestReply.getA());
        if (bestReply.getB().isPassive) {
          if (bestReply.getB().isDefault) {
            item.setTransportMode(IRoutedItem.TransportMode.DEFAULT);
          } else {
            item.setTransportMode(IRoutedItem.TransportMode.PASSIVE);
          }
        } else {
          item.setTransportMode(IRoutedItem.TransportMode.ACTIVE);
        }
        item.setAdditionalTargetInformation(bestReply.getB().addInfo);
      }
    //}
    return item;
  }

  private Triplet<Integer, SinkReply, List<IFilter>> getBestReply(ItemStack stack,
      Item item, IRouter sourceRouter, List<ExitRoute> validDestinations,
      boolean excludeSource, List<Integer> jamList, @Nullable Triplet<Integer, SinkReply, List<IFilter>> result,
      boolean allowDefault) {
    if (result == null) {
      result = new Triplet<>(null, null, null);
    }

    outer:
    for (ExitRoute candidateRouter : validDestinations) {
      if (excludeSource) {
        if (candidateRouter.destination.getId().equals(sourceRouter.getId())) {
          continue;
        }
      }
      if (jamList.contains(candidateRouter.destination.getSimpleID())) {
        continue;
      }

      if (!candidateRouter.containsFlag(PipeRoutingConnectionType.CAN_ROUTE_TO)) {
        continue;
      }

      for (IFilter filter : candidateRouter.filters) {
        if (filter.blockRouting() || (filter.isBlocked() == filter.isFilteredItem(item))) {
          continue outer;
        }
      }

      SinkReply reply = LogisticsManager.canSink(stack, candidateRouter.destination, sourceRouter, excludeSource, item, result.getB(), false, allowDefault);

      if (reply == null) {
        continue;
      }
      if (result.getA() == null) {
        result.setA(candidateRouter.destination.getSimpleID());
        result.setB(reply);
        result.setC(candidateRouter.filters);
        continue;
      }

      if (reply.fixedPriority.ordinal() > result.getB().fixedPriority.ordinal()) {
        result.setA(candidateRouter.destination.getSimpleID());
        result.setB(reply);
        result.setC(new LinkedList<>());
        continue;
      }

      if (reply.fixedPriority == result.getB().fixedPriority && reply.customPriority > result.getB().customPriority) {
        result.setA(candidateRouter.destination.getSimpleID());
        result.setB(reply);
        result.setC(new LinkedList<>());
      }
    }
    if (result.getA() != null) {
      CoreRoutedPipe pipe =
          SimpleServiceLocator.routerManager.getServerRouter(result.getA()).getPipe();
      pipe.useEnergy(result.getB().energyUse);
      //pipe.spawnParticle(Particles.BlueParticle, 10);
    }
    return result;
  }

 @Nullable
  public static SinkReply canSink(ItemStack stack, IRouter destination,
      @Nullable IRouter sourceRouter, boolean excludeSource, Item item, @Nullable SinkReply result,
      boolean activeRequest, boolean allowDefault) {
    return canSink(stack, destination, sourceRouter, excludeSource, item, result, activeRequest, allowDefault, true);
  }

  @Nullable
  public static SinkReply canSink(ItemStack stack, IRouter destination,
      @Nullable IRouter sourceRouter, boolean excludeSource, Item item, @Nullable SinkReply result,
      boolean activeRequest, boolean allowDefault, boolean forcePassive) {

    SinkReply reply;
    LogisticsModule module = destination.getLogisticsModule();
    CoreRoutedPipe crp = destination.getPipe();
    if (module == null) {
      return null;
    }
    if (!(module.receivePassive() || activeRequest)) {
      return null;
    }
    if (crp == null) {
      return null;
    }
    if (excludeSource && sourceRouter != null) {
      if (crp.isOnSameContainer(sourceRouter.getPipe())) {
        return null;
      }
    }
    if (result == null) {
      reply = module.sinksItem(stack, item, -1, 0, allowDefault, true, forcePassive);
    } else {
      reply = module.sinksItem(stack, item, result.fixedPriority.ordinal(), result.customPriority, allowDefault, true, forcePassive);
    }
    if (result != null && result.maxNumberOfItems < 0) {
      return null;
    }
    return reply;
  }



  @Override
  public LinkedList<Item> getCraftableItems(List<ExitRoute> list) {
    return null;
  }

  @Override
  public Map<Item, Integer> getAvailableItems(List<ExitRoute> list) {
    return Map.of();
  }

  @Override
  public String getBetterRouterName(IRouter r) {
    return "";
  }

  @Override
  public int getAmountFor(Item item, List<ExitRoute> validDestinations) {
    return 0;
  }
}
