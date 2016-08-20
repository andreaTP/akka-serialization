package eu.unicredit.scalapb

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

import org.reflections.Reflections
import scala.collection.JavaConverters._

import com.trueaccord.scalapb._

object GeneratedMessageCompanion {

  def all: Map[Class[_], GeneratedMessageCompanion[_]] = macro getAll

  def getAll(c: Context) = {
    import c.universe._

    val reflections = new Reflections()

    val comps =
      reflections.getSubTypesOf(classOf[GeneratedMessage]).asScala

    def typeSelect(terms: List[String], part: Option[Tree] = None): Select =
      _termSelect(terms, (p, s) => Select(p, TypeName(s)))

    def termSelect(terms: List[String], part: Option[Tree] = None): Select =
      _termSelect(terms, (p, s) => Select(p, TermName(s)))

    def _termSelect(terms: List[String], last: (Tree, String) => Select, part: Option[Tree] = None): Select = {
      part match {
        case None => _termSelect(terms.tail, last, Some(Ident(TermName(terms.head))))
        case Some(p) =>
          if (terms.tail.isEmpty) last(p, terms.head)
          else _termSelect(terms.tail, last, Some(Select(p, TermName(terms.head))))
      }
    }

    val bindings =
      comps.map(c => {
        val name = c.getCanonicalName().split('.').toList
        val typ = typeSelect(name)
        val term = termSelect(name)

        q"(classOf[$typ], $term)"
      })

    c.Expr[Map[Class[_], GeneratedMessageCompanion[_]]](
      q"Map(..$bindings)"
    )
  }
}
