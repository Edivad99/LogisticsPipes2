package logisticspipes.network.to_client;

import io.netty.buffer.Unpooled;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IClientState;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PipeBlockEntityStatePacket extends CoordinatesPacket {

  public static final CustomPacketPayload.Type<PipeBlockEntityStatePacket> TYPE =
      new CustomPacketPayload.Type<>(LogisticsPipes.rl("pipe_block_entity_state"));

  public static StreamCodec<RegistryFriendlyByteBuf, PipeBlockEntityStatePacket> STREAM_CODEC =
      StreamCodec.ofMember(PipeBlockEntityStatePacket::encode, PipeBlockEntityStatePacket::new);

  private final int statePacketId;
  private IClientState pipe;

  private byte[] bytesPipe;

  public PipeBlockEntityStatePacket(BlockPos blockPos, int statePacketId, IClientState pipe) {
    super(blockPos);
    this.statePacketId = statePacketId;
    this.pipe = pipe;
  }

  public PipeBlockEntityStatePacket(RegistryFriendlyByteBuf buffer) {
    super(buffer);
    this.statePacketId = buffer.readInt();
    this.bytesPipe = buffer.readByteArray();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    super.encode(buffer);
    buffer.writeInt(statePacketId);
    buffer.writeByteArray(writeData(pipe));
  }

  private static byte[] writeData(IClientState state) {
    var dataBuffer = Unpooled.buffer();
    var buff = new FriendlyByteBuf(dataBuffer);
    state.writeData(buff);
    byte[] data = new byte[dataBuffer.readableBytes()];
    dataBuffer.getBytes(0, data);
    dataBuffer.release();
    return data;
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(PipeBlockEntityStatePacket message, IPayloadContext iPayloadContext) {
    LogisticsGenericPipeBlockEntity<?> pipe = message.getPipe(iPayloadContext.player().level());
    if (pipe.statePacketId < message.statePacketId || true) {
      if (message.bytesPipe.length != 0) {
        pipe.pipe.readData(new FriendlyByteBuf(Unpooled.wrappedBuffer(message.bytesPipe)));
      }
      pipe.statePacketId = message.statePacketId;
    }
  }
}
