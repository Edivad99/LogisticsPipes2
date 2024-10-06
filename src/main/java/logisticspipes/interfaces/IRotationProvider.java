package logisticspipes.interfaces;

import net.minecraft.core.Direction;

public interface IRotationProvider {

  @Deprecated
  int getRotation();

  default Direction getFacing() {
    return switch (getRotation()) {
      case 0 -> Direction.WEST;
      case 1 -> Direction.EAST;
      case 2 -> Direction.NORTH;
      default -> Direction.SOUTH;
    };
  }

  @Deprecated
  void setRotation(int rotation);

  default void setFacing(Direction facing) {
    switch (facing) {
      case NORTH:
        setRotation(3);
        break;
      case DOWN:
      case UP:
      case SOUTH:
        setRotation(2);
        break;
      case WEST:
        setRotation(1);
        break;
      case EAST:
        setRotation(0);
        break;
    }
  }
}
