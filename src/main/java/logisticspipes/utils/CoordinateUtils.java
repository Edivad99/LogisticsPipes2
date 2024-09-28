package logisticspipes.utils;

import net.minecraft.core.Direction;

public final class CoordinateUtils {

  private CoordinateUtils() {}

  public static DoubleCoordinates add(DoubleCoordinates coords, Direction direction) {
    coords.setXCoord(coords.getXCoord() + direction.getNormal().getX());
    coords.setYCoord(coords.getYCoord() + direction.getNormal().getY());
    coords.setZCoord(coords.getZCoord() + direction.getNormal().getZ());
    return coords;
  }

  public static DoubleCoordinates add(DoubleCoordinates coords, Direction direction, double times) {
    coords.setXCoord(coords.getXCoord() + direction.getNormal().getX() * times);
    coords.setYCoord(coords.getYCoord() + direction.getNormal().getY() * times);
    coords.setZCoord(coords.getZCoord() + direction.getNormal().getZ() * times);
    return coords;
  }

  public static DoubleCoordinates sum(DoubleCoordinates coords, Direction direction) {
    DoubleCoordinates ret = new DoubleCoordinates(coords);
    return CoordinateUtils.add(ret, direction);
  }

}
