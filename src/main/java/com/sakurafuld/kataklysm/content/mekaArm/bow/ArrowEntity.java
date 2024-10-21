package com.sakurafuld.kataklysm.content.mekaArm.bow;

import com.sakurafuld.kataklysm.content.ModSounds;
import com.sakurafuld.kataklysm.network.PacketHandler;
import com.sakurafuld.kataklysm.network.mekaArm.ClientboundArrowVanish;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.sakurafuld.kataklysm.Deets.*;

public class ArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_POWER = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_INACCURACY = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_SUB = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_SUB_MULTIPLIER = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.FLOAT);
    private boolean piercing = false;
    private final List<Integer> PIERCED = new ArrayList<>();
    private boolean homing = false;



    private boolean hasBeenShot = false;
    private boolean leftOwner = false;
    private BlockState lastState = null;

    public ArrowEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setBaseDamage(3f);
    }
    protected ArrowEntity(EntityType<? extends AbstractArrow> pEntityType, double pX, double pY, double pZ, Level pLevel) {
        super(pEntityType, pX, pY, pZ, pLevel);
        this.setBaseDamage(3f);
    }
    public ArrowEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel, LivingEntity owner) {
        super(pEntityType, owner, pLevel);
        this.setBaseDamage(3f);
    }

    public float getPower(){
        return this.getEntityData().get(DATA_POWER);
    }
    public void setPower(float power){
        this.getEntityData().set(DATA_POWER, power);
    }
    public float getInaccuracy(){
        return this.getEntityData().get(DATA_INACCURACY);
    }
    public void setInaccuracy(float inaccuracy){
        this.getEntityData().set(DATA_INACCURACY, inaccuracy);
    }
    public float getSubMultiplier(){
        return this.getEntityData().get(DATA_SUB_MULTIPLIER);
    }
    public void setSubMultiplier(float subMultiplier){
        this.getEntityData().set(DATA_SUB_MULTIPLIER, subMultiplier);
    }
    public boolean isSub(){
        return this.getEntityData().get(DATA_SUB);
    }
    public void setSub(boolean sub){
        this.getEntityData().set(DATA_SUB, sub);
    }
    public boolean isPiercing(){
        return this.piercing;
    }
    public void setPiercing(boolean piercing){
        this.piercing = piercing;
    }
    public boolean isHoming(){
        return this.homing;
    }
    public void setHoming(boolean homing){
        this.homing = homing;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_POWER, 0f);
        this.getEntityData().define(DATA_INACCURACY, 0f);
        this.getEntityData().define(DATA_SUB, false);
        this.getEntityData().define(DATA_SUB_MULTIPLIER, 1f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("Piercing", this.isPiercing());
        pCompound.putIntArray("Pierced", this.PIERCED);
        pCompound.putBoolean("Homing", this.isHoming());
//        pCompound.putBoolean("Sub", this.isSub());
        pCompound.putBoolean("HasBeenShot", this.hasBeenShot);
        pCompound.putBoolean("LeftOwner", this.leftOwner);
        if (this.lastState != null) {
            pCompound.put("LastState", NbtUtils.writeBlockState(this.lastState));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setPiercing(pCompound.getBoolean("Piercing"));
        this.PIERCED.addAll(Arrays.stream(pCompound.getIntArray("Pierced")).boxed().toList()) ;
        this.setHoming(pCompound.getBoolean("Homing"));
//        this.setSub(pCompound.getBoolean("Sub"));
        this.hasBeenShot = pCompound.getBoolean("HasBeenShot");
        this.leftOwner = pCompound.getBoolean("LeftOwner");
        if (pCompound.contains("LastState", 10)) {
            this.lastState = NbtUtils.readBlockState(pCompound.getCompound("LastState"));
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner(), this.blockPosition());
            this.hasBeenShot = true;
        }

        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        super.baseTick();

        boolean flag = this.isNoPhysics();
        Vec3 delta = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = delta.horizontalDistance();
            this.setYRot((float)(Mth.atan2(delta.x, delta.z) * (double)(180F / (float)Math.PI)));
            this.setXRot((float)(Mth.atan2(delta.y, d0) * (double)(180F / (float)Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos pos = this.blockPosition();
        BlockState state = this.level.getBlockState(pos);
        if (!state.isAir() && !flag) {
            VoxelShape voxelshape = state.getCollisionShape(this.level, pos);
            if (!voxelshape.isEmpty()) {
                Vec3 position = this.position();

                for(AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(pos).contains(position)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain() || state.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            if (this.lastState != state && this.shouldFall()) {
                this.startFalling();
            } else if (!this.level.isClientSide) {
                this.tickDespawn();
            }

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            Vec3 vec32 = this.position();
            Vec3 vec33 = vec32.add(delta);
            HitResult hitresult = this.level.clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hitresult.getType() != HitResult.Type.MISS) {
                vec33 = hitresult.getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult instanceof EntityHitResult && hitresult.getType() != HitResult.Type.MISS) {
                    Entity target = ((EntityHitResult)hitresult).getEntity();
                    Entity owner = this.getOwner();
                    if (target instanceof Player && owner instanceof Player && !((Player)owner).canHarmPlayer((Player)target)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !flag && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
                    this.onHit(hitresult);
                    this.hasImpulse = true;
                }

                if (entityhitresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
            }

            delta = this.getDeltaMovement();
            double d5 = delta.x;
            double d6 = delta.y;
            double d1 = delta.z;

            double d7 = this.getX() + d5;
            double d2 = this.getY() + d6;
            double d3 = this.getZ() + d1;
            double d4 = delta.horizontalDistance();
            if (flag) {
                this.setYRot((float)(Mth.atan2(-d5, -d1) * (double)(180F / (float)Math.PI)));
            } else {
                this.setYRot((float)(Mth.atan2(d5, d1) * (double)(180F / (float)Math.PI)));
            }

            this.setXRot((float)(Mth.atan2(d6, d4) * (double)(180F / (float)Math.PI)));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f = 0.99F;
            if (this.isInWater()) {
                for(int j = 0; j < 4; ++j) {
                    this.level.addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25D, d2 - d6 * 0.25D, d3 - d1 * 0.25D, d5, d6, d1);
                }
                f = this.getWaterInertia();
            }

            this.setDeltaMovement(delta.scale(f));
            if (!this.isNoGravity() && !flag) {
                Vec3 vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - (double)0.05F, vec34.z);
            }

            this.setPos(d7, d2, d3);
            this.checkInsideBlocks();
        }
        if(this.inGroundTime >= (this.isSub() ? 5 : 40)) this.discard();

        if(this.tickCount >= 400) this.discard();

//        if(this.tickCount % 10 == 0) {
//            LOG.debug("{}-PreDelta={}", side(), this.getDeltaMovement());
//            double length = this.getDeltaMovement().length();
//            Vec3 v = this.getDeltaMovement().normalize().scale(length);
//            LOG.debug("{}-PostDelta={}", side(), v);
//        }
    }

    @Override
    protected float getWaterInertia() {
        return (float) (super.getWaterInertia() - (this.getDeltaMovement().length() / 10));
    }

    private boolean shouldFall() {
        return this.inGround && this.level.noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
    }

    private void startFalling() {
        this.discard();
    }
    private boolean checkLeftOwner() {
        Entity entity = this.getOwner();
        if (entity != null) {
            for(Entity entity1 : this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (entity1) ->
                    !entity1.isSpectator() && entity1.isPickable())) {
                if (entity1.getRootVehicle() == entity.getRootVehicle()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
        double d0 = Double.MAX_VALUE;
        Entity entity = null;
        Vec3 vec = null;

        for(Entity entity1 : this.getLevel().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity)) {
            AABB aabb = entity1.getBoundingBox().inflate(0.3f);
            Optional<Vec3> optional = aabb.clip(pStartVec, pEndVec);
            if (optional.isPresent()) {
                double d1 = pStartVec.distanceToSqr(optional.get());
                if (d1 < d0) {
                    entity = entity1;
                    d0 = d1;
                    vec = optional.get();
                }
            }
        }

        return entity == null ? null : new EntityHitResult(entity, vec);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        this.lastState = this.level.getBlockState(pResult.getBlockPos());

        BlockState blockstate = this.level.getBlockState(pResult.getBlockPos());
        blockstate.onProjectileHit(this.level, blockstate, pResult, this);

        Vec3 vec3 = pResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.playHitSound();
        this.inGround = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity target = pResult.getEntity();
        if(this.getPierceLevel() > 0){
            if(this.PIERCED.contains(target.getId())){
                LOG.info("{}-ContainsPierced={}", side(), target.getId());
                return;
            }
            LOG.info("{}-Pierced={}", side(), target.getId());
            this.PIERCED.add(target.getId());
        }

        float speedFactor = (float)this.getDeltaMovement().length();

        float damage = Mth.ceil(Mth.clamp(this.getBaseDamage() * speedFactor, 0.0D, 2.147483647E9D));

        if(this.isSub()) damage *= this.getSubMultiplier();

        if(this.isCritArrow()) {
//            damage += Mth.floor(Mth.lerp(this.random.nextFloat(), damage / 8, damage / 4));
            damage += this.random.nextInt(-5, 5);
        } else {
            long j = this.random.nextInt(Mth.ceil(damage) / 2 + 2);
            damage = Math.min(j + damage, 2147483647L);
        }

        Entity owner = this.getOwner();
        DamageSource damagesource;
        if (owner == null) {
            damagesource = DamageSource.arrow(this, this);
        } else {
            damagesource = DamageSource.arrow(this, owner);
            if (owner instanceof LivingEntity) {
                ((LivingEntity)owner).setLastHurtMob(target);
            }
        }

        boolean flag = target.getType() == EntityType.ENDERMAN;
        if (this.isOnFire() && !flag) {
            target.setSecondsOnFire(5);
        }

        if(this.isSub() && this.getOwner() instanceof LivingEntity living && living.getLastHurtMob() == target) {
            target.invulnerableTime = 0;
        }

        if (target.hurt(damagesource, damage)) {
            LOG.info("{}-dealDamage={}", side(), damage);
            if (flag) {
                return;
            }

            if (target instanceof LivingEntity living) {

                if (this.getKnockback() > 0) {
                    Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)this.getKnockback() * 0.6D);
                    if (vec3.lengthSqr() > 0.0D) {
                        living.push(vec3.x, 0.1D, vec3.z);
                    }
                }

                if (!this.level.isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(living, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)owner, living);
                }

                this.doPostHurtEffects(living);
                if (living != owner && living instanceof Player && owner instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer)owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }
            }

            this.playHitSound();

            if (this.getPierceLevel() <= 0 || this.PIERCED.size() > this.getPierceLevel()) {
                this.discardAt(pResult.getLocation());
            } else {
                this.playVanish(pResult.getLocation(), 3);
            }
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        if(pReason == RemovalReason.DISCARDED){
            this.playVanish(this.position(), 6);
        }
        super.remove(pReason);
    }
    private void discardAt(Vec3 position){

       this.playVanish(position, 6);

        this.setRemoved(RemovalReason.DISCARDED);
        this.invalidateCaps();
    }

    private void playHitSound(){
        float pitch = (this.random.nextFloat() * 0.2F + 0.9F);
        this.playSound(SoundEvents.ARROW_HIT, 1.5F, pitch * 2f);
        this.playSound(SoundEvents.PISTON_EXTEND, 1F,  pitch * 2f);
    }
    private void playVanish(Vec3 position, int count){
        this.getLevel().playSound(null, position.x(), position.y(), position.z(), ModSounds.VANISH.get(), this.getSoundSource(), 1.5F, (this.random.nextFloat() * 0.2F + 0.9F) * 1.5f);
        required(LogicalSide.SERVER).run(() ->
                PacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(this.getLevel()::dimension), new ClientboundArrowVanish(position, count)));
    }

    @Override
    public boolean isCritArrow() {
        return this.getPower() >= 2;
    }

//    @Override
//    public void setCritArrow(boolean pCritArrow) {}

//    @Override
//    public byte getPierceLevel() {
//        return this.isPiercing() ? Byte.MAX_VALUE : 0;
//    }
//
//    @Override
//    public void setPierceLevel(byte pPierceLevel) {}

    @Override
    public boolean shotFromCrossbow() {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
//        LOG.info("{}-CanHit={}-ID={}", side(), pTarget.getDisplayName().getString(), pTarget.getId());
        if (!pTarget.isSpectator() && pTarget.isAlive() && pTarget.isPickable() ) {
            Entity entity = this.getOwner();
            return (entity == null || this.leftOwner || !entity.isPassengerOfSameVehicle(pTarget)) && (this.PIERCED.isEmpty() || !this.PIERCED.contains(pTarget.getId()));
        } else {
            return false;
        }
    }
    @Override
    public void playerTouch(Player pEntity) {
    }
}
