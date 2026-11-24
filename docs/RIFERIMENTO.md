# Riferimento funzionale e decisioni

Fonte: suite "Scadenze e Termini" di avvocatoandreani.it (https://www.avvocatoandreani.it/app/scadenze_termini.html).
Usata solo come riferimento per le funzioni. L'elenco dei termini di JudgeTerm Day va ricostruito dai testi normativi, non copiato.

## Decisioni prese (24/11/2026)

- **Date future soltanto:** niente regimi storici. Non c'è la sospensione feriale 1/8–15/9 in vigore fino al 2014 e non c'è la sospensione COVID 2020. Vale solo la feriale 1–31 agosto.
- **Niente termine "libero":** si conta sempre dal giorno successivo al dies a quo, in avanti o a ritroso.
- **Come si mostra la scadenza** (`TermResult.displayLines()`):
  - se cade in un giorno utile: solo "Scadenza";
  - se cade di sabato o in un festivo infrasettimanale: "Termine pratico" (il giorno utile prima), "Termine esatto" e "Termine possibile ultimo" (il primo giorno utile dopo);
  - se cade di domenica: "Termine pratico" (venerdì), "Termine effettivo" (domenica) e "Termine possibile ultimo" (lunedì, o il primo giorno utile dopo). Il "possibile ultimo" compare solo per i termini in avanti.
- **Catalogo:**
  - la prima schermata mostra gli eventi principali;
  - l'ultima voce è "Altri termini", divisa per categoria;
  - si sceglie un *evento* con la sua data e si ottengono tutte le scadenze collegate.

  Esempio: "Udienza Cartabia – atto di citazione" calcola gli artt. 163-bis, 166 e 171-ter, tutti a ritroso. "Udienza di rimessione in decisione" calcola l'art. 189. Per questo non servono calcolatori separati per le memorie.

## Da fare

1. **UI:**
   - selettore della data;
   - elenco degli eventi principali e voce "Altri termini";
   - dettaglio dell'evento con tutte le scadenze;
   - aggiunta al calendario (Intent `CalendarContract`);
   - "usa come nuovo dies a quo".
2. **Catalogo:** verificare riga per riga le voci `toVerify` e la sospensione feriale per esecuzione, cautelare e lavoro. Poi ampliare le categorie.
3. **Core:** TODO in `TermCalculator`, il caso in cui la proroga cade dentro agosto mentre la sospensione feriale è attiva.
4. **Patrono del foro:** opzione nelle impostazioni (`ItalianHolidays.extraHolidays`).
