import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.minecraftsolutions"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.devnatan:inventory-framework-platform-bukkit:3.0.8")
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("me.clip:placeholderapi:2.11.3")
    implementation("com.github.cryptomorin:XSeries:11.2.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks {
    shadowJar {
        relocate("com.cryptomorin.xseries", "com.minecraftsolutions.vip.util.xseries")
        relocate("org.bstats", "com.minecraftsolutions.vip.util.bstats")
    }
    build {
        dependsOn(shadowJar)
    }
}

bukkit {
    name = "ms-vip"
    version = "${project.version}"
    main = "com.minecraftsolutions.vip.VipPlugin"
    depend = listOf("MinecraftSolutions")
    softDepend = listOf("DiscordSRV")
    authors = listOf("ReeachyZ_")
    description = "Free VIPs Plugin"
    website = "https://minecraft-solutions.com"
    apiVersion = "1.13"
    commands {
        register("vip"){
            aliases = listOf("vips")
        }
        register("changevip"){
            aliases = listOf("trocarvip")
        }
        register("timevip"){
            aliases = listOf("tempovip", "viptempo", "viptime", "vipstime", "vipstempo", "tempovips")
        }
        register ("usekey") {
            aliases = listOf("usarkey", "usarkeys", "usakey", "usakeys", "usekeys", "key");
        }
    }
}
