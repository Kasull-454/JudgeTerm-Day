package it.termidredd.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

enum class TermUnit { DAYS, MONTHS, YEARS }

/** FORWARD = termine "in avanti" (es. 30 gg per appellare); BACKWARD = termine "a ritroso" (es. 20 gg prima dell'udienza). */
enum class Direction { FORWARD, BACKWARD }

/** Tipo del giorno in cui cade la scadenza calcolata. */
enum class DayKind { WORKING, SATURDAY, SUNDAY, HOLIDAY }

data class TermRequest(
    val start: LocalDate,
    val amount: Int,
    val unit: TermUnit,
    val direction: Direction = Direction.FORWARD,
    /** Sospensione feriale dei termini 1-31 agosto (L. 742/1969). */
    val applyFeriale: Boolean = true,
    /** Sabato equiparato a festivo per la scadenza (art. 155, co. 5, c.p.c.). */
    val saturdayIsHoliday: Boolean = true,
) {
    init {
        require(amount > 0) { "La durata del termine deve essere positiva" }
    }
}

/**
 * Esito del calcolo. Regole di presentazione:
 * - giorno utile: solo [nominal];
 * - sabato o festivo infrasettimanale: "termine pratico" = [practical], "termine esatto" = [nominal];
 * - domenica: "termine pratico" = [practical] (venerdì), "termine effettivo" = [nominal],
 *   "termine possibile ultimo" = [lastPossible] (lunedì).
 */
data class TermResult(
    /** Giorno risultante dal computo puro (termine esatto / effettivo). */
    val nominal: LocalDate,
    val kind: DayKind,
    /** Ultimo giorno utile prima di [nominal] (coincide con [nominal] se è un giorno utile). */
    val practical: LocalDate,
    /** Primo giorno utile dopo [nominal], cioè la proroga di legge. Solo per termini in avanti che cadono in un giorno non utile. */
    val lastPossible: LocalDate?,
    val direction: Direction,
    val notes: List<String>,
) {
    /** Scadenza secondo legge: proroga in avanti (art. 155 c.p.c.), anticipazione a ritroso. */
    val legalDeadline: LocalDate
        get() = if (direction == Direction.FORWARD) lastPossible ?: nominal else practical
}

/**
 * Calcolo dei termini processuali:
 * - il giorno iniziale non si computa (art. 155, co. 1, c.p.c.);
 * - mesi e anni secondo il calendario comune; se manca il giorno corrispondente, ultimo del mese (art. 2963 c.c.);
 * - sospensione feriale 1-31 agosto, se richiesta (l'app lavora su date future: niente regimi storici);
 * - scadenza in giorno non utile: termine pratico (giorno utile precedente) e, in avanti, proroga al successivo.
 */
class TermCalculator(
    private val holidays: ItalianHolidays = ItalianHolidays(),
) {

    fun compute(req: TermRequest): TermResult {
        val notes = mutableListOf<String>()
        val nominal = when (req.unit) {
            TermUnit.DAYS -> countDays(req, notes)
            TermUnit.MONTHS -> countCalendar(req, req.amount.toLong(), notes)
            TermUnit.YEARS -> countCalendar(req, req.amount.toLong() * 12, notes)
        }

        val kind = classify(nominal, req.saturdayIsHoliday)
        if (kind == DayKind.WORKING) {
            return TermResult(nominal, kind, nominal, null, req.direction, notes)
        }
        val practical = walk(nominal, -1, req.saturdayIsHoliday)
        // TODO: valutare la proroga che finisce dentro agosto con sospensione attiva (es. 31 luglio di sabato).
        val lastPossible = if (req.direction == Direction.FORWARD) walk(nominal, +1, req.saturdayIsHoliday) else null
        return TermResult(nominal, kind, practical, lastPossible, req.direction, notes)
    }

    fun classify(date: LocalDate, saturdayIsHoliday: Boolean = true): DayKind = when {
        date.dayOfWeek == DayOfWeek.SUNDAY -> DayKind.SUNDAY
        saturdayIsHoliday && date.dayOfWeek == DayOfWeek.SATURDAY -> DayKind.SATURDAY
        holidays.isHoliday(date) -> DayKind.HOLIDAY
        else -> DayKind.WORKING
    }

    /** Primo giorno utile strettamente prima (step = -1) o dopo (step = +1) di [from]. */
    private fun walk(from: LocalDate, step: Long, saturdayIsHoliday: Boolean): LocalDate {
        var d = from.plusDays(step)
        while (holidays.isNonWorkingDay(d, saturdayIsHoliday)) d = d.plusDays(step)
        return d
    }

    private fun countDays(req: TermRequest, notes: MutableList<String>): LocalDate {
        val step = if (req.direction == Direction.FORWARD) 1L else -1L
        var d = req.start
        var counted = 0
        var skipped = 0
        while (counted < req.amount) {
            d = d.plusDays(step)
            if (req.applyFeriale && isFeriale(d)) skipped++ else counted++
        }
        if (skipped > 0) notes += "Sospensione feriale: esclusi $skipped giorni di agosto"
        return d
    }

    private fun countCalendar(req: TermRequest, months: Long, notes: MutableList<String>): LocalDate {
        val forward = req.direction == Direction.FORWARD
        // Se il dies a quo cade in agosto, il computo parte dalla fine (in avanti) o dall'inizio (a ritroso) del periodo feriale.
        val base = when {
            !req.applyFeriale || !isFeriale(req.start) -> req.start
            forward -> LocalDate.of(req.start.year, Month.AUGUST, 31)
            else -> LocalDate.of(req.start.year, Month.AUGUST, 1)
        }
        var end = if (forward) base.plusMonths(months) else base.minusMonths(months)
        if (!req.applyFeriale) return end

        // Ogni periodo feriale attraversato allunga il termine di 31 giorni (iterando: l'allungamento può includerne un altro).
        var applied = 0
        while (true) {
            val crossed = if (forward) augustsCrossed(base, end) else augustsCrossed(end, base)
            if (crossed == applied) break
            val extra = 31L * (crossed - applied)
            end = if (forward) end.plusDays(extra) else end.minusDays(extra)
            applied = crossed
        }
        if (applied > 0) notes += "Sospensione feriale: termine allungato di ${31 * applied} giorni"
        return end
    }

    /** Numero di periodi feriali (agosti) che si intersecano con l'intervallo aperto (from, to). */
    private fun augustsCrossed(from: LocalDate, to: LocalDate): Int =
        (from.year..to.year).count { y ->
            val aug1 = LocalDate.of(y, Month.AUGUST, 1)
            val aug31 = LocalDate.of(y, Month.AUGUST, 31)
            aug1 < to && aug31 > from
        }

    private fun isFeriale(d: LocalDate) = d.month == Month.AUGUST
}
