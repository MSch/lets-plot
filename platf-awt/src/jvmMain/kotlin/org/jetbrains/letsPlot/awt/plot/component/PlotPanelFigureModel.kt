/*
 * Copyright (c) 2024. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.awt.plot.component

import org.jetbrains.letsPlot.awt.plot.FigureModel
import org.jetbrains.letsPlot.awt.plot.component.PlotPanel.Companion.actualPlotComponentFromProvidedComponent
import org.jetbrains.letsPlot.core.interact.event.ToolEventDispatcher
import org.jetbrains.letsPlot.core.interact.event.ToolEventSpec.EVENT_INTERACTION_NAME
import org.jetbrains.letsPlot.core.interact.event.ToolEventSpec.EVENT_INTERACTION_ORIGIN
import org.jetbrains.letsPlot.core.interact.event.ToolEventSpec.EVENT_NAME
import org.jetbrains.letsPlot.core.interact.event.ToolEventSpec.INTERACTION_ACTIVATED
import org.jetbrains.letsPlot.core.interact.event.ToolEventSpec.INTERACTION_DEACTIVATED
import java.awt.Dimension
import javax.swing.JComponent

// See: PlotPanel.ResizeHook
internal class PlotPanelFigureModel(
    private val plotPanel: PlotPanel,
    providedComponent: JComponent?,
    private val plotComponentFactory: (Dimension) -> JComponent,
    private val applicationContext: ApplicationContext,
) : FigureModel {

    private var toolEventHandler: ((Map<String, Any>) -> Unit)? = null
    private val activeInteractionsByOrigin: MutableMap<String, MutableList<String>> = HashMap()

    private var toolEventDispatcher: ToolEventDispatcher? = toolEventDispatcherFromProvidedComponent(providedComponent)
        set(value) {
            // ToDo: deactivate, then re-activate current interactions?
            field = value
        }

    override fun onToolEvent(callback: (Map<String, Any>) -> Unit) {
        toolEventHandler = callback
    }

    override fun activateInteraction(origin: String, interactionSpec: Map<String, Any>) {
        val response: List<Map<String, Any>> = toolEventDispatcher?.activateInteraction(origin, interactionSpec)
            ?: return

        response.forEach {
            processToolEvent(it)
        }
    }

    override fun deactivateInteractions(origin: String) {
        val originAndInteractionList = activeInteractionsByOrigin.flatMap { (origin, interactionList) ->
            interactionList.map {
                Pair(origin, it)
            }
        }

        originAndInteractionList.forEach { (origin, interaction) ->
            val response: Map<String, Any>? = toolEventDispatcher?.deactivateInteraction(origin, interaction)
            if (response != null) {
                processToolEvent(response)
            }
        }
    }

    private fun processToolEvent(event: Map<String, Any>) {
        val origin = event.getValue(EVENT_INTERACTION_ORIGIN) as String
        val interactionName = event.getValue(EVENT_INTERACTION_NAME) as String
        when (event.getValue(EVENT_NAME) as String) {
            INTERACTION_ACTIVATED -> {
                activeInteractionsByOrigin.getOrPut(origin) { ArrayList<String>() }.add(interactionName)
            }

            INTERACTION_DEACTIVATED -> {
                activeInteractionsByOrigin[origin]?.remove(interactionName)
            }

            else -> {
                throw IllegalStateException("Unexpected tool event: $event")
            }
        }
        toolEventHandler?.invoke(event)
    }

    override fun updateView() {
        rebuildPlotComponent()
    }

    internal fun rebuildPlotComponent(
        onComponentCreated: (JComponent) -> Unit = {},
        expared: () -> Boolean = { false }
    ) {
        val action = Runnable {

            val containerSize = plotPanel.size
            if (containerSize == null) return@Runnable

            val providedComponent = plotComponentFactory(containerSize)
            onComponentCreated(providedComponent)
            toolEventDispatcher = toolEventDispatcherFromProvidedComponent(providedComponent)

            plotPanel.revalidate()
        }

        applicationContext.invokeLater(action, expared)
    }

    companion object {
        fun toolEventDispatcherFromProvidedComponent(providedComponent: JComponent?): ToolEventDispatcher? {
            if (providedComponent == null) return null
            val actualPlotComponent = actualPlotComponentFromProvidedComponent(providedComponent)
            return actualPlotComponent.getClientProperty(ToolEventDispatcher::class) as? ToolEventDispatcher
        }
    }
}