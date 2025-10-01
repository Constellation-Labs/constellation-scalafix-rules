# Constellation Scalafix Rules

Custom Scalafix rules for Constellation Network Scala projects to enforce best practices and prevent common bugs.

## Rules

### NoSetSum

**Severity:** Error

Disallows calling `.sum` on Set types (Set, SortedSet, etc.) which can cause incorrect results or compilation errors.

**Problem:** Sets automatically deduplicate elements. When you call `.sum` or `.map().sum` on a Set, duplicate values after mapping are removed before summation, leading to incorrect results.

**Example:**
```scala
// ❌ Bad
Set(10, 20, 10).sum  // equals 30, not 40
Set(1, 2, 3).map(_ * 2).sum  // may give unexpected results

// ✅ Good
Set(10, 20, 10).toList.sum  // equals 40
Set(1, 2, 3).toList.map(_ * 2).sum
```

### NoSetMap

**Severity:** Warning

Disallows calling `.map` on Set types which can silently discard elements due to deduplication.

**Problem:** Sets automatically deduplicate elements, which means `.map` can silently lose data. This violates functor laws where mapping should preserve the number of elements.

**Example:**
```scala
// ❌ Bad
Set(1, 2, 3).map(_ => 0)  // results in Set(0) with only one element

// ✅ Good
Set(1, 2, 3).toList.map(_ => 0)  // results in List(0, 0, 0)
Set(1, 2, 3).toList.map(transform).toSet  // if you need a Set result
```

### NoMapConcat

**Severity:** Warning

Disallows using `++` operator on Map types, which can silently overwrite values.

**Problem:** The `++` operator on Maps favors the right-hand side when keys conflict, which can lead to silent data loss.

**Example:**
```scala
// ❌ Bad
Map("a" -> 1) ++ Map("a" -> 2)  // results in Map("a" -> 2), loses first value

// ✅ Good
Map("a" -> 1).foldLeft(Map("a" -> 2)) { case (acc, (k, v)) =>
  acc.updated(k, mergeFunction(acc.get(k), v))
}
```

## Requirements

- Scalafix 0.13.0 or greater

## Installation

Add the following to your `build.sbt`:

```scala
// Enable scalafix
ThisBuild / scalafixDependencies += "io.constellationnetwork" %% "constellation-scalafix-rules" % "0.1.0"
```

Add the rules to your `.scalafix.conf`:

```hocon
rules = [
  NoSetSum,
  NoSetMap,
  NoMapConcat
]
```

## Usage

Run scalafix to check your code:

```bash
sbt "scalafix --check"
```

Or enable automatic checking on compile (in `build.sbt`):

```scala
scalafixOnCompile := true
```

## Development

### Building locally

```bash
sbt clean compile
```

### Publishing locally for testing

```bash
sbt +publishLocal
```

### Cross-compilation

This project supports Scala 2.12.19 and 2.13.16:

```bash
sbt +compile
sbt +publishLocal
```

## Publishing

Artifacts are automatically published to Sonatype Central when a version tag is pushed:

```bash
git tag v0.1.0
git push origin v0.1.0
```

## License

Apache-2.0 License - see LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/new-rule`)
3. Add your rule implementation
4. Add tests if applicable
5. Commit your changes (`git commit -m 'Add new rule'`)
6. Push to the branch (`git push origin feature/new-rule`)
7. Open a Pull Request

## Maintainers

Maintained by Constellation Labs core team.
