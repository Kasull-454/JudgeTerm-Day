package it.termidredd.core

import java.time.DateTimeException
import java.time.LocalDate

object DateInput {

    /** Tiene solo le cifre, al massimo [max] (es. giorno 2, mese 2, anno 4). */
    fun digits(raw: String, max: Int): String = raw.filter { it.isDigit() }.take(max)

    /** Il giorno è completo con 2 cifre, o con 1 sola se non può esserci una seconda (4-9). */
    fun isDayComplete(day: String) = day.length == 2 || (day.length == 1 && day[0] in '4'..'9')

    /** Il mese è completo con 2 cifre, o con 1 sola se non può esserci una seconda (2-9). */
    fun isMonthComplete(month: String) = month.length == 2 || (month.length == 1 && month[0] in '2'..'9')

    /**
     * Anno sottinteso: la prossima volta che cade quel giorno/mese, oggi compreso.
     * Il 24/11/2026: 15/02 → 2027, 18/12 → 2026, 24/11 → 2026, 29/02 → 2028.
     */
    fun inferredYear(day: String, month: String, today: LocalDate = LocalDate.now()): Int? {
        val d = day.toIntOrNull() ?: return null
        val m = month.toIntOrNull() ?: return null
        for (y in today.year..today.year + 8) {
            val date = safeDate(y, m, d) ?: continue
            if (!date.isBefore(today)) return y
        }
        return null
    }

    /**
     * Giorno, mese e anno digitati in campi separati; null se incompleti o data inesistente (es. 31/02).
     * Anno vuoto = [inferredYear].
     */
    fun of(day: String, month: String, year: String, today: LocalDate = LocalDate.now()): LocalDate? {
        val d = day.toIntOrNull() ?: return null
        val m = month.toIntOrNull() ?: return null
        val y = when {
            year.isEmpty() -> inferredYear(day, month, today) ?: return null
            year.length == 4 -> year.toIntOrNull() ?: return null
            else -> return null
        }
        return safeDate(y, m, d)
    }

    private fun safeDate(y: Int, m: Int, d: Int): LocalDate? = try {
        LocalDate.of(y, m, d)
    } catch (_: DateTimeException) {
        null
    }
}
