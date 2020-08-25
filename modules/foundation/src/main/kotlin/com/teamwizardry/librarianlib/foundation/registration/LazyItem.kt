package com.teamwizardry.librarianlib.foundation.registration

import net.minecraft.item.Item
import kotlin.reflect.KProperty

/**
 * A lazy access to an item instance. The result of adding an [ItemSpec] to a registration manager. Instances can be
 * created with an item directly if needed.
 */
class LazyItem(private var itemInstance: (() -> Item)?) {
    constructor(spec: ItemSpec): this(spec::itemInstance)
    constructor(itemInstance: Item): this({ itemInstance })

    /**
     * Creates an empty item which can be configured later using [from]. This is useful for creating final fields which
     * can be populated later.
     */
    constructor(): this(null)

    /**
     * Copies the other LazyItem into this LazyItem. This is useful for creating final fields which can be populated
     * later.
     */
    fun from(other: LazyItem) {
        itemInstance = other.itemInstance
    }

    /**
     * Get the item instance
     */
    fun get(): Item {
        return (itemInstance ?: throw IllegalStateException("LazyItem not initialized"))()
    }

    @JvmSynthetic
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Item {
        return get()
    }
}