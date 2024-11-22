package io.github.haykam821.blocklobbers.game.phase;

import io.github.haykam821.blocklobbers.game.BlockLobbersConfig;
import io.github.haykam821.blocklobbers.game.map.BlockLobbersMap;
import io.github.haykam821.blocklobbers.game.map.BlockLobbersMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class BlockLobbersWaitingPhase implements GameActivityEvents.RequestStart, GamePlayerEvents.Accept, PlayerDamageEvent, PlayerDeathEvent {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final BlockLobbersMap map;
	private final BlockLobbersConfig config;

	public BlockLobbersWaitingPhase(GameSpace gameSpace, ServerWorld world, BlockLobbersMap map, BlockLobbersConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.BLOCK_DROPS);
		activity.deny(GameRuleType.BREAK_BLOCKS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.DISMOUNT_VEHICLE);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.FIRE_TICK);
		activity.deny(GameRuleType.FLUID_FLOW);
		activity.deny(GameRuleType.ICE_MELT);
		activity.deny(GameRuleType.MODIFY_ARMOR);
		activity.deny(GameRuleType.MODIFY_INVENTORY);
		activity.deny(GameRuleType.PICKUP_ITEMS);
		activity.deny(GameRuleType.PLACE_BLOCKS);
		activity.deny(GameRuleType.PLAYER_PROJECTILE_KNOCKBACK);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.PVP);
		activity.deny(GameRuleType.THROW_ITEMS);
		activity.deny(GameRuleType.TRIDENTS_LOYAL_IN_VOID);
		activity.deny(GameRuleType.UNSTABLE_TNT);
		activity.deny(GameRuleType.USE_BLOCKS);
		activity.deny(GameRuleType.USE_ENTITIES);
		activity.deny(GameRuleType.USE_ITEMS);
	}

	public static GameOpenProcedure open(GameOpenContext<BlockLobbersConfig> context) {
		BlockLobbersConfig config = context.game().config();
		MinecraftServer server = context.server();

		BlockLobbersMapBuilder mapBuilder = new BlockLobbersMapBuilder(config);
		BlockLobbersMap map = mapBuilder.create(server);

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
			.setGenerator(map.createGenerator(server));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			BlockLobbersWaitingPhase phase = new BlockLobbersWaitingPhase(activity.getGameSpace(), world, map, config);
			GameWaitingLobby.addTo(activity, config.getPlayerConfig());

			BlockLobbersWaitingPhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.REQUEST_START, phase);
			activity.listen(GamePlayerEvents.ACCEPT, phase);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
			activity.listen(PlayerDamageEvent.EVENT, phase);
			activity.listen(PlayerDeathEvent.EVENT, phase);
		});
	}

	// Listeners
	@Override
	public GameResult onRequestStart() {
		BlockLobbersActivePhase.open(this.gameSpace, this.world, this.map, this.config);
		return GameResult.ok();
	}

	@Override
	public JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.world, this.map.getWaitingSpawnPos()).thenRunForEach(player -> {
			player.changeGameMode(GameMode.SURVIVAL);
		});
	}

	@Override
	public EventResult onDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return EventResult.DENY;
	}

	@Override
	public EventResult onDeath(ServerPlayerEntity player, DamageSource source) {
		this.map.spawnAtWaiting(this.world, player);
		return EventResult.DENY;
	}
}