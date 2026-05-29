pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

dependencyResolution {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

rootProject.name = "WomenHealthApp"

include(":app")

include(":core:model")
include(":core:database")
include(":core:network")
include(":core:datastore")
include(":core:common")

include(":feature:cycle")
include(":feature:hormone")
include(":feature:recommendation")
include(":feature:diet")
include(":feature:healthsync")
include(":feature:profile")
include(":feature:onboarding")
