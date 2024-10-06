package logisticspipes.client.renderer.blockentity.state;

import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

@Getter
public class ConnectionMatrix {

  /**
   * -- GETTER --
   *  Return a mask representing the connectivity for all sides.<br>
   *  Mask in EnumFacing order, the least significant bit = first entry
   */
  private int mask = 0;
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
  }

  public void clean() {
    dirty = false;
  }

  public void writeData(FriendlyByteBuf buf) {
    buf.writeVarInt(mask);
  }

  public void readData(FriendlyByteBuf buf) {
    int newMask = buf.readVarInt();

    if (newMask != mask) {
      mask = newMask;
      dirty = true;
    }
  }
}
