package logisticspipes.client.renderer.blockentity.state;

import lombok.Getter;
import net.minecraft.core.Direction;

public class ConnectionMatrix {

  /**
   * -- GETTER --
   *  Return a mask representing the connectivity for all sides.
   *
   * @return mask in EnumFacing order, the least significant bit = first entry
   */
  @Getter
  private int mask = 0;
  private int isBCPipeMask = 0;
  private int isTDPipeMask = 0;
  @Getter
  private boolean dirty = false;

  public boolean isConnected(Direction direction) {
    // test if the direction.ordinal()'th bit of mask is set
    return (mask & (1 << direction.ordinal())) != 0;
  }

  public void setConnected(Direction direction, boolean value) {
    if (isConnected(direction) != value) {
      // invert the direction.ordinal()'th bit of mask
      mask ^= 1 << direction.ordinal();
      dirty = true;
    }
    if (!value) {
      //REMOVE
      setBCConnected(direction, false);
      setTDConnected(direction, false);
    }
  }

  public boolean isBCConnected(Direction direction) {
    // test if the direction.ordinal()'th bit of mask is set
    return direction != null && (isBCPipeMask & (1 << direction.ordinal())) != 0;
  }

  public void setBCConnected(Direction direction, boolean value) {
    if (isBCConnected(direction) != value) {
      // invert the direction.ordinal()'th bit of mask
      isBCPipeMask ^= 1 << direction.ordinal();
      dirty = true;
    }
  }

  public boolean isTDConnected(Direction direction) {
    // test if the direction.ordinal()'th bit of mask is set
    return direction != null && (isTDPipeMask & (1 << direction.ordinal())) != 0;
  }

  public void setTDConnected(Direction direction, boolean value) {
    if (isTDConnected(direction) != value) {
      // invert the direction.ordinal()'th bit of mask
      isTDPipeMask ^= 1 << direction.ordinal();
      dirty = true;
    }
  }

  public void clean() {
    dirty = false;
  }
}
