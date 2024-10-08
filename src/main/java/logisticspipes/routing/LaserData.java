package logisticspipes.routing;

import java.util.EnumSet;
import java.util.Objects;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import lombok.Getter;
import net.minecraft.core.Direction;

@Getter
public class LaserData {

  private int posX, posY, posZ;
  private boolean finalPipe = true;
  private boolean startPipe = false;
  private int length = 1;
  private Direction dir;
  private EnumSet<PipeRoutingConnectionType> connectionType;

  public LaserData(int posX, int posY, int posZ, Direction dir, EnumSet<PipeRoutingConnectionType> connectionType) {
    this.posX = posX;
    this.posY = posY;
    this.posZ = posZ;
    this.dir = dir;
    this.connectionType = connectionType;
  }

  public LaserData(IPipeInformationProvider provider, Direction dir, EnumSet<PipeRoutingConnectionType> connectionType) {
    this(provider.getX(), provider.getY(), provider.getZ(), dir, connectionType);
  }

  public LaserData setPosX(int posX) {
    this.posX = posX;
    return this;
  }

  public LaserData setPosY(int posY) {
    this.posY = posY;
    return this;
  }

  public LaserData setPosZ(int posZ) {
    this.posZ = posZ;
    return this;
  }

  public LaserData setDir(Direction dir) {
    this.dir = dir;
    return this;
  }

  public LaserData setConnectionType(EnumSet<PipeRoutingConnectionType> connectionType) {
    this.connectionType = connectionType;
    return this;
  }


  public LaserData setFinalPipe(boolean finalPipe) {
    this.finalPipe = finalPipe;
    return this;
  }

  public LaserData setStartPipe(boolean startPipe) {
    this.startPipe = startPipe;
    return this;
  }

  public LaserData setLength(int length) {
    this.length = length;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof LaserData other)) return false;
    if (this.getPosX() != other.getPosX()) return false;
    if (this.getPosY() != other.getPosY()) return false;
    if (this.getPosZ() != other.getPosZ()) return false;
    if (!Objects.equals(this.getDir(), other.getDir())) return false;
    if (!Objects.equals(this.getConnectionType(), other.getConnectionType())) return false;
    if (this.isFinalPipe() != other.isFinalPipe()) return false;
    if (this.isStartPipe() != other.isStartPipe()) return false;
    return this.getLength() == other.getLength();
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.getPosX();
    result = result * PRIME + this.getPosY();
    result = result * PRIME + this.getPosZ();
    result = result * PRIME + this.getDir().hashCode();
    result = result * PRIME + this.getConnectionType().hashCode();
    result = result * PRIME + (this.isFinalPipe() ? 79 : 97);
    result = result * PRIME + (this.isStartPipe() ? 79 : 97);
    result = result * PRIME + this.getLength();
    return result;
  }

  public String toString() {
    return "LaserData(posX=" + this.getPosX() + ", posY=" + this.getPosY() + ", posZ=" + this.getPosZ() + ", dir=" + this.getDir() + ", connectionType=" + this.getConnectionType() + ", finalPipe=" + this.isFinalPipe() + ", startPipe=" + this.isStartPipe() + ", length=" + this.getLength() + ")";
  }
}
