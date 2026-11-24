package it.termidredd.core

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateInputTest {

    @Test
    fun of() {
        assertEquals(LocalDate.of(2026, 11, 24), DateInput.of("24", "11", "2026"))
        assertEquals(LocalDate.of(2027, 3, 8), DateInput.of("8", "3", "2027"))
        assertNull(DateInput.of("24", "11", "202"))  // anno incompleto
        assertNull(DateInput.of("", "11", "2026"))
        assertNull(DateInput.of("31", "02", "2026")) // inesistente
        assertNull(DateInput.of("29", "02", "2027")) // non bisestile
        assertNull(DateInput.of("10", "13", "2026"))
    }

    @Test
    fun `anno vuoto = prossima occorrenza`() {
        val today = LocalDate.of(2026, 11, 24)
        assertEquals(LocalDate.of(2027, 2, 15), DateInput.of("15", "02", "", today))
        assertEquals(LocalDate.of(2026, 12, 18), DateInput.of("18", "12", "", today))
        assertEquals(LocalDate.of(2026, 11, 24), DateInput.of("24", "11", "", today)) // oggi
        assertEquals(LocalDate.of(2027, 11, 23), DateInput.of("23", "11", "", today)) // ieri → anno prossimo
        assertEquals(LocalDate.of(2028, 2, 29), DateInput.of("29", "02", "", today))
        assertNull(DateInput.of("18", "", "", today))
        assertNull(DateInput.of("31", "04", "", today))
        // Con l'anno scritto si usa quello, anche se passato.
        assertEquals(LocalDate.of(2026, 2, 15), DateInput.of("15", "02", "2026", today))
    }

    @Test
    fun `cifre complete`() {
        assertEquals(listOf(false, false, false, true, true), listOf("1", "2", "3", "4", "9").map(DateInput::isDayComplete))
        assertEquals(listOf(false, true, true), listOf("1", "2", "9").map(DateInput::isMonthComplete))
        assertEquals(true, DateInput.isDayComplete("31"))
        assertEquals(true, DateInput.isMonthComplete("12"))
    }

    @Test
    fun digits() {
        assertEquals("24", DateInput.digits("2a4x9", 2))
        assertEquals("2026", DateInput.digits("20/26", 4))
    }
}
