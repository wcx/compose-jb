val composeBuild = gradle.includedBuild("support")
fun Task.dependsOnComposeTask(name: String) = dependsOn(composeBuild.task(name))

val isOelPublication = project.findProperty("oel.publication") == "true"
val isWebExist = composeBuild.projectDir.resolve(".jbWebExistsMarker").exists()

val publishableComponents =
    listOf(
        ":compose:animation:animation",
        ":compose:animation:animation-core",
        ":compose:foundation:foundation",
        ":compose:foundation:foundation-layout",
        ":compose:material:material",
        ":compose:material:material-icons-core",
        ":compose:material:material-ripple",
        ":compose:runtime:runtime",
        ":compose:runtime:runtime-saveable",
        ":compose:ui:ui",
        ":compose:ui:ui-geometry",
        ":compose:ui:ui-graphics",
        ":compose:ui:ui-test",
        ":compose:ui:ui-test-junit4",
        ":compose:ui:ui-text",
        ":compose:ui:ui-tooling",
        ":compose:ui:ui-tooling-preview",
        ":compose:ui:ui-unit",
        ":compose:ui:ui-util",
    )

// To show all projects which use `xxx` task, run:
// ./gradlew -p frameworks/support help --task xxx

tasks.register("publishComposeJb") {
    dependsOnComposeTask(":compose:compiler:compiler:publishMavenPublicationToMavenRepository")
    dependsOnComposeTask(":compose:compiler:compiler-hosted:publishMavenPublicationToMavenRepository")
    dependsOnComposeTask(":compose:ui:ui-tooling-data:publishMavenPublicationToMavenRepository")

    dependsOnComposeTask(":compose:desktop:desktop:publishKotlinMultiplatformPublicationToMavenRepository")
    dependsOnComposeTask(":compose:desktop:desktop:publishJvmPublicationToMavenRepository")
    dependsOnComposeTask(":compose:desktop:desktop:publishJvmlinux-x64PublicationToMavenRepository")
    dependsOnComposeTask(":compose:desktop:desktop:publishJvmlinux-arm64PublicationToMavenRepository")
    dependsOnComposeTask(":compose:desktop:desktop:publishJvmmacos-x64PublicationToMavenRepository")
    dependsOnComposeTask(":compose:desktop:desktop:publishJvmmacos-arm64PublicationToMavenRepository")
    dependsOnComposeTask(":compose:desktop:desktop:publishJvmwindows-x64PublicationToMavenRepository")

    publishableComponents.forEach {
        dependsOnComposeTask("$it:publishKotlinMultiplatformPublicationToMavenRepository")
        dependsOnComposeTask("$it:publishDesktopPublicationToMavenRepository")

        if (!isOelPublication) {
            dependsOnComposeTask("$it:publishAndroidDebugPublicationToMavenRepository")
            dependsOnComposeTask("$it:publishAndroidReleasePublicationToMavenRepository")
        }
    }

    if (isWebExist) {
        listOf(
            ":compose:runtime:runtime",
        ).forEach {
            dependsOnComposeTask("$it:publishJsPublicationToMavenRepository")
        }
    }
}

// separate task that cannot be built in parallel (because it requires too much RAM).
// should be run with "--max-workers=1"
tasks.register("publishComposeJbExtendedIcons") {
    listOf(
        ":compose:material:material-icons-extended",
    ).forEach {
        dependsOnComposeTask("$it:publishKotlinMultiplatformPublicationToMavenRepository")
        dependsOnComposeTask("$it:publishDesktopPublicationToMavenRepository")

        if (!isOelPublication) {
            dependsOnComposeTask("$it:publishAndroidDebugPublicationToMavenRepository")
            dependsOnComposeTask("$it:publishAndroidReleasePublicationToMavenRepository")
        }
    }
}

tasks.register("testComposeJbDesktop") {
    dependsOnComposeTask(":compose:desktop:desktop:jvmTest")
    dependsOnComposeTask(":compose:animation:animation:desktopTest")
    dependsOnComposeTask(":compose:animation:animation-core:desktopTest")
    dependsOnComposeTask(":compose:ui:ui:desktopTest")
    dependsOnComposeTask(":compose:ui:ui-graphics:desktopTest")
    dependsOnComposeTask(":compose:ui:ui-text:desktopTest")
    dependsOnComposeTask(":compose:foundation:foundation:desktopTest")
    dependsOnComposeTask(":compose:foundation:foundation-layout:desktopTest")
    dependsOnComposeTask(":compose:material:material:desktopTest")
    dependsOnComposeTask(":compose:material:material-ripple:desktopTest")
    dependsOnComposeTask(":compose:runtime:runtime:desktopTest")
    dependsOnComposeTask(":compose:runtime:runtime-saveable:desktopTest")
}

if (isWebExist) {
    tasks.register("testComposeJbWeb") {
        dependsOnComposeTask(":compose:runtime:runtime:jsTest")
        dependsOnComposeTask(":compose:runtime:runtime:test")
    }
}

tasks.register("buildNativeDemo") {
    dependsOnComposeTask(":compose:native:demo:assemble")
}

tasks.register("testRuntimeNative") {
    dependsOnComposeTask(":compose:runtime:runtime:macosX64Test")
}

tasks.register("testComposeModules") { // used in https://github.com/JetBrains/androidx/tree/jb-main/.github/workflows
    // TODO: download robolectrict to run ui:ui:test
    // dependsOnComposeTask(":compose:ui:ui:test")

    dependsOnComposeTask(":compose:ui:ui-graphics:test")
    dependsOnComposeTask(":compose:ui:ui-geometry:test")
    dependsOnComposeTask(":compose:ui:ui-unit:test")
    dependsOnComposeTask(":compose:ui:ui-util:test")
    dependsOnComposeTask(":compose:runtime:runtime:test")
    dependsOnComposeTask(":compose:runtime:runtime-saveable:test")
    dependsOnComposeTask(":compose:material:material:test")
    dependsOnComposeTask(":compose:material:material-ripple:test")
    dependsOnComposeTask(":compose:foundation:foundation:test")
    dependsOnComposeTask(":compose:animation:animation:test")
    dependsOnComposeTask(":compose:animation:animation-core:test")
    dependsOnComposeTask(":compose:animation:animation-core:test")

    // TODO: enable ui:ui-text:test
    // dependsOnComposeTask(":compose:ui:ui-text:test")
    // compose/out/androidx/compose/ui/ui-text/build/intermediates/tmp/manifest/test/debug/tempFile1ProcessTestManifest10207049054096217572.xml Error:
    // android:exported needs to be explicitly specified for <activity>. Apps targeting Android 12 and higher are required to specify an explicit value for `android:exported` when the corresponding component has an intent filter defined.
}

tasks.register("run") {
    dependsOnComposeTask(":compose:desktop:desktop:desktop-samples:run")
}
