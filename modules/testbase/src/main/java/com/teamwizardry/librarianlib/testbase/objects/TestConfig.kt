package com.teamwizardry.librarianlib.testbase.objects

import net.minecraftforge.fml.ModLoadingContext

abstract class TestConfig {
    val modid: String = ModLoadingContext.get().activeContainer.modId

    inline fun client(block: ClientActions.() -> Unit) = ClientActions.also { it.block() }
    inline fun server(block: ServerActions.() -> Unit) = ServerActions.also { it.block() }
    inline fun common(block: CommonActions.() -> Unit) = CommonActions.also { it.block() }
}