plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")

    `ani-mpp-lib-targets`
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.app.shared.appData)
        api(projects.app.shared.appPlatform)
        api(projects.utils.platform)
        api(libs.kotlinx.coroutines.core)
        implementation(projects.danmaku.danmakuApi)
        api(libs.kotlinx.collections.immutable)
        implementation(libs.kotlinx.serialization.protobuf)
        implementation(projects.app.shared.placeholder)
        
        api(libs.coil.compose.core)
        api(libs.coil.svg)
        api(libs.coil.network.ktor2)

        implementation(compose.components.resources)
        api(libs.compose.lifecycle.viewmodel.compose)
        api(libs.compose.lifecycle.runtime.compose)
        api(libs.compose.navigation.compose)
        api(libs.compose.navigation.runtime)
        api(libs.compose.material3.adaptive.core.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        api(libs.compose.material3.adaptive.layout.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        api(libs.compose.material3.adaptive.navigation0.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }

        implementation(projects.utils.bbcode)
        implementation(libs.constraintlayout.compose)
        
        api(libs.koin.core)
    }
    sourceSets.commonTest.dependencies {
        api(projects.utils.uiTesting)
    }
    sourceSets.androidMain.dependencies {
        api(libs.androidx.compose.ui.tooling.preview)
        api(libs.androidx.compose.ui.tooling)
        api(libs.compose.material3.adaptive.core.get().toString()) {
            exclude("androidx.window.core", "window-core")
        }
        // Preview only
    }
    sourceSets.desktopMain.dependencies {
        implementation(libs.jna)
        implementation(libs.jna.platform)
        api(libs.directories)
    }
}

android {
    namespace = "me.him188.ani.app.foundation"
}

compose.resources {
    publicResClass = true
    packageOfResClass = "me.him188.ani.app.ui.foundation"
}
