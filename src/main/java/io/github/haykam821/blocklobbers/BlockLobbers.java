package io.github.haykam821.blocklobbers;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import io.github.haykam821.blocklobbers.game.BlockLobbersConfig;
import io.github.haykam821.blocklobbers.game.lobbable.LobbableBehavior;
import io.github.haykam821.blocklobbers.game.lobbable.LobbableEntity;
import io.github.haykam821.blocklobbers.game.phase.BlockLobbersWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameType;

public class BlockLobbers implements ModInitializer {
	private static final String MOD_ID = "blocklobbers";

	private static final Identifier BLOCK_LOBBERS_ID = BlockLobbers.identifier("block_lobbers");
	public static final GameType<BlockLobbersConfig> BLOCK_LOBBERS_TYPE = GameType.register(BLOCK_LOBBERS_ID, BlockLobbersConfig.CODEC, BlockLobbersWaitingPhase::open);

	private static final Identifier LOBBABLE_ID = BlockLobbers.identifier("lobbable");
	private static final RegistryKey<EntityType<?>> LOBBABLE_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, LOBBABLE_ID);

	public static final EntityType<LobbableEntity> LOBBABLE_ENTITY_TYPE = EntityType.Builder.<LobbableEntity>create(LobbableEntity::new, SpawnGroup.MISC)
		.dimensions(0.98f, 0.98f)
		.disableSaving()
		.disableSummon()
		.maxTrackingRange(10)
		.trackingTickInterval(20)
		.build(LOBBABLE_KEY);

	@Override
	public void onInitialize() {
		Registry.register(Registries.ENTITY_TYPE, LOBBABLE_KEY, LOBBABLE_ENTITY_TYPE);
		PolymerEntityUtils.registerType(LOBBABLE_ENTITY_TYPE);

		LobbableBehavior.registerDefaults();
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
