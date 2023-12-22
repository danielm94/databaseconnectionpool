plugins {
    id("java")
}

group = "com.github.danielm94"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.mysql:mysql-connector-j:8.2.0")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testCompileOnly ("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor ("org.projectlombok:lombok:1.18.30")

    implementation("com.google.flogger:flogger-system-backend:0.8")
    implementation("com.google.flogger:flogger:0.8")
}

tasks.test {
    useJUnitPlatform()
}