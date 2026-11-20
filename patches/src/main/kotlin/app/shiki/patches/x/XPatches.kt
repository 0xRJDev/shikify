package app.shiki.patches.x

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

// Classic X (view-based). 12.43 and later are a rewritten Compose app and are not supported.
private val xCompatibility = Compatibility(
    name = "X",
    packageName = "com.twitter.android",
    targets = listOf(AppTarget(version = "12.19.1-release.0")),
)

private const val EXTENSION = "Lapp/morphe/extension/shikify/x/ShikifyX;"

// X's Application.onCreate lives in an obfuscated base class (com.snap.stuffing...), so match it by
// shape: the direct Application subclass that the app's own Application class extends.
private val applicationOnCreate = Fingerprint(
    name = "onCreate",
    returnType = "V",
    parameters = emptyList(),
    custom = { _, classDef -> classDef.superclass == "Landroid/app/Application;" && classDef.type.startsWith("Lcom/snap/stuffing/") },
)

@Suppress("unused")
val xMaterial3Patch = bytecodePatch(
    name = "Material 3 for X (beta)",
    description = "Beta: Material 3 for X 12.19.1 alongside Piko. Posts and threads on rounded cards, floating glass bottom bar and reply pill, pill tabs, a blurred post header and an M3 post button, coloured from your wallpaper.",
) {
    compatibleWith(xCompatibility)
    category("Appearance")
    extendWith("extensions/x.mpe")
    execute {
        val method = applicationOnCreate.method
        method.addInstruction(0, "invoke-static/range {p0 .. p0}, $EXTENSION->install(Landroid/app/Application;)V")
    }
}
