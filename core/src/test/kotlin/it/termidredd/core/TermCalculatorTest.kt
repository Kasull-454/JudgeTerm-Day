package it.termidredd.core

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TermCalculatorTest {

    private val calc = TermCalculator()
    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)

    @Test
    fun easter() {
        assertEquals(d(2025, 4, 20), ItalianHolidays.easterSunday(2025))
        assertEquals(d(2026, 4, 5), ItalianHolidays.easterSunday(2026))
        assertTrue(ItalianHolidays().isHoliday(d(2026, 4, 6))) // Pasquetta
    }

    @Test
    fun `giorno utile - un solo termine`() {
        val r = calc.compute(TermRequest(d(2026, 1, 10), 30, TermUnit.DAYS))
        assertEquals(d(2026, 2, 9), r.nominal)
        assertEquals(DayKind.WORKING, r.kind)
        assertEquals(r.nominal, r.practical)
        assertNull(r.lastPossible)
    }

    @Test
    fun `sabato - pratico venerdi, esatto sabato`() {
        val r = calc.compute(TermRequest(d(2025, 12, 31), 10, TermUnit.DAYS))
        assertEquals(DayKind.SATURDAY, r.kind)
        assertEquals(d(2026, 1, 9), r.practical)
        assertEquals(d(2026, 1, 10), r.nominal)
        assertEquals(d(2026, 1, 12), r.legalDeadline)
    }

    @Test
    fun `sabato non festivo se disattivato`() {
        val r = calc.compute(TermRequest(d(2025, 12, 31), 10, TermUnit.DAYS, saturdayIsHoliday = false))
        assertEquals(DayKind.WORKING, r.kind)
        assertEquals(d(2026, 1, 10), r.legalDeadline)
    }

    @Test
    fun `festivo infrasettimanale - pratico giorno prima`() {
        val r = calc.compute(TermRequest(d(2026, 5, 23), 10, TermUnit.DAYS))
        assertEquals(DayKind.HOLIDAY, r.kind) // 2 giugno, martedì
        assertEquals(d(2026, 6, 1), r.practical)
        assertEquals(d(2026, 6, 2), r.nominal)
    }

    @Test
    fun `domenica - pratico venerdi, effettivo domenica, ultimo lunedi`() {
        val r = calc.compute(TermRequest(d(2026, 1, 1), 10, TermUnit.DAYS))
        assertEquals(DayKind.SUNDAY, r.kind)
        assertEquals(d(2026, 1, 9), r.practical)
        assertEquals(d(2026, 1, 11), r.nominal)
        assertEquals(d(2026, 1, 12), r.lastPossible)
    }

    @Test
    fun `domenica di Pasqua - ultimo possibile salta Pasquetta`() {
        val r = calc.compute(TermRequest(d(2026, 3, 26), 10, TermUnit.DAYS))
        assertEquals(d(2026, 4, 5), r.nominal)
        assertEquals(d(2026, 4, 3), r.practical)
        assertEquals(d(2026, 4, 7), r.lastPossible)
    }

    @Test
    fun `sospensione feriale su termine a giorni`() {
        val r = calc.compute(TermRequest(d(2026, 7, 20), 20, TermUnit.DAYS))
        assertEquals(d(2026, 9, 9), r.legalDeadline)
    }

    @Test
    fun `termine a giorni che inizia in agosto`() {
        val r = calc.compute(TermRequest(d(2026, 8, 10), 30, TermUnit.DAYS))
        assertEquals(d(2026, 9, 30), r.legalDeadline)
    }

    @Test
    fun `mesi con giorno mancante va a fine mese`() {
        val r = calc.compute(TermRequest(d(2026, 1, 31), 1, TermUnit.MONTHS))
        assertEquals(d(2026, 2, 28), r.nominal) // sabato
        assertEquals(d(2026, 2, 27), r.practical)
        assertEquals(d(2026, 3, 2), r.legalDeadline)
    }

    @Test
    fun `mesi con sospensione feriale aggiunge 31 giorni`() {
        val senza = calc.compute(TermRequest(d(2026, 6, 15), 6, TermUnit.MONTHS, applyFeriale = false))
        assertEquals(d(2026, 12, 15), senza.legalDeadline)
        val con = calc.compute(TermRequest(d(2026, 6, 15), 6, TermUnit.MONTHS))
        assertEquals(d(2027, 1, 15), con.legalDeadline)
    }

    @Test
    fun `a ritroso - sabato anticipato, nessun ultimo possibile`() {
        val r = calc.compute(TermRequest(d(2026, 3, 20), 20, TermUnit.DAYS, Direction.BACKWARD))
        assertEquals(d(2026, 2, 28), r.nominal)
        assertEquals(d(2026, 2, 27), r.practical)
        assertEquals(d(2026, 2, 27), r.legalDeadline)
        assertNull(r.lastPossible)
    }

    @Test
    fun `a ritroso con sospensione feriale`() {
        val r = calc.compute(TermRequest(d(2026, 9, 15), 20, TermUnit.DAYS, Direction.BACKWARD))
        assertEquals(d(2026, 7, 26), r.nominal) // domenica
        assertEquals(d(2026, 7, 24), r.legalDeadline)
    }
}
