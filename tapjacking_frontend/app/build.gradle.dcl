androidApplication {
    namespace = "org.example.app"

    dependencies {
        // Material is optional, we add it to provide Material components if used
        implementation("com.google.android.material:material:1.12.0")
        implementation("org.apache.commons:commons-text:1.11.0")
        implementation(project(":utilities"))
    }
}
