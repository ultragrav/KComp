# KComp
A kotlin compiler extension to allow inline components in MiniMessage strings

## Example
```kotlin
val testComponent = Component.text("test comp")
val newComponent = "test $testComponent test".toComp()
```
Instead of the default kotlin behaviour of calling testComponent#toString, the compiler plugin replaces the component with a MiniMessage placeholder, so the component is correctly inserted
