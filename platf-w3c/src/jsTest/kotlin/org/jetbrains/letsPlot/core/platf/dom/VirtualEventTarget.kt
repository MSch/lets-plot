/*
 * Copyright (c) 2024. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.platf.dom

import org.jetbrains.letsPlot.commons.event.MouseEvent
import org.jetbrains.letsPlot.commons.event.MouseEventSource
import org.jetbrains.letsPlot.commons.event.MouseEventSpec
import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.intern.observable.event.EventHandler
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.MouseEventInit
import kotlin.test.Test

class VirtualEventTarget {
    @Test
    fun test() {
        val eventsQueue = mutableListOf<Triple<String, MouseEventSpec, MouseEvent>>()
        val target = kotlinx.browser.document.createElement("div") as HTMLDivElement
        target.style.width = "1200px"
        target.style.height = "600px"

        val leftArea = DomMouseEventMapper(target, eventArea = DoubleRectangle.XYWH(0.0, 0.0, 600.0, 600.0))
        logEvents(leftArea, "left", eventsQueue)

        val rightArea = DomMouseEventMapper(target, eventArea = DoubleRectangle.XYWH(600.0, 0.0, 600.0, 600.0))
        logEvents(rightArea, "right", eventsQueue)

        target.dispatchEvent(
            org.w3c.dom.events.MouseEvent("mouseenter", object : MouseEventInit {
                override var clientX: Int? = 100
                override var clientY: Int? = 100
            })
        )
        target.dispatchEvent(
            org.w3c.dom.events.MouseEvent("mousemove", object : MouseEventInit {
                override var clientX: Int? = 300
                override var clientY: Int? = 300
            })
        )
        target.dispatchEvent(
            org.w3c.dom.events.MouseEvent("mouseleave", object : MouseEventInit {
                override var clientX: Int? = 500
                override var clientY: Int? = 500
            })
        )


        println(eventsQueue.joinToString { (id, spec, event) -> "$id: $spec: ${event.x}, ${event.y}"} )
    }

    private fun logEvents(eventTarget: MouseEventSource, id: String, eventsQueue: MutableList<Triple<String, MouseEventSpec, MouseEvent>>) {
        eventTarget.addEventHandler(MouseEventSpec.MOUSE_ENTERED,
            object : EventHandler<MouseEvent> {
                override fun onEvent(event: MouseEvent) {
                    eventsQueue += Triple(id, MouseEventSpec.MOUSE_ENTERED, event)
                }
            })

        eventTarget.addEventHandler(MouseEventSpec.MOUSE_LEFT,
            object : EventHandler<MouseEvent> {
                override fun onEvent(event: MouseEvent) {
                    eventsQueue += Triple(id, MouseEventSpec.MOUSE_LEFT, event)
                }
            })

        eventTarget.addEventHandler(MouseEventSpec.MOUSE_MOVED,
            object : EventHandler<MouseEvent> {
                override fun onEvent(event: MouseEvent) {
                    eventsQueue += Triple(id, MouseEventSpec.MOUSE_MOVED, event)
                }
            })
    }
}