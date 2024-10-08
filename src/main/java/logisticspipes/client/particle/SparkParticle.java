package logisticspipes.client.particle;

import logisticspipes.particle.SparkleParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SparkParticle extends TextureSheetParticle {

  protected SparkParticle(ClientLevel level, double x, double y, double z, float scale,
      float red, float green, float blue, int multiplier, SpriteSet sprites) {
    super(level, x, y, z, 0, 0, 0);
    this.rCol = red;
    this.gCol = green;
    this.bCol = blue;
    this.gravity = 0.07F;
    this.xd = this.yd = this.zd = 0;
    this.scale(scale);
    this.lifetime = 3 * multiplier -1;
    this.hasPhysics = false;
    this.setSpriteFromAge(sprites);
  }

  @Override
  public void tick() {
    var player = Minecraft.getInstance().player;
    if (player.distanceToSqr(getPos()) > 50) {
      this.remove();
    }

    this.xo = x;
    this.yo = y;
    this.zo = z;

    if (this.age++ >= this.lifetime) {
      this.remove();
    } else {
      this.xd -= 0.05D * this.gravity - 0.1D * this.gravity * this.level.getRandom().nextDouble();
      this.yd -= 0.05D * this.gravity - 0.1D * this.gravity * this.level.getRandom().nextDouble();
      this.zd -= 0.05D * this.gravity - 0.1D * this.gravity * this.level.getRandom().nextDouble();

      this.move(this.xd, this.yd, this.zd);

      this.xd *= 0.98D;
      this.yd *= 0.98D;
      this.zd *= 0.98D;

      if (this.onGround) {
        this.xd *= 0.7D;
        this.zd *= 0.7D;
      }
    }
  }

  @Override
  public ParticleRenderType getRenderType() {
    return SparkParticleRenderType.SPARK_PARTICLE_RENDER_TYPE;
  }

  public static class Provider implements ParticleProvider<SparkleParticleOptions> {

    private final SpriteSet sprites;

    public Provider(SpriteSet sprites) {
      this.sprites = sprites;
    }

    @Override
    public Particle createParticle(SparkleParticleOptions options, ClientLevel level,
        double x, double y, double z, double dx, double dy, double dz) {

      float boundary = 0.4F;
      int pipeWidth = 1;

      float width = boundary + level.getRandom().nextInt(pipeWidth) / 10.0F;
      float length = boundary + level.getRandom().nextInt(pipeWidth) / 10.0F;
      float height = level.getRandom().nextInt(7) / 10.0F + 0.2F;

      float scaleMultiplier = 1f + (float) Math.log10(options.amount());

      return new SparkParticle(level, x + length, y + height, z + width,
          scaleMultiplier, options.red(), options.green(), options.blue(),
          6 + level.getRandom().nextInt(3), this.sprites);
    }
  }
}
