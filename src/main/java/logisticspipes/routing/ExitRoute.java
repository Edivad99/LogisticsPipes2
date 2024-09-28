package logisticspipes.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.routing.debug.ExitRouteDebug;
import net.minecraft.core.Direction;

public class ExitRoute implements Comparable<ExitRoute> {

  public final double destinationDistanceToRoot;
  public final int blockDistance;
  public final EnumSet<PipeRoutingConnectionType> connectionDetails;
  public final IRouter destination;
  @Nullable
  public Direction exitOrientation, insertOrientation;
  public double distanceToDestination;
  @Nullable
  public IRouter root;
  public List<IFilter> filters = Collections.unmodifiableList(new ArrayList<>(0));

  /**
   * Used to store debug information. No use in the actual Routing table calculation
   */
  public ExitRouteDebug debug = new ExitRouteDebug();

  public ExitRoute(@Nullable IRouter source, IRouter destination,
      @Nullable Direction exitOrientation, @Nullable Direction insertOrientation, double metric,
      EnumSet<PipeRoutingConnectionType> connectionDetails, int blockDistance) {
    this.destination = destination;
    this.root = source;
    this.exitOrientation = exitOrientation;
    this.insertOrientation = insertOrientation;
    this.connectionDetails = connectionDetails;
    if (connectionDetails.contains(PipeRoutingConnectionType.CAN_ROUTE_TO)) {
      this.distanceToDestination = metric;
    } else {
      this.distanceToDestination = Integer.MAX_VALUE;
    }
    if (connectionDetails.contains(PipeRoutingConnectionType.CAN_REQUEST_FROM)) {
      this.destinationDistanceToRoot = metric;
    } else {
      this.destinationDistanceToRoot = Integer.MAX_VALUE;
    }
    this.blockDistance = blockDistance;
  }

  public ExitRoute(IRouter source, IRouter destination, double distance,
      EnumSet<PipeRoutingConnectionType> enumSet, List<IFilter> filterA, List<IFilter> filterB,
      int blockDistance) {
    this(source, destination, null, null, distance, enumSet, blockDistance);
    List<IFilter> filter = new ArrayList<>(filterA.size() + filterB.size());
    filter.addAll(filterA);
    filter.addAll(filterB);
    filters = Collections.unmodifiableList(filter);
  }

  @Override
  public boolean equals(Object aThat) {
    //check for self-comparison
    if (this == aThat) {
      return true;
    }

    if (!(aThat instanceof ExitRoute that)) {
      return false;
    }
    return Objects.equals(exitOrientation, that.exitOrientation) &&
        Objects.equals(insertOrientation, that.insertOrientation) &&
        connectionDetails.equals(that.connectionDetails) &&
        distanceToDestination == that.distanceToDestination &&
        destinationDistanceToRoot == that.destinationDistanceToRoot &&
        destination == that.destination && filters.equals(that.filters);
  }

  public boolean isSameWay(ExitRoute that) {
    if (equals(that)) {
      return true;
    }
    return connectionDetails.equals(that.connectionDetails) &&
        destination == that.destination && filters.equals(that.filters);
  }

  @Override
  public String toString() {
    return String.format("ExitRoute(exitdir=%s, insertdir=%s, distance=%s, reverse distance=%s, conn. details=%s, filters=%s)",
        exitOrientation, insertOrientation, distanceToDestination, destinationDistanceToRoot,
        connectionDetails, filters);
  }

  public void removeFlags(EnumSet<PipeRoutingConnectionType> flags) {
    connectionDetails.removeAll(flags);
  }

  public boolean containsFlag(PipeRoutingConnectionType flag) {
    return connectionDetails.contains(flag);
  }

  public boolean hasActivePipe() {
    return destination.getCachedPipe() != null;
  }

  //copies
  public EnumSet<PipeRoutingConnectionType> getFlags() {
    return EnumSet.copyOf(connectionDetails);
  }

  // Doesn't copy
  public Set<PipeRoutingConnectionType> getFlagsNoCopy() {
    return Collections.unmodifiableSet(connectionDetails);
  }

  @Override
  public int compareTo(ExitRoute o) {
    final int c = Double.compare(distanceToDestination, o.distanceToDestination);
    if (c == 0) {
      return Integer.compare(destination.getSimpleID(), o.destination.getSimpleID());
    }
    return c;
  }
}
