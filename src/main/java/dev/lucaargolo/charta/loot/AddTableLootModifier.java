package dev.lucaargolo.charta.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class AddTableLootModifier extends LootModifier {

    public static final Supplier<Codec<AddTableLootModifier>> CODEC = Suppliers.memoize(
        () -> RecordCodecBuilder.create(instance -> codecStart(instance)
            .and(ResourceLocation.CODEC.fieldOf("table").forGetter(m -> m.table))
            .apply(instance, AddTableLootModifier::new)
        )
    );

    private final ResourceLocation table;

    public AddTableLootModifier(LootItemCondition[] conditionsIn, ResourceLocation table) {
        super(conditionsIn);
        this.table = table;
    }

    public ResourceLocation table() {
        return this.table;
    }


    @SuppressWarnings("deprecation")
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        List<ItemStack> additionalItems = new ArrayList<>();

        context.getLevel().getServer().getLootData().getLootTable(table).getRandomItemsRaw(context, additionalItems::add);

        generatedLoot.addAll(additionalItems);

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
