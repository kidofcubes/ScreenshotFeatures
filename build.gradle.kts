plugins {
    id("java")
    alias(libs.plugins.loom)
}

val mod_version = project.property("mod_version")!!
val maven_group = project.property("maven_group")!!
val archives_base_name = project.property("archives_base_name")!!

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
//    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://masa.dy.fi/maven/sakura-ryoko")
        content {includeGroupAndSubgroups("fi.dy.masa")}
    }
    maven { url = uri("https://maven.fallenbreath.me/releases")
        content {includeGroupAndSubgroups("me.fallenbreath")}
    }
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    api(libs.malilib)
    api(libs.modmenu)
//    implementation(libs.fabrishot)

    implementation(libs.iris)
    implementation(libs.sodium) //iris
    implementation(libs.voxy)

    implementation(libs.antlr) //iris
    implementation(libs.glsl.transformer) //iris
    implementation(libs.jcpp) //iris

    implementation(libs.jgit)
    include(libs.jgit)

    //debugging
//    implementation("io.github.java-diff-utils:java-diff-utils:4.12")
//    include("io.github.java-diff-utils:java-diff-utils:4.12")
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
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}