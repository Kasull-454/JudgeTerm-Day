package it.termidredd

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import it.termidredd.ui.DateEntryScreen
import it.termidredd.ui.ResultsScreen
import it.termidredd.ui.theme.JudgeTermDayTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Nessun campo credenziali: senza questo i password manager scambiano il campo data
        // (tastierino NumberPassword) per un PIN e propongono di salvarlo.
        window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        setContent {
            JudgeTermDayTheme {
                // Data scelta, salvata come ISO per sopravvivere a rotazioni / process death.
                var selected by rememberSaveable { mutableStateOf<String?>(null) }
                var halved by rememberSaveable { mutableStateOf(false) }
                val date = selected?.let(LocalDate::parse)
                if (date == null) {
                    DateEntryScreen(onSubmit = { d, h -> selected = d.toString(); halved = h })
                } else {
                    ResultsScreen(date, halved, onBack = { selected = null })
                }
            }
        }
    }
}
