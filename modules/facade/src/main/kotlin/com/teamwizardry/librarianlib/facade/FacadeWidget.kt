package com.teamwizardry.librarianlib.facade

import com.mojang.blaze3d.systems.RenderSystem
import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.facade.component.GuiLayer
import com.teamwizardry.librarianlib.facade.component.GuiLayerEvents
import com.teamwizardry.librarianlib.facade.component.GuiDrawContext
import com.teamwizardry.librarianlib.facade.provided.SafetyNetErrorScreen
import com.teamwizardry.librarianlib.math.Matrix3dStack
import com.teamwizardry.librarianlib.math.vec
import net.minecraft.client.gui.screen.Screen
import org.lwjgl.glfw.GLFW

/**
 *
 */
open class FacadeWidget(
    private val screen: Screen
) {
    val root = GuiLayer()
    val main = GuiLayer()

    /**
     * We keep track of the mouse position ourselves both so we can provide deltas for move events and so we can provide
     * subpixel mouse positions in [render]
     */
    private var mouseX = 0.0
    private var mouseY = 0.0

    fun mouseMoved(_xPos: Double, _yPos: Double) {
        val s = Client.guiScaleFactor // rescale to absolute screen coordinates
        val xPos = _xPos * s
        val yPos = _yPos * s
        computeMouseOver(xPos, yPos)
        root.triggerEvent(GuiLayerEvents.MouseMove(vec(xPos, yPos), vec(mouseX, mouseY)))
        mouseX = xPos
        mouseY = yPos
    }

    fun mouseClicked(_xPos: Double, _yPos: Double, button: Int) {
        val s = Client.guiScaleFactor // rescale to absolute screen coordinates
        val xPos = _xPos * s
        val yPos = _yPos * s
        computeMouseOver(xPos, yPos)
        root.triggerEvent(GuiLayerEvents.MouseDown(vec(xPos, yPos), button))
    }

    fun isMouseOver(xPos: Double, yPos: Double) {
    }

    fun mouseReleased(_xPos: Double, _yPos: Double, button: Int) {
        val s = Client.guiScaleFactor // rescale to absolute screen coordinates
        val xPos = _xPos * s
        val yPos = _yPos * s
        computeMouseOver(xPos, yPos)
        root.triggerEvent(GuiLayerEvents.MouseUp(vec(xPos, yPos), button))
    }

    fun mouseScrolled(_xPos: Double, _yPos: Double, _delta: Double) {
        val s = Client.guiScaleFactor // rescale to absolute screen coordinates
        val xPos = _xPos * s
        val yPos = _yPos * s
        val delta = _delta * s
        computeMouseOver(xPos, yPos)
        root.triggerEvent(GuiLayerEvents.MouseScroll(vec(xPos, yPos), vec(0.0, delta)))
    }

    fun mouseDragged(_xPos: Double, _yPos: Double, button: Int, _deltaX: Double, _deltaY: Double) {
        val s = Client.guiScaleFactor // rescale to absolute screen coordinates
        val xPos = _xPos * s
        val yPos = _yPos * s
        val deltaX = _deltaX * s
        val deltaY = _deltaY * s
        computeMouseOver(xPos, yPos)
        root.triggerEvent(GuiLayerEvents.MouseDrag(vec(xPos, yPos), vec(xPos - deltaX, yPos - deltaY), button))
    }

    private fun computeMouseOver(xPos: Double, yPos: Double) {
        val mouseOver = root.computeMouseInfo(vec(xPos, yPos), Matrix3dStack())
        generateSequence(mouseOver) { it.parent }.forEach {
            it.mouseOver = true
        }
    }

    fun changeFocus(reverse: Boolean) {
        // todo
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) {
        if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
            screen.onClose()
        }
        root.triggerEvent(GuiLayerEvents.KeyDown(keyCode, scanCode, modifiers))
    }

    fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
        root.triggerEvent(GuiLayerEvents.KeyUp(keyCode, scanCode, modifiers))
    }

    fun charTyped(codepoint: Char, modifiers: Int) {
        root.triggerEvent(GuiLayerEvents.CharTyped(codepoint, modifiers))
    }

    fun render() {
        try {
            val s = Client.guiScaleFactor // rescale to absolute screen coordinates
            root.pos = vec(0, 0)
            root.scale = s
            root.size = vec(Client.window.scaledWidth, Client.window.scaledHeight)
            main.pos = ((root.size - main.size) / 2).round()

            root.triggerEvent(GuiLayerEvents.Update())
//            updateLayout()

            RenderSystem.pushMatrix()
            RenderSystem.scaled(1/s, 1/s, 1.0)
            val context = GuiDrawContext(Matrix3dStack(), false)
            root.renderLayer(context)
            RenderSystem.popMatrix()
        } catch (e: Exception) {
            logger.error("Error in GUI:", e)
            Client.displayGuiScreen(SafetyNetErrorScreen(e))
        }
    }
}