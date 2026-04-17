plugins {
    id("java")
    alias(libs.plugins.loom)
}

val mod_version: String by project
val maven_group: String by project
val archives_base_name: String by project

version = mod_version
group = maven_group

base {
    archivesName.set(rootProject.name)
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
}

dependencies {
    minecraft(libs.minecraft)
//    mappings(variantOf(libs.yarn) { classifier("v2") })
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(files("libs/voxy.jar")) //use modrinth later when its up to latest

    modApi(libs.malilib)
    modApi(libs.modmenu)
    modImplementation(libs.fabrishot)

    modImplementation(libs.iris)
    modImplementation(libs.sodium) //iris

    implementation(libs.antlr) //iris
    implementation(libs.glsl.transformer) //iris
    implementation(libs.jcpp) //iris

    implementation(libs.jgit)
    include(libs.jgit)
}

tasks.processResources {
    val replaceProperties = mapOf(
        "version" to project.version,
        "loader_version" to libs.versions.loader.get(),
        "minecraft_version" to libs.versions.minecraft.get()
    )

    inputs.properties(replaceProperties)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(replaceProperties)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}