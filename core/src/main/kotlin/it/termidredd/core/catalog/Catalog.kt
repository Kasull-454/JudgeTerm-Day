package it.termidredd.core.catalog

import it.termidredd.core.Direction
import it.termidredd.core.Direction.BACKWARD
import it.termidredd.core.Direction.FORWARD
import it.termidredd.core.TermCalculator
import it.termidredd.core.TermRequest
import it.termidredd.core.TermResult
import it.termidredd.core.TermUnit
import it.termidredd.core.TermUnit.DAYS
import it.termidredd.core.TermUnit.MONTHS
import java.time.LocalDate

enum class Category(val label: String) {
    COGNIZIONE("Cognizione ordinaria"),
    DECISIONE("Decisione"),
    IMPUGNAZIONI("Impugnazioni"),
    MONITORIO("Decreto ingiuntivo"),
    ESECUZIONE("Esecuzione"),
    CAUTELARE("Cautelare"),
    LAVORO("Rito del lavoro"),
}

/** Un singolo termine di legge, che si calcola dalla data dell'evento. */
data class TermDefinition(
    val article: String,
    val label: String,
    val amount: Int,
    val unit: TermUnit,
    val direction: Direction,
    /** Il termine è soggetto a sospensione feriale. */
    val feriale: Boolean = true,
    /** Il dato normativo va ricontrollato prima del rilascio. */
    val toVerify: Boolean = false,
)

/**
 * Evento processuale da cui decorrono uno o più termini: l'utente sceglie l'evento, inserisce la data
 * e ottiene tutte le scadenze collegate (es. "Udienza Cartabia – atto di citazione" → 163-bis, 166, 171-ter).
 */
data class LegalEvent(
    val id: String,
    val title: String,
    /** Cosa rappresenta la data da inserire. */
    val dateLabel: String,
    val category: Category,
    val featured: Boolean,
    val terms: List<TermDefinition>,
)

/**
 * Un termine calcolato, con l'evento da cui decorre.
 * Se i termini sono dimezzati, [definition] ha la durata dimezzata e [halvedFrom] quella di legge.
 */
data class ComputedTerm(
    val event: LegalEvent,
    val definition: TermDefinition,
    val result: TermResult,
    val halvedFrom: TermDefinition? = null,
)

/**
 * Termine dimezzato, con arrotondamento prudenziale (scadenza più vicina):
 * - giorni: in avanti per difetto (15 → 7), a ritroso per eccesso (15 → 8);
 * - mesi pari: metà dei mesi (6 → 3); mesi dispari: in giorni da 30 (3 mesi → 45 gg, 1 mese → 15 gg);
 * - anni: in mesi (1 anno → 6 mesi).
 */
fun TermDefinition.halved(): TermDefinition {
    fun halfDays(days: Int) = maxOf(1, if (direction == BACKWARD) (days + 1) / 2 else days / 2)
    return when (unit) {
        DAYS -> copy(amount = halfDays(amount))
        MONTHS -> if (amount % 2 == 0) copy(amount = amount / 2) else copy(amount = halfDays(amount * 30), unit = DAYS)
        TermUnit.YEARS -> copy(amount = amount * 6, unit = MONTHS)
    }
}

fun LegalEvent.computeAll(
    date: LocalDate,
    calculator: TermCalculator = TermCalculator(),
    saturdayIsHoliday: Boolean = true,
    halved: Boolean = false,
): List<ComputedTerm> = terms
    .map { original ->
        val t = if (halved) original.halved() else original
        ComputedTerm(
            event = this,
            definition = t,
            halvedFrom = if (halved) original else null,
            result = calculator.compute(
                TermRequest(
                    start = date,
                    amount = t.amount,
                    unit = t.unit,
                    direction = t.direction,
                    applyFeriale = t.feriale,
                    saturdayIsHoliday = saturdayIsHoliday,
                ),
            ),
        )
    }
    .sortedBy { it.result.nominal }

/**
 * Catalogo iniziale. Va ricontrollato riga per riga sui testi vigenti (c.p.c. post D.Lgs. 149/2022 e 164/2024).
 * Le voci con toVerify = true hanno dubbi noti (soprattutto se si applica la sospensione feriale).
 */
object Catalog {

