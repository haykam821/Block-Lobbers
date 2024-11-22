package io.github.haykam821.blocklobbers.game.map;

import java.io.IOException;

import io.github.haykam821.blocklobbers.game.BlockLobbersConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

public class BlockLobbersMapBuilder {
	private final BlockLobbersConfig config;

	public BlockLobbersMapBuilder(BlockLobbersConfig config) {
		this.config = config;
	}

	public BlockLobbersMap create(MinecraftServer server) {
		try {
			MapTemplate template = MapTemplateSerializer.loadFromResource(server, this.config.getMap());
			return new BlockLobbersMap(template);
		} catch (IOException exception) {
			throw new GameOpenException(Text.translatable("text.blocklobbers.template_load_failed"), exception);
		}
	}
}