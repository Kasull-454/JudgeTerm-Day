package it.termidredd.core

import java.time.LocalDate

/** Riga da mostrare all'utente: etichetta + data. */
data class DeadlineLine(val label: String, val date: LocalDate)

/**
 * Regole di presentazione:
 * - giorno utile          → "Scadenza"
 * - sabato o festivo      → "Termine pratico" (giorno utile prima), "Termine esatto", "Termine possibile ultimo"
 * - domenica              → "Termine pratico" (venerdì), "Termine effettivo", "Termine possibile ultimo" (lunedì)
 * Il "possibile ultimo" (proroga ex art. 155 c.p.c.) compare solo per i termini in avanti.
 */
fun TermResult.displayLines(): List<DeadlineLine> = when (kind) {
    DayKind.WORKING -> listOf(DeadlineLine("Scadenza", nominal))
    DayKind.SATURDAY, DayKind.HOLIDAY -> listOfNotNull(
        DeadlineLine("Termine pratico", practical),
        DeadlineLine("Termine esatto", nominal),
        lastPossible?.let { DeadlineLine("Termine possibile ultimo", it) },
    )
    DayKind.SUNDAY -> listOfNotNull(
        DeadlineLine("Termine pratico", practical),
        DeadlineLine("Termine effettivo", nominal),
        lastPossible?.let { DeadlineLine("Termine possibile ultimo", it) },
    )
}
