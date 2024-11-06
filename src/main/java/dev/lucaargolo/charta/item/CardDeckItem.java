package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardDeck;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CardDeckItem extends Item {

    public CardDeckItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        CardDeck deck = getDeck(stack);
        return deck != null ? deck.getName() : super.getName(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        CardDeck deck = getDeck(stack);
        if(deck != null) {
            tooltipComponents.add(Component.literal(String.valueOf(deck.getCards().size())).append(" ").append(Component.translatable("charta.cards")).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Nullable
    public static CardDeck getDeck(ItemStack stack) {
        ResourceLocation deckId = stack.get(ModDataComponentTypes.CARD_DECK);
        return deckId != null ? Charta.CARD_DECKS.getDeck(deckId) : null;
    }

}
