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

Il catalogo copre i termini **a carico delle parti, con durata fissa e calcolabili da una sola data**, dei quattro libri del c.p.c. (oltre 160 termini, divisi in più di 90 eventi). Sono esclusi i termini fissati dal giudice e quelli per giudice e cancelleria.

La ricognizione completa, articolo per articolo, compresi i termini esclusi dall'app, è in:

- [docs/TERMINI-LIBRO-I.md](docs/TERMINI-LIBRO-I.md): competenza, ricusazione, udienze da remoto e note scritte, notificazioni, regole generali (artt. 152–155);
- [docs/TERMINI-LIBRO-II.md](docs/TERMINI-LIBRO-II.md): cognizione, decisione, appello, Cassazione, revocazione, lavoro, famiglia;
- [docs/TERMINI-LIBRO-III.md](docs/TERMINI-LIBRO-III.md): precetto, pignoramento, vendita, distribuzione, rilascio, opposizioni, estinzione;
- [docs/TERMINI-LIBRO-IV.md](docs/TERMINI-LIBRO-IV.md): decreto ingiuntivo, sfratto, cautelare e possessorio, camerali, arbitrato, azione di classe.

**Categorie nell'app:** competenza, ricusazione e udienze · cognizione · decisione · appello e revocazione · Cassazione · lavoro · famiglia · esecuzione · opposizioni esecutive · decreto ingiuntivo · sfratto · cautelare · camerali · arbitrato · azione di classe. Si aggiungono i due appelli amministrativo e tributario (art. 92 c.p.a., art. 51 D.Lgs. 546/1992).

La sospensione feriale è esclusa per lavoro, cautelare, sfratto e opposizioni esecutive (art. 3 L. 742/1969, art. 92 ord. giud.). Le voci segnate "dato da verificare" nell'app hanno dubbi noti, soprattutto sulla sospensione feriale:

- 52, ricusazione;
- 303 e 477, notifica collettiva agli eredi;
- 481 e 497, inefficacia di precetto e pignoramento;
- 551-bis, efficacia del pignoramento di crediti;
- 567, documentazione ipocatastale;
- 591-ter e 630, reclami nell'esecuzione;
- 617, opposizione agli atti esecutivi;
- 820, termine per il lodo.

## Limiti noti

- **Date passate:** l'app è pensata per date future. Non gestisce regimi storici: niente sospensione feriale 1/8–15/9 in vigore fino al 2014, niente sospensione COVID 2020.
- **Catalogo:** mancano i termini fissati dal giudice, quelli delle leggi speciali (amministrativo, tributario, crisi d'impresa, mediazione) e quelli delle disposizioni di attuazione. I termini "dilatori" (es. 482: esecuzione non prima di 10 giorni) sono mostrati come una scadenza: la data indicata è l'ultimo giorno di attesa.
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
