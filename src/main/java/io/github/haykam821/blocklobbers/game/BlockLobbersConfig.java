package io.github.haykam821.blocklobbers.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public class BlockLobbersConfig {
	public static final Codec<BlockLobbersConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("map").forGetter(BlockLobbersConfig::getMap),
			PlayerConfig.CODEC.fieldOf("players").forGetter(BlockLobbersConfig::getPlayerConfig),
			Codec.BOOL.optionalFieldOf("consume_lobbables", true).forGetter(BlockLobbersConfig::shouldConsumeLobbables)
		).apply(instance, BlockLobbersConfig::new);
	});

	private final Identifier map;
	private final PlayerConfig playerConfig;
	private final boolean consumeLobbables;

	public BlockLobbersConfig(Identifier map, PlayerConfig playerConfig, boolean consumeLobbables) {
		this.map = map;
		this.playerConfig = playerConfig;
		this.consumeLobbables = consumeLobbables;
	}

	public Identifier getMap() {
		return this.map;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public boolean shouldConsumeLobbables() {
		return this.consumeLobbables;
	}
}