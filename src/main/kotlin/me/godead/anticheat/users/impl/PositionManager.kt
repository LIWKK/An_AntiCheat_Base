package me.godead.anticheat.users.impl

import me.godead.anticheat.extensions.isGliding
import me.godead.anticheat.plugin.AntiCheatManager
import me.godead.anticheat.ticks.Ticks
import me.godead.anticheat.users.BoundingBox
import me.godead.anticheat.users.User
import me.godead.anticheat.utils.EvictingList
import me.godead.anticheat.utils.XMaterial
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Boat
import org.bukkit.entity.Minecart
import kotlin.math.hypot


class PositionManager(val user: User) {

    val locationHistory = EvictingList<Location>(50)

    val location get() = getLocation(0)

    val lastLocation get() = getLocation(1)

    fun getLocation(index: Int) =
        try {
            locationHistory.getReversed(index)
        } catch (ex: Exception) {
            user.player.location
        }

    var onGround = false
        private set

    var boundingBox = BoundingBox(location)
        private set

    val deltaXZ get() = hypot((location.x - lastLocation.x), (location.z - lastLocation.z))

    val deltaY get() = location.y - lastLocation.y

    val lastDeltaXZ get() = hypot((lastLocation.x - getLocation(2).x), (lastLocation.z - getLocation(2).z))

    val lastDeltaY get() = lastLocation.y - getLocation(2).x

    val deltaPitch get() = location.pitch - lastLocation.pitch

    val deltaYaw get() = location.yaw - lastLocation.yaw

    val lastDeltaPitch get() = lastLocation.pitch - getLocation(2).pitch

    val lastDeltaYaw get() = lastLocation.yaw - getLocation(2).yaw

    val slimeTicks = Ticks(-99)

    val airTicks = Ticks(-99)

    val climbableTicks = Ticks(-99)

    val elytraTicks = Ticks(-99)

    var isNearBoat = false
        private set

    fun onGround(isOnGround: Boolean) = run { onGround = isOnGround }

    fun handle(location: Location) {
        boundingBox = BoundingBox(user.player.location)
        boundingBox.expand(0.5, 0.07, 0.5).move(0.0, -0.55, 0.0)
        locationHistory.add(user.player.location)
        if (user.collisionManager.touchingAny(
                XMaterial.SLIME_BLOCK,
                XMaterial.HONEY_BLOCK
            )
        ) slimeTicks.setTicks(System.currentTimeMillis())

        if (!user.collisionManager.touchingAll(XMaterial.AIR))
            airTicks.setTicks(System.currentTimeMillis())


        if (user.collisionManager.touchingAny(
                XMaterial.LADDER,
                XMaterial.VINE,
                XMaterial.TWISTING_VINES_PLANT,
                XMaterial.WEEPING_VINES_PLANT
            )
        ) climbableTicks.setTicks(System.currentTimeMillis())

        if (user.isGliding()) elytraTicks.setTicks(System.currentTimeMillis())

        Bukkit.getScheduler().runTask(
            AntiCheatManager.plugin,
            Runnable {
                isNearBoat = user.player.getNearbyEntities(4.0, 4.0, 4.0).stream()
                    .anyMatch { entity: Any -> entity is Minecart || entity is Boat }
            })
    }

}