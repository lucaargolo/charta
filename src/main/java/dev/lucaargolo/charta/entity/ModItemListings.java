package dev.lucaargolo.charta.entity;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.item.DeckItem;
import dev.lucaargolo.charta.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.BasicItemListing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ModItemListings {

    public static final ItemListing DRINKS = new ItemListing() {
        @Nullable
        @Override
        public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
            if (trader instanceof VillagerDataHolder villagerdataholder) {
                VillagerType type = villagerdataholder.getVillagerData().getType();
                if(type == VillagerType.DESERT) {
                    return new MerchantOffer(new ItemStack(Items.EMERALD, 2 + random.nextInt(7)), ModBlocks.CACTUS_WINE_GLASS.get().asItem().getDefaultInstance(), 32, 12, 1f);
                }else if(type == VillagerType.SAVANNA) {
                    return new MerchantOffer(new ItemStack(Items.EMERALD, 2 + random.nextInt(7)), ModBlocks.SORGHUM_BEER_GLASS.get().asItem().getDefaultInstance(), 32, 12, 1f);
                }else if(type == VillagerType.TAIGA) {
                    return new MerchantOffer(new ItemStack(Items.EMERALD, 2 + random.nextInt(7)), ModBlocks.BERRY_WINE_GLASS.get().asItem().getDefaultInstance(), 32, 12, 1f);
                }else{
                    return new MerchantOffer(new ItemStack(Items.EMERALD, 2 + random.nextInt(7)), ModBlocks.WHEAT_BEER_GLASS.get().asItem().getDefaultInstance(), 32, 12, 1f);
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

    public static final ItemListing IRON_LEAD = new BasicItemListing(32, ModItems.IRON_LEAD.get().getDefaultInstance(), 4, 50);

    private static ItemListing getDecksByRarity(Rarity rarity) {
        return (trader, random) -> {
            List<ItemStack> decks = Charta.CARD_DECKS.getDecks().entrySet().stream().filter(c -> c.getValue().isTradeable() && c.getValue().getRarity() == rarity).map(Map.Entry::getKey).map(DeckItem::getDeck).toList();
            return decks.isEmpty() ? null : new MerchantOffer(new ItemStack(Items.EMERALD, (6 + random.nextInt(11)) * (rarity.ordinal() + 1)), decks.get(random.nextInt(decks.size())), 4, 25, 1f);
        };
    }

}
