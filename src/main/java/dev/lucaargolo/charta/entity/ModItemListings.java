package dev.lucaargolo.charta.entity;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.item.CardDeckItem;
import dev.lucaargolo.charta.item.ModItems;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModItemListings {

    public static final ItemListing DRINKS = new ItemListing() {
        @Nullable
        @Override
        public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
            if (trader instanceof VillagerDataHolder villagerdataholder) {
                VillagerType type = villagerdataholder.getVillagerData().getType();
                if(type == VillagerType.DESERT) {
                    return new MerchantOffer(new ItemCost(Items.EMERALD, 2 + random.nextInt(7)), ModBlocks.CACTUS_WINE_GLASS.asItem().getDefaultInstance(), 32, 12, 1f);
                }else if(type == VillagerType.SAVANNA) {
                    return new MerchantOffer(new ItemCost(Items.EMERALD, 2 + random.nextInt(7)), ModBlocks.SORGHUM_BEER_GLASS.asItem().getDefaultInstance(), 32, 12, 1f);
                }else if(type == VillagerType.TAIGA) {
                    return new MerchantOffer(new ItemCost(Items.EMERALD, 2 + random.nextInt(7)), ModBlocks.BERRY_WINE_GLASS.asItem().getDefaultInstance(), 32, 12, 1f);
                }else{
                    return new MerchantOffer(new ItemCost(Items.EMERALD, 2 + random.nextInt(7)), ModBlocks.WHEAT_BEER_GLASS.asItem().getDefaultInstance(), 32, 12, 1f);
                }
            } else {
                return null;
            }
        }
    };

    public static final ItemListing COMMON_DECKS = getDecksByRarity(Rarity.COMMON);
    public static final ItemListing UNCOMMON_DECKS = getDecksByRarity(Rarity.UNCOMMON);
    public static final ItemListing RARE_DECKS = getDecksByRarity(Rarity.RARE);
    public static final ItemListing EPIC_DECKS = getDecksByRarity(Rarity.EPIC);

    public static final ItemListing IRON_LEAD = new BasicItemListing(32, ModItems.IRON_LEAD.getDefaultInstance(), 4, 50);

    private static ItemListing getDecksByRarity(Rarity rarity) {
        return (trader, random) -> {
            List<ItemStack> decks = Charta.CARD_DECKS.getDecks().entrySet().stream().filter(c -> c.getValue().isTradeable() && c.getValue().getRarity() == rarity).map(Map.Entry::getKey).map(CardDeckItem::getDeck).toList();
            return decks.isEmpty() ? null : new MerchantOffer(new ItemCost(Items.EMERALD, (6 + random.nextInt(11)) * (rarity.ordinal() + 1)), decks.get(random.nextInt(decks.size())), 4, 25, 1f);
        };
    }

    public static class BasicItemListing implements ItemListing {

        protected final ItemStack firstInput;
        protected final ItemStack secondInput;
        protected final ItemStack output;

        protected final int limit;
        protected final int experience;
        protected final float multiplier;

        public BasicItemListing(ItemStack firstInput, ItemStack secondInput, ItemStack output, int limit, int experience, float multiplier) {
            this.firstInput = firstInput;
            this.secondInput = secondInput;
            this.output = output;
            this.limit = limit;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        public BasicItemListing(ItemStack firstInput, ItemStack output, int limit, int experience, float multiplier) {
            this(firstInput, ItemStack.EMPTY, output, limit, experience, multiplier);
        }

        public BasicItemListing(int emeralds, ItemStack output, int limit, int experience) {
            this(new ItemStack(Items.EMERALD, emeralds), output, limit, experience, 1);
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            ItemCost cost = new ItemCost(firstInput.getItemHolder(), firstInput.getCount(), DataComponentPredicate.EMPTY, firstInput); // Porting 1.20.5 do something proper for the components here
            Optional<ItemCost> optionalSecondCost = secondInput.isEmpty() ? Optional.empty() : Optional.of(new ItemCost(secondInput.getItemHolder(), secondInput.getCount(), DataComponentPredicate.EMPTY, secondInput));
            return new MerchantOffer(cost, optionalSecondCost, output, limit, experience, multiplier);
        }
    }

}
