# Package Info First

An IntelliJ IDEA plugin that keeps a real Java `package-info.java` at the top of
its package in the standard Project view.

Only files inside project Java source roots are affected. A file with the same
name in a resource root, excluded directory, library, or arbitrary folder keeps
IntelliJ IDEA's normal ordering.

## Development

The project targets IntelliJ IDEA 2026.2.1 and requires JDK 25 or newer.

```powershell
.\gradlew.bat test
.\gradlew.bat runIde
```

For local development, the build also accepts an installed IDEA directory:

```powershell
.\gradlew.bat runIde -PlocalIdePath="C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1"
```
