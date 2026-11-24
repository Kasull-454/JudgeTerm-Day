plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Versione ricavata dal tag git più recente vX.Y.Z (es. v0.2.1):
// - versionName: "0.2.1", oppure "0.2.1-3-gabc1234" se ci sono commit dopo il tag;
// - versionCode: X*10000 + Y*100 + Z (v0.2.1 → 201), così ogni release ha un codice crescente
//   (lo usano Android e i repository F-Droid per riconoscere gli aggiornamenti).
// Senza git o senza tag: 0.0.0 / 1.
val gitDescribe: String = runCatching {
    providers.exec {
        commandLine("git", "describe", "--tags", "--match", "v[0-9]*", "--abbrev=7")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
}.getOrDefault("")
val versionMatch = Regex("""^v(\d+)\.(\d+)\.(\d+)(-\d+-g[0-9a-f]+)?$""").find(gitDescribe)
val (vMajor, vMinor, vPatch) = versionMatch?.groupValues?.drop(1)?.take(3)?.map { it.toInt() } ?: listOf(0, 0, 0)
val appVersionCode = maxOf(1, vMajor * 10000 + vMinor * 100 + vPatch)
val appVersionName = versionMatch?.let { gitDescribe.removePrefix("v") } ?: "0.0.0"

android {
    namespace = "it.termidredd"
    compileSdk = 36

    defaultConfig {
        applicationId = "it.termidredd"
        minSdk = 26 // java.time disponibile nativamente
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        // Chiave di debug condivisa (nel repo, password standard "android"): gli APK compilati
        // in locale e dalla GitHub Action hanno la stessa firma e si aggiornano a vicenda.
        // Non è una chiave segreta: serve solo per i build di debug.
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
