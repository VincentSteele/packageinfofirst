plugins {
    java
}

sourceSets {
    create("secondary") {
        java.srcDir("src2/main/java")
        resources.srcDir("src2/main/resources")
    }
}
