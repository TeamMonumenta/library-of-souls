import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
	id("com.playmonumenta.gradle-config") version "3+"
}

tasks.javadoc {
	(options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

monumenta {
	name("LibraryOfSouls")
	id("LibraryOfSouls")
	pluginProject(":LibraryOfSouls")
	paper(
		"com.playmonumenta.libraryofsouls.LibraryOfSouls", BukkitPluginDescription.PluginLoadOrder.POSTWORLD, "1.20",
		depends = listOf("CommandAPI", "NBTEditor"),
		softDepends = listOf("MonumentaRedisSync"),
	)
	versionAdapterApi("adapter_api", paper = "1.20.4")
	versionAdapter("adapter_v1_20_R3", "1.20.4")
	versionAdapterUnsupported("adapter_unsupported")
}
