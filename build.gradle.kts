import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
	java
	`maven-publish`
	id("com.palantir.git-version") version "0.12.2"
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
	id("net.ltgt.errorprone") version "2.0.2"
	id("net.ltgt.nullaway") version "1.3.0"
	id("com.playmonumenta.deployment") version "1.+"
	checkstyle
	pmd
}

repositories {
	mavenLocal()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://jitpack.io")
	maven("https://oss.sonatype.org/content/repositories/snapshots/")
	maven("https://maven.playmonumenta.com/releases")
	// NBT API, pulled in by CommandAPI
	maven("https://repo.codemc.org/repository/maven-public/")
	maven("https://repo.maven.apache.org/maven2/")
	maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
	compileOnly("dev.jorel:commandapi-bukkit-core:9.4.1")
	compileOnly("de.tr7zw:item-nbt-api-plugin:2.12.0-SNAPSHOT")
	compileOnly("com.playmonumenta:redissync:4.1:all")
	compileOnly("com.playmonumenta:nbteditor:4.1:all")
	compileOnly("com.google.code.gson:gson:2.8.5")
	errorprone("com.google.errorprone:error_prone_core:2.10.0")
	errorprone("com.uber.nullaway:nullaway:0.9.5")
}

group = "com.playmonumenta"
val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()
description = "libraryofsouls"
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

// Configure plugin.yml generation
bukkit {
	load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
	main = "com.playmonumenta.libraryofsouls.LibraryOfSouls"
	apiVersion = "1.18"
	name = "LibraryOfSouls"
	authors = listOf("The Monumenta Team")
	depend = listOf("CommandAPI", "NBTEditor")
	softDepend = listOf("MonumentaRedisSync")
}

pmd {
	isConsoleOutput = true
	toolVersion = "6.41.0"
	ruleSets = listOf("$rootDir/pmd-ruleset.xml")
	setIgnoreFailures(true)
}

java {
	withJavadocJar()
	withSourcesJar()
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
		}
	}
	repositories {
		maven {
			name = "MonumentaMaven"
			url = when (version.toString().endsWith("SNAPSHOT")) {
				true -> uri("https://maven.playmonumenta.com/snapshots")
				false -> uri("https://maven.playmonumenta.com/releases")
			}

			credentials {
				username = System.getenv("USERNAME")
				password = System.getenv("TOKEN")
			}
		}
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.compilerArgs.add("-Xmaxwarns")
	options.compilerArgs.add("10000")
	options.compilerArgs.add("-Xlint:deprecation")

	options.errorprone {
		option("NullAway:AnnotatedPackages", "com.playmonumenta")

		allErrorsAsWarnings.set(true)

		/*** Disabled checks ***/
		// These we almost certainly don't want
		check(
			"InlineMeSuggester",
			CheckSeverity.OFF
		) // We won't keep deprecated stuff around long enough for this to matter
		check("CatchAndPrintStackTrace", CheckSeverity.OFF) // This is the primary way a lot of exceptions are handled
		check(
			"FutureReturnValueIgnored",
			CheckSeverity.OFF
		) // This one is dumb and doesn't let you check return values with .whenComplete()
		check(
			"ImmutableEnumChecker",
			CheckSeverity.OFF
		) // Would like to turn this on but we'd have to annotate a bunch of base classes
		check(
			"LockNotBeforeTry",
			CheckSeverity.OFF
		) // Very few locks in our code, those that we have are simple and refactoring like this would be ugly
		check("StaticAssignmentInConstructor", CheckSeverity.OFF) // We have tons of these on purpose
		check("StringSplitter", CheckSeverity.OFF) // We have a lot of string splits too which are fine for this use
		check(
			"MutablePublicArray",
			CheckSeverity.OFF
		) // These are bad practice but annoying to refactor and low risk of actual bugs
	}
}

ssh.easySetup(tasks.named<ShadowJar>("shadowJar").get(), "LibraryOfSouls")
