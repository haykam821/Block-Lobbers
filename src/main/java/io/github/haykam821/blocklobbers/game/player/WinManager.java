package io.github.haykam821.blocklobbers.game.player;

import io.github.haykam821.blocklobbers.game.phase.BlockLobbersActivePhase;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class WinManager {
	private final BlockLobbersActivePhase phase;
	private final boolean singleplayer;

	public WinManager(BlockLobbersActivePhase phase, boolean singleplayer) {
		this.phase = phase;
		this.singleplayer = singleplayer;
	}

	private Text getNoWinnersText() {
		return new TranslatableText("text.blocklobbers.no_winners").formatted(Formatting.GOLD);
	}

	public boolean checkForWinner() {
		int playerCount = this.phase.getPlayers().size();
		if (playerCount == 1 && !singleplayer) {
			PlayerEntry player = this.phase.getPlayers().iterator().next();
			this.phase.getGameSpace().getPlayers().sendMessage(player.getWinText());

			return true;
		} else if (playerCount == 0) {
			this.phase.getGameSpace().getPlayers().sendMessage(this.getNoWinnersText());
			return true;
		}

		return false;
	}
}
