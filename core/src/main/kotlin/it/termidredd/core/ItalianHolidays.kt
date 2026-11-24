package it.termidredd.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay

/**
 * Calendario delle festività nazionali italiane.
 *
 * @param extraHolidays festività locali ricorrenti (es. santo patrono del foro), opzionali.
 */
class ItalianHolidays(
    private val extraHolidays: Set<MonthDay> = emptySet(),
) {

    fun isHoliday(date: LocalDate): Boolean {
        val md = MonthDay.from(date)
        if (md in FIXED || md in extraHolidays) return true
        // San Francesco d'Assisi, festa nazionale di nuovo dal 2026 (L. 151/2025). Da verificare.
        if (md == SAN_FRANCESCO && date.year >= 2026) return true
        val easter = easterSunday(date.year)
        return date == easter || date == easter.plusDays(1)
    }

    /** Sabato e domenica non sono giorni utili per la scadenza (art. 155, commi 4-5, c.p.c.). */
    fun isNonWorkingDay(date: LocalDate, saturdayIsHoliday: Boolean): Boolean =
        date.dayOfWeek == DayOfWeek.SUNDAY ||
            (saturdayIsHoliday && date.dayOfWeek == DayOfWeek.SATURDAY) ||
            isHoliday(date)

    companion object {
        private val SAN_FRANCESCO = MonthDay.of(10, 4)

        private val FIXED = setOf(
            MonthDay.of(1, 1),   // Capodanno
            MonthDay.of(1, 6),   // Epifania
            MonthDay.of(4, 25),  // Liberazione
            MonthDay.of(5, 1),   // Festa del lavoro
            MonthDay.of(6, 2),   // Festa della Repubblica
            MonthDay.of(8, 15),  // Ferragosto
            MonthDay.of(11, 1),  // Ognissanti
            MonthDay.of(12, 8),  // Immacolata
            MonthDay.of(12, 25), // Natale
            MonthDay.of(12, 26), // Santo Stefano
        )

        /** Domenica di Pasqua (calendario gregoriano, algoritmo di Meeus/Jones/Butcher). */
        fun easterSunday(year: Int): LocalDate {
            val a = year % 19
            val b = year / 100
            val c = year % 100
            val d = b / 4
            val e = b % 4
            val f = (b + 8) / 25
            val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4
            val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = (h + l - 7 * m + 114) % 31 + 1
            return LocalDate.of(year, month, day)
        }
    }
}
