package it.termidredd.core

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class PresentationTest {

    private val calc = TermCalculator()

    @Test
    fun `sabato mostra pratico, esatto e possibile ultimo`() {
        val lines = calc.compute(TermRequest(LocalDate.of(2025, 12, 31), 10, TermUnit.DAYS)).displayLines()
        assertEquals(listOf("Termine pratico", "Termine esatto", "Termine possibile ultimo"), lines.map { it.label })
        assertEquals(LocalDate.of(2026, 1, 12), lines.last().date)
    }

    @Test
    fun `festivo infrasettimanale mostra possibile ultimo il giorno dopo`() {
        val lines = calc.compute(TermRequest(LocalDate.of(2026, 5, 23), 10, TermUnit.DAYS)).displayLines()
        assertEquals(LocalDate.of(2026, 6, 3), lines.last().date)
    }

    @Test
    fun `sabato a ritroso non mostra il possibile ultimo`() {
        val lines = calc.compute(
            TermRequest(LocalDate.of(2026, 3, 20), 20, TermUnit.DAYS, Direction.BACKWARD),
        ).displayLines()
        assertEquals(listOf("Termine pratico", "Termine esatto"), lines.map { it.label })
    }

    @Test
    fun `domenica mostra pratico, effettivo e possibile ultimo`() {
        val lines = calc.compute(TermRequest(LocalDate.of(2026, 1, 1), 10, TermUnit.DAYS)).displayLines()
        assertEquals(listOf("Termine pratico", "Termine effettivo", "Termine possibile ultimo"), lines.map { it.label })
        assertEquals(LocalDate.of(2026, 1, 12), lines.last().date)
    }

    @Test
    fun `domenica a ritroso non mostra il possibile ultimo`() {
        val lines = calc.compute(
            TermRequest(LocalDate.of(2026, 9, 15), 20, TermUnit.DAYS, Direction.BACKWARD),
        ).displayLines()
        assertEquals(listOf("Termine pratico", "Termine effettivo"), lines.map { it.label })
    }
}
