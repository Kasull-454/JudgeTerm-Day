package it.termidredd.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.termidredd.core.Direction
import it.termidredd.core.TermUnit
import it.termidredd.core.catalog.Catalog
import it.termidredd.core.catalog.ComputedTerm
import it.termidredd.core.catalog.TermDefinition
import it.termidredd.core.catalog.TermSearch
import it.termidredd.core.displayLines
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LONG = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val SHORT = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.ITALIAN)

/** Tutte le scadenze calcolate da [date], raggruppate per categoria, in ordine di articolo, con ricerca. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultsScreen(date: LocalDate, halved: Boolean, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val all = remember(date, halved) { Catalog.computeAllGrouped(date, halved = halved) }
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(all, query) { TermSearch.filter(all, query) }
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            Column(Modifier.background(colors.background)) {
                CenterAlignedTopAppBar(
                    navigationIcon = { IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.headlineMedium) } },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Scadenze", style = MaterialTheme.typography.titleLarge)
                            Text(date.format(LONG), style = MaterialTheme.typography.bodyMedium, color = colors.primary)
                            if (halved) {
                                Text("TERMINI DIMEZZATI", style = MaterialTheme.typography.labelSmall, color = colors.tertiary)
                            }
                        }
                    },
                )
                SearchBar(query, onQuery = { query = it }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (shown.isEmpty()) {
                item {
                    Text(
                        "Nessun termine contiene “$query”",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    )
                }
            }
            shown.forEach { (category, terms) ->
                stickyHeader(key = category.name) {
                    Text(
                        category.label.uppercase(Locale.ITALIAN),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.background)
                            .padding(top = 12.dp, bottom = 4.dp, start = 4.dp),
                    )
                }
                items(terms, key = { "${it.event.id}/${it.definition.article}/${it.definition.label}" }) { TermCard(it) }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQuery: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = colors.surfaceContainerHigh,
        border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) colors.primary else colors.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(start = 20.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                decorationBox = { inner ->
                    Box(Modifier.padding(vertical = 16.dp)) {
                        if (query.isEmpty()) {
                            Text("Cerca un termine (es. mem, 171, appello)", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                        }
                        inner()
                    }
                },
                modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused },
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQuery("") }) { Text("✕", color = colors.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun TermCard(term: ComputedTerm) {
    val def = term.definition
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceContainerHigh,
        contentColor = colors.onSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DurationBadge(def, halved = term.halvedFrom != null)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Art. ${def.article}", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                Text(def.label, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(
                    "dalla ${term.event.dateLabel.lowercase(Locale.ITALIAN)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    term.result.displayLines().forEachIndexed { i, line ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(line.label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                            Text(
                                line.date.format(SHORT),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (i == 0) colors.primary else colors.onSurface,
                            )
                        }
                    }
                }
                val notes = buildList {
                    term.halvedFrom?.let { add("Dimezzato da ${durationText(it)}") }
                    if (!def.feriale) add("Sospensione feriale non applicata")
                    addAll(term.result.notes)
                }
                notes.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
                if (def.toVerify) {
                    Text("• Dato da verificare", style = MaterialTheme.typography.bodySmall, color = colors.tertiary)
                }
            }
        }
    }
}

/** Riquadro a sinistra con la durata: "40 / gg / prima". */
@Composable
private fun DurationBadge(def: TermDefinition, halved: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.background,
        border = if (halved) BorderStroke(1.dp, colors.tertiary) else null,
        modifier = Modifier.size(64.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("${def.amount}", style = MaterialTheme.typography.titleLarge, color = if (halved) colors.tertiary else colors.onSurface)
            Text(unitText(def), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Text(
                if (def.direction == Direction.BACKWARD) "prima" else "dopo",
                style = MaterialTheme.typography.labelSmall,
                color = colors.secondary,
            )
        }
    }
}

private fun unitText(def: TermDefinition) = when (def.unit) {
    TermUnit.DAYS -> "gg"
    TermUnit.MONTHS -> if (def.amount == 1) "mese" else "mesi"
    TermUnit.YEARS -> if (def.amount == 1) "anno" else "anni"
}

private fun durationText(def: TermDefinition) = "${def.amount} ${unitText(def)}"
