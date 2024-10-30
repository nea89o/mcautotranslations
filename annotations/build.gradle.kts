plugins {
	kotlin("jvm")
}

dependencies {
}

publishing.publications {
	create("maven", MavenPublication::class.java) {
		from(components["java"])
	}
}
