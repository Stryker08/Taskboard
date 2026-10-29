// Declare plugins here; apply them in the app module.
buildscript {
    dependencies {
        // Firebase Auth 25 requires Kotlin 2.4 metadata support.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
}
