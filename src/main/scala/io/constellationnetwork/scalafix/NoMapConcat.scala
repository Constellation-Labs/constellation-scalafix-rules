package io.constellationnetwork.scalafix

import scalafix.v1._
import scalafix.lint.LintSeverity
import scala.meta._

class NoMapConcat extends SemanticRule("NoMapConcat") {

  override def description: String =
    "Disallows using ++ operator on Map types, which can silently overwrite values"

  override def isRewrite: Boolean = false

  private val mapMatcher = SymbolMatcher.normalized(
    "scala/collection/Map#",
    "scala/collection/immutable/Map#",
    "scala/collection/mutable/Map#",
    "scala/collection/immutable/SortedMap#",
    "scala/collection/SortedMap#"
  )

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case t @ Term.ApplyInfix.After_4_6_0(lhs, Term.Name("++"), _, _) if isMapType(lhs) =>
        Patch.lint(
          Diagnostic(
            id = "NoMapConcat",
            message = "Using ++ on Map silently overwrites duplicate keys. " +
              "Use explicit merge strategies like .foldLeft, .merged, or ++ with explicit conflict resolution.",
            position = t.pos,
            explanation = "The ++ operator on Maps favors the right-hand side when keys conflict, " +
              "which can lead to silent data loss. For example: Map(\"a\" -> 1) ++ Map(\"a\" -> 2) " +
              "results in Map(\"a\" -> 2), discarding the first value without warning. " +
              "Use explicit merge strategies to handle conflicts intentionally.",
            severity = LintSeverity.Warning
          )
        )
    }.asPatch
  }

  private def isMapType(term: Term)(implicit doc: SemanticDocument): Boolean = {
    term.symbol.info.exists { info =>
      mapMatcher.matches(info.symbol) ||
      info.signature.toString.contains("Map[") ||
      info.symbol.value.contains("Map")
    }
  }
}