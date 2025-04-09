plugins {
	kotlin("jvm")
	id("com.google.devtools.ksp")
	`maven-publish`
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
	implementation("com.google.devtools.ksp:symbol-processing-api:2.1.20-2.0.0")
	implementation(project(":annotations"))

	ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
	compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")

	testImplementation(kotlin("test-junit5"))
	testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
	testImplementation("dev.zacsweers.kctfork:core:0.7.0")
	testImplementation("dev.zacsweers.kctfork:ksp:0.7.0")
}


publishing.publications {
	create("maven", MavenPublication::class.java) {
		from(components["java"])
	}
}
