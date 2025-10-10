dependencies {
	// NOTE - Make sure if you add another version here you make sure to exclude it from minimization below!
	implementation(project(":adapter_api"))
	implementation(project(":adapter_unsupported"))
	implementation(project(":adapter_v1_20_R3", "reobf"))

	compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

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

// Relocation / shading
tasks {
	shadowJar {
//		relocate("com.opencsv", "com.playmonumenta.plugins.internal.com.opencsv") // /generateitems
//		relocate(
//			"org.apache.commons.lang3",
//			"com.playmonumenta.plugins.internal.org.apache.commons.lang3"
//		) // Dependency of several things
//		relocate(
//			"org.apache.commons.math3",
//			"com.playmonumenta.plugins.internal.org.apache.commons.math3"
//		) // Dependency of several things
		minimize {
			exclude(project(":adapter_api"))
			exclude(project(":adapter_unsupported"))
			exclude(project(":adapter_v1_20_R3"))
		}

//		dependsOn(":adapter_v1_20_R3:remapAccessWidener")
	}
}
