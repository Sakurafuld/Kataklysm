package com.sakurafuld.kataklysm.content.mekaArm.bow;

import com.google.common.collect.Lists;
import com.sakurafuld.kataklysm.content.ModEntities;
import com.sakurafuld.kataklysm.content.ModSounds;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleAimAdjustmentUnit;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleArrowSpeedUnit;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleBarrageUnit;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

import static com.sakurafuld.kataklysm.Deets.*;

public class BowItem extends ItemEnergized implements IModuleContainerItem, IModeItem {
    private static class Shoot {
        private FloatingLong energy;
        private float power;
        private float inaccuracy;
        private boolean cancel;

        public Shoot(FloatingLong energy, float power, float inaccuracy) {
            this.energy = energy;
            this.power = power;
            this.inaccuracy = inaccuracy;
            this.cancel = power < 0.1;
        }
    }

    public BowItem(Properties properties) {
        super(() -> MekanismConfig.gear.mekaToolBaseChargeRate.get().multiply(0.5), () -> MekanismConfig.gear.mekaToolBaseEnergyCapacity.get().multiply(0.5), properties.rarity(Rarity.EPIC).setNoRepair());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }
    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if(energyContainer == null || (!pPlayer.getAbilities().instabuild && energyContainer.getEnergy().smallerThan(this.getShootRate())))
            return InteractionResultHolder.fail(stack);

        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.consume(stack);
    }
    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if((stack.getUseDuration() - player.getUseItemRemainingTicks()) / 20f == 0.9f) {
            float pitch = (player.getRandom().nextFloat() * 0.2F + 0.9F);
            player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), MekanismSounds.LOGISTICAL_SORTER.get(), SoundSource.PLAYERS, 0.8f, pitch * 3f);
            player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.PLAYERS, 0.8f, 2f);
        }
    }
    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if(!(pLivingEntity instanceof Player player)) return;

        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(pStack, 0);
        if(energyContainer == null) return;

