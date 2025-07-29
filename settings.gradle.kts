pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        // >>> UPDATED: Pointing directly to your Downloads folder <<<
        maven { url = uri("file:///Users/aniket/Downloads") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Local maven (.m2) repository:
        mavenLocal()
        // Remote repositories:
        google()
        mavenCentral()
        // >>> UPDATED: Pointing directly to your Downloads folder <<<
        maven { url = uri("file:///Users/aniket/Downloads") }
    }
}

rootProject.name = "Google Home API Sample App"
include(":app")