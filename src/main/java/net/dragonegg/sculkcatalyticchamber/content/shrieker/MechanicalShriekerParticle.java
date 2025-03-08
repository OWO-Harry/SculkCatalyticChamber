package net.dragonegg.sculkcatalyticchamber.content.shrieker;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class MechanicalShriekerParticle extends TextureSheetParticle {

    private static final Vector3f ROTATION_VECTOR = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);

    private int delay;
    private final Vec3i normal;

    protected MechanicalShriekerParticle(ClientLevel pLevel, double pX, double pY, double pZ, int delay, Direction direction) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.quadSize = 0.85F;
        this.delay = delay;
        this.lifetime = 90;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.normal = direction.getNormal();
        this.xd = normal.getX() * 0.1D;
        this.yd = normal.getY() * 0.1D;
        this.zd = normal.getZ() * 0.1D;
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) * 2 / 45, 0.0F, 1.0F);
    }

    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        if (this.delay <= 0) {
            this.alpha = 1.0F - Mth.clamp(((float)this.age + pPartialTicks) / (float)this.lifetime, 0.0F, 1.0F);
            this.renderRotatedParticle(pBuffer, pRenderInfo, pPartialTicks, (quad) -> {
                quad.mul((new Quaternionf()).rotateXYZ(
                        -(float)Math.PI * 0.5F * (normal.getX() - 1), 0,
                        -(float)Math.PI * 0.5F * normal.getZ()
                ));
            });
            this.renderRotatedParticle(pBuffer, pRenderInfo, pPartialTicks, (quad) -> {
                quad.mul((new Quaternionf()).rotateXYZ(
                        (float)Math.PI * 0.5F * (normal.getX() + 1), -(float)Math.PI,
                        (float)Math.PI * 0.5F * normal.getZ()
                ));
            });
        }
    }

    private void renderRotatedParticle(VertexConsumer pConsumer, Camera pCamera, float pPartialTick, Consumer<Quaternionf> pQuaternion) {
        Vec3 vec3 = pCamera.getPosition();
        float f = (float)(Mth.lerp(pPartialTick, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp(pPartialTick, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp(pPartialTick, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
        pQuaternion.accept(quaternionf);
        quaternionf.transform(TRANSFORM_VECTOR);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(pPartialTick);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternionf);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        int j = this.getLightColor(pPartialTick);
        this.makeCornerVertex(pConsumer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pConsumer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[3], this.getU0(), this.getV1(), j);
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVertex, float pU, float pV, int pPackedLight) {
        pConsumer.vertex(pVertex.x(), pVertex.y(), pVertex.z()).uv(pU, pV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(pPackedLight).endVertex();
    }

    public int getLightColor(float pPartialTick) {
        return 240;
    }

    @Override
    public void tick() {
        if (this.delay > 0) {
            --this.delay;
        } else {
            super.tick();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<MechanicalShriekerParticleData> {
        private final SpriteSet sprite;

        public Factory(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(MechanicalShriekerParticleData pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            MechanicalShriekerParticle particle = new MechanicalShriekerParticle(pLevel, pX, pY, pZ, pType.getDelay(), pType.getDirection());
            particle.pickSprite(this.sprite);
            particle.setAlpha(1.0F);
            return particle;
        }
    }
}