//        FloatingLong energy = this.getShootRate();
//
//        float power = this.getPowerForTime(this.getUseDuration(pStack) - pTimeCharged);
//        float inaccuracy = 1;
//        boolean cancel = power < 0.1;

        List<Consumer<ArrowEntity>> post = Lists.newArrayList();
        NonNullList<Consumer<Shoot>> nonnull = NonNullList.withSize(8, shoot -> {});

        for(Module<?> module : this.getModules(pStack)){
            if(module.isEnabled()){
                if(module.getCustomInstance() instanceof ModuleArrowSpeedUnit arrowSpeed) {
                    nonnull.set(0, shoot -> {
                        LOG.debug("{}-ArrowSpeed", side());
                        shoot.power *= arrowSpeed.getSpeed();
                        shoot.energy = shoot.energy.multiply(shoot.power).multiply(arrowSpeed.getEnergyMultiplier());
                        shoot.inaccuracy *= arrowSpeed.getInaccuracy();
                        shoot.cancel = (arrowSpeed.getSpeed() > 0.5f && shoot.power < 0.1 * arrowSpeed.getSpeed());
                    });
//                    power *= arrowSpeed.getSpeed();
//                    energy = energy.multiply(power).multiply(arrowSpeed.getEnergyMultiplier());
//                    inaccuracy *= arrowSpeed.getInaccuracy();
//                    cancel = (arrowSpeed.getSpeed() > 0.5f && power < 0.1 * arrowSpeed.getSpeed());
                }
                if(module.getCustomInstance() instanceof ModuleBarrageUnit barrage) {
                    if(this.getUseDuration(pStack) - pTimeCharged >= 10) {
                        post.add(arrow -> {
                            required(LogicalSide.SERVER).run(() -> {
                                if(!pStack.getOrCreateTag().contains("Subarrow")) {
                                    pStack.getOrCreateTag().putInt("Subarrow", barrage.getCount() * 100);
                                }
                            });
                        });
                    }
                }
                if(module.getCustomInstance() instanceof ModuleAimAdjustmentUnit aimAdjustment) {
                    nonnull.set(1, shoot -> {
                        LOG.debug("{}-AimAdjustment", side());
                        shoot.inaccuracy *= aimAdjustment.getAdjustment();
                    });

//                    post.add(arrow -> {
//                        arrow.setInaccuracy(arrow.getInaccuracy() * aimAdjustment.getAdjustment());
//                        LOG.debug("{}-Aiming={}", side(), arrow.getInaccuracy());
//                    });
                }
                /*
                * 近接8/
                * 速度4/
                * ブレ2/
                * 貫通6Piercing
                * 　ブロック貫通1Ghosting
                * ホーミング４Homing
                * 連射4Barrage/
                * 追い撃ち3Ambush Shot
                * 重力2Aerodynamics control
                * サブアローにも強化1
                *
                * */
            }
        }

        Shoot shoot = new Shoot(this.getShootRate(), this.getPowerForTime(this.getUseDuration(pStack) - pTimeCharged), 1);

        for(Consumer<Shoot> shooter : nonnull) {
            shooter.accept(shoot);
        }

        if(shoot.cancel || (!player.getAbilities().instabuild && energyContainer.getEnergy().smallerThan(shoot.energy))) {
            return;
        }

        ArrowEntity arrow = new ArrowEntity(ModEntities.ARROW.get(), pLevel, player);
        arrow.setPower(shoot.power);
        arrow.setInaccuracy(shoot.inaccuracy);

        for(Consumer<ArrowEntity> consumer : post) {
            consumer.accept(arrow);
        }

        this.shoot(arrow, player);

        pStack.getOrCreateTag().putString("LastEnergy", shoot.energy.toString());
        pStack.getOrCreateTag().putFloat("LastPower", arrow.getPower());
        pStack.getOrCreateTag().putFloat("LastInaccuracy", arrow.getInaccuracy());

        if(!player.getAbilities().instabuild)
            energyContainer.extract(shoot.energy, Action.EXECUTE, AutomationType.MANUAL);

    }
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if(!(pEntity instanceof Player player)) return;
        //ItemProperties用.
        pStack.getOrCreateTag().putBoolean("Selected", pIsSelected);

        required(LogicalSide.SERVER).run(() -> {
            CompoundTag tag = pStack.getOrCreateTag();
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(pStack, 0);
            if(energyContainer != null && tag.contains("Subarrow")) {
                if (pIsSelected) {
//                    LOG.debug("{}-ContainsSubarrow", side());
                    int count = tag.getInt("Subarrow");

                    tag.putInt("Subarrow", count - 1);

                    if (tag.getInt("Subarrow") <= 0) {
                        LOG.debug("{}-RemoveLowerZERO", side());
                        tag.remove("Subarrow");
                    }


                    switch (count) {
                        case 399, 398, 397, 396,
                                298, 296, 294,
                                198, 196,
                                98 -> {
//                            LOG.debug("{}-CountDown", side());
                            FloatingLong energy = FloatingLong.parseFloatingLong(tag.getString("LastEnergy")).multiply(0.5);
                            float power = (tag.getFloat("LastPower") * (count == 98 ? 1 : (float) 2 / 3));
                            float inaccuracy = (tag.getFloat("LastInaccuracy"));
//                            energy = energy.multiply(power);
                            power *= player.getRandom().nextFloat(0.8f, 1.2f);
                            if (!player.getAbilities().instabuild && energyContainer.getEnergy().smallerThan(energy)) {
                                LOG.debug("{}-ReturnSmallerEnergy={}", side(), energy);
                                return;
                            }

                            ArrowEntity arrow = new ArrowEntity(ModEntities.ARROW.get(), pLevel, player);
                            arrow.setSub(true);
                            arrow.setPower(power);
                            arrow.setInaccuracy(inaccuracy);
                            //0.2=7, 0.3=10, 0.4=13, 0.6=22
                            if (count >= 101) {
                                arrow.setSubMultiplier((float) 1 / 2);
                            }
                            if (count >= 201) {
                                arrow.setSubMultiplier((float) 1 / 3);
                            }
                            if (count >= 301) {
                                arrow.setSubMultiplier((float) 1 / 4);
                            }

                            this.shoot(arrow, player);

                            if (!player.getAbilities().instabuild)
                                energyContainer.extract(energy, Action.EXECUTE, AutomationType.MANUAL);

                            switch (count) {
                                case 396, 294, 196, 98 -> {
//                                    LOG.debug("{}-RemoveCountDowned", side());

                                    tag.remove("Subarrow");
                                }
                            }
                        }
                    }
                } else {
//                    LOG.debug("{}-RemoveNonSelected", side());
                    tag.remove("Subarrow");
                }
            }
        });

    }
    private void shoot(ArrowEntity arrow, Player player){
//        if(power < 0.1f) return;
//        LOG.debug("{}-ShootPower={}-=isSub={}", side(), arrow.getPower() * 3f, arrow.isSub());
        arrow.setPos(player.getX(), player.getEyeY() - 0.1f, player.getZ());
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0,  arrow.getPower() * 3f, arrow.getInaccuracy());
        player.getLevel().addFreshEntity(arrow);
        required(LogicalSide.SERVER).run(() -> {
            float pitch = (player.getRandom().nextFloat() * 0.2F + 0.9F);
            if(arrow.getPower() < 2 || arrow.isSub())
                player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1f, 3F / pitch);
            else player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CHARGE_SHOOT.get(), SoundSource.PLAYERS, 1f, 3F / pitch);
            player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GOAT_LONG_JUMP, SoundSource.PLAYERS, 1f, 1.5F / pitch);
        });
    }

    private float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && super.isFoil(stack) && IModuleContainerItem.hasOtherEnchants(stack);
    }
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        IModule<ModuleAttackAmplificationUnit> attackModule = this.getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        if (attackModule != null && attackModule.isEnabled() && action == ToolActions.SWORD_DIG) {
            return true;
        }
        return this.getModules(stack).stream().anyMatch(module -> module.isEnabled() && this.canPerformAction(module, action));
    }
    private <MODULE extends ICustomModule<MODULE>> boolean canPerformAction(IModule<MODULE> module, ToolAction action) {
        return module.getCustomInstance().canPerformAction(module, action);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        for (Module<?> module : this.getModules(context.getItemInHand())) {
            if (module.isEnabled()) {
                InteractionResult result = this.onModuleUse(module, context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.useOn(context);
    }
    private <MODULE extends ICustomModule<MODULE>> InteractionResult onModuleUse(IModule<MODULE> module, UseOnContext context) {
        return module.getCustomInstance().onItemUse(module, context);
    }
    @Nonnull
    @Override
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand) {
        for (Module<?> module : this.getModules(stack)) {
            if (module.isEnabled()) {
                InteractionResult result = this.onModuleInteract(module, player, entity, hand);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }
    private <MODULE extends ICustomModule<MODULE>> InteractionResult onModuleInteract(IModule<MODULE> module, @Nonnull Player player, @Nonnull LivingEntity entity,
                                                                                      @Nonnull InteractionHand hand) {
        return module.getCustomInstance().onInteract(module, player, entity, hand);
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull Entity target) {
        if (target.getType().is(MekanismTags.Entities.HURTABLE_VEHICLES)) {
            if (target.isAttackable() && !target.skipAttackInteraction(player)) {
                int maxDamage = MekanismConfig.gear.mekaToolBaseDamage.get();
                IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = this.getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
                if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
                    maxDamage += attackAmplificationUnit.getCustomInstance().getDamage();
                }
                target.hurt(DamageSource.playerAttack(player), maxDamage);
            }
        }
        return super.onLeftClickEntity(stack, player, target);
    }
    @Override
    public boolean hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
        FloatingLong energyCost = FloatingLong.ZERO;
        int minDamage = 0, maxDamage = minDamage;
        IModule<ModuleAttackAmplificationUnit> attackAmplificationUnit = getModule(stack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
        if (attackAmplificationUnit != null && attackAmplificationUnit.isEnabled()) {
            maxDamage += attackAmplificationUnit.getCustomInstance().getDamage();
            if (maxDamage > minDamage) {
                energyCost = MekanismConfig.gear.mekaToolEnergyUsageWeapon.get().multiply(32).multiply((maxDamage - minDamage) / 4F);
            }
            minDamage = Math.min(minDamage, maxDamage);
        }
        int damageDifference = maxDamage - minDamage;

        double percent = 1;
        if (energy.smallerThan(energyCost)) {
            percent = energy.divideToLevel(energyCost);
        }
        float damage = (float) (minDamage + damageDifference * percent);
        boolean creative = false;
        if (attacker instanceof Player player) {
            creative = player.getAbilities().instabuild;
            target.hurt(DamageSource.playerAttack(player), creative ? maxDamage : damage);
        } else {
            target.hurt(DamageSource.mobAttack(attacker), damage);
        }
        if (energyContainer != null && !energy.isZero() && !creative) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            this.addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return IModeItem.super.supportsSlotType(stack, slotType) && this.getModules(stack).stream().anyMatch(Module::handlesModeChange);
    }
    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, boolean displayChangeMessage) {
        for (Module<?> module : this.getModules(stack)) {
            if (module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChangeMessage);
                return;
            }
        }
    }

    public FloatingLong getShootRate(){
        return MekanismConfig.gear.mekaToolEnergyUsageWeapon.get().multiply(5);
    }
    @Override
    protected FloatingLong getChargeRate(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = this.getModule(stack, MekanismModules.ENERGY_UNIT);
        return module != null ? MekanismConfig.gear.mekaToolBaseChargeRate.get().multiply(Math.pow(2, module.getInstalledCount())).multiply(0.5) : MekanismConfig.gear.mekaToolBaseChargeRate.get().multiply(0.5);
    }
    @Override
    protected FloatingLong getMaxEnergy(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = this.getModule(stack, MekanismModules.ENERGY_UNIT);
        return module != null ? MekanismConfig.gear.mekaToolBaseEnergyCapacity.get().multiply(Math.pow(2, module.getInstalledCount())).multiply(0.5) : MekanismConfig.gear.mekaToolBaseEnergyCapacity.get().multiply(0.5);
    }
}
