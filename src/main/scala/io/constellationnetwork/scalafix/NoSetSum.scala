package io.constellationnetwork.scalafix

import scalafix.v1._
import scalafix.lint.LintSeverity
import scala.meta._

class NoSetSum extends SemanticRule("NoSetSum") {

  override def description: String =
    "Disallows calling .sum on Set types (Set, SortedSet, etc.) which can cause incorrect results or compilation errors"

  override def isRewrite: Boolean = false

  // SymbolMatcher is more efficient than string checking
  private val setMatcher = SymbolMatcher.normalized(
    "scala/collection/Set#",
    "scala/collection/immutable/Set#",
    "scala/collection/mutable/Set#",
    "scala/collection/immutable/SortedSet#",
    "scala/collection/SortedSet#"
  )

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      // Match: set.map(...).sum
      case t @ Term.Select(
            Term.Apply.After_4_6_0(Term.Select(qualifier, Term.Name("map")), _),
            Term.Name("sum")
          ) if isSetType(qualifier) =>
        Patch.lint(
          Diagnostic(
            id = "NoSetSum",
            message = "Calling .sum on a Set discards duplicate values, leading to incorrect results. " +
              "Use .toList.map(...).sum or compute the sum before converting to Set.",
            position = t.pos,
            explanation = "Sets automatically deduplicate elements. When you call .map().sum on a Set, " +
              "any duplicate values are removed before summation. " +
              "For example: Set(10, 20, 10).sum equals 30 instead of 40. " +
              "Convert to List/Seq before mapping, or sum before creating the Set.",
            severity = LintSeverity.Error
          )
        )

      // Match: set.sum
      case t @ Term.Select(qualifier, Term.Name("sum")) if isSetType(qualifier) =>
        Patch.lint(
          Diagnostic(
            id = "NoSetSum",
            message = "Calling .sum directly on a Set can cause incorrect results or compilation errors. " +
              "Convert to List first: .toList.sum",
            position = t.pos,
            explanation = "Sets automatically deduplicate elements, and the Scala compiler has issues " +
              "with .sum operations on Set types. Convert the Set to a List before summing.",
            severity = LintSeverity.Error
          )
        )
    }.asPatch
  }

  private def isSetType(term: Term)(implicit doc: SemanticDocument): Boolean = {
    term.symbol.info.exists { info =>
      // Try SymbolMatcher first (most precise)
      setMatcher.matches(info.symbol) ||
      // Fallback to signature/symbol string matching for edge cases
      info.signature.toString.contains("Set[") ||
      info.symbol.value.contains("Set")
    }
  }
}