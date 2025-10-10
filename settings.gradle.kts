rootProject.name = "libraryofsouls"
include(":adapter_api")
include(":adapter_unsupported")
include(":adapter_v1_20_R3")
include(":LibraryOfSouls")
project(":LibraryOfSouls").projectDir = file("paper")

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://maven.playmonumenta.com/releases/")
	}
}
