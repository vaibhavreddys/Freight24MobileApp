pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.android.*")
                excludeGroupByRegex("com\\.google.*")
                excludeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "Freight24"

// App Modules
include(":app-trucker")
include(":app-connect")

// Core Modules
include(":core:auth")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:ui")
include(":core:notifications")
include(":core:rest")
