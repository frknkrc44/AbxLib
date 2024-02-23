# AbxLib

A simple library for read/write ABX files.

## Usage

Read ABX file

```kotlin
val fileName = "storage.xml"
val abxReader = AbxReader(fileName)
abxReader.startRead()
var result = abxReader.getTree()
```

Write ABX file

```kotlin
val fileName = "storage.xml"
val abxWriter = AbxWriter(fileName)
val tree: AbxXMLElement /* A non-null AbxXMLElement */
abxWriter.startWrite(tree)
```
