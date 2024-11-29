package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.Charta;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardDecks {

    public static final Map<ResourceLocation, CardDeck> DECKS = new HashMap<>();
    public static final Map<String, List<ResourceLocation>> GROUPS = new HashMap<>();
    
    public static final CardDeck STANDARD_BLACK = register(Charta.id("standard/black"), CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/black")));
    public static final CardDeck STANDARD_BLUE = register(Charta.id("standard/blue"), CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/blue")));
    public static final CardDeck STANDARD_GREEN = register(Charta.id("standard/green"), CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/green")));
    public static final CardDeck STANDARD_RED = register(Charta.id("standard/red"), CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/red")));
    public static final CardDeck STANDARD_YELLOW = register(Charta.id("standard/yellow"), CardDeck.simple(Rarity.COMMON, true, Charta.id("standard"), Charta.id("standard/yellow")));

    public static final CardDeck LIGHT_AQUA = register(Charta.id("light/aqua"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/aqua")));
    public static final CardDeck LIGHT_BLUE = register(Charta.id("light/blue"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/blue")));
    public static final CardDeck LIGHT_GREEN = register(Charta.id("light/green"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/green")));
    public static final CardDeck LIGHT_ORANGE = register(Charta.id("light/orange"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/orange")));
    public static final CardDeck LIGHT_PINK = register(Charta.id("light/pink"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/pink")));
    public static final CardDeck LIGHT_RED = register(Charta.id("light/red"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/red")));
    public static final CardDeck LIGHT_YELLOW = register(Charta.id("light/yellow"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("light/yellow")));

    public static final CardDeck DARK_AQUA = register(Charta.id("dark/aqua"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/aqua")));
    public static final CardDeck DARK_BLUE = register(Charta.id("dark/blue"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/blue")));
    public static final CardDeck DARK_GREEN = register(Charta.id("dark/green"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/green")));
    public static final CardDeck DARK_ORANGE = register(Charta.id("dark/orange"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/orange")));
    public static final CardDeck DARK_PINK = register(Charta.id("dark/pink"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/pink")));
    public static final CardDeck DARK_RED = register(Charta.id("dark/red"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/red")));
    public static final CardDeck DARK_YELLOW = register(Charta.id("dark/yellow"), CardDeck.simple(Rarity.UNCOMMON, true, Charta.id("light"), Charta.id("dark"), Charta.id("dark/yellow")));

    public static final CardDeck INVERTED = register(Charta.id("inverted"), CardDeck.simple(Rarity.RARE, true, Charta.id("inverted"), Charta.id("inverted")));

    public static final CardDeck FUN = register(Charta.id("fun"), CardDeck.fun(Rarity.RARE, true, Charta.id("fun"), Charta.id("fun")));
    public static final CardDeck FUN_MINIMAL = register(Charta.id("fun_minimal"), CardDeck.fun(Rarity.RARE, true, Charta.id("fun"), Charta.id("fun_minimal"), Charta.id("fun_minimal")));
    public static final CardDeck FUN_LIGHT = register(Charta.id("fun_light"), CardDeck.fun(Rarity.RARE, true, Charta.id("fun_light"), Charta.id("fun_light")));

    public static final CardDeck FLAGS_ARGENTINA = register(Charta.id("flags/argentina"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/argentina")));
    public static final CardDeck FLAGS_AUSTRALIA = register(Charta.id("flags/australia"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/australia")));
    public static final CardDeck FLAGS_BELGIUM = register(Charta.id("flags/belgium"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/belgium")));
    public static final CardDeck FLAGS_BRAZIL = register(Charta.id("flags/brazil"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/brazil")));
    public static final CardDeck FLAGS_CANADA = register(Charta.id("flags/canada"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/canada")));
    public static final CardDeck FLAGS_CHINA = register(Charta.id("flags/china"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/china")));
    public static final CardDeck FLAGS_FRANCE = register(Charta.id("flags/france"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/france")));
    public static final CardDeck FLAGS_GERMANY = register(Charta.id("flags/germany"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/germany")));
    public static final CardDeck FLAGS_INDIA = register(Charta.id("flags/india"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/india")));
    public static final CardDeck FLAGS_INDONESIA = register(Charta.id("flags/indonesia"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/indonesia")));
    public static final CardDeck FLAGS_IRELAND = register(Charta.id("flags/ireland"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/ireland")));
    public static final CardDeck FLAGS_ITALY = register(Charta.id("flags/italy"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/italy")));
    public static final CardDeck FLAGS_JAPAN = register(Charta.id("flags/japan"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/japan")));
    public static final CardDeck FLAGS_MEXICO = register(Charta.id("flags/mexico"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/mexico")));
    public static final CardDeck FLAGS_NETHERLANDS = register(Charta.id("flags/netherlands"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/netherlands")));
    public static final CardDeck FLAGS_PHILIPPINES = register(Charta.id("flags/philippines"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/philippines")));
    public static final CardDeck FLAGS_POLAND = register(Charta.id("flags/poland"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/poland")));
    public static final CardDeck FLAGS_PORTUGAL = register(Charta.id("flags/portugal"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/portugal")));
    public static final CardDeck FLAGS_RAINBOW = register(Charta.id("flags/rainbow"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/rainbow")));
    public static final CardDeck FLAGS_RUSSIA = register(Charta.id("flags/russia"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/russia")));
    public static final CardDeck FLAGS_SPAIN = register(Charta.id("flags/spain"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/spain")));
    public static final CardDeck FLAGS_THAILAND = register(Charta.id("flags/thailand"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/thailand")));
    public static final CardDeck FLAGS_UKRAINE = register(Charta.id("flags/ukraine"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/ukraine")));
    public static final CardDeck FLAGS_UNITED_KINGDOM = register(Charta.id("flags/united_kingdom"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/united_kingdom")));
    public static final CardDeck FLAGS_USA = register(Charta.id("flags/usa"), CardDeck.simple(Rarity.COMMON, false, Charta.id("standard"), Charta.id("flags/usa")));

    public static final CardDeck NEON_AQUA = register(Charta.id("neon/aqua"), CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/aqua")));
    public static final CardDeck NEON_BLUE = register(Charta.id("neon/blue"), CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/blue")));
    public static final CardDeck NEON_GREEN = register(Charta.id("neon/green"), CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/green")));
    public static final CardDeck NEON_ORANGE = register(Charta.id("neon/orange"), CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/orange")));
    public static final CardDeck NEON_PINK = register(Charta.id("neon/pink"), CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/pink")));
    public static final CardDeck NEON_RED = register(Charta.id("neon/red"), CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/red")));
    public static final CardDeck NEON_YELLOW = register(Charta.id("neon/yellow"), CardDeck.simple(Rarity.UNCOMMON, false, Charta.id("neon"), Charta.id("neon/yellow")));

    public static final CardDeck FUN_INVERTED = register(Charta.id("fun_inverted"), CardDeck.fun(Rarity.RARE, false, Charta.id("fun_inverted"), Charta.id("fun_inverted")));
    public static final CardDeck FUN_CLASSIC = register(Charta.id("fun_classic"), CardDeck.fun(Rarity.RARE, false, Charta.id("fun_classic"), Charta.id("fun_classic")));

    public static final CardDeck METALS_COPPER = register(Charta.id("metals/copper"), CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("metals/copper"), Charta.id("metals/copper")));
    public static final CardDeck METALS_IRON = register(Charta.id("metals/iron"), CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("metals/iron"), Charta.id("metals/iron")));
    public static final CardDeck METALS_GOLD = register(Charta.id("metals/gold"), CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("metals/gold"), Charta.id("metals/gold")));

    public static final CardDeck GEMS_DIAMOND = register(Charta.id("gems/diamond"), CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/diamond"), Charta.id("gems/diamond")));
    public static final CardDeck GEMS_EMERALD = register(Charta.id("gems/emerald"), CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/emerald"), Charta.id("gems/emerald")));
    public static final CardDeck GEMS_RUBY = register(Charta.id("gems/ruby"), CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/ruby"), Charta.id("gems/ruby")));
    public static final CardDeck GEMS_SAPPHIRE = register(Charta.id("gems/sapphire"), CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/sapphire"), Charta.id("gems/sapphire")));
    public static final CardDeck GEMS_AMETHYST = register(Charta.id("gems/amethyst"), CardDeck.simple(Rarity.RARE, false, Charta.id("metallic"), Charta.id("gems/amethyst"), Charta.id("gems/amethyst")));

    public static CardDeck register(ResourceLocation resourceLocation, CardDeck deck) {
        if(DECKS.containsKey(resourceLocation)) {
            throw new IllegalArgumentException("Duplicate resource location: " + resourceLocation);
        }
        if(resourceLocation.getPath().split("/").length == 2) {
            String group = resourceLocation.getPath().split("/")[0];
            GROUPS.computeIfAbsent(group, k -> new ArrayList<>()).add(resourceLocation);
        }
        DECKS.put(resourceLocation, deck);
        return deck;
    }
    
}
