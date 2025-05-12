package dev.lucaargolo.charta.entity;

import dev.lucaargolo.charta.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class IronLeashFenceKnotEntity extends LeashFenceKnotEntity {

    public IronLeashFenceKnotEntity(EntityType<IronLeashFenceKnotEntity> entityType, Level level) {
        super(entityType, level);
    }

    public IronLeashFenceKnotEntity(Level level, BlockPos pos) {
        super(ModEntityTypes.IRON_LEASH_KNOT.get(), level);
        this.pos = pos;
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static LeashFenceKnotEntity getOrCreateIronKnot(Level level, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        List<LeashFenceKnotEntity> entities = level.getEntitiesOfClass(LeashFenceKnotEntity.class, new AABB((double)i - 1.0, (double)j - 1.0, (double)k - 1.0, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0));

        for (LeashFenceKnotEntity ironFenceKnotEntity : entities) {
            if (ironFenceKnotEntity.getPos().equals(pos)) {
                return ironFenceKnotEntity;
            }
        }

        LeashFenceKnotEntity ironFenceKnotEntity = new IronLeashFenceKnotEntity(level, pos);
        level.addFreshEntity(ironFenceKnotEntity);
        return ironFenceKnotEntity;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.IRON_LEAD.get());
    }

}
