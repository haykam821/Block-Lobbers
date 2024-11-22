package io.github.haykam821.blocklobbers.game.lobbable;

import java.util.List;
import java.util.function.Consumer;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.MobAnchorElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.DataTracker.SerializedEntry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class LobbableEntity extends ProjectileEntity implements PolymerEntity {
	private static final Vec3d ANCHOR_OFFSET = new Vec3d(-0.5d, 0, -0.5d);

	private final Lobbable lobbable;

	private final ElementHolder holder;
	private final EntityAttachment attachment;
	private final MobAnchorElement rideAnchor = new MobAnchorElement();

	public LobbableEntity(EntityType<? extends LobbableEntity> entityType, World world, Lobbable lobbable) {
		super(entityType, world);
		this.lobbable = lobbable;

		this.holder = new ElementHolder() {
			@Override
			protected void notifyElementsOfPositionUpdate(Vec3d newPos, Vec3d delta) {
				LobbableEntity.this.rideAnchor.notifyMove(this.currentPos, newPos, delta);
			}

			@Override
			public Vec3d getPos() {
				return this.getAttachment().getPos();
			}
		};

		rideAnchor.setOffset(ANCHOR_OFFSET);
		this.holder.addElement(rideAnchor);

		BlockDisplayElement blockDisplay = new BlockDisplayElement(lobbable.getBlock().getDefaultState());
		blockDisplay.setOffset(ANCHOR_OFFSET);
		blockDisplay.ignorePositionUpdates();
		this.holder.addElement(blockDisplay);

		VirtualEntityUtils.addVirtualPassenger(this, blockDisplay.getEntityId());

		this.attachment = new EntityAttachment(this.holder, this, false);
	}

	public LobbableEntity(EntityType<? extends LobbableEntity> entityType, World world) {
		this(entityType, world, new Lobbable(Blocks.AIR.getDefaultState()));
	}

	protected boolean onBlockHit(VoxelShape shape) {
		if (!this.getWorld().isClient()) {
			ServerWorld world = (ServerWorld) this.getWorld();
			return this.lobbable.onCollide(world, this, this.getPos());
		}

		return false;
	}

	protected boolean onEntityHit(Entity entity) {
		if (!this.getWorld().isClient()) {
			ServerWorld world = (ServerWorld) this.getWorld();
			return this.lobbable.onCollide(world, this, entity.getEyePos());
		}

		return false;
	}

	@Override
	protected boolean canHit(Entity entity) {
		return super.canHit(entity) && (this.getOwner() != entity || this.age > 5);
	}

	@Override
	public void tick() {
		super.tick();

		// Update position
		Vec3d velocity = this.getVelocity();

		double x = this.getX() + velocity.getX();
		double y = this.getY() + velocity.getY();
		double z = this.getZ() + velocity.getZ();

		this.setPosition(x, y, z);

		// Update velocity
		velocity = velocity.multiply(0.99);

		if (!this.hasNoGravity()) {
			velocity = velocity.subtract(0, this.getGravity(), 0);
		}

		this.setVelocity(velocity);

		this.updateRotation();
		this.tickBlockCollision();

		World world = this.getWorld();

		// Handle collisions
		for (VoxelShape shape : world.getBlockCollisions(this, this.getBoundingBox())) {
			if (!shape.isEmpty() && this.onBlockHit(shape)) {
				this.discard();
				return;
			}
		}

		for (Entity entity : world.getOtherEntities(this, this.getBoundingBox(), this::canHit)) {
			if (this.onEntityHit(entity)) {
				this.discard();
				return;
			}
		}

		this.holder.tick();
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);

		this.holder.destroy();
		this.attachment.destroy();
	}

	@Override
	public EntityType<?> getPolymerEntityType(PacketContext context) {
		return EntityType.ARMOR_STAND;
	}

	@Override
	public void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
		if (packet instanceof EntityPassengersSetS2CPacket passengersSetS2CPacket) {
			IntList passengers = IntList.of(passengersSetS2CPacket.getPassengerIds());
			packet = VirtualEntityUtils.createRidePacket(this.rideAnchor.getEntityId(), passengers);
		}

		consumer.accept(packet);
	}
	
	@Override
	public void modifyRawTrackedData(List<SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
		data.add(DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
		data.add(DataTracker.SerializedEntry.of(ArmorStandEntity.ARMOR_STAND_FLAGS, (byte) ArmorStandEntity.MARKER_FLAG));
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		return;
	}

	protected double getGravity() {
		return 0.05;
	}
}
