import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    java
    id("org.jetbrains.intellij.platform")
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    intellijPlatform {
        val localIdePath = providers.gradleProperty("localIdePath")
        if (localIdePath.isPresent) {
            local(localIdePath.get())
        } else {
            intellijIdea(providers.gradleProperty("platformVersion"))
        }

        bundledPlugin("com.intellij.java")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    buildSearchableOptions = false
    sandboxContainer = layout.buildDirectory.dir("idea-sandbox")

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            val localIdePath = providers.gradleProperty("localIdePath")
            if (localIdePath.isPresent) {
                local(file(localIdePath.get()))
            } else {
                current()
            }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 21
        options.encoding = "UTF-8"
    }

    runIde {
        args(layout.projectDirectory.dir("manual-test-project").asFile.absolutePath)
    }

    test {
        useJUnit()
    }
}
