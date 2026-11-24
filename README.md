# JudgeTerm Day

> **Avviso.** Questa app è *AI slop*: l'ha scritta quasi interamente un modello di intelligenza artificiale, per mio uso personale. È solo in italiano. Non c'è alcuna garanzia che i calcoli siano corretti né che il catalogo dei termini sia completo o aggiornato. Le scadenze vanno sempre verificate sul codice vigente. Non è pensata per essere distribuita né mantenuta per altri.

App Android: inserisci una data e ottieni tutti i termini processuali del catalogo calcolati a partire da quella data.

## Cosa fa

1. **Inserisci una data** in tre caselle: giorno, mese e anno.
   - L'anno è facoltativo. Se manca, si usa la prossima occorrenza di quel giorno, oggi compreso: il 24/11/2026, "15/02" diventa 15/02/2027.
   - I pulsanti **IERI** e **OGGI** compilano la data.
   - L'interruttore **TERMINI DIMEZZATI** dimezza tutti i termini (regole sotto).
2. **Premi Invio** o "Calcola scadenze". Si apre l'elenco di **tutti** i termini del catalogo, ciascuno calcolato come se la data inserita fosse il suo dies a quo. Per i termini a ritroso, ad esempio "20 giorni prima dell'udienza", la data è quella dell'udienza.
3. I termini sono **raggruppati per categoria** e, dentro ogni categoria, **ordinati per articolo**: 50 → 163-bis → 166 → 171-ter n. 1/2/3…
4. Ogni voce mostra:
   - articolo e descrizione;
   - l'evento che la data rappresenta per quel termine (es. "dalla data di notifica della sentenza");
   - durata e direzione (es. "40 gg prima", "6 mesi dopo");
   - le date di scadenza (vedi sotto);
   - eventuali note: sospensione feriale applicata o esclusa, termine dimezzato, dato da verificare.
5. **Ricerca** in alto: filtra per parti di parola, senza distinguere maiuscole e accenti. Cerca in descrizione, articolo, evento e categoria. "mem" trova le memorie dell'art. 171-ter e dell'art. 189. Con più parole servono tutte: "terza mem".

## Regole di calcolo

- **Dies a quo:** non si computa (art. 155, co. 1, c.p.c.). Si conta dal giorno successivo, in avanti o a ritroso.
- **Mesi e anni:** secondo il calendario comune. Se nel mese di arrivo manca il giorno corrispondente, si usa l'ultimo del mese (art. 2963 c.c.).
- **Sospensione feriale:** 1–31 agosto (L. 742/1969). Si applica solo ai termini che vi sono soggetti; non si applica, ad esempio, a lavoro e cautelare.
  - **Termini a giorni:** i giorni di agosto non si contano.
  - **Termini a mesi:** si aggiungono 31 giorni per ogni agosto attraversato.
  - **Dies a quo in agosto:** il computo parte dal 1° settembre (o dal 31 luglio, a ritroso).
- **Sabato:** è trattato come festivo ai fini della scadenza (art. 155, co. 5, c.p.c.).
- **Festività:** quelle nazionali, più Pasqua, Pasquetta e il 4 ottobre (dal 2026).
- **Scadenza in un giorno non utile:**
  - **giorno utile:** "Scadenza";
  - **sabato o festivo infrasettimanale:** "Termine pratico" (il giorno utile prima), "Termine esatto" e "Termine possibile ultimo" (il primo giorno utile dopo, cioè la proroga di legge);
  - **domenica:** "Termine pratico" (venerdì), "Termine effettivo" (domenica) e "Termine possibile ultimo" (lunedì, o il primo giorno utile dopo);
  - **termini a ritroso:** il "possibile ultimo" non compare, perché la scadenza si anticipa.
- **Termini dimezzati:** si arrotonda verso la scadenza più vicina.
  - **giorni:** in avanti per difetto (15 → 7), a ritroso per eccesso (15 → 8);
  - **mesi pari:** si dimezzano (6 → 3);
  - **mesi dispari:** si convertono in giorni da 30 (3 mesi → 45 gg, 1 mese → 15 gg);
  - **anni:** si convertono in mesi (1 anno → 6 mesi).

  Il dimezzamento vale per *tutti* i termini, senza le eccezioni previste da singoli riti.

## Catalogo attuale

