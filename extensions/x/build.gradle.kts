extension {
    name = "extensions/x.mpe"
}
android {
    namespace = "app.shiki.x.extension"
    defaultConfig { minSdk = 28 } // X 12.19 requires Android 9.
    sourceSets.getByName("main").java.setSrcDirs(listOf("src/main/java/app/morphe/extension/shikify/x"))
}
