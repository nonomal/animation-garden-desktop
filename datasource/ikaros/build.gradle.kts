plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    idea
    `flatten-source-sets`
}

sourceSets.main {
    kotlin.srcDir("gen")
}

idea {
    module {
        generatedSourceDirs.add(file("gen"))
    }
}

dependencies {
    api(projects.datasource.datasourceApi)
    implementation(projects.utils.serialization)
    implementation(projects.utils.logging)

    api(libs.kotlinx.coroutines.core)
    api(libs.ktor.client.core)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jsoup)
    implementation(libs.slf4j.api)
    implementation(projects.utils.logging)

    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("com.squareup.moshi:moshi-adapters:1.14.0")


    testImplementation(libs.slf4j.simple)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // why is there a duplicate?
}
