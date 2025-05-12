package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.DeckScreen;
import dev.lucaargolo.charta.client.item.DeckItemExtensions;
import dev.lucaargolo.charta.game.CardDeck;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class CardDeckItem extends Item {

    public CardDeckItem(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new DeckItemExtensions());
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        CardDeck deck = getDeck(stack);
        return deck != null ? deck.getName() : super.getName(stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if(level.isClientSide()) {
            CardDeck deck = getDeck(stack);
            if(deck != null)
                openScreen(deck);
        }
        return InteractionResultHolder.success(stack);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(CardDeck deck) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new DeckScreen(null, deck));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        CardDeck deck = getDeck(stack);
        if(deck != null) {
            tooltipComponents.add(Component.literal(String.valueOf(deck.getCards().size())).append(" ").append(Component.translatable("charta.cards")).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    public static boolean hasDeck(ItemStack stack) {
        return Optional.ofNullable(stack.getTag()).map(c -> c.contains("CardDeck")).orElse(false);
    }

    @Nullable
    public static CardDeck getDeck(ItemStack stack) {
        if(hasDeck(stack)) {
            ResourceLocation deckId = ResourceLocation.tryParse(stack.getOrCreateTag().getString("CardDeck"));
            return deckId != null ? Charta.CARD_DECKS.getDeck(deckId) : null;
        }else{
            return null;
        }
    }

    public static ItemStack getDeck(CardDeck cardDeck) {
        ResourceLocation deckId = Charta.CARD_DECKS.getDecks()
            .entrySet()
            .stream()
            .filter(e -> e.getValue() == cardDeck)
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(Charta.id("missing"));
        return getDeck(deckId);
    }

    public static ItemStack getDeck(ResourceLocation deckId) {
        CardDeck deck = Charta.CARD_DECKS.getDeck(deckId);
        ItemStack stack = ModItems.DECK.get().getDefaultInstance();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("Rarity", deck.getRarity().ordinal());
        tag.putString("CardDeck", deckId.toString());
        return stack;
    }

}
