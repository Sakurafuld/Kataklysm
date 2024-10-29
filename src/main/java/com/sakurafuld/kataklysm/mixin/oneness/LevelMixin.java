package com.sakurafuld.kataklysm.mixin.oneness;

import com.google.common.collect.Lists;
import com.sakurafuld.kataklysm.content.oneness.OnenessBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin {
    //JadeとかgetEnergy表示用.
    //もっといいやり方があると思うの.
    @Redirect(method = "tickBlockEntities()V", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<TickingBlockEntity> tickBlockEntitiesOneness(List<TickingBlockEntity> instance) {
        List<TickingBlockEntity> list = Lists.newArrayList();
        List<TickingBlockEntity> filtered = Lists.newArrayList(instance.stream().filter(value -> {
            if (value instanceof RebindableTickingBlockEntityWrapperAccessor tickerAccessor
                    && tickerAccessor.getTicker() instanceof BoundTickingBlockEntityAccessor blockAccessor
                    && blockAccessor.getBlockEntity() instanceof OnenessBlockEntity) {
                list.add(value);
                return false;
            }
            return true;
        }).toList());
        filtered.addAll(0, list);
        return filtered.iterator();
    }
}
