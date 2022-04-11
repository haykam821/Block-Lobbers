package io.github.haykam821.blocklobbers.game.lobbable;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipSection;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class Lobbable {
	private final Block block;

	public Lobbable(Block block) {
		this.block = block;
	}

	public Block getBlock() {
		return this.block;
	}

	public ItemStack createStack(boolean top) {
		ItemStackBuilder builder = ItemStackBuilder.of(this.block);

		if (top) {
			builder.addEnchantment(Enchantments.INFINITY, 1);
			builder.hideFlag(TooltipSection.ENCHANTMENTS);
		}

		return builder.build();
	}
}