| Categoria | Data inserita = | Termini |
|---|---|---|
| Cognizione | udienza di prima comparizione (citazione, rito Cartabia) | 163-bis notifica Italia 120 gg / estero 150 gg; 166 costituzione convenuto 70 gg; 171-ter memorie 40, 20, 10 gg (tutti a ritroso) |
| Cognizione | udienza del procedimento semplificato | 281-undecies notifica 40 gg (Italia) / 60 gg (estero); costituzione convenuto 10 gg (a ritroso) |
| Cognizione | conoscenza dell'evento interruttivo | 305 riassunzione 3 mesi |
| Cognizione | cessazione della causa di sospensione | 297 istanza di fissazione dell'udienza 3 mesi |
| Cognizione | comunicazione dell'ordinanza di incompetenza | 50 riassunzione 3 mesi |
| Decisione | udienza di rimessione in decisione | 189 note di precisazione delle conclusioni 60 gg, comparse conclusionali 30 gg, repliche 15 gg (a ritroso) |
| Impugnazioni | notifica della sentenza | 325 appello 30 gg, cassazione 60 gg |
| Impugnazioni | pubblicazione della sentenza | 327 termine lungo 6 mesi |
| Impugnazioni | notifica del ricorso per cassazione | 369 deposito 20 gg; 370 controricorso 40 gg |
| Impugnazioni | notifica della sentenza (amministrativo / tributario) | 92 c.p.a. appello al CdS 60 gg; 51 D.Lgs. 546/1992 appello CGT 60 gg |
| Decreto ingiuntivo | notifica del decreto | 641 opposizione 40 gg (Italia), 50 gg (UE), 60 gg (extra UE) |
| Decreto ingiuntivo | pronuncia del decreto | 644 notifica 60 gg (Italia), 90 gg (estero) |
| Esecuzione | notifica del precetto | 481 perdita di efficacia 90 gg |
| Esecuzione | consegna dell'atto di pignoramento | 557 / 518 iscrizione a ruolo 15 gg; 543 iscrizione a ruolo 30 gg |
| Esecuzione | pignoramento | 497 istanza di vendita 45 gg |
| Esecuzione | conoscenza dell'atto esecutivo | 617 opposizione agli atti 20 gg |
| Cautelare | provvedimento cautelare | 669-octies inizio del merito 60 gg; 669-terdecies reclamo 15 gg |
| Lavoro | udienza di discussione | 416 costituzione del convenuto 10 gg (a ritroso) |
| Lavoro | notifica della sentenza | 434 appello 30 gg |

Le voci segnate "dato da verificare" nell'app (481, 497, 617) hanno dubbi noti, soprattutto sull'applicazione della sospensione feriale.

## Limiti noti

- **Date passate:** l'app è pensata per date future. Non gestisce regimi storici: niente sospensione feriale 1/8–15/9 in vigore fino al 2014, niente sospensione COVID 2020.
- **Catalogo parziale:** mancano molti termini, ad esempio famiglia, sfratto, CTU, molti termini di esecuzione e del processo amministrativo e tributario.
- **Santo patrono:** il patrono del foro è supportato nel codice (`ItalianHolidays.extraHolidays`), ma non ancora nell'interfaccia.
- **Termine "libero":** non è gestito; ad esempio l'art. 163-bis, che è un termine libero, viene calcolato come termine ordinario.
- **Proroga in agosto:** non è gestito il caso in cui la proroga fa finire la scadenza dentro agosto.

## Struttura

| Modulo | Contenuto |
|---|---|
| `core/` | Kotlin puro (JVM), testato con JUnit: calcolo dei termini, festività, catalogo, ordinamento per articolo, ricerca, dimezzamento, interpretazione della data. |
| `app/` | App Android (Jetpack Compose, Material 3, minSdk 26, target 36), tema scuro con palette Chiyuki. |
| `art/icon/` | Script che genera l'icona adattiva da `giudice-robot.svg`. |
| `docs/` | Note sul riferimento funzionale e decisioni di progetto. |

## Scaricare l'APK

Una GitHub Action ([build-apk.yml](.github/workflows/build-apk.yml)) compila l'APK di debug:

- **A ogni push su `main`:** l'APK si scarica dalla pagina dell'esecuzione, nel tab *Actions*, sotto *Artifacts → JudgeTermDay-apk* (serve essere loggati su GitHub).
- **Con un tag `vX.Y.Z`:** l'APK viene allegato anche a una *Release*.

L'APK è firmato con una chiave di debug inclusa nel repo (`app/debug.keystore`). Gli APK di build diverse si aggiornano l'uno sull'altro senza disinstallare.

## Build

Requisiti: JDK 17+ (va bene il JBR incluso in Android Studio) e Android SDK (platform 36).

```bash
./gradlew :core:test          # test del motore di calcolo
./gradlew :app:assembleDebug  # APK in app/build/outputs/apk/debug/
```
