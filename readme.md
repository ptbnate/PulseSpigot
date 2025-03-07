# PulseSpigot  
A 1.8 fork of PaperSpigot that aims to optimize performance, provide long-term support for 1.8.9, and introduce useful APIs.  

## Download
The latest pre-build server JAR can be downloaded by clicking the button below.

[![Download](https://img.shields.io/badge/Download-Nightly-blue?style=for-the-badge&logo=github)](https://nightly.link/ptbnate/PulseSpigot/workflows/build/main/PulseSpigot.zip)

## API
See our API patches [here](./patches/api/).
<details>
<summary>Maven</summary>

```xml
<repositories>
    <repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
</repositories>

<dependencies>
	<dependency>
	    <groupId>com.github.ptbnate</groupId>
	    <artifactId>PulseSpigot</artifactId>
	    <version>main-SNAPSHOT</version>
	</dependency>
</dependencies>
```
</details>

<details>
<summary>Gradle (kts)</summary>

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("com.github.ptbnate:PulseSpigot:main-SNAPSHOT")
}
```
</details>

## Credits  
- **Nate** – Original author and primary contributor, with the most valuable code contributions to the project. 
- **Heath Logan Campbell** – Various NMS optimizations.  
- **IonSpigot** – Lag-compensated ticking, movement caching, and flushing.  
- **Albert** – Configurable pearls.  
- **Beanes** - Base of the profile system.  
- **P3ridot** – Most of the NMS optimizations.  
- **uRyanxD (PandaSpigot)** – Backported the modern tick loop from 1.13+.  

If I missed anyone, credit goes to them as well.  

**Note**: If you decide to use the code from this spigot then credits should be given, otherwise it'll count as skidding.

## License  
This project is licensed under `AGPL-3.0-or-later`. Please check the `LICENSE` file for more information.
