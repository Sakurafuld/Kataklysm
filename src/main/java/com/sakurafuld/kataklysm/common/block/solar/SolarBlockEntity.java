package com.sakurafuld.kataklysm.common.block.solar;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.kataklysm.common.capability.SolarChunkProvider;
import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import com.sakurafuld.kataklysm.common.intergration.block.BlockEntityWithBotania;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.mana.spark.SparkUpgradeType;

import java.util.List;

public class SolarBlockEntity extends BlockEntityWithBotania implements IManaReceiver, ISparkAttachable, IWandHUD {
    public static final int MAX = 100000;
    public static final int RATE = 800;
    private boolean active;
    private int mana;
    private boolean packet;

    public SolarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR.get(), pos, state);
        this.active = false;
        this.mana =  0;
        this.packet = false;
    }

    //setRemovedでは絶対にパケットを送ってはいけない(2敗).
    /*@Override
    public void setRemoved() {
        super.setRemoved();
        this.level.getChunkAt(this.worldPosition).getCapability(SolarChunkProvider.SOLAR).ifPresent(solar-> {
            solar.setActive(null);
            PacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(this.level::dimension), new S2CSolarCapabilityUpdate(this.worldPosition, false));
        });
    }*/

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Mana", this.mana);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.mana = tag.getInt("Mana");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mana", this.mana);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T tile){
        SolarBlockEntity self = ((SolarBlockEntity) tile);
        level.getChunkAt(pos).getCapability(SolarChunkProvider.SOLAR)
                .ifPresent(solar-> {
                    boolean old = self.active;
                    self.active = self.mana > 0;
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.ENABLED, self.active));

                    if(old != self.active){
                        if(!solar.setActive(self.active ? self.worldPosition : null)) {
                            level.destroyBlock(pos, true);
                            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.2f, Explosion.BlockInteraction.BREAK);
                        }
                    }
                });



        if(!level.isClientSide && self.packet &&  level.getGameTime() % 10 == 0){
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
            self.packet = false;
        }


        IManaSpark spark = self.getAttachedSpark();
        if (spark != null)
            SparkHelper.getSparksAround(level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, spark.getNetwork())
                    .filter((otherSpark) -> spark != otherSpark && otherSpark.getAttachedManaReceiver() instanceof IManaPool)
                    .forEach((os) ->{
                        os.registerTransfer(spark);
                        if(spark.getUpgrade() == SparkUpgradeType.ISOLATED)
                            os.getTransfers().remove(spark);
                    });


        self.receiveMana(-RATE);
    }


    @Override
    public Level getManaReceiverLevel() {
        return this.level;
    }
    @Override
    public BlockPos getManaReceiverPos() {
        return this.worldPosition;
    }
    @Override
    public int getCurrentMana() {
        return this.mana;
    }
    @Override
    public boolean isFull() {
        return this.mana >= MAX;
    }
    @Override//Server
    public void receiveMana(int mana) {
        int old = this.mana;
        this.mana = Math.max(0, Math.min(this.mana + mana, MAX));
        if(old != this.mana){
            this.setChanged();
            this.packet = true;
        }
    }
    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }


    @Override
    public boolean canAttachSpark(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return MAX - this.mana;
    }

    @Override
    public IManaSpark getAttachedSpark() {
        List<Entity> sparks = this.level.getEntitiesOfClass(Entity.class, new AABB(this.worldPosition.above(), this.worldPosition.above().offset(1, 1, 1)), Predicates.instanceOf(IManaSpark.class));
        if (sparks.size() == 1) {
            Entity e = sparks.get(0);
            return (IManaSpark)e;
        } else {
            return null;
        }
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }

    @Override
    public void renderHUD(PoseStack poseStack, Minecraft mc) {
        int color = 4474111;
        BotaniaAPIClient.instance().drawSimpleManaHUD(poseStack, color, this.mana, MAX, (this.active ? ChatFormatting.GOLD : "") + I18n.get("block.kataklysm.solar"));
    }

}
