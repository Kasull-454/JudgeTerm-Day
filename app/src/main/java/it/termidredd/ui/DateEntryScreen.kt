package it.termidredd.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.termidredd.R
import it.termidredd.core.DateInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PREVIEW = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)

/**
 * Prima schermata: giorno / mese / anno in tre caselle separate.
 * Il cursore passa da solo alla casella successiva; Invio (o "Calcola") apre le scadenze.
 * Completato il mese la tastiera si chiude; l'anno si può lasciare vuoto: vale la prossima
 * occorrenza di quel giorno (il 24/11/2026, "15/02" = 15/02/2027), mostrata in grigio nella casella.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateEntryScreen(onSubmit: (date: LocalDate, halved: Boolean) -> Unit) {
    var day by rememberSaveable { mutableStateOf("") }
    var month by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var halved by rememberSaveable { mutableStateOf(false) }
    val (dayFocus, monthFocus, yearFocus) = remember { FocusRequester.createRefs() }
    val focusManager = LocalFocusManager.current
    // Chiude la tastiera: si riapre solo toccando di nuovo una casella (es. l'anno).
    fun closeKeyboard() = focusManager.clearFocus()

    val date = DateInput.of(day, month, year)

    fun submit() {
        if (date == null) showError = true else onSubmit(date, halved)
    }

    LaunchedEffect(Unit) { dayFocus.requestFocus() }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Calcolo scadenze", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Inserisci una data: l'app calcola tutti i termini che ne derivano, in avanti e a ritroso.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text("Data", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DateBox(
                    label = "Giorno", placeholder = "gg", value = day, maxDigits = 2,
                    focus = dayFocus, isError = showError, modifier = Modifier.weight(1f),
                    onValue = {
                        day = it; showError = false
                        if (DateInput.isDayComplete(it)) monthFocus.requestFocus()
                    },
                    onEnter = { monthFocus.requestFocus() },
                )
                DateBox(
                    label = "Mese", placeholder = "mm", value = month, maxDigits = 2,
                    focus = monthFocus, isError = showError, modifier = Modifier.weight(1f),
                    onValue = {
                        month = it; showError = false
                        if (DateInput.isMonthComplete(it)) closeKeyboard()
                    },
                    onEnter = { if (year.isEmpty() && date != null) submit() else yearFocus.requestFocus() },
                    onBackspaceWhenEmpty = { dayFocus.requestFocus() },
                )
                DateBox(
                    label = "Anno", placeholder = (DateInput.inferredYear(day, month) ?: LocalDate.now().year).toString(), value = year, maxDigits = 4,
                    focus = yearFocus, isError = showError, modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Go,
                    onValue = {
                        year = it; showError = false
                        if (it.length == 4) closeKeyboard() // anno completo: come dopo il mese
                    },
                    onEnter = { submit() },
                    onBackspaceWhenEmpty = { monthFocus.requestFocus() },
                )
            }

            // Anteprima / errore sotto le caselle.
            Text(
                text = when {
                    showError -> "Data non valida"
                    date != null -> date.format(PREVIEW)
                    else -> " "
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (showError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickDateButton("IERI", Modifier.weight(1f)) {
                    fill(LocalDate.now().minusDays(1)) { d, m, y -> day = d; month = m; year = y }
                    showError = false
                    closeKeyboard()
                }
                QuickDateButton("OGGI", Modifier.weight(1f)) {
                    fill(LocalDate.now()) { d, m, y -> day = d; month = m; year = y }
                    showError = false
                    closeKeyboard()
                }
            }

            HalvedToggle(checked = halved, onToggle = { halved = !halved })

            Button(
                onClick = { submit() },
                enabled = day.isNotEmpty() && month.isNotEmpty() && (year.isEmpty() || year.length == 4),
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 4.dp),
            ) { Text("Calcola scadenze", style = MaterialTheme.typography.labelLarge) }

            OutlinedButton(
                onClick = {
                    day = ""; month = ""; year = ""; showError = false; halved = false
                    dayFocus.requestFocus()
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text("Cancella", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}

/** Casella stile "card": etichetta piccola in alto, valore grande sotto, bordo arrotondato. */
@Composable
private fun DateBox(
    label: String,
    placeholder: String,
    value: String,
    maxDigits: Int,
    focus: FocusRequester,
    isError: Boolean,
    onValue: (String) -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onBackspaceWhenEmpty: () -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val borderColor = when {
        isError -> colors.error
        focused -> colors.primary
        else -> colors.outline
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(if (focused) 2.dp else 1.dp, borderColor),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (focused) colors.primary else colors.onSurfaceVariant)
            BasicTextField(
                // Cursore sempre in fondo: si digita in sequenza.
                value = TextFieldValue(value, selection = TextRange(value.length)),
                onValueChange = { onValue(DateInput.digits(it.text, maxDigits)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                // NumberPassword = tastierino di sole cifre, senza suggerimenti né calcolatrice.
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    autoCorrectEnabled = false,
                    imeAction = imeAction,
                ),
                keyboardActions = KeyboardActions(onNext = { onEnter() }, onGo = { onEnter() }, onDone = { onEnter() }),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.headlineMedium, color = colors.outline)
                    }
                    inner()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .focusRequester(focus)
                    .onFocusChanged { focused = it.isFocused }
                    .onPreviewKeyEvent {
                        if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when {
                            it.key == Key.Enter || it.key == Key.NumPadEnter -> { onEnter(); true }
                            it.key == Key.Backspace && value.isEmpty() -> { onBackspaceWhenEmpty(); true }
                            else -> false
                        }
                    },
            )
        }
    }
}

private inline fun fill(date: LocalDate, set: (String, String, String) -> Unit) =
    set("%02d".format(date.dayOfMonth), "%02d".format(date.monthValue), date.year.toString())

@Composable
private fun QuickDateButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.height(52.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface) }
}

/** Pulsante sottile on/off: se attivo, tutti i termini vengono dimezzati. */
@Composable
private fun HalvedToggle(checked: Boolean, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = checked,
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = if (checked) colors.primary else colors.background,
        contentColor = if (checked) colors.onPrimary else colors.onSurfaceVariant,
        border = BorderStroke(1.dp, if (checked) colors.primary else colors.outline),
        modifier = Modifier.fillMaxWidth().height(36.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                if (checked) "✓  TERMINI DIMEZZATI" else "TERMINI DIMEZZATI",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp,
            )
        }
    }
}
