package io.github.haykam821.blocklobbers.game.lobbable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipSection;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class Lobbable {
	private final BlockState state;

	public Lobbable(BlockState state) {
		this.state = state;
	}

	public Block getBlock() {
		return this.state.getBlock();
	}

	public int getRawId() {
		return Block.getRawIdFromState(this.state);
	}

	public ItemStack createStack(boolean top) {
		ItemStackBuilder builder = ItemStackBuilder.of(this.state.getBlock());

		if (top) {
			builder.addEnchantment(Enchantments.INFINITY, 1);
			builder.hideFlag(TooltipSection.ENCHANTMENTS);
		}

		return builder.build();
	}

	/**
	 * @return whether the lobbable entity should be discarded after the collision
	 */
	public boolean onCollide(ServerWorld world, LobbableEntity entity, Vec3d pos) {
		world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, new BlockPos(pos), this.getRawId());
		return true;
	}
}
