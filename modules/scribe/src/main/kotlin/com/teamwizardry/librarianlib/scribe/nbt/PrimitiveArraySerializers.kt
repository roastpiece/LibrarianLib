package com.teamwizardry.librarianlib.scribe.nbt

import net.minecraft.nbt.NbtByteArray
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtIntArray
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtLongArray
import net.minecraft.nbt.AbstractNbtNumber

internal object PrimitiveDoubleArraySerializer: NbtSerializer<DoubleArray>() {
    override fun deserialize(tag: NbtElement, existing: DoubleArray?): DoubleArray {
        @Suppress("NAME_SHADOWING") val tag = tag.expectType<NbtList>("tag")
        val array = if(tag.size == existing?.size) existing else DoubleArray(tag.size)
        tag.forEachIndexed { index, NbtElement ->
            array[index] = NbtElement.expectType<AbstractNbtNumber>("index $index").doubleValue()
        }
        return array
    }

    override fun serialize(value: DoubleArray): NbtElement {
        return value.mapTo(NbtList()) { NbtDouble.of(it) }
    }
}

internal object PrimitiveFloatArraySerializer: NbtSerializer<FloatArray>() {
    override fun deserialize(tag: NbtElement, existing: FloatArray?): FloatArray {
        @Suppress("NAME_SHADOWING") val tag = tag.expectType<NbtList>("tag")
        val array = if(tag.size == existing?.size) existing else FloatArray(tag.size)
        tag.forEachIndexed { index, NbtElement ->
            array[index] = NbtElement.expectType<AbstractNbtNumber>("index $index").floatValue()
        }
        return array
    }

    override fun serialize(value: FloatArray): NbtElement {
        return value.mapTo(NbtList()) { NbtFloat.of(it) }
    }
}

internal object PrimitiveLongArraySerializer: NbtSerializer<LongArray>() {
    override fun deserialize(tag: NbtElement, existing: LongArray?): LongArray {
        @Suppress("NAME_SHADOWING") val tag = tag.expectType<NbtLongArray>("tag")
        if(tag.longArray.size == existing?.size) {
            tag.longArray.copyInto(existing)
            return existing
        } else {
            return tag.longArray
        }
    }

    override fun serialize(value: LongArray): NbtElement {
        return NbtLongArray(value)
    }
}

internal object PrimitiveIntArraySerializer: NbtSerializer<IntArray>() {
    override fun deserialize(tag: NbtElement, existing: IntArray?): IntArray {
        @Suppress("NAME_SHADOWING") val tag = tag.expectType<NbtIntArray>("tag")
        if(tag.intArray.size == existing?.size) {
            tag.intArray.copyInto(existing)
            return existing
        } else {
            return tag.intArray
        }
    }

    override fun serialize(value: IntArray): NbtElement {
        return NbtIntArray(value)
    }
}

internal object PrimitiveShortArraySerializer: NbtSerializer<ShortArray>() {
    override fun deserialize(tag: NbtElement, existing: ShortArray?): ShortArray {
        @Suppress("NAME_SHADOWING") val tag = tag.expectType<NbtIntArray>("tag")
        if(tag.intArray.size == existing?.size) {
            tag.intArray.forEachIndexed { index, value ->
                existing[index] = value.toShort()
            }
            return existing
        } else {
            return ShortArray(tag.intArray.size) { tag.intArray[it].toShort() }
        }
    }

    override fun serialize(value: ShortArray): NbtElement {
        return NbtIntArray(IntArray(value.size) { value[it].toInt() })
    }
}

internal object PrimitiveCharArraySerializer: NbtSerializer<CharArray>() {
    override fun deserialize(tag: NbtElement, existing: CharArray?): CharArray {
        @Suppress("NAME_SHADOWING") val tag = tag.expectType<NbtIntArray>("tag")
        if(tag.intArray.size == existing?.size) {
            tag.intArray.forEachIndexed { index, value ->
                existing[index] = value.toChar()
            }
            return existing
        } else {
            return CharArray(tag.intArray.size) { tag.intArray[it].toChar() }
        }
    }

    override fun serialize(value: CharArray): NbtElement {
        return NbtIntArray(IntArray(value.size) { value[it].toInt() })
    }
}

internal object PrimitiveByteArraySerializer: NbtSerializer<ByteArray>() {
    override fun deserialize(tag: NbtElement, existing: ByteArray?): ByteArray {
        @Suppress("NAME_SHADOWING") val tag = tag.expectType<NbtByteArray>("tag")
        if(tag.byteArray.size == existing?.size) {
            tag.byteArray.copyInto(existing)
            return existing
        } else {
            return tag.byteArray
        }
    }

    override fun serialize(value: ByteArray): NbtElement {
        return NbtByteArray(value)
    }
}

internal object PrimitiveBooleanArraySerializer: NbtSerializer<BooleanArray>() {
    override fun deserialize(tag: NbtElement, existing: BooleanArray?): BooleanArray {
        @Suppress("NAME_SHADOWING") val tag = tag.expectType<NbtByteArray>("tag")
        if(tag.byteArray.size == existing?.size) {
            tag.byteArray.forEachIndexed { index, value ->
                existing[index] = value != 0.toByte()
            }
            return existing
        } else {
            return BooleanArray(tag.byteArray.size) { tag.byteArray[it] != 0.toByte() }
        }
    }

    override fun serialize(value: BooleanArray): NbtElement {
        return NbtByteArray(ByteArray(value.size) { if(value[it]) 1.toByte() else 0.toByte() })
    }
}
