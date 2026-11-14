package com.example.minidesk.deck

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetSizingTest {
    // cell 64dp, gap 8dp -> one cell = 64dp, two cells = 136dp, three = 208dp

    @Test fun spanRoundsUpToWholeCells() {
        assertEquals(1, WidgetSizing.span(40f, 64f, 8f))
        assertEquals(1, WidgetSizing.span(64f, 64f, 8f))
        assertEquals(2, WidgetSizing.span(65f, 64f, 8f))
        assertEquals(2, WidgetSizing.span(136f, 64f, 8f))
        assertEquals(3, WidgetSizing.span(200f, 64f, 8f))
    }

    @Test fun nonResizableWidgetIsFixedAtDefault() {
        val s = WidgetSizing.compute(
            defWdp = 130f, defHdp = 130f, minWdp = 40f, minHdp = 40f, maxWdp = 0f, maxHdp = 0f,
            canResizeH = false, canResizeV = false, cellDp = 64f, gapDp = 8f, cols = 4,
        )
        assertEquals(Sizing(2, 2, Limits(2, 2, 2, 2)), s)
    }

    @Test fun resizableWidgetCanShrinkToMinAndGrowToColumns() {
        val s = WidgetSizing.compute(
            defWdp = 250f, defHdp = 110f, minWdp = 110f, minHdp = 40f, maxWdp = 0f, maxHdp = 0f,
            canResizeH = true, canResizeV = true, cellDp = 64f, gapDp = 8f, cols = 4,
        )
        assertEquals(4, s.defW)
        assertEquals(2, s.defH)
        assertEquals(2, s.limits.minW)
        assertEquals(1, s.limits.minH)
        assertEquals(4, s.limits.maxW)
        assertEquals(WidgetSizing.MAX_ROWS, s.limits.maxH)
    }

    @Test fun declaredMaxIsRespected() {
        val s = WidgetSizing.compute(
            defWdp = 130f, defHdp = 130f, minWdp = 60f, minHdp = 60f, maxWdp = 210f, maxHdp = 210f,
            canResizeH = true, canResizeV = true, cellDp = 64f, gapDp = 8f, cols = 4,
        )
        assertEquals(3, s.limits.maxW)
        assertEquals(3, s.limits.maxH)
    }

    @Test fun defaultWiderThanGridIsClampedToColumns() {
        val s = WidgetSizing.compute(
            defWdp = 900f, defHdp = 64f, minWdp = 64f, minHdp = 64f, maxWdp = 0f, maxHdp = 0f,
            canResizeH = true, canResizeV = false, cellDp = 64f, gapDp = 8f, cols = 4,
        )
        assertEquals(4, s.defW)
    }

    @Test fun limitsClampRespectsRoomToTheRight() {
        val l = Limits(2, 1, 4, 4)
        assertEquals(3, l.clampW(4, room = 3))
        assertEquals(2, l.clampW(1, room = 4))
        assertEquals(2, l.clampW(4, room = 1))   // room smaller than minimum: engine shifts the column left
        assertEquals(4, l.clampH(9))
    }
}
