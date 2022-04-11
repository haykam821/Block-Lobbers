package io.github.haykam821.blocklobbers;

import io.github.haykam821.blocklobbers.game.BlockLobbersConfig;
import io.github.haykam821.blocklobbers.game.phase.BlockLobbersWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;

public class BlockLobbers implements ModInitializer {
	public static final String MOD_ID = "blocklobbers";

	private static final Identifier BLOCK_LOBBERS_ID = new Identifier(MOD_ID, "block_lobbers");
	public static final GameType<BlockLobbersConfig> BLOCK_LOBBERS_TYPE = GameType.register(BLOCK_LOBBERS_ID, BlockLobbersConfig.CODEC, BlockLobbersWaitingPhase::open);

	@Override
	public void onInitialize() {
		return;
	}
}
