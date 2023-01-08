package io.github.haykam821.blocklobbers.game.lobbable;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World.ExplosionSourceType;

public class ExplosiveLobbable extends Lobbable {
	public ExplosiveLobbable(BlockState state) {
		super(state);
	}

	@Override
	public boolean onCollide(ServerWorld world, LobbableEntity entity, Vec3d pos) {
		world.createExplosion(entity, pos.getX(), pos.getY(), pos.getZ(), 5, ExplosionSourceType.NONE);
		return true;
	}
}
