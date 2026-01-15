buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0") // upgraded
        classpath("com.google.gms:google-services:4.4.3") // optional upgrade
        classpath(kotlin("gradle-plugin", version = "1.9.22"))
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
