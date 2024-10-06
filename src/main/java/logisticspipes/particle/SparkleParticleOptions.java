package logisticspipes.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SparkleParticleOptions(float red, float green, float blue, int amount) implements ParticleOptions {

  public static final MapCodec<SparkleParticleOptions> CODEC =
      RecordCodecBuilder.mapCodec(instance -> instance.group(
          Codec.FLOAT.fieldOf("red").forGetter(SparkleParticleOptions::red),
          Codec.FLOAT.fieldOf("green").forGetter(SparkleParticleOptions::green),
          Codec.FLOAT.fieldOf("blue").forGetter(SparkleParticleOptions::blue),
          Codec.INT.fieldOf("amount").forGetter(SparkleParticleOptions::amount)
      ).apply(instance, SparkleParticleOptions::new));

  public static final StreamCodec<FriendlyByteBuf, SparkleParticleOptions> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.FLOAT, SparkleParticleOptions::red,
          ByteBufCodecs.FLOAT, SparkleParticleOptions::green,
          ByteBufCodecs.FLOAT, SparkleParticleOptions::blue,
          ByteBufCodecs.INT, SparkleParticleOptions::amount,
          SparkleParticleOptions::new);

  @Override
  public ParticleType<?> getType() {
    return LogisticsPipesParticleTypes.SPARKLE.get();
  }

}
