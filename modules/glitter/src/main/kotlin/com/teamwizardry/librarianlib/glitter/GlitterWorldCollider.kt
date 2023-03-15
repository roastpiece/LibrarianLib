package com.teamwizardry.librarianlib.glitter

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.kotlin.threadLocal
import com.teamwizardry.librarianlib.etcetera.DirectRaycaster
import com.teamwizardry.librarianlib.etcetera.IntersectingBlocksIterator
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.block.Blocks
import net.minecraft.util.math.Box
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.chunk.ChunkSection
import net.minecraft.world.chunk.ChunkStatus
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * A class designed to efficiently raytrace collisions with the world. This class uses custom raytracing code to
 * eliminate short-lived objects such as [Vec3d]s.
 *
 * This class makes two main sacrifices in the name of speed:
 *
 * 1. It doesn't clear its cache every tick. [blockCacheManager], [airCacheManager], and [clearCache] can be used to
 * clear the caches immediately if needed.
 * 2. It doesn't handle collision boxes that extend outside the bounds of their block.
 */
public object GlitterWorldCollider {

    private val blockCache = Long2ObjectOpenHashMap<List<Box>>()
    private val shapeCache = Object2ObjectOpenHashMap<VoxelShape, List<Box>>()
    private val airCache = LongOpenHashSet()
    private val intersectingIterator: IntersectingBlocksIterator by threadLocal { IntersectingBlocksIterator() }
    private val raycaster: DirectRaycaster by threadLocal { DirectRaycaster() }

    /**
     * The cache of block collision AABBs. Refreshes every 10 ticks (0.5 seconds) by default
     */
    public val blockCacheManager: CacheManager = CacheManager(10) { blockCache.clear() }

    /**
     * The cache of [VoxelShapes][VoxelShape] to AABBs. Refreshes every 1200 ticks (60 seconds) by default
     */
    public val shapeCacheManager: CacheManager = CacheManager(1200) { shapeCache.clear() }

    /**
     * The cache of chunk sections' empty status. Refreshes every 40 ticks (2 seconds) by default
     */
    public val airCacheManager: CacheManager = CacheManager(40) { airCache.clear() }

    /**
     * Request that the cache be cleared. Use this sparingly as it can negatively impact performance. Individual caches
     * can be cleared using [blockCacheManager], [shapeCacheManager], and [airCacheManager]
     *
     * This method _immediately_ clears all the caches, meaning calling it repeatedly between [collide] calls can
     * severely impact performance.
     */
    public fun clearCaches() {
        blockCache.clear()
        shapeCache.clear()
        airCache.clear()
    }

    /**
     * Traces a collision with the world given the specified start position and velocity.
     *
     * The collision uses raytracing to find the impact point on the current world's collision box. The ray begins at
     * the passed `pos` and extends in the direction and for the length of the passed `vel`. Each component of `vel`
     * is clamped to ±[maxBounds] in order to avoid accidental infinite loops or other such nastiness.
     */
    @JvmOverloads
    public fun collide(
        result: RayHitResult,
        posX: Double,
        posY: Double,
        posZ: Double,
        velX: Double,
        velY: Double,
        velZ: Double,
        maxBounds: Double = 5.0
    ) {
        result.collisionFraction = 1.0
        result.collisionNormalX = 0.0
        result.collisionNormalY = 0.0
        result.collisionNormalZ = 0.0
        result.collisionBlockX = 0
        result.collisionBlockY = 0
        result.collisionBlockZ = 0

        @Suppress("NAME_SHADOWING")
        val velX = min(maxBounds, max(-maxBounds, velX))

        @Suppress("NAME_SHADOWING")
        val velY = min(maxBounds, max(-maxBounds, velY))

        @Suppress("NAME_SHADOWING")
        val velZ = min(maxBounds, max(-maxBounds, velZ))

        val minTestX = floor(min(posX, posX + velX)).toInt()
        val minTestY = floor(min(posY, posY + velY)).toInt()
        val minTestZ = floor(min(posZ, posZ + velZ)).toInt()
        val maxTestX = floor(max(posX, posX + velX)).toInt()
        val maxTestY = floor(max(posY, posY + velY)).toInt()
        val maxTestZ = floor(max(posZ, posZ + velZ)).toInt()

        val invVelX = 1 / velX
        val invVelY = 1 / velY
        val invVelZ = 1 / velZ

        val tiny = 0.000_000_000_1

        val raycaster = this.raycaster
        raycaster.reset()

        if (minTestX == maxTestX && minTestY == maxTestY && minTestZ == maxTestZ) {
            // the entire ray is within a single block. No need to go through the iterator
            val boxes = getBoundingBoxes(minTestX, minTestY, minTestZ)
            for (i in boxes.indices) {
                val bb = boxes[i]
                if (raycaster.cast(
                        true,
                        bb.minX - tiny, bb.minY - tiny, bb.minZ - tiny,
                        bb.maxX + tiny, bb.maxY + tiny, bb.maxZ + tiny,
                        posX - minTestX, posY - minTestY, posZ - minTestZ,
                        invVelX, invVelY, invVelZ
                    )) {
                    // if there was a hit, copy the data, including the block position, to the result
                    result.collisionFraction = raycaster.distance
                    result.collisionNormalX = raycaster.normalX
                    result.collisionNormalY = raycaster.normalY
                    result.collisionNormalZ = raycaster.normalZ
                    result.collisionBlockX = minTestX
                    result.collisionBlockY = minTestY
                    result.collisionBlockZ = minTestZ
                }
            }
        } else {
            // Go through all the blocks the ray intersects with and test them
            val intersectingBlocksIterator = this.intersectingIterator
            intersectingBlocksIterator.reset(posX, posY, posZ, posX + velX, posY + velY, posZ + velZ)

            for (block in intersectingBlocksIterator) {
                val boxes = getBoundingBoxes(block.x, block.y, block.z)
                for (i in boxes.indices) {
                    val bb = boxes[i]
                    if (raycaster.cast(
                            true,
                            bb.minX - tiny, bb.minY - tiny, bb.minZ - tiny,
                            bb.maxX + tiny, bb.maxY + tiny, bb.maxZ + tiny,
                            posX - block.x, posY - block.y, posZ - block.z,
                            invVelX, invVelY, invVelZ
                        )) {
                        // if there was a hit, copy the data, including the block position, to the result
                        result.collisionFraction = raycaster.distance
                        result.collisionNormalX = raycaster.normalX
                        result.collisionNormalY = raycaster.normalY
                        result.collisionNormalZ = raycaster.normalZ
                        result.collisionBlockX = block.x
                        result.collisionBlockY = block.y
                        result.collisionBlockZ = block.z
                    }
                }
                if (result.collisionFraction != 1.0)
                    break // stop on the first hit
            }
        }
    }

