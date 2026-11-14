package com.example.minidesk.deck

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GridEngineTest {
    private val engine = GridEngine(4)

    private fun assertValid(cells: List<Cell>) {
        for (c in cells) {
            assertTrue("inside columns: $c", c.col >= 0 && c.right <= 4 && c.row >= 0 && c.w >= 1 && c.h >= 1)
        }
        for (i in cells.indices) for (j in i + 1 until cells.size) {
            assertFalse("overlap: ${cells[i]} vs ${cells[j]}", cells[i].overlaps(cells[j]))
        }
    }

    @Test fun clampKeepsCellInsideColumns() {
        assertEquals(Cell("a", 2, 0, 2, 1), engine.clamp(Cell("a", 3, -5, 2, 0)))
        assertEquals(Cell("a", 0, 0, 4, 1), engine.clamp(Cell("a", 1, 0, 9, 1)))
    }

    @Test fun moveOntoNeighbourPushesItDown() {
        val items = listOf(Cell("a", 0, 0, 2, 2), Cell("b", 2, 0, 2, 2))
        val out = engine.place(items, items[0].copy(col = 2)).associateBy { it.id }
        assertEquals(Cell("a", 2, 0, 2, 2), out.getValue("a"))
        assertEquals(2, out.getValue("b").row)
    }

    @Test fun pushCascades() {
        val items = listOf(Cell("a", 0, 0, 4, 1), Cell("b", 0, 1, 4, 1), Cell("c", 0, 2, 4, 1))
        val out = engine.place(items, Cell("n", 0, 0, 4, 1)).associateBy { it.id }
        assertEquals(0, out.getValue("n").row)
        assertEquals(1, out.getValue("a").row)
        assertEquals(2, out.getValue("b").row)
        assertEquals(3, out.getValue("c").row)
    }

    @Test fun nonCollidingItemsDoNotMove() {
        val items = listOf(Cell("a", 0, 0, 2, 2), Cell("far", 0, 6, 2, 2))
        val out = engine.place(items, items[0].copy(col = 2)).associateBy { it.id }
        assertEquals(Cell("far", 0, 6, 2, 2), out.getValue("far"))
    }

    @Test fun resizeGrowingPushesNeighbour() {
        val items = listOf(Cell("a", 0, 0, 2, 2), Cell("b", 0, 2, 2, 2))
        val out = engine.place(items, items[0].copy(h = 3)).associateBy { it.id }
        assertEquals(3, out.getValue("b").row)
    }

    @Test fun findFreeUsesFirstGap() {
        val items = listOf(Cell("a", 0, 0, 2, 2), Cell("b", 2, 0, 2, 1))
        assertEquals(2 to 1, engine.findFree(items, 2, 1))
        assertEquals(0 to 2, engine.findFree(items, 3, 1))
    }

    @Test fun placeKeepsCallerOrderAndAddsNewItem() {
        val items = listOf(Cell("a", 0, 0, 1, 1), Cell("b", 1, 0, 1, 1))
        val out = engine.place(items, Cell("n", 0, 0, 1, 1))
        assertEquals(listOf("a", "b", "n"), out.map { it.id })
    }

    @Test fun randomOperationsAlwaysGiveValidLayouts() {
        val rnd = Random(42)
        var cells = listOf<Cell>()
        repeat(2000) { step ->
            val op = rnd.nextInt(4)
            cells = when {
                cells.isEmpty() || op == 0 -> {
                    val w = rnd.nextInt(1, 5); val h = rnd.nextInt(1, 4)
                    val (c, r) = engine.findFree(cells, w, h)
                    cells + Cell("id$step", c, r, w, h)
                }
                op == 1 -> engine.place(cells, cells.random(rnd).let { it.copy(col = rnd.nextInt(-2, 7), row = rnd.nextInt(-2, 12)) })
                op == 2 -> engine.place(cells, cells.random(rnd).let { it.copy(w = rnd.nextInt(1, 5), h = rnd.nextInt(1, 5)) })
                else -> cells - cells.random(rnd)
            }
            assertValid(cells)
        }
    }

    @Test fun deltaCellsRoundsToNearest() {
        assertEquals(0, GridMath.deltaCells(30f, 100f))
        assertEquals(1, GridMath.deltaCells(51f, 100f))
        assertEquals(-2, GridMath.deltaCells(-190f, 100f))
        assertEquals(0, GridMath.deltaCells(500f, 0f))
    }

    @Test fun spanPxIncludesGaps() {
        assertEquals(100f, GridMath.spanPx(1, 100f, 8f), 0.001f)
        assertEquals(316f, GridMath.spanPx(3, 100f, 8f), 0.001f)
    }
}
