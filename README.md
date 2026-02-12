# Playit.gg Minecraft Plugin

This is the official Minecraft Java Plugin for https://playit.gg. Download the latest version [here](https://github.com/playit-cloud/playit-minecraft-plugin/releases/latest/download/playit-minecraft-plugin.jar).

Not sure how to use the playit plugin? Watch our [YouTube video](https://youtu.be/QQYRdgBL-4o).

## Compatibility Matrix
Contributions welcome!

❌ = not planned

🚧 = planned

✔️ = implemented

### Server Types
| Server Type  | Playit works | Real IP works | Plugin Download                                                                                                                         |
|--------------| ------------ |---------------|-----------------------------------------------------------------------------------------------------------------------------------|
| [Spigot 1.19](https://getbukkit.org/download/spigot)  | ✔️ | ✔️ | [0.1.4](https://github.com/playit-cloud/playit-minecraft-plugin/releases/download/v0.1.4/playit-minecraft-plugin.jar) |
| [Spigot 1.16.5](https://getbukkit.org/download/spigot) | ✔️ | ✔️ | [0.1.4-mc1.16](https://github.com/playit-cloud/playit-minecraft-plugin/releases/download/v0.1.4/playit-minecraft-plugin-1.16.jar) |
| [Paper 1.19](https://papermc.io/)   | ✔️ | ✔️ | [0.1.4](https://github.com/playit-cloud/playit-minecraft-plugin/releases/download/v0.1.4/playit-minecraft-plugin.jar) |
| [Paper 1.8 - 1.18](https://papermc.io/legacy) | ✔️ | ❌ | [0.1.4](https://github.com/playit-cloud/playit-minecraft-plugin/releases/download/v0.1.4/playit-minecraft-plugin.jar) & [0.1.4-mc1.16](https://github.com/playit-cloud/playit-minecraft-plugin/releases/download/v0.1.4/playit-minecraft-plugin-1.16.jar)|
| [Magma 1.18](https://magmafoundation.org/) | ✔️ | ❌ | [0.1.4](https://github.com/playit-cloud/playit-minecraft-plugin/releases/download/v0.1.4/playit-minecraft-plugin.jar)
| [BungeeCord](https://www.spigotmc.org/wiki/bungeecord/) | 🚧 |   |


### Plugins

| Plugin | Playit works |
| ------ | ------------ |
| [Geysermc](https://geysermc.org/) | 🚧 |
| [Dynmap](https://www.spigotmc.org/resources/dynmap%C2%AE.274/) | 🚧 |
| [Nova Modding Framwork](https://www.spigotmc.org/resources/nova-modding-framework-1-19-1-1-19-2.93648/) | ❌ |



## Developer

### Building the plugin

Requires Java 17+ and [Gradle](https://gradle.org/).

```bash
./gradlew build
```

The Minecraft plugin jar is written to `build/libs/playit-minecraft-plugin-$VERSION.jar` (e.g. `playit-minecraft-plugin-0.2.0.jar`). This fat jar includes all dependencies and is ready to drop into your server's `plugins` folder. Only this JAR is produced; use it for deployment.

### Other links
* https://www.spigotmc.org/resources/playit-gg.105566/
