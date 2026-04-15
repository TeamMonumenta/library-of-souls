# Library of Souls
An extension of NBTEditor to allow managing a vast library of Minecraft mobs

## Dependencies
- [MonumentaCommon](https://github.com/TeamMonumenta/monumenta-server-management/tree/master/monumenta-common) — shared logging (hard dependency)
- [NBTEditor](https://github.com/TeamMonumenta/monumenta-nbt-editor) — mob NBT editing

Minimum Java version: Java 17
Recommended Minecraft Version: 1.16.5+ (older versions may work too, but are not supported)

## Maven dependency
Maven:
```xml
<repository>
	<id>monumenta</id>
	<name>Monumenta Maven Repo</name>
	<url>https://maven.playmonumenta.com/releases</url>
</repository>
```
Gradle (kotlin):
```kts
maven {
    name = "monumenta"
    url = uri("https://maven.playmonumenta.com/releases")
}
```
Gradle (groovy):
```groovy
maven {
    name "monumenta"
    url "https://maven.playmonumenta.com/releases"
}
```