    val events: List<LegalEvent> = listOf(
        // ---------- Prima schermata ----------
        LegalEvent(
            id = "udienza_citazione_cartabia",
            title = "Udienza Cartabia – atto di citazione",
            dateLabel = "Data dell'udienza di prima comparizione",
            category = Category.COGNIZIONE,
            featured = true,
            terms = listOf(
                TermDefinition("163-bis c.p.c.", "Notifica dell'atto di citazione (Italia)", 120, DAYS, BACKWARD),
                TermDefinition("163-bis c.p.c.", "Notifica dell'atto di citazione (estero)", 150, DAYS, BACKWARD),
                TermDefinition("166 c.p.c.", "Costituzione del convenuto", 70, DAYS, BACKWARD),
                TermDefinition("171-ter n. 1 c.p.c.", "Prima memoria integrativa", 40, DAYS, BACKWARD),
                TermDefinition("171-ter n. 2 c.p.c.", "Seconda memoria integrativa", 20, DAYS, BACKWARD),
                TermDefinition("171-ter n. 3 c.p.c.", "Terza memoria integrativa", 10, DAYS, BACKWARD),
            ),
        ),
        LegalEvent(
            id = "udienza_rimessione_189",
            title = "Udienza di rimessione in decisione (art. 189)",
            dateLabel = "Data dell'udienza di rimessione in decisione",
            category = Category.DECISIONE,
            featured = true,
            terms = listOf(
                TermDefinition("189 n. 1 c.p.c.", "Note di precisazione delle conclusioni", 60, DAYS, BACKWARD),
                TermDefinition("189 n. 2 c.p.c.", "Comparse conclusionali", 30, DAYS, BACKWARD),
                TermDefinition("189 n. 3 c.p.c.", "Memorie di replica", 15, DAYS, BACKWARD),
            ),
        ),
        LegalEvent(
            id = "notifica_sentenza",
            title = "Notifica della sentenza",
            dateLabel = "Data di notifica della sentenza",
            category = Category.IMPUGNAZIONI,
            featured = true,
            terms = listOf(
                TermDefinition("325 c.p.c.", "Appello (termine breve)", 30, DAYS, FORWARD),
                TermDefinition("325 c.p.c.", "Ricorso per cassazione (termine breve)", 60, DAYS, FORWARD),
            ),
        ),
        LegalEvent(
            id = "pubblicazione_sentenza",
            title = "Pubblicazione della sentenza",
            dateLabel = "Data di pubblicazione della sentenza",
            category = Category.IMPUGNAZIONI,
            featured = true,
            terms = listOf(
                TermDefinition("327 c.p.c.", "Impugnazione (termine lungo)", 6, MONTHS, FORWARD),
            ),
        ),
        LegalEvent(
            id = "notifica_decreto_ingiuntivo",
            title = "Notifica del decreto ingiuntivo",
            dateLabel = "Data di notifica del decreto ingiuntivo",
            category = Category.MONITORIO,
            featured = true,
            terms = listOf(
                TermDefinition("641 c.p.c.", "Opposizione (debitore in Italia)", 40, DAYS, FORWARD),
            ),
        ),
        LegalEvent(
            id = "notifica_precetto",
            title = "Notifica del precetto",
            dateLabel = "Data di notifica del precetto",
            category = Category.ESECUZIONE,
            featured = true,
            terms = listOf(
                TermDefinition("481 c.p.c.", "Perdita di efficacia del precetto", 90, DAYS, FORWARD, toVerify = true),
            ),
        ),

        // ---------- Altri termini ----------
        LegalEvent(
            "udienza_semplificato", "Udienza – procedimento semplificato",
            "Data dell'udienza", Category.COGNIZIONE, false,
            listOf(
                TermDefinition("281-undecies c.p.c.", "Notifica di ricorso e decreto (Italia)", 40, DAYS, BACKWARD),
                TermDefinition("281-undecies c.p.c.", "Notifica di ricorso e decreto (estero)", 60, DAYS, BACKWARD),
                TermDefinition("281-undecies c.p.c.", "Costituzione del convenuto", 10, DAYS, BACKWARD),
            ),
        ),
        LegalEvent(
            "interruzione", "Interruzione del processo",
            "Data di conoscenza dell'evento interruttivo", Category.COGNIZIONE, false,
            listOf(TermDefinition("305 c.p.c.", "Riassunzione o prosecuzione", 3, MONTHS, FORWARD)),
        ),
        LegalEvent(
            "sospensione", "Cessazione della causa di sospensione",
            "Data di cessazione della causa di sospensione", Category.COGNIZIONE, false,
            listOf(TermDefinition("297 c.p.c.", "Istanza di fissazione della nuova udienza", 3, MONTHS, FORWARD)),
        ),
        LegalEvent(
            "incompetenza", "Ordinanza che dichiara l'incompetenza",
            "Data di comunicazione dell'ordinanza", Category.COGNIZIONE, false,
            listOf(TermDefinition("50 c.p.c.", "Riassunzione davanti al giudice competente", 3, MONTHS, FORWARD)),
        ),
        LegalEvent(
            "notifica_ricorso_cassazione", "Notifica del ricorso per cassazione",
            "Data di notifica del ricorso", Category.IMPUGNAZIONI, false,
            listOf(
                TermDefinition("369 c.p.c.", "Deposito del ricorso (ricorrente)", 20, DAYS, FORWARD),
                TermDefinition("370 c.p.c.", "Controricorso", 40, DAYS, FORWARD),
            ),
        ),
        LegalEvent(
            "notifica_sentenza_amm_trib", "Notifica sentenza (amministrativo / tributario)",
            "Data di notifica della sentenza", Category.IMPUGNAZIONI, false,
            listOf(
                TermDefinition("92 c.p.a.", "Appello al Consiglio di Stato", 60, DAYS, FORWARD),
                TermDefinition("51 D.Lgs. 546/1992", "Appello alla CGT di secondo grado", 60, DAYS, FORWARD),
            ),
        ),
        LegalEvent(
            "notifica_di_estero", "Notifica del decreto ingiuntivo (debitore all'estero)",
            "Data di notifica del decreto ingiuntivo", Category.MONITORIO, false,
            listOf(
                TermDefinition("641 c.p.c.", "Opposizione (debitore in UE)", 50, DAYS, FORWARD),
                TermDefinition("641 c.p.c.", "Opposizione (debitore fuori UE)", 60, DAYS, FORWARD),
            ),
        ),
        LegalEvent(
            "pronuncia_di", "Pronuncia del decreto ingiuntivo",
            "Data di pronuncia del decreto", Category.MONITORIO, false,
            listOf(
                TermDefinition("644 c.p.c.", "Notifica del decreto (Italia)", 60, DAYS, FORWARD),
                TermDefinition("644 c.p.c.", "Notifica del decreto (estero)", 90, DAYS, FORWARD),
            ),
        ),
        LegalEvent(
            "consegna_pignoramento", "Consegna dell'atto di pignoramento al creditore",
            "Data di consegna", Category.ESECUZIONE, false,
            listOf(
                TermDefinition("557 c.p.c.", "Iscrizione a ruolo – pignoramento immobiliare", 15, DAYS, FORWARD),
                TermDefinition("518 c.p.c.", "Iscrizione a ruolo – pignoramento mobiliare", 15, DAYS, FORWARD),
                TermDefinition("543 c.p.c.", "Iscrizione a ruolo – pignoramento presso terzi", 30, DAYS, FORWARD),
            ),
        ),
        LegalEvent(
            "pignoramento", "Pignoramento eseguito",
            "Data del pignoramento", Category.ESECUZIONE, false,
            listOf(TermDefinition("497 c.p.c.", "Istanza di vendita / assegnazione (pena inefficacia)", 45, DAYS, FORWARD, toVerify = true)),
        ),
        LegalEvent(
            "atto_esecutivo", "Atto esecutivo da opporre",
            "Data di conoscenza dell'atto", Category.ESECUZIONE, false,
            listOf(TermDefinition("617 c.p.c.", "Opposizione agli atti esecutivi", 20, DAYS, FORWARD, feriale = false, toVerify = true)),
        ),
        LegalEvent(
            "provvedimento_cautelare", "Provvedimento cautelare",
            "Data di pronuncia / comunicazione", Category.CAUTELARE, false,
            listOf(
                TermDefinition("669-octies c.p.c.", "Inizio del giudizio di merito", 60, DAYS, FORWARD, feriale = false),
                TermDefinition("669-terdecies c.p.c.", "Reclamo", 15, DAYS, FORWARD, feriale = false),
            ),
        ),
        LegalEvent(
            "lavoro_udienza", "Udienza – rito del lavoro",
            "Data dell'udienza di discussione", Category.LAVORO, false,
            listOf(TermDefinition("416 c.p.c.", "Costituzione del convenuto", 10, DAYS, BACKWARD, feriale = false)),
        ),
        LegalEvent(
            "lavoro_notifica_sentenza", "Notifica sentenza – rito del lavoro",
            "Data di notifica della sentenza", Category.LAVORO, false,
            listOf(TermDefinition("434 c.p.c.", "Deposito del ricorso in appello", 30, DAYS, FORWARD, feriale = false)),
        ),
    )

    val featured: List<LegalEvent> get() = events.filter { it.featured }

    /** Voce "Altri termini": tutto il resto, raggruppato per categoria. */
    val othersByCategory: Map<Category, List<LegalEvent>>
        get() = events.filterNot { it.featured }.groupBy { it.category }.toSortedMap()

    fun byId(id: String): LegalEvent? = events.firstOrNull { it.id == id }

    /**
     * Tutte le scadenze del catalogo calcolate dalla stessa data, raggruppate per categoria
     * (nell'ordine dell'enum) e, dentro ogni categoria, in ordine di articolo.
     */
    fun computeAllGrouped(
        date: LocalDate,
        calculator: TermCalculator = TermCalculator(),
        saturdayIsHoliday: Boolean = true,
        halved: Boolean = false,
    ): Map<Category, List<ComputedTerm>> = events
        .flatMap { it.computeAll(date, calculator, saturdayIsHoliday, halved) }
        .groupBy { it.event.category }
        .toSortedMap()
        .mapValues { (_, list) ->
            list.sortedWith(compareBy(ArticleOrder) { it: ComputedTerm -> it.definition.article }.thenBy { it.definition.label })
        }
}
