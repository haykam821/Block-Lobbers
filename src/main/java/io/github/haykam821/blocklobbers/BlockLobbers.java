package io.github.haykam821.blocklobbers;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import io.github.haykam821.blocklobbers.game.BlockLobbersConfig;
import io.github.haykam821.blocklobbers.game.lobbable.LobbableBehavior;
import io.github.haykam821.blocklobbers.game.lobbable.LobbableEntity;
import io.github.haykam821.blocklobbers.game.phase.BlockLobbersWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;

public class BlockLobbers implements ModInitializer {
	public static final String MOD_ID = "blocklobbers";

	private static final Identifier BLOCK_LOBBERS_ID = new Identifier(MOD_ID, "block_lobbers");
	public static final GameType<BlockLobbersConfig> BLOCK_LOBBERS_TYPE = GameType.register(BLOCK_LOBBERS_ID, BlockLobbersConfig.CODEC, BlockLobbersWaitingPhase::open);

	private static final Identifier LOBBABLE_ID = new Identifier(MOD_ID, "lobbable");
	public static final EntityType<LobbableEntity> LOBBABLE_ENTITY_TYPE = FabricEntityTypeBuilder.create()
		.<LobbableEntity>entityFactory(LobbableEntity::new)
		.spawnGroup(SpawnGroup.MISC)
		.dimensions(EntityDimensions.changing(0.98f, 0.98f))
		.disableSaving()
		.disableSummon()
		.trackRangeChunks(10)
		.trackedUpdateRate(20)
		.build();

	@Override
	public void onInitialize() {
		Registry.register(Registries.ENTITY_TYPE, LOBBABLE_ID, LOBBABLE_ENTITY_TYPE);
		PolymerEntityUtils.registerType(LOBBABLE_ENTITY_TYPE);

		LobbableBehavior.registerDefaults();
	}
}
