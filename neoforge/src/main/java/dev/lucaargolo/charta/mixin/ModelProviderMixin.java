package dev.lucaargolo.charta.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.data.fabric.FabricLikeDataOutput;
import dev.lucaargolo.charta.data.fabric.FabricLikeModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(ModelProvider.class)
public class ModelProviderMixin {

	@Unique
	private FabricLikeDataOutput charta$fabricDataOutput;

	@Unique
	private static final ThreadLocal<FabricLikeDataOutput> charta$dataOutputThreadLocal = new ThreadLocal<>();

	@Unique
	private static final ThreadLocal<Map<Block, BlockStateGenerator>> charta$stateMapThreadLocal = new ThreadLocal<>();

	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(PackOutput output, CallbackInfo ci) {
		if (output instanceof FabricLikeDataOutput fabricLikeDataOutput) {
			this.charta$fabricDataOutput = fabricLikeDataOutput;
		}
	}

	@WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/models/BlockModelGenerators;run()V"))
	private void registerBlockStateModels(BlockModelGenerators instance, Operation<Void> original) {
		if ((Object) this instanceof FabricLikeModelProvider fabricModelProvider) {
			fabricModelProvider.generateBlockStateModels(instance);
		} else {
			original.call(instance);
		}
	}

	@WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/models/ItemModelGenerators;run()V"))
	private void registerItemModels(ItemModelGenerators instance, Operation<Void> original) {
		if ((Object) this instanceof FabricLikeModelProvider fabricModelProvider) {
			fabricModelProvider.generateItemModels(instance);
		} else {
			original.call(instance);
		}
	}

	@Inject(method = "run", at = @At(value = "INVOKE_ASSIGN", target = "com/google/common/collect/Maps.newHashMap()Ljava/util/HashMap;", ordinal = 0, remap = false))
	private void runHead(CachedOutput writer, CallbackInfoReturnable<CompletableFuture<?>> cir, @Local Map<Block, BlockStateGenerator> map) {
		charta$dataOutputThreadLocal.set(charta$fabricDataOutput);
		charta$stateMapThreadLocal.set(map);
	}

	@Inject(method = "run", at = @At("TAIL"))
	private void runTail(CachedOutput writer, CallbackInfoReturnable<CompletableFuture<?>> cir) {
		charta$dataOutputThreadLocal.remove();
		charta$stateMapThreadLocal.remove();
	}

	// Target the first .filter() call, to filter out blocks that are not from the mod we are processing.
	@ModifyArg(method = "run", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 0, remap = false))
	private Predicate<Map.Entry<ResourceKey<Block>, Block>> filterBlocksForProcessingMod(Predicate<Map.Entry<ResourceKey<Block>, Block>> original) {
		if (charta$fabricDataOutput != null) {
			return original
					.and(e -> charta$fabricDataOutput.isStrictValidationEnabled())
					// Skip over blocks that are not from the mod we are processing.
					.and(e -> e.getKey().location().getNamespace().equals(ChartaMod.MOD_ID));
		}

		return original;
	}

	@Inject(method = "lambda$run$4", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/models/model/ModelLocationUtils;getModelLocation(Lnet/minecraft/world/item/Item;)Lnet/minecraft/resources/ResourceLocation;"), cancellable = true)
	private static void filterItemsForProcessingMod(Set<Item> set, Map<ResourceLocation, Supplier<JsonElement>> map, Block block, CallbackInfo ci, @Local Item item) {
		FabricLikeDataOutput dataOutput = charta$dataOutputThreadLocal.get();

		if (dataOutput != null) {
			// Only generate the item model if the block state json was registered
			if (!charta$stateMapThreadLocal.get().containsKey(block)) {
				ci.cancel();
				return;
			}

			if (!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ChartaMod.MOD_ID)) {
				// Skip over any items from other mods.
				ci.cancel();
			}
		}
	}
}
