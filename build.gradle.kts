plugins {
    id("com.soywiz.korge")
}

repositories {
    maven("https://jitpack.io")
}

korge {
    targetJvm()
    targetJs()
}

val ldtkApiVersion: String by project
val korgeVersion: String by project

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.lehaine.kt-ldtk-api:ldtk-api:$ldtkApiVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}