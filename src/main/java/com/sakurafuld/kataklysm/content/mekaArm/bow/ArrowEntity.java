package com.sakurafuld.kataklysm.content.mekaArm.bow;

import com.sakurafuld.kataklysm.content.ModSounds;
import com.sakurafuld.kataklysm.network.PacketHandler;
import com.sakurafuld.kataklysm.network.mekaArm.ClientboundArrowVanish;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

import java.util.*;
import java.util.stream.Stream;

import static com.sakurafuld.kataklysm.Deets.*;

public class ArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Float> DATA_POWER = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_INACCURACY = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_SUB = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_SUB_MULTIPLIER = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_HOMING_FREQUENCY = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HOMING_DISTANCE = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HOMING_BACK = SynchedEntityData.defineId(ArrowEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean piercing = false;
    private long targetingStart = -1;
    private Vec3 origin = null;
    private final List<Integer> PIERCED = new ArrayList<>();

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
        this.origin = new Vec3(owner.getX(), owner.getEyeY() - 0.1f, owner.getZ());
    }

    public float getPower() {
        return this.getEntityData().get(DATA_POWER);
    }
    public void setPower(float power) {
        this.getEntityData().set(DATA_POWER, power);
    }
    public float getInaccuracy() {
        return this.getEntityData().get(DATA_INACCURACY);
    }
    public void setInaccuracy(float inaccuracy) {
        this.getEntityData().set(DATA_INACCURACY, inaccuracy);
    }
    public float getSubMultiplier() {
        return this.getEntityData().get(DATA_SUB_MULTIPLIER);
    }
    public void setSubMultiplier(float subMultiplier) {
        this.getEntityData().set(DATA_SUB_MULTIPLIER, subMultiplier);
    }
    public boolean isSub() {
        return this.getEntityData().get(DATA_SUB);
    }
    public void setSub(boolean sub) {
        this.getEntityData().set(DATA_SUB, sub);
    }
    public boolean isPiercing() {
        return this.piercing;
    }
    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }
    public int getHomingFrequency() {
        return this.getEntityData().get(DATA_HOMING_FREQUENCY);
    }
    public void setHomingFrequency(int homingFrequency) {
        this.getEntityData().set(DATA_HOMING_FREQUENCY, homingFrequency);
    }
    public int getHomingDistance() {
        return this.getEntityData().get(DATA_HOMING_DISTANCE);
    }
    public void setHomingDistance(int homingDistance) {
        this.getEntityData().set(DATA_HOMING_DISTANCE, homingDistance);
    }
    public boolean isHomingBack() {
        return this.getEntityData().get(DATA_HOMING_BACK);
    }
    public void setHomingBack(boolean homingBack) {
        this.getEntityData().set(DATA_HOMING_BACK, homingBack);
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
        this.getEntityData().define(DATA_HOMING_FREQUENCY, -1);
        this.getEntityData().define(DATA_HOMING_DISTANCE, 0);
        this.getEntityData().define(DATA_HOMING_BACK, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putLong("TargetingStart", this.targetingStart);
        if(this.origin != null)
            tag.put("Origin", this.newDoubleList(origin.x(), origin.y(), origin.z()));
        tag.putBoolean("Piercing", this.isPiercing());
        tag.putIntArray("Pierced", this.PIERCED);

        tag.putBoolean("HasBeenShot", this.hasBeenShot);
        tag.putBoolean("LeftOwner", this.leftOwner);
        if (this.lastState != null) {
            tag.put("LastState", NbtUtils.writeBlockState(this.lastState));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
//        this.targeting = tag.getBoolean("HomingTarget");
        this.targetingStart = tag.getLong("TargetingStar");
        if(tag.contains("Origin")) {
            ListTag origin = tag.getList("Origin", Tag.TAG_DOUBLE);
            this.origin = new Vec3(origin.getDouble(0), origin.getDouble(1), origin.getDouble(2));
        }
        this.setPiercing(tag.getBoolean("Piercing"));
        this.PIERCED.addAll(Arrays.stream(tag.getIntArray("Pierced")).boxed().toList()) ;
//        this.setHoming(pCompound.getFloat("Homing"));
//        this.setSub(pCompound.getBoolean("Sub"));
        this.hasBeenShot = tag.getBoolean("HasBeenShot");
        this.leftOwner = tag.getBoolean("LeftOwner");
        if (tag.contains("LastState", 10)) {
            this.lastState = NbtUtils.readBlockState(tag.getCompound("LastState"));
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

        boolean noPhysic = this.isNoPhysics();
        Vec3 delta = this.getDeltaMovement();

        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = delta.horizontalDistance();
            this.setYRot((float)(Mth.atan2(delta.x, delta.z) * (180 / Math.PI)));
            this.setXRot((float)(Mth.atan2(delta.y, d0) * (180 / Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos pos = this.blockPosition();
        BlockState state = this.level.getBlockState(pos);
        if (!state.isAir() && !noPhysic) {
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

        if (this.inGround && !noPhysic) {
            if (this.lastState != state && this.shouldFall()) {
                this.startFalling();
            }
            ++this.inGroundTime;

        } else {
            this.inGroundTime = 0;
            Entity owner = this.getOwner();
            Vec3 position = this.position();
            Vec3 objective = position.add(delta);
            boolean lerpMotion = true;

            boolean homing = this.getPower() * 3 >= 3 && delta.length() >= 0.5 && !this.inGround && this.getHomingFrequency() > 0 && (this.targetingStart < 0 || (this.getLevel().getGameTime() - this.targetingStart) % this.getHomingFrequency() == 0);
            if(side().isServer() && homing && !this.isRemoved()) {
                int reach = 5;
//                LOG.debug("{}-HomingDist={}", side(), this.getOwner().distanceToSqr(objective));
                if(owner == null || owner.distanceToSqr(position) > reach * reach) {
                    LOG.debug("{}-HomingUnleashed", side());
                    List<Entity> list = this.getLevel().getEntities(this, this.getBoundingBox().inflate(this.getHomingDistance()), this::canHitEntity);

//                    Vec3 reached = this.getOwner() == null
//                            ? position : this.getOwner().distanceToSqr(position) <= reach * reach
//                            ? position/*(position.add(delta.normalize().scale(reach)))*/ : position;
//
//                    double scale = this.getOwner() == null
//                            ? delta.length() : this.getOwner().distanceToSqr(position) <= reach * reach
//                            ? delta.length()/*objective.subtract(reached).length()*/ : delta.length();

                    Stream<Entity> stream = noPhysic ? list.stream() : list.stream().filter(entity -> {
                        HitResult hit = this.getLevel().clip(new ClipContext(this.position(), entity.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
//                        LOG.debug("{}-HomingFilter={}", side(), hit.getType());
                        return hit.getType() == HitResult.Type.MISS;
                    });

                    stream = owner == null || this.isHomingBack() ? stream : stream.filter(entity -> {
                        Vec3 vector = entity.getBoundingBox().getCenter().subtract(owner.getEyePosition().add(0, 0.5, 0)).normalize();
                        double angle = Math.acos(owner.getViewVector(1).normalize().dot(vector));
                        return angle < Math.toRadians(70);
                    });

                    Optional<Entity> op = stream.min(Comparator.comparingDouble(entity -> {
                        LOG.debug("{}-HomingMin", side());
                        return this.distanceTo(entity);
                    }));

                    if(op.isPresent()) {
                        if(this.targetingStart < 0)
                            this.targetingStart = this.getLevel().getGameTime();
                        Entity target = op.get();
                        LOG.debug("{}-target={}", side(), target.getDisplayName().getString());
                        Vec3 center = target.getBoundingBox().getCenter();
                        Vec3 vector = center.subtract(position).normalize().scale(delta.length());
                        this.setDeltaMovement(vector);
                        delta = this.getDeltaMovement();
//                        position = reached;
                        LOG.debug("{}-position={}", side(), position);
                        objective = position.add(vector);
                        LOG.debug("{}-objective={}", side(), objective);
//                        LOG.debug("{}-vectorScale={}", side(), vector.scale(scale).length());
                        lerpMotion = false;
                        this.getLevel().playSound(null, position.x(), position.y(), position.z(), SoundEvents.GOAT_LONG_JUMP, SoundSource.NEUTRAL, 4, 2);
                        Vec3 normalized = delta.normalize();
                        ((ServerLevel) this.getLevel()).sendParticles(ParticleTypes.CRIT, position.x(), position.y(), position.z(), 0, -normalized.x(), -normalized.y(), -normalized.z(), 2);
                    }
                }
            }

            HitResult hitresult = this.getLevel().clip(new ClipContext(position, objective, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hitresult.getType() != HitResult.Type.MISS) {
                objective = hitresult.getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityhitresult = this.findHitEntity(position, objective);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult instanceof EntityHitResult && hitresult.getType() != HitResult.Type.MISS) {
                    Entity target = ((EntityHitResult)hitresult).getEntity();
//                    Entity owner = this.getOwner();
                    if (target instanceof Player && owner instanceof Player && !((Player)owner).canHarmPlayer((Player)target)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !noPhysic && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
                    this.onHit(hitresult);
                    this.hasImpulse = true;
                }

                if (entityhitresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
            }

            delta = this.getDeltaMovement();
            double dx = delta.x;
            double dy = delta.y;
            double dz = delta.z;
            double distance = delta.horizontalDistance();

            if (noPhysic) {
                this.setYRot((float)(Mth.atan2(-dx, -dz) * (180 / Math.PI)));
            } else {
                this.setYRot((float)(Mth.atan2(dx, dz) * (180 / Math.PI)));
            }

            this.setXRot((float)(Mth.atan2(dy, distance) * (180 / Math.PI)));
            if(lerpMotion) {
                this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
                this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            }

//            objective = position.add(delta);
            double ox = objective.x();
            double oy = objective.y();
            double oz = objective.z();

            float f = 0.99F;
            if (this.isInWater()) {
                for(int j = 0; j < 4; ++j) {
                    this.getLevel().addParticle(ParticleTypes.BUBBLE, ox - dx * 0.25D, oy - dy * 0.25D, oz - dz * 0.25D, dx, dy, dz);
                }
                f = this.getWaterInertia();
            }


            this.setDeltaMovement(delta.scale(f));
            if (!this.isNoGravity() && !noPhysic) {
                Vec3 dm = this.getDeltaMovement();
                this.setDeltaMovement(dm.x, dm.y - 0.05, dm.z);
            }



            this.setPos(ox, oy, oz);
            this.checkInsideBlocks();
        }

        required(LogicalSide.SERVER).run(() -> {
            if(this.inGroundTime >= (this.isSub() ? 5 : 40)) {
                LOG.debug("{}-InGround={}", side(), this.position());
                this.discardAt(this.position());
            }

            if(this.tickCount >= 300) {
                LOG.debug("{}-Count={}", side(), this.position());
                this.discardAt(this.position());
            }
        });

    }

    @Override
    protected float getWaterInertia() {
        return (float) (super.getWaterInertia() - (this.getDeltaMovement().length() / 10));
    }

    private boolean shouldFall() {
        return this.inGround && this.level.noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
    }

    private void startFalling() {
        required(LogicalSide.SERVER).run(() -> {
            LOG.debug("{}-Falling={}", side(), this.position());
            this.discardAt(this.position());
        });
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

        for(Entity entity1 : this.getLevel().getEntities(this, this.getBoundingBox().expandTowards(pEndVec.subtract(pStartVec)).inflate(1.0D), this::canHitEntity)) {
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

        if(this.isSub())
            damage *= this.getSubMultiplier();

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
                if (living != owner && living instanceof Player && owner instanceof ServerPlayer serverPlayer&& !this.isSilent()) {
                    serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
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

    private void playHitSound() {
        float pitch = (this.random.nextFloat() * 0.2F + 0.9F);
        this.playSound(SoundEvents.ARROW_HIT, 1.5F, pitch * 2f);
        this.playSound(SoundEvents.PISTON_EXTEND, 1F,  pitch * 2f);
    }
    private void playVanish(Vec3 position, int count) {
        this.getLevel().playSound(null, position.x(), position.y(), position.z(), ModSounds.VANISH.get(), this.getSoundSource(), 1.5F, (this.random.nextFloat() * 0.2F + 0.9F) * 1.5f);
        required(LogicalSide.SERVER).run(() ->
                PacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(this.getLevel()::dimension), new ClientboundArrowVanish(position, count)));
    }

    @Override
    public boolean isCritArrow() {
        return this.getPower() >= 2;
    }

    @Override
    public boolean shotFromCrossbow() {
        return false;
    }

    @Override
    protected boolean canHitEntity(@Nullable Entity pTarget) {
//        LOG.info("{}-CanHit={}-ID={}", side(), pTarget.getDisplayName().getString(), pTarget.getId());
        if(pTarget == null)
            return false;
        if (pTarget instanceof LivingEntity && !pTarget.isSpectator() && pTarget.isAlive() && pTarget.isPickable() ) {
            Entity owner = this.getOwner();
            if(pTarget instanceof Player targetP && owner instanceof Player ownerP && !ownerP.canHarmPlayer(targetP))
                return false;
            return (owner == null || (!owner.equals(pTarget) && !owner.isPassengerOfSameVehicle(pTarget))) && (this.PIERCED.isEmpty() || !this.PIERCED.contains(pTarget.getId()));
        } else {
            return false;
        }
    }
    @Override
    public void playerTouch(Player pEntity) {
    }
}
