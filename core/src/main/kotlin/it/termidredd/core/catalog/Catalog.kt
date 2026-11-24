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
import it.termidredd.core.TermUnit.YEARS
import java.time.LocalDate

enum class Category(val label: String) {
    // Libro I
    GENERALI("Competenza, ricusazione e udienze"),
    // Libro II
    COGNIZIONE("Cognizione ordinaria"),
    DECISIONE("Decisione"),
    IMPUGNAZIONI("Appello e revocazione"),
    CASSAZIONE("Cassazione"),
    LAVORO("Rito del lavoro"),
    FAMIGLIA("Persone, minorenni e famiglie"),
    // Libro III
    ESECUZIONE("Esecuzione"),
    OPPOSIZIONI_ESECUTIVE("Opposizioni esecutive"),
    // Libro IV
    MONITORIO("Decreto ingiuntivo"),
    SFRATTO("Convalida di sfratto"),
    CAUTELARE("Cautelare e possessorio"),
    CAMERALI("Camera di consiglio e divisione"),
    ARBITRATO("Arbitrato"),
    CLASSE("Azione di classe"),
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

/** Termine del c.p.c.: in avanti, a giorni, soggetto a sospensione feriale salvo [nf]. */
private fun cpc(
    article: String,
    label: String,
    amount: Int,
    direction: Direction = FORWARD,
    unit: TermUnit = DAYS,
) = TermDefinition("$article c.p.c.", label, amount, unit, direction)

/** Termine non soggetto a sospensione feriale (lavoro, cautelare, sfratto, opposizioni esecutive...). */
private fun TermDefinition.nf() = copy(feriale = false)

/** Termine con dubbi noti, da verificare (di solito sulla sospensione feriale). */
private fun TermDefinition.tv() = copy(toVerify = true)

/** Evento della voce "Altri termini". */
private fun ev(id: String, title: String, dateLabel: String, category: Category, vararg terms: TermDefinition) =
    LegalEvent(id, title, dateLabel, category, featured = false, terms = terms.toList())

/**
 * Catalogo dei termini del c.p.c. (Libri I–IV), ricavato da docs/TERMINI-LIBRO-*.md sul testo vigente
 * (post D.Lgs. 149/2022 e 164/2024).
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
                TermDefinition("482 c.p.c.", "Inizio dell'esecuzione possibile dopo (termine dilatorio)", 10, DAYS, FORWARD),
                TermDefinition("617 c.p.c.", "Opposizione agli atti contro il precetto", 20, DAYS, FORWARD, feriale = false),
            ),
        ),

        // ---------- Altri termini ----------
        // Le voci seguono docs/TERMINI-LIBRO-I..IV.md: solo i termini a carico delle parti con durata fissa,
        // calcolabili da una sola data. Restano fuori quelli fissati dal giudice e quelli per giudice e cancelleria.

        // ===== Libro I =====
        ev(
            "incompetenza", "Ordinanza sulla competenza",
            "Data di comunicazione dell'ordinanza", Category.GENERALI,
            cpc("47", "Notifica del ricorso per regolamento di competenza", 30),
            cpc("50", "Riassunzione davanti al giudice competente", 3, unit = MONTHS),
        ),
        ev(
            "notifica_regolamento", "Notifica del ricorso per regolamento di competenza",
            "Data della (ultima) notifica del ricorso", Category.GENERALI,
            cpc("47", "Deposito del ricorso in Cassazione (dall'ultima notifica)", 20),
            cpc("47", "Scritture difensive delle altre parti", 40),
        ),
        ev(
            "ordinanza_ricusazione", "Ordinanza che decide sulla ricusazione",
            "Data di comunicazione dell'ordinanza", Category.GENERALI,
            cpc("54", "Riassunzione del processo dopo la ricusazione", 6, unit = MONTHS),
        ),
        ev(
            "udienza_generica", "Udienza (qualsiasi)",
            "Data dell'udienza", Category.GENERALI,
            cpc("52", "Ricusazione del giudice (se ne conosci il nome)", 2, BACKWARD).tv(),
            cpc("192", "Ricusazione o astensione del CTU", 3, BACKWARD),
            cpc("263", "Deposito del rendiconto", 5, BACKWARD),
            cpc("127-bis", "Comunicazione dell'udienza da remoto (minimo)", 15, BACKWARD),
        ),
        ev(
            "comunicazione_127", "Provvedimento che dispone udienza da remoto o note scritte",
            "Data di comunicazione del provvedimento", Category.GENERALI,
            cpc("127-bis", "Istanza per l'udienza in presenza", 5),
            cpc("127-ter", "Opposizione alla trattazione scritta", 5),
        ),

        // ===== Libro II – Cognizione =====
        ev(
            "notifica_citazione", "Notifica dell'atto di citazione",
            "Data di notifica della citazione (l'ultima, se più convenuti)", Category.COGNIZIONE,
            cpc("165", "Costituzione dell'attore", 10),
        ),
        ev(
            "cancellazione_ruolo", "Cancellazione della causa dal ruolo",
            "Data della cancellazione", Category.COGNIZIONE,
            cpc("38", "Riassunzione davanti al giudice indicato (adesione all'incompetenza)", 3, unit = MONTHS),
            cpc("307", "Riassunzione dopo la cancellazione dal ruolo", 3, unit = MONTHS),
        ),
        ev(
            "ordinanza_istruttore", "Ordinanza del giudice istruttore",
            "Data di pronuncia in udienza o di comunicazione", Category.COGNIZIONE,
            cpc("178", "Reclamo al collegio", 10),
            cpc("186-quater", "Ricorso per ottenere la sentenza", 30),
            cpc("281-octies", "Richiesta di discussione davanti al collegio", 10),
            cpc("289", "Integrazione dei provvedimenti istruttori", 6, unit = MONTHS),
        ),
        ev(
            "notifica_ordinanza_contumace", "Notifica di ordinanza (ingiunzione o pena pecuniaria)",
            "Data di notifica dell'ordinanza", Category.COGNIZIONE,
            cpc("179", "Reclamo contro la condanna a pena pecuniaria", 3),
            cpc("186-ter", "Costituzione del contumace contro l'ordinanza-ingiunzione", 20),
        ),
        ev(
            "interruzione", "Interruzione del processo",
            "Data di conoscenza dell'evento interruttivo", Category.COGNIZIONE,
            cpc("305", "Riassunzione o prosecuzione", 3, unit = MONTHS),
        ),
        ev(
            "morte_parte", "Morte della parte",
            "Data della morte", Category.COGNIZIONE,
            cpc("303", "Notifica collettiva e impersonale agli eredi", 1, unit = YEARS).nf().tv(),
        ),
        ev(
            "sospensione", "Cessazione della causa di sospensione",
            "Data di cessazione della causa di sospensione", Category.COGNIZIONE,
            cpc("297", "Istanza di fissazione della nuova udienza", 3, unit = MONTHS),
        ),
        ev(
            "scadenza_sospensione_concordata", "Fine della sospensione concordata (art. 296)",
            "Data di scadenza della sospensione", Category.COGNIZIONE,
            cpc("297", "Istanza di prosecuzione dopo la sospensione concordata", 10, BACKWARD),
        ),
        ev(
            "udienza_semplificato", "Udienza – procedimento semplificato",
            "Data dell'udienza", Category.COGNIZIONE,
            cpc("281-undecies", "Notifica di ricorso e decreto (Italia)", 40, BACKWARD),
            cpc("281-undecies", "Notifica di ricorso e decreto (estero)", 60, BACKWARD),
            cpc("281-undecies", "Costituzione del convenuto", 10, BACKWARD),
        ),
        ev(
            "semplificato_termini_281duodecies", "Prima udienza – semplificato, concessione dei termini",
            "Data dell'udienza in cui il giudice concede i termini", Category.COGNIZIONE,
            // Durate massime: il giudice può assegnare termini più brevi. Il secondo termine segue il primo.
            cpc("281-duodecies", "Memoria: precisare domande ed eccezioni, mezzi di prova (max 20 gg)", 20),
            cpc("281-duodecies", "Replica e prova contraria (max 20 + 10 gg)", 30),
        ),
        ev(
            "udienza_discussione_collegio", "Udienza di discussione orale davanti al collegio",
            "Data dell'udienza di discussione", Category.DECISIONE,
            cpc("275-bis", "Note di precisazione delle conclusioni (discussione orale)", 30, BACKWARD),
            cpc("275-bis", "Note conclusionali (discussione orale)", 15, BACKWARD),
        ),

        // ===== Libro II – Impugnazioni =====
        ev(
            "notifica_citazione_appello", "Notifica dell'atto di appello",
            "Data di notifica della citazione in appello", Category.IMPUGNAZIONI,
            cpc("347", "Costituzione dell'appellante", 10),
        ),
        ev(
            "udienza_appello", "Prima udienza d'appello",
            "Data della prima udienza (citazione o art. 349-bis)", Category.IMPUGNAZIONI,
            cpc("342", "Notifica dell'atto di appello (Italia)", 90, BACKWARD),
            cpc("342", "Notifica dell'atto di appello (estero)", 150, BACKWARD),
            cpc("343", "Appello incidentale nella comparsa di risposta", 20, BACKWARD),
            cpc("347", "Costituzione dell'appellato", 20, BACKWARD),
        ),
        ev(
            "udienza_rimessione_appello", "Udienza di rimessione in decisione in appello",
            "Data dell'udienza di rimessione in decisione", Category.IMPUGNAZIONI,
            cpc("352 n. 1", "Note di precisazione delle conclusioni in appello", 60, BACKWARD),
            cpc("352 n. 2", "Comparse conclusionali in appello", 30, BACKWARD),
            cpc("352 n. 3", "Note di replica in appello", 15, BACKWARD),
        ),
        ev(
            "sentenza_appello_rimessione", "Sentenza d'appello che rimette al primo giudice",
            "Data di notifica della sentenza d'appello", Category.IMPUGNAZIONI,
            cpc("354", "Riassunzione davanti al primo giudice", 3, unit = MONTHS),
        ),
        ev(
            "scoperta_revocazione", "Scoperta del dolo o del falso, o recupero del documento",
            "Data della scoperta o del recupero", Category.IMPUGNAZIONI,
            cpc("395", "Revocazione straordinaria (nn. 1, 2, 3, 6)", 30),
        ),
        ev(
            "notifica_revocazione", "Notifica della citazione per revocazione",
            "Data di notifica della citazione", Category.IMPUGNAZIONI,
            cpc("399", "Deposito della citazione per revocazione", 20),
        ),

        // ===== Libro II – Cassazione =====
        ev(
            "notifica_ricorso_cassazione", "Notifica del ricorso per cassazione",
            "Data di notifica del ricorso", Category.CASSAZIONE,
            cpc("369", "Deposito del ricorso (ricorrente)", 20),
            cpc("370", "Controricorso", 40),
        ),
        ev(
            "ricorso_incidentale_cassazione", "Deposito del ricorso incidentale per cassazione",
            "Data di deposito del ricorso incidentale", Category.CASSAZIONE,
            cpc("371", "Controricorso al ricorso incidentale", 40),
        ),
        ev(
            "udienza_cassazione", "Udienza o adunanza in Cassazione",
            "Data dell'udienza o dell'adunanza", Category.CASSAZIONE,
            cpc("372", "Deposito dei documenti sull'ammissibilità", 15, BACKWARD),
            cpc("376", "Istanza di rimessione alle Sezioni Unite", 15, BACKWARD),
            cpc("378", "Memorie illustrative (udienza pubblica)", 10, BACKWARD),
            cpc("380-bis.1", "Memorie illustrative (camera di consiglio)", 10, BACKWARD),
        ),
        ev(
            "proposta_definizione", "Proposta di definizione accelerata",
            "Data di comunicazione della proposta", Category.CASSAZIONE,
            cpc("380-bis", "Istanza di decisione (altrimenti il ricorso è rinunciato)", 40),
        ),
        ev(
            "scadenza_integrazione_cassazione", "Scadenza del termine per integrare il contraddittorio in Cassazione",
            "Data di scadenza del termine assegnato", Category.CASSAZIONE,
            cpc("371-bis", "Deposito dell'atto di integrazione del contraddittorio", 20),
        ),
        ev(
            "sentenza_giurisdizione", "Sentenza della Cassazione sulla giurisdizione",
            "Data di comunicazione della sentenza", Category.CASSAZIONE,
            cpc("367", "Riassunzione del processo di merito", 6, unit = MONTHS),
        ),
        ev(
            "notifica_pronuncia_cassazione", "Notifica di una pronuncia della Cassazione",
            "Data di notifica", Category.CASSAZIONE,
            cpc("391-bis", "Revocazione per errore di fatto (termine breve)", 60),
        ),
        ev(
            "pubblicazione_pronuncia_cassazione", "Pubblicazione di una pronuncia della Cassazione",
            "Data di pubblicazione", Category.CASSAZIONE,
            cpc("391-bis", "Revocazione per errore di fatto (termine lungo)", 6, unit = MONTHS),
            cpc("392", "Riassunzione davanti al giudice di rinvio", 3, unit = MONTHS),
        ),
        ev(
            "decreto_estinzione_cassazione", "Decreto di estinzione per rinuncia (Cassazione)",
            "Data di comunicazione del decreto", Category.CASSAZIONE,
            cpc("391", "Istanza di fissazione dell'udienza", 10),
        ),
        ev(
            "sentenza_cedu", "Sentenza definitiva della Corte EDU",
            "Data di pubblicazione della sentenza CEDU", Category.CASSAZIONE,
            cpc("391-quater", "Revocazione per contrarietà alla CEDU", 60),
        ),
        ev(
            "notifica_sentenza_amm_trib", "Notifica sentenza (amministrativo / tributario)",
            "Data di notifica della sentenza", Category.IMPUGNAZIONI,
            TermDefinition("92 c.p.a.", "Appello al Consiglio di Stato", 60, DAYS, FORWARD),
            TermDefinition("51 D.Lgs. 546/1992", "Appello alla CGT di secondo grado", 60, DAYS, FORWARD),
        ),

        // ===== Libro II – Lavoro (niente sospensione feriale) =====
        ev(
            "lavoro_conciliazione", "Richiesta di conciliazione – lavoro",
            "Data di ricevimento della richiesta", Category.LAVORO,
            cpc("410", "Memoria della controparte che accetta la conciliazione", 20).nf(),
        ),
        ev(
            "lavoro_decreto_fissazione", "Decreto di fissazione dell'udienza – lavoro",
            "Data di pronuncia del decreto", Category.LAVORO,
            cpc("415", "Notifica di ricorso e decreto al convenuto", 10).nf(),
        ),
        ev(
            "lavoro_udienza", "Udienza – rito del lavoro",
            "Data dell'udienza di discussione", Category.LAVORO,
            cpc("415", "Notifica al convenuto, minimo (Italia)", 30, BACKWARD).nf(),
            cpc("415", "Notifica al convenuto, minimo (estero)", 40, BACKWARD).nf(),
            cpc("416", "Costituzione del convenuto", 10, BACKWARD).nf(),
            cpc("420", "Costituzione del terzo chiamato", 10, BACKWARD).nf(),
        ),
        ev(
            "lavoro_notifica_sentenza", "Notifica sentenza – rito del lavoro",
            "Data di notifica della sentenza", Category.LAVORO,
            cpc("434", "Deposito del ricorso in appello", 30).nf(),
            cpc("434", "Deposito del ricorso in appello (notifica all'estero)", 40).nf(),
        ),
        ev(
            "lavoro_avviso_sentenza_420bis", "Sentenza sui contratti collettivi (art. 420-bis)",
            "Data di comunicazione dell'avviso di deposito", Category.LAVORO,
            cpc("420-bis", "Ricorso immediato per cassazione", 60).nf(),
        ),
        ev(
            "lavoro_decreto_appello", "Decreto presidenziale in appello – lavoro",
            "Data di deposito del decreto", Category.LAVORO,
            cpc("435", "Notifica di ricorso e decreto all'appellato", 10).nf(),
        ),
        ev(
            "lavoro_udienza_appello", "Udienza d'appello – rito del lavoro",
            "Data dell'udienza di discussione in appello", Category.LAVORO,
            cpc("435", "Notifica all'appellato, minimo (Italia)", 25, BACKWARD).nf(),
            cpc("435", "Notifica all'appellato, minimo (estero)", 60, BACKWARD).nf(),
            cpc("436", "Costituzione dell'appellato e appello incidentale", 10, BACKWARD).nf(),
        ),
        ev(
            "previdenza_sospensione", "Cessazione della sospensione – previdenza",
            "Data di cessazione della causa di sospensione", Category.LAVORO,
            cpc("443", "Riassunzione dopo il ricorso amministrativo", 180).nf(),
        ),
        ev(
            "previdenza_ctu_atp", "Deposito della CTU nell'ATP previdenziale",
            "Data di comunicazione del deposito", Category.LAVORO,
            cpc("445-bis", "Dichiarazione di dissenso dalla CTU", 30).nf(),
        ),
        ev(
            "previdenza_dissenso", "Dichiarazione di dissenso nell'ATP previdenziale",
            "Data della dichiarazione di dissenso", Category.LAVORO,
            cpc("445-bis", "Ricorso introduttivo del giudizio", 30).nf(),
        ),

        // ===== Libro II – Famiglia =====
        ev(
            "famiglia_udienza", "Prima udienza – rito famiglia",
            "Data dell'udienza di prima comparizione", Category.FAMIGLIA,
            cpc("473-bis.14", "Notifica di ricorso e decreto (Italia)", 60, BACKWARD),
            cpc("473-bis.14", "Notifica di ricorso e decreto (estero)", 90, BACKWARD),
            cpc("473-bis.14", "Costituzione del convenuto", 30, BACKWARD),
            cpc("473-bis.17", "Memoria dell'attore", 20, BACKWARD),
            cpc("473-bis.17", "Memoria del convenuto", 10, BACKWARD),
            cpc("473-bis.17", "Replica dell'attore (prova contraria)", 5, BACKWARD),
        ),
        ev(
            "famiglia_provvedimenti", "Provvedimento temporaneo o di attuazione – famiglia",
            "Data di pronuncia in udienza o di comunicazione", Category.FAMIGLIA,
            cpc("473-bis.24", "Reclamo contro i provvedimenti temporanei", 10),
            cpc("473-bis.38", "Opposizione ai provvedimenti di attuazione", 10),
        ),
        ev(
            "famiglia_rimessione", "Udienza di rimessione in decisione – famiglia",
            "Data dell'udienza di rimessione in decisione", Category.FAMIGLIA,
            cpc("473-bis.28", "Note di precisazione delle conclusioni (famiglia)", 60, BACKWARD),
            cpc("473-bis.28", "Comparse conclusionali (famiglia)", 30, BACKWARD),
            cpc("473-bis.28", "Memorie di replica (famiglia)", 15, BACKWARD),
        ),
        ev(
            "famiglia_udienza_appello", "Udienza d'appello – rito famiglia",
            "Data dell'udienza in appello", Category.FAMIGLIA,
            cpc("473-bis.31", "Notifica all'appellato, minimo (Italia)", 90, BACKWARD),
            cpc("473-bis.31", "Notifica all'appellato, minimo (estero)", 150, BACKWARD),
            cpc("473-bis.32", "Costituzione dell'appellato e appello incidentale", 30, BACKWARD),
            cpc("473-bis.32", "Replica dell'appellante", 20, BACKWARD),
            cpc("473-bis.32", "Replica dell'appellato", 10, BACKWARD),
        ),
        ev(
            "ordine_protezione_urgente", "Ordine di protezione adottato d'urgenza",
            "Data del decreto", Category.FAMIGLIA,
            cpc("473-bis.71", "Notifica di ricorso e decreto (massimo)", 8).nf(),
        ),

        // ===== Libro III – Esecuzione =====
        ev(
            "morte_debitore", "Morte del debitore",
            "Data della morte", Category.ESECUZIONE,
            cpc("477", "Notifica collettiva del titolo agli eredi", 1, unit = YEARS).nf().tv(),
        ),
        ev(
            "notifica_titolo_eredi", "Notifica del titolo agli eredi",
            "Data di notifica del titolo", Category.ESECUZIONE,
            cpc("477", "Precetto agli eredi possibile dopo (termine dilatorio)", 10),
        ),
        ev(
            "consegna_pignoramento", "Consegna dell'atto di pignoramento al creditore",
            "Data di consegna", Category.ESECUZIONE,
            cpc("557", "Iscrizione a ruolo – pignoramento immobiliare", 15),
            cpc("518", "Iscrizione a ruolo – pignoramento mobiliare", 15),
            cpc("543", "Iscrizione a ruolo – pignoramento presso terzi", 30),
        ),
        ev(
            "pignoramento", "Pignoramento eseguito",
            "Data del pignoramento", Category.ESECUZIONE,
            cpc("497", "Istanza di vendita / assegnazione (pena inefficacia)", 45).tv(),
            cpc("498", "Avviso ai creditori iscritti", 5),
            cpc("501", "Istanza di vendita possibile dopo (termine dilatorio)", 10),
            cpc("518", "Individuazione definitiva dei beni, se la stima è differita", 30),
            cpc("567", "Deposito della documentazione ipocatastale", 45).tv(),
        ),
        ev(
            "notifica_pignoramento_terzi", "Notifica del pignoramento presso terzi",
            "Data di notifica al terzo", Category.ESECUZIONE,
            cpc("543", "Dichiarazione del terzo al creditore", 10),
            cpc("551-bis", "Perdita di efficacia del pignoramento di crediti", 10, unit = YEARS).nf().tv(),
        ),
        ev(
            "trascrizione_pignoramento_veicoli", "Pignoramento di autoveicoli",
            "Data di comunicazione della trascrizione", Category.ESECUZIONE,
            cpc("521-bis", "Iscrizione a ruolo – autoveicoli", 30),
        ),
        ev(
            "deposito_intervento", "Intervento del creditore senza titolo",
            "Data di deposito del ricorso d'intervento", Category.ESECUZIONE,
            cpc("499", "Notifica del ricorso d'intervento al debitore", 10),
        ),
        ev(
            "ordinanza_assegnazione_crediti", "Ordinanza di assegnazione dei crediti",
            "Data di pronuncia o di comunicazione", Category.ESECUZIONE,
            cpc("553", "Notifica al terzo (altrimenti si fermano gli interessi)", 90),
        ),
        ev(
            "udienza_569", "Udienza per l'autorizzazione della vendita (art. 569)",
            "Data dell'udienza", Category.ESECUZIONE,
            cpc("568-bis", "Istanza di vendita diretta", 10, BACKWARD),
            cpc("568-bis", "Notifica dell'istanza di vendita diretta", 5, BACKWARD),
            cpc("569", "Nota del credito residuo", 30, BACKWARD),
        ),
        ev(
            "termine_offerte", "Termine per le offerte o data dell'incanto",
            "Data del termine per le offerte (o dell'incanto)", Category.ESECUZIONE,
            cpc("490", "Pubblicità sui siti internet", 45, BACKWARD),
            cpc("624-bis", "Istanza di sospensione concordata", 20, BACKWARD),
        ),
        ev(
            "udienza_vendita", "Udienza di vendita",
            "Data dell'udienza fissata per la vendita", Category.ESECUZIONE,
            cpc("588", "Istanza di assegnazione", 10, BACKWARD),
        ),
        ev(
            "incanto", "Incanto",
            "Data dell'incanto", Category.ESECUZIONE,
            cpc("583", "Nomina della persona da parte del procuratore aggiudicatario", 3),
            cpc("584", "Offerte in aumento di un quinto", 10),
        ),
        ev(
            "atto_delegato", "Atto del professionista delegato o del commissionario",
            "Data dell'atto o della sua conoscenza", Category.ESECUZIONE,
            cpc("591-ter", "Reclamo al giudice dell'esecuzione", 20).tv(),
        ),
        ev(
            "accesso_rilascio", "Accesso dell'ufficiale giudiziario per il rilascio",
            "Data dell'accesso", Category.ESECUZIONE,
            cpc("608", "Preavviso di rilascio (minimo)", 10, BACKWARD),
        ),
        ev(
            "scadenza_sospensione_esecuzione", "Fine della sospensione concordata dell'esecuzione",
            "Data di scadenza della sospensione", Category.ESECUZIONE,
            cpc("624-bis", "Istanza di prosecuzione dell'esecuzione", 10),
        ),
        ev(
            "estinzione_esecuzione", "Ordinanza sull'estinzione dell'esecuzione",
            "Data dell'udienza o della comunicazione", Category.ESECUZIONE,
            cpc("630", "Reclamo contro l'ordinanza di estinzione", 20).tv(),
        ),
        ev(
            "sentenza_opposizione_627", "Sentenza che rigetta l'opposizione (esecuzione sospesa)",
            "Data del giudicato (1° grado) o di comunicazione (appello)", Category.ESECUZIONE,
            cpc("627", "Riassunzione del processo esecutivo (al più tardi)", 6, unit = MONTHS),
        ),

        // ===== Libro III – Opposizioni (niente sospensione feriale) =====
        ev(
            "atto_esecutivo", "Atto esecutivo da opporre",
            "Data di conoscenza dell'atto", Category.OPPOSIZIONI_ESECUTIVE,
            cpc("617", "Opposizione agli atti esecutivi", 20).nf().tv(),
        ),
        ev(
            "primo_atto_esecuzione", "Primo atto di esecuzione",
            "Data del primo atto di esecuzione", Category.OPPOSIZIONI_ESECUTIVE,
            cpc("617", "Opposizione agli atti su titolo o precetto (dopo l'inizio dell'esecuzione)", 20).nf(),
            cpc("650", "Opposizione tardiva a decreto ingiuntivo (ultimo giorno)", 10),
        ),

        // ===== Libro IV – Monitorio =====
        ev(
            "notifica_di_estero", "Notifica del decreto ingiuntivo (debitore all'estero)",
            "Data di notifica del decreto ingiuntivo", Category.MONITORIO,
            cpc("641", "Opposizione (debitore in UE)", 50),
            cpc("641", "Opposizione (debitore fuori UE)", 60),
        ),
        ev(
            "pronuncia_di", "Pronuncia del decreto ingiuntivo",
            "Data di pronuncia del decreto", Category.MONITORIO,
            cpc("644", "Notifica del decreto (Italia)", 60),
            cpc("644", "Notifica del decreto (estero)", 90),
        ),
        ev(
            "notifica_opposizione_di_lavoro", "Notifica dell'opposizione a decreto per crediti di lavoro",
            "Data di notifica dell'opposizione", Category.MONITORIO,
            cpc("646", "Denuncia dell'opposizione al sindacato", 5).nf(),
        ),

        // ===== Libro IV – Sfratto (niente sospensione feriale) =====
        ev(
            "udienza_convalida", "Udienza di convalida di sfratto",
            "Data dell'udienza di convalida", Category.SFRATTO,
            cpc("660", "Notifica dell'intimazione (minimo)", 20, BACKWARD).nf(),
        ),
        ev(
            "esecuzione_sfratto", "Esecuzione dello sfratto convalidato",
            "Data dell'esecuzione", Category.SFRATTO,
            cpc("668", "Opposizione tardiva dopo la convalida", 10).nf(),
        ),

        // ===== Libro IV – Cautelare (niente sospensione feriale) =====
        ev(
            "provvedimento_cautelare", "Provvedimento cautelare",
            "Data di pronuncia / comunicazione", Category.CAUTELARE,
            cpc("669-octies", "Inizio del giudizio di merito", 60).nf(),
            cpc("669-terdecies", "Reclamo", 15).nf(),
            cpc("675", "Esecuzione del sequestro autorizzato", 30).nf(),
        ),
        ev(
            "decreto_cautelare", "Decreto cautelare inaudita altera parte",
            "Data del decreto", Category.CAUTELARE,
            cpc("669-sexies", "Notifica di ricorso e decreto (massimo)", 8).nf(),
        ),
        ev(
            "reclamo_possessorio", "Provvedimento possessorio (o decisione sul reclamo)",
            "Data di comunicazione del provvedimento", Category.CAUTELARE,
            cpc("703", "Istanza di prosecuzione del giudizio di merito", 60).nf(),
        ),

        // ===== Libro IV – Camerali e divisione =====
        ev(
            "decreto_camerale", "Decreto in camera di consiglio",
            "Data di comunicazione (o notifica, se più parti)", Category.CAMERALI,
            cpc("739", "Reclamo delle parti", 10),
            cpc("740", "Reclamo del pubblico ministero", 10),
        ),
        ev(
            "avviso_progetto_divisione", "Avviso del progetto di divisione (art. 791-bis)",
            "Data di ricezione dell'avviso", Category.CAMERALI,
            cpc("791-bis", "Opposizione al progetto o alla vendita", 30),
        ),

        // ===== Libro IV – Arbitrato =====
        ev(
            "invito_nomina_arbitro", "Invito a nominare l'arbitro",
            "Data di notifica dell'invito", Category.ARBITRATO,
            cpc("810", "Nomina dell'arbitro della controparte", 20),
        ),
        ev(
            "nomina_arbitro", "Nomina o accettazione dell'arbitro",
            "Data di notifica della nomina (o di conoscenza della causa)", Category.ARBITRATO,
            cpc("813", "Istanza di decadenza per omessa dichiarazione", 10),
            cpc("815", "Ricusazione dell'arbitro", 10),
        ),
        ev(
            "accettazione_arbitri", "Accettazione della nomina da parte degli arbitri",
            "Data dell'accettazione", Category.ARBITRATO,
            cpc("820", "Pronuncia del lodo (salvo diverso termine)", 240).tv(),
        ),
        ev(
            "notifica_lodo", "Notifica del lodo",
            "Data di notifica del lodo", Category.ARBITRATO,
            cpc("828", "Impugnazione per nullità (termine breve)", 90),
        ),
        ev(
            "sottoscrizione_lodo", "Ultima sottoscrizione del lodo",
            "Data dell'ultima sottoscrizione", Category.ARBITRATO,
            cpc("828", "Impugnazione per nullità (termine lungo)", 6, unit = MONTHS),
        ),
        ev(
            "comunicazione_lodo", "Comunicazione del lodo",
            "Data di comunicazione del lodo", Category.ARBITRATO,
            cpc("826", "Istanza di correzione del lodo", 1, unit = YEARS),
        ),
        ev(
            "decreto_esecutorieta_lodo", "Decreto sull'esecutorietà del lodo",
            "Data di comunicazione del decreto", Category.ARBITRATO,
            cpc("825", "Reclamo alla corte d'appello", 30),
        ),
        ev(
            "decreto_lodo_straniero", "Decreto sul lodo straniero",
            "Data di comunicazione (diniego) o notifica (concessione)", Category.ARBITRATO,
            cpc("840", "Opposizione alla corte d'appello", 30),
        ),
        ev(
            "giudicato_competenza_arbitrale", "Pronuncia definitiva sulla competenza arbitrale",
            "Data del passaggio in giudicato", Category.ARBITRATO,
            cpc("819-quater", "Prosecuzione davanti agli arbitri o al giudice", 3, unit = MONTHS),
        ),

        // ===== Libro IV – Azione di classe =====
        ev(
            "ordinanza_ammissibilita_classe", "Ordinanza sull'ammissibilità dell'azione di classe",
            "Data di comunicazione o notifica", Category.CLASSE,
            cpc("840-ter", "Reclamo alla corte d'appello", 30),
        ),
        ev(
            "decreto_diritti_aderenti", "Decreto sui diritti degli aderenti",
            "Data di comunicazione del decreto", Category.CLASSE,
            cpc("840-undecies", "Opposizione al tribunale", 30),
        ),
        // ===== Termini con sola durata minima o massima (fissati dal giudice, dagli arbitri o dall'ufficio) =====
        // L'app calcola la data estrema consentita dalla legge: "(massimo)" = la scadenza più lontana possibile,
        // "(minimo)" = la scadenza più vicina possibile. Il termine effettivo è quello scritto nel provvedimento.

        // Libro I
        ev(
            "provvedimento_127ter", "Provvedimento che sostituisce l'udienza con note scritte",
            "Data del provvedimento", Category.GENERALI,
            cpc("127-ter", "Deposito delle note scritte (termine non inferiore a 15 gg, minimo)", 15),
        ),

        // Libro II – cognizione
        ev(
            "sospensione_concordata", "Sospensione su istanza di tutte le parti (art. 296)",
            "Data del provvedimento di sospensione", Category.COGNIZIONE,
            cpc("296", "Fine della sospensione concordata (massimo 3 mesi)", 3, unit = MONTHS),
        ),
        ev(
            "termine_giudice_307", "Termine del giudice per rinnovare, proseguire o riassumere (art. 307)",
            "Data del provvedimento che assegna il termine", Category.COGNIZIONE,
            cpc("307", "Termine assegnato dal giudice (minimo 1 mese)", 1, unit = MONTHS),
            cpc("307", "Termine assegnato dal giudice (massimo 3 mesi)", 3, unit = MONTHS),
        ),
        ev(
            "cassazione_questione_ufficio", "Ordinanza della Cassazione su questione rilevata d'ufficio",
            "Data di comunicazione dell'ordinanza", Category.CASSAZIONE,
            cpc("384", "Osservazioni sulla questione rilevata d'ufficio (minimo)", 20),
            cpc("384", "Osservazioni sulla questione rilevata d'ufficio (massimo)", 60),
        ),

        // Libro II – lavoro
        ev(
            "lavoro_udienza_rinvio", "Udienza di rinvio – rito del lavoro (art. 420)",
            "Data dell'udienza di rinvio", Category.LAVORO,
            cpc("420", "Note difensive (fino a 5 gg prima)", 5, BACKWARD).nf(),
        ),
        ev(
            "lavoro_udienza_discussione_note", "Udienza di discussione con note (art. 429)",
            "Data dell'udienza in cui il giudice concede le note", Category.LAVORO,
            cpc("429", "Note difensive prima della sentenza (massimo)", 10).nf(),
            cpc("429", "Deposito della sentenza nelle cause complesse (massimo, per il giudice)", 60).nf(),
        ),
        ev(
            "lavoro_ctu", "Incarico al CTU con relazione scritta – lavoro",
            "Data dell'udienza di conferimento", Category.LAVORO,
            cpc("424", "Relazione scritta del CTU (massimo, non prorogabile)", 20).nf(),
            cpc("445", "Relazione del CTU in previdenza, se prorogata (massimo)", 60).nf(),
        ),
        ev(
            "lavoro_incompetenza", "Ordinanza di incompetenza – rito del lavoro",
            "Data dell'ordinanza", Category.LAVORO,
            cpc("428", "Riassunzione con rito speciale (massimo)", 30).nf(),
        ),
        ev(
            "lavoro_licenziamento_udienza", "Udienza – licenziamenti con termini ridotti (art. 441-bis)",
            "Data dell'udienza di discussione", Category.LAVORO,
            cpc("441-bis", "Notifica al convenuto o al terzo, minimo con termini dimezzati", 20, BACKWARD).nf(),
        ),
        ev(
            "lavoro_arbitrato_mandato", "Conferimento del mandato al collegio di conciliazione (art. 412)",
            "Data del conferimento del mandato", Category.LAVORO,
            cpc("412", "Emanazione del lodo (massimo)", 60).nf(),
        ),

        // Libro II – famiglia
        ev(
            "ordine_protezione_esecuzione", "Esecuzione dell'ordine di protezione",
            "Data di esecuzione dell'ordine", Category.FAMIGLIA,
            cpc("473-bis.70", "Durata dell'ordine di protezione (massimo, salvo proroga)", 1, unit = YEARS).nf().tv(),
        ),

        // Libro III – esecuzione
        ev(
            "conversione_pignoramento", "Ordinanza di conversione del pignoramento",
            "Data dell'ordinanza", Category.ESECUZIONE,
            cpc("495", "Rateizzazione della somma (massimo 48 mesi)", 48, unit = MONTHS).nf().tv(),
        ),
        ev(
            "ordinanza_vendita", "Ordinanza di vendita",
            "Data dell'ordinanza di vendita", Category.ESECUZIONE,
            cpc("569", "Presentazione delle offerte d'acquisto (minimo)", 90),
            cpc("569", "Presentazione delle offerte d'acquisto (massimo)", 120),
            cpc("530", "Versamento rateale del prezzo, beni mobili (massimo 12 mesi)", 12, unit = MONTHS).tv(),
            cpc("510", "Accantonamento per i creditori senza titolo (massimo 3 anni)", 3, unit = YEARS).nf().tv(),
        ),
        ev(
            "aggiudicazione", "Aggiudicazione dell'immobile",
            "Data dell'aggiudicazione", Category.ESECUZIONE,
            cpc("569", "Versamento del saldo prezzo, vendita senza incanto (massimo)", 120),
            cpc("576", "Versamento del saldo prezzo, vendita con incanto (massimo)", 60),
            cpc("569-bis", "Versamento del prezzo, vendita diretta", 90),
        ),
        ev(
            "vendita_diretta_ammissibile", "Offerta di vendita diretta dichiarata ammissibile",
            "Data del provvedimento", Category.ESECUZIONE,
            cpc("569-bis", "Pubblicità dell'offerta (massimo)", 45),
            cpc("569-bis", "Ulteriori offerte d'acquisto", 90),
        ),
        ev(
            "vendita_diretta_integrazione", "Prezzo base superiore all'offerta di vendita diretta",
            "Data del provvedimento", Category.ESECUZIONE,
            cpc("569-bis", "Integrazione dell'offerta e della cauzione", 10),
        ),
        ev(
            "nuove_condizioni_vendita", "Nuove condizioni o nuovo prezzo di vendita (art. 591)",
            "Data del provvedimento", Category.ESECUZIONE,
            cpc("591", "Nuove offerte d'acquisto (minimo)", 60),
            cpc("591", "Nuove offerte d'acquisto (massimo)", 90),
        ),
        ev(
            "amministrazione_giudiziaria", "Amministrazione giudiziaria dell'immobile",
            "Data dell'ordinanza", Category.ESECUZIONE,
            cpc("592", "Durata dell'amministrazione, proroghe comprese (massimo)", 3, unit = YEARS).nf().tv(),
        ),
        ev(
            "sospensione_esecuzione_concordata", "Sospensione dell'esecuzione su istanza dei creditori",
            "Data dell'ordinanza di sospensione", Category.ESECUZIONE,
            cpc("624-bis", "Fine della sospensione (massimo 24 mesi)", 24, unit = MONTHS).nf().tv(),
        ),

        // Libro IV
        ev(
            "notifica_di_termine_modificato", "Notifica del decreto ingiuntivo con termine ridotto o aumentato",
            "Data di notifica del decreto", Category.MONITORIO,
            cpc("641", "Opposizione, termine ridotto (minimo, Italia)", 10),
            cpc("641", "Opposizione, termine aumentato (massimo, Italia)", 60),
            cpc("641", "Opposizione, termine ridotto (minimo, UE)", 20),
            cpc("641", "Opposizione (minimo, fuori UE)", 30),
            cpc("641", "Opposizione (massimo, fuori UE)", 120),
        ),
        ev(
            "sfratto_contestazione_canoni", "Ordinanza di pagamento dei canoni non contestati",
            "Data dell'ordinanza", Category.SFRATTO,
            cpc("666", "Pagamento della somma non controversa (massimo)", 20).nf(),
        ),
        ev(
            "accettazione_arbitri_proroga", "Lodo con proroga di legge (art. 820)",
            "Data dell'accettazione della nomina", Category.ARBITRATO,
            cpc("820", "Pronuncia del lodo con una proroga di 180 gg", 420).tv(),
        ),
        ev(
            "classe_uscita_ricorrenti", "Uscita di tutti i ricorrenti dall'azione di classe",
            "Data del provvedimento del tribunale", Category.CLASSE,
            cpc("840-bis", "Prosecuzione da parte degli aderenti (minimo)", 60),
            cpc("840-bis", "Prosecuzione da parte degli aderenti (massimo)", 90),
        ),
        ev(
            "classe_adesione", "Pubblicazione dell'ordinanza di ammissione o della sentenza (azione di classe)",
            "Data di pubblicazione sul portale", Category.CLASSE,
            cpc("840-quinquies", "Adesione all'azione di classe (minimo)", 60),
            cpc("840-quinquies", "Adesione all'azione di classe (massimo)", 150),
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
