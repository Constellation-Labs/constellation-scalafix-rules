package io.constellationnetwork.scalafix

import scalafix.v1._
import scalafix.lint.LintSeverity
import scala.meta._

class NoSetMap extends SemanticRule("NoSetMap") {

  override def description: String =
    "Disallows calling .map on Set types which can silently discard elements due to deduplication"

  override def isRewrite: Boolean = false

  private val setMatcher = SymbolMatcher.normalized(
    "scala/collection/Set#",
    "scala/collection/immutable/Set#",
    "scala/collection/mutable/Set#",
    "scala/collection/immutable/SortedSet#",
    "scala/collection/SortedSet#"
  )

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case t @ Term.Apply.After_4_6_0(Term.Select(qualifier, Term.Name("map")), _) if isSetType(qualifier) =>
        Patch.lint(
          Diagnostic(
            id = "NoSetMap",
            message = "Calling .map on a Set can silently discard elements due to deduplication. " +
              "Use .toList.map(...) or .toList.map(...).toSet if you need a Set result.",
            position = t.pos,
            explanation = "Sets automatically deduplicate elements, which means .map can silently lose data. " +
              "This violates functor laws where mapping should preserve the number of elements. " +
              "For example: Set(1, 2, 3).map(_ => 0) results in Set(0) with only one element. " +
              "Convert to List before mapping for predictable behavior.",
            severity = LintSeverity.Warning
          )
        )
    }.asPatch
  }

  private def isSetType(term: Term)(implicit doc: SemanticDocument): Boolean = {
    term.symbol.info.exists { info =>
      setMatcher.matches(info.symbol) ||
      info.signature.toString.contains("Set[") ||
      info.symbol.value.contains("Set")
    }
  }
}