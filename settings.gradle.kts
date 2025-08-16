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
        // ✅ Correct Zego repo URL
        maven { setUrl( "https://storage.zego.im/maven/releases/") }
        maven { setUrl("https://www.jitpack.io")  }
        google()
        mavenCentral()
    }
}

rootProject.name = "ChatFy"
include(":app")