    private val sectionPos = BlockPos.Mutable()
    private val mutablePos = BlockPos.Mutable()

    @Suppress("ReplacePutWithAssignment")
    private fun getBoundingBoxes(x: Int, y: Int, z: Int): List<Box> {
        val world = Client.minecraft.world ?: return emptyList()

        // blocks outside the world never have collision
        if (y < 0 || y > world.height)
            return emptyList()

        // check if the sub-chunk is known to be empty
        sectionPos.set(x shr 4, y shr 4, z shr 4)
        if (airCache.contains(sectionPos.asLong()))
            return emptyList()

        mutablePos.set(x, y, z)
        val toLong = mutablePos.asLong()
        // we can't use getOrPut because it uses the boxed Long
        blockCache.get(toLong)?.let { return it }

        // get the chunk without trying to load or generate it
        val chunk = world.getChunk(x shr 4, z shr 4, ChunkStatus.EMPTY, false)
        if (chunk == null) {
            // the entire chunk is unloaded. Mark all its sub-chunks as empty
            for (i in 0 until 16) {
                sectionPos.set(x shr 4, i, z shr 4)
                airCache.add(sectionPos.asLong())
            }
            return emptyList()
        }

        val section = chunk.sectionArray[y shr 4]
        if (section.isEmpty) {
            // if the section is empty, make note of that for future calls
            airCache.add(sectionPos.asLong())
            return emptyList()
        }

        val state = section.getBlockState(x and 15, y and 15, z and 15)

        val boxes = if (state == Blocks.AIR.defaultState || state.isAir
            || state.material.let { !it.blocksMovement() || it.isLiquid }) {
            // ignore air, non-solid, and liquid blocks
            emptyList()
        } else {
            val shape = state.getCollisionShape(world, mutablePos)
            shapeCache.getOrPut(shape) { shape.boundingBoxes }
        }

        // we survived the gauntlet, now cache the resulting list for next time
        blockCache.put(toLong, boxes)
        return boxes
    }

    public fun tickCaches() {
        blockCacheManager.tick()
        shapeCacheManager.tick()
        airCacheManager.tick()
    }

    public class CacheManager(public var interval: Int, private val clearFunction: () -> Unit) {
        private var age = 0

        public fun tick() {
            if (interval < 0) {
                age = 0
                return
            }
            age++

            if (age >= interval) {
                clear()
            }
        }

        public fun clear() {
            clearFunction()
            age = 0
        }
    }
}

public class RayHitResult {
    /**
     * The fraction along the raytrace that an impact occurred, or 1.0 if no impact occurred
     */
    public var collisionFraction: Double = 0.0

    /**
     * The X component of the impacted face's normal, or 0.0 if no impact occurred
     */
    public var collisionNormalX: Double = 0.0

    /**
     * The Y component of the impacted face's normal, or 0.0 if no impact occurred
     */
    public var collisionNormalY: Double = 0.0

    /**
     * The Z component of the impacted face's normal, or 0.0 if no impact occurred
     */
    public var collisionNormalZ: Double = 0.0

    /**
     * The X component of the impacted block's position, or 0.0 if no impact occurred. Test if [collisionFraction] is
     * less than 1.0 to tell between a collision at (0,0,0) and no collision.
     */
    public var collisionBlockX: Int = 0

    /**
     * The Y component of the impacted block's position, or 0.0 if no impact occurred. Test if [collisionFraction] is
     * less than 1.0 to tell between a collision at (0,0,0) and no collision.
     */
    public var collisionBlockY: Int = 0

    /**
     * The Z component of the impacted block's position, or 0.0 if no impact occurred. Test if [collisionFraction] is
     * less than 1.0 to tell between a collision at (0,0,0) and no collision.
     */
    public var collisionBlockZ: Int = 0
}
