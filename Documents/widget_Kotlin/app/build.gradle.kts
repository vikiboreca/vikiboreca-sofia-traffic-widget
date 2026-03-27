
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.secrets)
    alias(libs.plugins.goggle.services)
}
val properties = Properties().apply {
    load(rootProject.file("secrets.properties").inputStream())
}

android {

    namespace = "com.example.widget_kotlin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.widget_kotlin"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MAPS_API_KEY", "\"${properties["MAPS_API_KEY"]}\"")
        buildConfigField("String", "WEB_CLIENT_ID", "\"${properties["WEB_CLIENT_ID"]}\"")
    }

    packaging{
        resources{
            excludes+="META-INF/DEPENDENCIES"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
    composeOptions{
        kotlinCompilerExtensionVersion = "2.2.10"
    }
    kotlin {
        jvmToolchain(11)
    }
    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
}

secrets{
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "secrets.defaults.properties"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.gtfs)
    implementation(libs.gtfs.protobuf)
    implementation(libs.firebase)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.android.libraries.identity.googleid)


    implementation(libs.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material)
    implementation(libs.androidx.glance.material3)
    //Holy Compose
    implementation(libs.compose.ui)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.test.manifest)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.androidx.work)
    implementation(libs.jsoup)
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
}