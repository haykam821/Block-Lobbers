package io.github.haykam821.blocklobbers.game.player;

import java.util.Collections;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class VelocityHelper {
	private static final double LEAP_MULTIPLIER = 0.9;

	private static final double LEAP_MIN_Y = 0.15;
	private static final double STEALTHY_LEAP_MIN_Y = 0;

	public static void sendVelocityPacket(ServerPlayerEntity player, Vec3d velocity) {
		player.networkHandler.sendPacket(new ExplosionS2CPacket(0, 0, 0, 0, Collections.emptyList(), velocity));
	}

	public static void sendLeapVelocityPacket(ServerPlayerEntity player) {
		VelocityHelper.sendVelocityPacket(player, VelocityHelper.getLeapVelocity(player));
	}

	public static Vec3d getLeapVelocity(ServerPlayerEntity player) {
		Vec3d facing = Vec3d
			.fromPolar(player.getPitch(), player.getYaw())
			.multiply(LEAP_MULTIPLIER);

		double y = Math.max(VelocityHelper.getLeapMinY(player), facing.getY());
		return new Vec3d(facing.getX(), y, facing.getZ());
	}

	private static double getLeapMinY(ServerPlayerEntity player) {
		if (player.isSneaking()) {
			return STEALTHY_LEAP_MIN_Y;
		} else {
			return LEAP_MIN_Y;
		}
	}
}
