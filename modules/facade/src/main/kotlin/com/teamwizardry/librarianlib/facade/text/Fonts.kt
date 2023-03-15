package com.teamwizardry.librarianlib.facade.text

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.facade.LibLibFacade
import dev.thecodewarrior.bitfont.data.Bitfont
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

public object Fonts : SimpleResourceReloadListener<Pair<Bitfont, Bitfont>> {
    public lateinit var classic: Bitfont
        private set
    public lateinit var unifont: Bitfont
        private set

    override fun getFabricId(): Identifier = Identifier("liblib-facade:bitfont-fonts")

    override fun load(
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Pair<Bitfont, Bitfont>> {
        val classicLoc = Identifier("liblib-facade:fonts/mcclassicplus.bitfont")
        val unifontLoc = Identifier("liblib-facade:fonts/unifont.bitfont")
        return CompletableFuture.supplyAsync {
            load(manager, classicLoc) to load(manager, unifontLoc)
        }
    }

    override fun apply(
        data: Pair<Bitfont, Bitfont>,
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            classic = data.first
            unifont = data.second
        }
    }

    private fun load(manager: ResourceManager, fontLocation: Identifier): Bitfont {
        try {
            logger.debug("Loading Bitfont font $fontLocation")
            val bytes = manager.getResource(fontLocation).get().inputStream
            val font = Bitfont.unpack(bytes)
            logger.debug("Finished loading font")
            return font
        } catch (e: Exception) {
            RuntimeException("Error loading $fontLocation", e).printStackTrace()
            return Bitfont("<err>", 10, 4, 9, 6, 2)
        }
    }

    private val logger = LibLibFacade.makeLogger<Fonts>()
}
