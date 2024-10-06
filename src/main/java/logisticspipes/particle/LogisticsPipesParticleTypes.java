package logisticspipes.particle;

import com.mojang.serialization.MapCodec;
import logisticspipes.LogisticsPipes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LogisticsPipesParticleTypes {

  private static final DeferredRegister<ParticleType<?>> deferredRegister =
      DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, LogisticsPipes.ID);

  public static void register(IEventBus modEventBus) {
    deferredRegister.register(modEventBus);
  }

  public static final DeferredHolder<ParticleType<?>, ParticleType<SparkleParticleOptions>> SPARKLE =
      deferredRegister.register("sparkle",
          () -> create(SparkleParticleOptions.CODEC, SparkleParticleOptions.STREAM_CODEC));

  private static <T extends ParticleOptions> ParticleType<T> create(
      MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
    return new ParticleType<>(false) {
      @Override
      public MapCodec<T> codec() {
        return codec;
      }

      @Override
      public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
      }
    };
  }
}
