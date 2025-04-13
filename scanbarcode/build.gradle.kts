plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    // 将library 发布到maven仓库
    id("maven-publish")
}

android {
    namespace = "com.alan.scanbarcode"
    compileSdk = 34

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Use this dependency to bundle the model with your app
    api("com.google.mlkit:barcode-scanning:17.3.0")
    // CameraX
    val cameraxVersion = "1.2.2"
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    implementation("androidx.camera:camera-extensions:$cameraxVersion")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui-tooling")

}

sourceSets {
    create("main") {
        java.srcDir("src/main/java")
    }
}
// 打包源码
val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets["main"].allSource)
//    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

}
publishing {
    // 配置发布产物
    publications {
        create<MavenPublication>("maven") {
            artifact(sourcesJar)
            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }
            groupId = "com.alan"
            artifactId = "scanbarcode"
            version = "1.0.1"
            /**
             * 转换为如下格式
             * <dependencies>
             *   <dependency>
             *       <groupId></groupId>
             *       <artifactId></artifactId>
             *       <version></version>
             *   </dependency>
             * </dependencies>
             */
            pom.withXml {
                val nodes = asNode().appendNode("dependencies")
                configurations.api.get().allDependencies.forEach {
                    val node = nodes.appendNode("dependency")
                    node.appendNode("groupId", it.group)
                    node.appendNode("artifactId", it.name)
                    node.appendNode("version", it.version)
                }
            }

        }
    }
    // 配置maven仓库
    repositories {
        // 本项目repo地址
//        maven {
//            url = uri("$rootDir/repo")
//        }
        // 本地仓库地址
//        mavenLocal()
        // 远程阿里云私有仓库
        maven {
            // 设置maven仓库地址
            setUrl("https://packages.aliyun.com/maven/repository/2143348-snapshot-idGB46")
            //允许非https链接
            isAllowInsecureProtocol = true
            credentials {
                username = "62902a17acd13cfd50e31663"
                password = "70Hgq[t8Cm]r"
            }
        }
    }
}