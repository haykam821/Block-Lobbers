package io.github.haykam821.blocklobbers.game.player;

import java.util.ArrayList;
import java.util.List;

import io.github.haykam821.blocklobbers.game.lobbable.Lobbable;
import io.github.haykam821.blocklobbers.game.phase.BlockLobbersActivePhase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;

public class PlayerEntry implements BlockBreakEvent, ItemUseEvent {
	private static final int MAX_LOBBABLES = 3;

	private static final int HOTBAR_START_SLOT = 0;
	private static final int HOTBAR_END_SLOT = 8;

	private static final ItemStackBuilder PICKAXE = ItemStackBuilder.of(Items.IRON_PICKAXE)
		.setUnbreakable();

	private final BlockLobbersActivePhase phase;
	private final ServerPlayerEntity player;
	private final List<Lobbable> lobbables = new ArrayList<>(MAX_LOBBABLES);

	public PlayerEntry(BlockLobbersActivePhase phase, ServerPlayerEntity player) {
		this.phase = phase;
		this.player = player;
	}

	public void start() {
		this.player.changeGameMode(GameMode.SURVIVAL);
		this.player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 1, true, false, false));
		this.updateHotbar();
	}

	public boolean tick() {
		// Eliminate players that are out of bounds
		if (!this.phase.getMap().getBounds().contains(this.player.getBlockPos())) {
			this.phase.eliminate(this, false);
			return true;
		}

		return false;
	}

	// Listeners
	@Override
	public ActionResult onBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.isAir()) return ActionResult.PASS;

		if (this.lobbables.size() < MAX_LOBBABLES) {
			Lobbable lobbable = new Lobbable(state.getBlock());
			this.lobbables.add(lobbable);

			this.updateHotbar();
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public TypedActionResult<ItemStack> onUse(ServerPlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		if (!this.lobbables.isEmpty()) {
			Lobbable lobbable = this.lobbables.get(0);
			this.lobbables.remove(0);
			this.updateHotbar();

			BlockPos pos = new BlockPos(player.getEyePos());
			BlockState state = lobbable.getBlock().getDefaultState();

			Entity entity = FallingBlockEntity.spawnFromBlock(player.getWorld(), pos, state);

			Vec3d facing = Vec3d.fromPolar(player.getPitch() - 20, player.getYaw()).multiply(0.8);
			entity.setVelocity(player.getVelocity().add(facing));
			entity.velocityModified = true;
		}

		return TypedActionResult.fail(stack);
	}

	// Utilities
	public void updateHotbar() {
		Inventory inventory = this.player.getInventory();

		for (int slot = HOTBAR_START_SLOT; slot <= HOTBAR_END_SLOT; slot += 1) {
			if (slot == HOTBAR_START_SLOT) {
				inventory.setStack(HOTBAR_START_SLOT, PICKAXE.build());
			} else if (lobbables.size() > HOTBAR_END_SLOT - slot) {
				Lobbable lobbable = lobbables.get(HOTBAR_END_SLOT - slot);
				inventory.setStack(slot, lobbable.createStack(slot == HOTBAR_END_SLOT));
			} else {
				inventory.setStack(slot, ItemStack.EMPTY);
			}
		}
	}


	public Text getWinText() {
		return new TranslatableText("text.blocklobbers.win", this.getName()).formatted(Formatting.GOLD);
	}

	public Text getEliminatedText(String suffix) {
		return new TranslatableText("text.blocklobbers.eliminated" + suffix, this.getName()).formatted(Formatting.RED);
	}

	// Getters
	public BlockLobbersActivePhase getPhase() {
		return this.phase;
	}

	public ServerPlayerEntity getPlayer() {
		return this.player;
	}

	public Text getName() {
		return this.player.getDisplayName();
	}
}
