import app.morphe.patcher.patch.Patch

/** The bundle holds patches for several apps; Boost's validators check Boost's patches only. */
fun Set<Patch<*>>.boostOnly(): Set<Patch<*>> =
    filter { patch -> patch.compatibility?.any { it.packageName == "com.rubenmayayo.reddit" } ?: true }.toSet()
