package logisticspipes.network.to_client;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class ListSyncPacket<E> extends CoordinatesPacket implements
    StreamCodec<RegistryFriendlyByteBuf, E> {

  @Getter(value = AccessLevel.PROTECTED)
  private List<E> list;

  public ListSyncPacket(BlockPos blockPos, List<E> list) {
    super(blockPos);
    this.list = list;
  }

  public ListSyncPacket(RegistryFriendlyByteBuf buffer) {
    super(buffer);
    this.list = buffer.readList(buffer1 -> this.decode((RegistryFriendlyByteBuf)buffer1));
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    super.encode(buffer);
    buffer.writeCollection(this.list, (buffer1, value) -> this.encode((RegistryFriendlyByteBuf)buffer1, value));
  }

  public ListSyncPacket<E> setList(List<E> list) {
    this.list = list;
    return this;
  }
}
