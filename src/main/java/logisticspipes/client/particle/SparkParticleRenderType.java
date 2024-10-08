package logisticspipes.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public class SparkParticleRenderType implements ParticleRenderType {

  static final SparkParticleRenderType SPARK_PARTICLE_RENDER_TYPE = new SparkParticleRenderType();

  @Override
  public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
    RenderSystem.depthMask(false);
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
    RenderSystem.clearColor(1, 1, 1, 1);
    return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
  }

  @Override
  public boolean isTranslucent() {
    return true;
  }

  @Override
  public String toString() {
    return "SPARK_PARTICLE_RENDER_TYPE";
  }
}
