package io.github.haykam821.blocklobbers.game.lobbable;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class LobbableEntity extends ThrownEntity implements PolymerEntity {
	private final Lobbable lobbable;

	public LobbableEntity(EntityType<? extends LobbableEntity> entityType, World world, Lobbable lobbable) {
		super(entityType, world);
		this.lobbable = lobbable;
	}

	public LobbableEntity(EntityType<? extends LobbableEntity> entityType, World world) {
		this(entityType, world, new Lobbable(Blocks.AIR.getDefaultState()));
	}

	@Override
	protected void onBlockHit(BlockHitResult hit) {
		if (!this.world.isClient()) {
			ServerWorld world = (ServerWorld) this.world;
			if (this.lobbable.onCollide(world, this, this.getPos())) {
				this.discard();
			}
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		if (!this.world.isClient()) {
			ServerWorld world = (ServerWorld) this.world;
			if (this.lobbable.onCollide(world, this, hit.getEntity().getEyePos())) {
				this.discard();
			}
		}
	}

	@Override
	public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
		return EntityType.FALLING_BLOCK;
	}
	
	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this, this.lobbable.getRawId());
	}

	@Override
	protected void initDataTracker() {
		return;
	}

	@Override
	protected float getGravity() {
		return 0.05f;
	}
}
