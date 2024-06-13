/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.platf.dom

import kotlinx.browser.document
import org.jetbrains.letsPlot.commons.event.*
import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.intern.observable.event.EventHandler
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.platf.dom.DomEventUtil.getButton
import org.jetbrains.letsPlot.core.platf.dom.DomEventUtil.getModifiers
import org.jetbrains.letsPlot.platf.w3c.dom.events.DomEventType
import org.jetbrains.letsPlot.platf.w3c.dom.on
import org.w3c.dom.Element
import org.w3c.dom.events.WheelEvent

typealias DomMouseEvent = org.w3c.dom.events.MouseEvent
typealias DomWheelEvent = WheelEvent

private const val ENABLE_DEBUG_LOG = false

class DomMouseEventMapper(
    private val eventSource: Element,
    private val bounds: DoubleRectangle? = null,
) : MouseEventSource {
    private val mouseEventPeer = MouseEventPeer()

    private var state: MouseState = HoverState()
        set(value) {
            if (ENABLE_DEBUG_LOG) {
                println("state($${this@DomMouseEventMapper.hashCode()}): ${field::class.simpleName} -> ${value::class.simpleName}")
            }
            field = value
        }

    init {
        handle(DomEventType.CLICK) { state.onMouseEvent(DomEventType.CLICK, it) }
        handle(DomEventType.DOUBLE_CLICK) { state.onMouseEvent(DomEventType.DOUBLE_CLICK, it) }
        handle(DomEventType.MOUSE_ENTER) { state.onMouseEvent(DomEventType.MOUSE_ENTER, it) }
        handle(DomEventType.MOUSE_LEAVE) { state.onMouseEvent(DomEventType.MOUSE_LEAVE, it) }
        handle(DomEventType.MOUSE_DOWN) { state.onMouseEvent(DomEventType.MOUSE_DOWN, it) }
        handle(DomEventType.MOUSE_UP) { state.onMouseEvent(DomEventType.MOUSE_UP, it) }
        handle(DomEventType.MOUSE_MOVE) { state.onMouseEvent(DomEventType.MOUSE_MOVE, it) }
        handle(DomEventType.MOUSE_WHEEL) { state.onMouseEvent(DomEventType.MOUSE_WHEEL, it) }
    }

    private fun handle(eventSpec: DomEventType<out DomMouseEvent>, handler: (DomMouseEvent) -> Unit) {
        eventSource.on(eventSpec, consumer = {
            val needHandle = bounds?.contains(DoubleVector(it.offsetX, it.offsetY)) ?: true
            if (needHandle) {
                handler(it)
            }
        })
    }

    private fun dispatch(eventSpec: MouseEventSpec, domMouseEvent: DomMouseEvent) {
        // DON'T USE preventDefault() - there is a Chrome bug that prevents dragging outside the canvas:
        // https://issues.chromium.org/issues/41195706

        // To test the Chrome bug do the following:
        //  - launch the demo to generate the demo html file
        //  - open the demo directory
        //  - run http server in the demo directory
        //  - create the iframe.html file with the content <html><body><iframe src="http://10.251.0.253:8181/demo.html"/></body></html>
        //  - open iframe.html in Chrome
        // Now dragging outside the iframe can be tested.
        //domMouseEvent.preventDefault() // Fix for Safari to prevent selection when user drags outside a canvas

        val targetClientOrigin = eventSource.getBoundingClientRect().let { DoubleVector(it.x, it.y) }
        val targetAbsoluteOrigin = bounds?.origin ?: DoubleVector.ZERO
        val eventClientCoord = DoubleVector(domMouseEvent.clientX.toDouble(), domMouseEvent.clientY.toDouble())
        val eventTargetCoord = eventClientCoord.subtract(targetClientOrigin).subtract(targetAbsoluteOrigin)

        val x = eventTargetCoord.x.toInt()
        val y = eventTargetCoord.y.toInt()
        val button = getButton(domMouseEvent)
        val modifiers = getModifiers(domMouseEvent)

        val mouseEvent = when (domMouseEvent) {
            is WheelEvent -> MouseWheelEvent(x, y, button, modifiers, domMouseEvent.deltaY)
            else -> MouseEvent(x, y, button, modifiers)
        }

        mouseEventPeer.dispatch(eventSpec, mouseEvent)

        if (mouseEvent.preventDefault) {
            domMouseEvent.preventDefault()
        }
    }

    private abstract inner class MouseState {
        fun onMouseEvent(type: DomEventType<out DomMouseEvent>, e: DomMouseEvent) {
            log(type.name)
            handleEvent(type, e)
        }

        abstract fun handleEvent(type: DomEventType<out DomMouseEvent>, e: DomMouseEvent)

        fun log(str: String) {
            if (ENABLE_DEBUG_LOG) {
                println("${this::class.simpleName}(${this@DomMouseEventMapper.hashCode()}): $str")
            }
        }
    }

    private inner class HoverState : MouseState() {
        override fun handleEvent(type: DomEventType<out DomMouseEvent>, e: DomMouseEvent) {
            if (type == DomEventType.MOUSE_DOWN) {
                dispatch(MouseEventSpec.MOUSE_PRESSED, e)
                state = ButtonDownState(eventCoord = DoubleVector(e.x, e.y))
                return
            }

            // Any event with already pressed button -> drag from another element -> ignore events until buttons release
            if (e.buttons > 0) {
                state = ForeignDragging()
                return
            }

            when (type) {
                DomEventType.MOUSE_MOVE -> dispatch(MouseEventSpec.MOUSE_MOVED, e)
                DomEventType.MOUSE_LEAVE -> dispatch(MouseEventSpec.MOUSE_LEFT, e)
                DomEventType.MOUSE_ENTER -> dispatch(MouseEventSpec.MOUSE_ENTERED, e)
                DomEventType.DOUBLE_CLICK -> dispatch(MouseEventSpec.MOUSE_DOUBLE_CLICKED, e) // wish can handle in ButtonDownState
                DomEventType.MOUSE_WHEEL -> dispatch(MouseEventSpec.MOUSE_WHEEL_ROTATED, e as DomWheelEvent)
                DomEventType.MOUSE_UP, DomEventType.CLICK -> {} // ignore to prevent ghost clicks on UI
            }
        }
    }

    private inner class ButtonDownState(
        private val eventCoord: DoubleVector,
        private val draggingTriggerDistance: Double = 3.0
    ) : MouseState() {

        override fun handleEvent(type: DomEventType<out DomMouseEvent>, e: DomMouseEvent) {
            when (type) {
                DomEventType.MOUSE_UP -> {
                    dispatch(MouseEventSpec.MOUSE_RELEASED, e)
                }

                // It's safe to set HoverState on CLICK as DOM raises CLICK event exactly after MOUSE_UP,
                DomEventType.CLICK -> {
                    dispatch(MouseEventSpec.MOUSE_CLICKED, e)
                    state = HoverState()
                }

                DomEventType.MOUSE_MOVE -> {
                    if (DoubleVector(e.x, e.y).subtract(eventCoord).length() > draggingTriggerDistance) {
                        dispatch(MouseEventSpec.MOUSE_DRAGGED, e)
                        state = Dragging()
                    }
                }
            }
        }
    }

    private inner class Dragging : MouseState() {
        private var myDocumentMouseEventsRegistration = Registration.from(
            document.on(DomEventType.MOUSE_MOVE, ::onDocumentMouseMove),
            document.on(DomEventType.MOUSE_UP, ::onDocumentMouseUp)
        )

        private fun onDocumentMouseMove(event: DomMouseEvent) {
            dispatch(MouseEventSpec.MOUSE_DRAGGED, event)
        }

        private fun onDocumentMouseUp(e: DomMouseEvent) {
            dispatch(MouseEventSpec.MOUSE_RELEASED, e)
            state = HoverState()
            myDocumentMouseEventsRegistration.dispose()
        }

        override fun handleEvent(type: DomEventType<out DomMouseEvent>, e: DomMouseEvent) {
            // All required events handled by onDocumentMouseMove()
        }
    }

    private inner class ForeignDragging : MouseState() {
        override fun handleEvent(type: DomEventType<out DomMouseEvent>, e: DomMouseEvent) {
            if (e.buttons > 0) {
                return
            }

            state = HoverState()
        }
    }

    override fun addEventHandler(eventSpec: MouseEventSpec, eventHandler: EventHandler<MouseEvent>): Registration {
        return mouseEventPeer.addEventHandler(eventSpec, eventHandler)
    }
}