import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
	id("com.playmonumenta.gradle-config") version "3+"
}

dependencies {
	compileOnly(libs.commandapi)
	compileOnly(libs.nbtapi)
	compileOnly(libs.mixinapi)
	compileOnly(libs.nbteditor) {
		artifact {
			classifier = "all"
		}
	}
	compileOnly(libs.redissync) {
		artifact {
			classifier = "all"
		}
	}
	compileOnly(libs.gson)
}

tasks.javadoc {
	(options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

monumenta {
	name("LibraryOfSouls")
	id("LibraryOfSouls")
	paper(
		"com.playmonumenta.libraryofsouls.LibraryOfSouls", BukkitPluginDescription.PluginLoadOrder.POSTWORLD, "1.18",
		depends = listOf("CommandAPI", "NBTEditor"),
		softDepends = listOf("MonumentaRedisSync"),
		apiJarVersion = "1.20-R0.1-SNAPSHOT"
	)
}
