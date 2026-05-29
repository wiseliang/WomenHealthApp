pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

dependencyResolution {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
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
