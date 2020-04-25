@file:Suppress("LocalVariableName")

package com.teamwizardry.librarianlib.facade.testmod

import com.teamwizardry.librarianlib.core.util.kotlin.toRl
import com.teamwizardry.librarianlib.facade.FacadeScreen
import com.teamwizardry.librarianlib.facade.component.GuiLayerEvents
import com.teamwizardry.librarianlib.facade.components.RectLayer
import com.teamwizardry.librarianlib.facade.layers.SpriteLayer
import com.teamwizardry.librarianlib.facade.layers.TextLayer
import com.teamwizardry.librarianlib.facade.text.BitfontAtlas
import com.teamwizardry.librarianlib.facade.text.attributedStringFromMC
import com.teamwizardry.librarianlib.math.vec
import com.teamwizardry.librarianlib.sprites.Texture
import com.teamwizardry.librarianlib.testbase.TestMod
import com.teamwizardry.librarianlib.testbase.objects.TestScreenConfig
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import java.awt.Color

@Mod("librarianlib-facade-test")
object LibrarianLibSpritesTestMod: TestMod("facade", "Facade", logger) {
    init {
        +FacadeScreenConfig("empty", "Empty") {

        }

        +FacadeScreenConfig("sprite", "Simple Sprite") { screen ->
            val dirt = Texture("minecraft:textures/block/dirt.png".toRl(), 16, 16)
            val layer = SpriteLayer(dirt.getSprite(""))
            screen.facade.root.add(layer)
        }

        +FacadeScreenConfig("layer_transform", "Layer Transform") { screen ->
            val dirt = Texture("minecraft:textures/block/dirt.png".toRl(), 16, 16).getSprite("")
            val stone = Texture("minecraft:textures/block/stone.png".toRl(), 16, 16).getSprite("")
            val layer = SpriteLayer(dirt)
            layer.pos = vec(32, 32)
            layer.rotation = Math.toRadians(15.0)

            val layer2 = SpriteLayer(dirt)
            layer2.pos = vec(32, 32)
            layer2.rotation = Math.toRadians(-15.0)
            layer.add(layer2)

            layer.BUS.hook<GuiLayerEvents.MouseMove> { e ->
                layer.sprite = if(layer.mouseOver) stone else dirt
            }
            layer2.BUS.hook<GuiLayerEvents.MouseMove> { e ->
                layer2.sprite = if(layer2.mouseOver) stone else dirt
            }
            screen.facade.root.add(layer)
        }

        +FacadeScreenConfig("simple_text", "Simple Text") { screen ->
            val bg = RectLayer(Color.WHITE, 0, 0, 200, 800)
            // https://minecraft.gamepedia.com/File:Minecraft_Formatting.gif
            val text = TextLayer(25, 25, 200, 800, "")
            text.text = attributedStringFromMC("""
                §nMinecraft Formatting

                §r§00 §11 §22 §33
                §44 §55 §66 §77
                §88 §99 §aa §bb
                §cc §dd §ee §ff

                §r§0k §kMinecraft
                
                §rl §lé ü ñ î
                §rl §lé ü ñ î
                
                §rm §mMinecraft
                §rn §nMinecraft
                §ro §oMinecraft
                §rr §rMinecraft
            """.trimIndent())
            text.updateText()
            screen.facade.root.add(bg, text)
        }
    }

    fun FacadeScreenConfig(id: String, name: String, block: (FacadeScreen) -> Unit): TestScreenConfig {
        return TestScreenConfig(id, name, itemGroup) {
            customScreen {
                val screen = FacadeScreen(StringTextComponent(name))
                block(screen)
                screen
            }
        }
    }
}



internal val logger = LogManager.getLogger("LibrarianLib: Facade Test")