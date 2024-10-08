package logisticspipes.network.to_client;

import org.jetbrains.annotations.Nullable;
import logisticspipes.LogisticsPipes;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PipePositionPacket extends CoordinatesPacket {

  public static final CustomPacketPayload.Type<PipePositionPacket> TYPE =
      new CustomPacketPayload.Type<>(LogisticsPipes.rl("pipe_position"));

  public static StreamCodec<RegistryFriendlyByteBuf, PipePositionPacket> STREAM_CODEC =
      StreamCodec.ofMember(PipePositionPacket::encode, PipePositionPacket::new);

  private final int travelId;
  private final float speed;
  private final float position;
  @Nullable
  private final Direction input;
  @Nullable
  private final Direction output;
  private final float yaw;

  public PipePositionPacket(BlockPos blockPos, int travelId, float speed, float position,
      @Nullable Direction input, @Nullable Direction output, float yaw) {
    super(blockPos);
    this.travelId = travelId;
    this.speed = speed;
    this.position = position;
    this.input = input;
    this.output = output;
    this.yaw = yaw;
  }

  public PipePositionPacket(RegistryFriendlyByteBuf buffer) {
    super(buffer);
    this.travelId = buffer.readVarInt();
    this.speed = buffer.readFloat();
    this.position = buffer.readFloat();
    this.input = buffer.readNullable(b -> b.readEnum(Direction.class));
    this.output = buffer.readNullable(b -> b.readEnum(Direction.class));
    this.yaw = buffer.readFloat();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    super.encode(buffer);
    buffer.writeVarInt(travelId);
    buffer.writeFloat(speed);
    buffer.writeFloat(position);
    buffer.writeNullable(input, FriendlyByteBuf::writeEnum);
    buffer.writeNullable(output, FriendlyByteBuf::writeEnum);
    buffer.writeFloat(yaw);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(PipePositionPacket message, IPayloadContext iPayloadContext) {
    LogisticsGenericPipeBlockEntity<?> blockEntity = message.getPipe(iPayloadContext.player().level());
    blockEntity.pipe.getTransport().handleItemPositionPacket(message.travelId, message.input,
        message.output, message.speed, message.position, message.yaw);
  }
}
