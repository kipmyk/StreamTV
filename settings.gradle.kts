pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StreamTV"

// Core modules
include(":core:model")
include(":core:player")
include(":core:network")

// Provider modules
include(":providers:api")
include(":providers:demo")
include(":providers:metadata-tmdb")
include(":providers:live-tv-m3u")

// Main Android TV Application
include(":app")
