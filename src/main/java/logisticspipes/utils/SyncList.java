package logisticspipes.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.jetbrains.annotations.Nullable;
import logisticspipes.network.to_client.ItemBufferSyncPacket;
import logisticspipes.network.to_client.ListSyncPacket;
import logisticspipes.proxy.MainProxy;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public class SyncList<T> implements List<T> {

  private final List<T> list;
  @Nullable
  private ListSyncPacket<T> payload;
  @Nullable
  private PlayerCollectionList watcherList = null;
  private boolean dirty = false;
  @Nullable
  private ServerLevel level;
  @Nullable
  private ChunkPos chunkPos;

  public SyncList() {
    this(null, new ArrayList<>());
  }

  public SyncList(ListSyncPacket<T> type) {
    this(type, new ArrayList<>());
  }

  public SyncList(@Nullable ListSyncPacket<T> type, List<T> list) {
    this.payload = type;
    this.list = list;
  }

  /**
   * Can be used to trigger update manually
   */
  public void markDirty() {
    if (this.payload == null) {
      return;
    }
    this.dirty = true;
  }

  public void sendUpdateToWaters() {
    if (this.payload == null) {
      return;
    }
    if (this.dirty) {
      this.dirty = false;
      if (this.watcherList != null) {
        //MainProxy.sendToPlayerList(this.packetType.template().setList(this.list), this.watcherList);
      } else {
        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, payload.setList(this.list));
        /*MainProxy.sendPacketToAllWatchingChunk(this.x, this.z, this.level,
            this.payload.template().setList(this.list));*/
      }
    }
  }

  public void setPacketType(ListSyncPacket<T> payload, ServerLevel level, ChunkPos chunkPos) {
    this.payload = payload;
    this.level = level;
    this.chunkPos = chunkPos;
    if (watcherList != null) {
      //MainProxy.sendToPlayerList(packetType.template().setList(this.list), watcherList);
    } else {
      PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, payload.setList(this.list));
      /*MainProxy.sendPacketToAllWatchingChunk(x, z, this.level,
          this.payload.template().setList(this.list));*/
    }
  }

  @Override
  public int size() {
    return this.list.size();
  }

  @Override
  public boolean isEmpty() {
    return this.list.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return this.list.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return this.list.iterator();
  }

  @Override
  public Object[] toArray() {
    return this.list.toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return this.list.toArray(a);
  }

  @Override
  public boolean add(T t) {
    boolean flag = this.list.add(t);
    this.markDirty();
    return flag;
  }

  @Override
  public boolean remove(Object o) {
    boolean flag = this.list.remove(o);
    this.markDirty();
    return flag;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return this.list.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    boolean flag = this.list.addAll(c);
    this.markDirty();
    return flag;
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    boolean flag = this.list.addAll(index, c);
    this.markDirty();
    return flag;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean flag = this.list.removeAll(c);
    this.markDirty();
    return flag;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    boolean flag = this.list.retainAll(c);
    this.markDirty();
    return flag;
  }

  @Override
  public void clear() {
    this.list.clear();
    this.markDirty();
  }

  @Override
  public T get(int index) {
    return this.list.get(index);
  }

  @Override
  public T set(int index, T element) {
    T object = this.list.set(index, element);
    this.markDirty();
    return object;
  }

  @Override
  public void add(int index, T element) {
    this.list.add(index, element);
    this.markDirty();
  }

  @Override
  public T remove(int index) {
    T object = this.list.remove(index);
    this.markDirty();
    return object;
  }

  @Override
  public int indexOf(Object o) {
    return this.list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    int index = this.list.lastIndexOf(o);
    this.markDirty();
    return index;
  }

  @Override
  public ListIterator<T> listIterator() {
    return new SyncListIterator(this.list.listIterator());
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return new SyncListIterator(this.list.listIterator(index));
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  private class SyncIterator implements Iterator<T> {

    private final Iterator<T> iter;

    protected SyncIterator(Iterator<T> iter) {
      this.iter = iter;
    }

    @Override
    public boolean hasNext() {
      return this.iter.hasNext();
    }

    @Override
    public T next() {
      return this.iter.next();
    }

    @Override
    public void remove() {
      this.iter.remove();
      markDirty();
    }
  }

  private class SyncListIterator extends SyncIterator implements ListIterator<T> {

    private final ListIterator<T> iter;

    protected SyncListIterator(ListIterator<T> iter) {
      super(iter);
      this.iter = iter;
    }

    @Override
    public void add(T paramE) {
      this.iter.add(paramE);
      markDirty();
    }

    @Override
    public boolean hasPrevious() {
      return this.iter.hasPrevious();
    }

    @Override
    public int nextIndex() {
      return this.iter.nextIndex();
    }

    @Override
    public T previous() {
      return this.iter.previous();
    }

    @Override
    public int previousIndex() {
      return this.iter.previousIndex();
    }

    @Override
    public void set(T paramE) {
      this.iter.set(paramE);
      markDirty();
    }
  }
}
