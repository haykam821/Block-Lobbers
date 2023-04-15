package io.github.haykam821.blocklobbers.game.phase;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.haykam821.blocklobbers.game.BlockLobbersConfig;
import io.github.haykam821.blocklobbers.game.map.BlockLobbersMap;
import io.github.haykam821.blocklobbers.game.player.PlayerEntry;
import io.github.haykam821.blocklobbers.game.player.WinManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerC2SPacketEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class BlockLobbersActivePhase implements BlockBreakEvent, GameActivityEvents.Enable, GameActivityEvents.Tick, GamePlayerEvents.Remove, GamePlayerEvents.Offer, ItemUseEvent, PlayerC2SPacketEvent, PlayerDamageEvent, PlayerDeathEvent {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final BlockLobbersMap map;
	private final BlockLobbersConfig config;
	private final Set<PlayerEntry> players;
	private final WinManager winManager;

	private boolean closing;

	public BlockLobbersActivePhase(GameSpace gameSpace, ServerWorld world, BlockLobbersMap map, BlockLobbersConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;

		this.players = gameSpace.getPlayers().stream().map(player -> {
			return new PlayerEntry(this, player);
		}).collect(Collectors.toSet());
		this.winManager = new WinManager(this, this.players.size() == 1);
	}

	public static void setRules(GameActivity activity) {
		activity.allow(GameRuleType.BREAK_BLOCKS);
	}

	public static void open(GameSpace gameSpace, ServerWorld world, BlockLobbersMap map, BlockLobbersConfig config) {
		gameSpace.setActivity(activity -> {
			BlockLobbersActivePhase phase = new BlockLobbersActivePhase(gameSpace, world, map, config);

			BlockLobbersWaitingPhase.setRules(activity);
			BlockLobbersActivePhase.setRules(activity);

			activity.listen(BlockBreakEvent.EVENT, phase);
			activity.listen(GameActivityEvents.ENABLE, phase);
			activity.listen(GameActivityEvents.TICK, phase);
			activity.listen(GamePlayerEvents.OFFER, phase);
			activity.listen(GamePlayerEvents.REMOVE, phase);
			activity.listen(ItemUseEvent.EVENT, phase);
			activity.listen(PlayerC2SPacketEvent.EVENT, phase);
			activity.listen(PlayerDamageEvent.EVENT, phase);
			activity.listen(PlayerDeathEvent.EVENT, phase);
		});
	}

	// Listeners
	@Override
	public ActionResult onBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry != null) {
			return entry.onBreak(player, world, pos);
		}

		return ActionResult.PASS;
	}

	@Override
	public void onEnable() {
		this.map.spawn(this.world, this.players);

		for (PlayerEntry entry : this.players) {
			entry.start();
		}
	}

	@Override
	public void onTick() {
		Iterator<PlayerEntry> playerIterator = this.players.iterator();
		while (playerIterator.hasNext()) {
			PlayerEntry entry = playerIterator.next();
			if (entry.tick()) {
				playerIterator.remove();
			}
		}

		// Attempt to determine a winner
		if (this.winManager.checkForWinner()) {
			this.closing = true;
			gameSpace.close(GameCloseReason.FINISHED);
		}
	}

	@Override
	public void onRemovePlayer(ServerPlayerEntity player) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry != null) {
			this.eliminate(entry, true);
		}
	}

	@Override
	public PlayerOfferResult onOfferPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getWaitingSpawnPos()).and(() -> {
			this.setSpectator(offer.player());
		});
	}

	@Override
	public TypedActionResult<ItemStack> onUse(ServerPlayerEntity player, Hand hand) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry != null) {
			return entry.onUse(player, hand);
		}

		ItemStack stack = player.getStackInHand(hand);
		return TypedActionResult.pass(stack);
	}

	@Override
	public ActionResult onPacket(ServerPlayerEntity player, Packet<?> packet) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry != null) {
			return entry.onPacket(player, packet);
		}

		return ActionResult.PASS;
	}

	@Override
	public ActionResult onDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	@Override
	public ActionResult onDeath(ServerPlayerEntity player, DamageSource source) {
		PlayerEntry entry = this.getPlayerEntry(player);
		if (entry != null) {
			this.eliminate(entry, true);
		}

		return ActionResult.FAIL;
	}

	// Getters
	public ServerWorld getWorld() {
		return this.world;
	}

	public GameSpace getGameSpace() {
		return this.gameSpace;
	}

	public BlockLobbersMap getMap() {
		return this.map;
	}

	public BlockLobbersConfig getConfig() {
		return this.config;
	}

	public Set<PlayerEntry> getPlayers() {
		return this.players;
	}

	// Utilities
	public PlayerEntry getPlayerEntry(ServerPlayerEntity player) {
		for (PlayerEntry entry : this.players) {
			if (player == entry.getPlayer()) {
				return entry;
			}
		}
		return null;
	}

	private void setSpectator(ServerPlayerEntity player) {
		player.changeGameMode(GameMode.SPECTATOR);
	}

	private boolean eliminate(PlayerEntry entry, String suffix, boolean remove) {
		if (this.closing) return false;

		Text message = entry.getEliminatedText(suffix);
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			player.sendMessage(message, false);
		}

		if (remove) {
			this.players.remove(entry);
		}
		this.setSpectator(entry.getPlayer());

		return true;
	}

	public boolean eliminate(PlayerEntry entry, boolean remove) {
		return this.eliminate(entry, "", remove);
	}
}