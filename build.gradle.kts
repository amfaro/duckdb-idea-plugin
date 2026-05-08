import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexerTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("org.jetbrains.changelog") version "2.5.0"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )
        bundledPlugin("com.intellij.database")
        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }
    publishing {
        token = providers.environmentVariable("JETBRAINS_MARKETPLACE_TOKEN")
    }
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }
    pluginVerification {
        ides {
            ide(
                providers.gradleProperty("platformType"),
                providers.gradleProperty("platformVersion"),
            )
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

grammarKit {
    grammarKitRelease.set("2022.3.2")
    jflexRelease.set("1.9.2")
}

sourceSets {
    main {
        java.srcDir("src/main/gen")
    }
}

tasks {
    named<GenerateLexerTask>("generateLexer") {
        sourceFile.set(file("src/main/grammar/DuckDb.flex"))
        targetOutputDir.set(file("src/main/gen/com/amfaro/duckdb/dialect/lexer"))
        purgeOldFiles.set(true)
        outputs.upToDateWhen { false }
    }

    named("compileJava") {
        dependsOn("generateLexer")
    }

    named("compileKotlin") {
        dependsOn("generateLexer")
    }

    named("runIde") {
        dependsOn("generateLexer")
    }

    test {
        useJUnit()
    }
}

kotlin { jvmToolchain(17) }

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
