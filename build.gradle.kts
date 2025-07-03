plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
buildscript {
    repositories {
        google()  // Required for Firebase
        mavenCentral()
        maven { url = uri("https://maven.cloudinary.com/nexus/content/repositories/releases/") }
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}

