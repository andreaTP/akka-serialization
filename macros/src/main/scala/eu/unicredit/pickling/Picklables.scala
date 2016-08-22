package eu.unicredit.pickling

import scala.reflect.api.Symbols
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

import org.reflections.Reflections
import scala.collection.JavaConverters._

object Picklables {

  def all: Map[Class[_], (((Any) => Array[Byte]),(Array[Byte] => Any))] = macro getAll

  def getAll(c: Context) = {
    import c.universe._

    val reflections = new Reflections()

    val comps =
      reflections.getSubTypesOf(classOf[java.io.Serializable]).asScala

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


    //filtering for debugging purposes...
    val filtered = comps.filter(x =>
      //(x.getName == "java.lang.String" ||
      /*x.getName == "akka.remote.RemoteWatcher$HeartbeatTick$"*/
      (x.getName == "java.lang.String" ||
       x.getName.startsWith("unicredit") ||
       x.getName == "eu.unicredit.test.Test")
    ).filterNot(x => List(
      "akka.remote.RemoteWatcher$HeartbeatTick$"
    ).contains(x.getName) ||
    x.getName.startsWith("scala.tools") ||
    x.getName.startsWith("scala.reflect") ||
    x.getName.startsWith("sbt") ||
    x.getName.startsWith("javax")
    )
    println("comps are \n"+filtered.mkString("\n"))

    val bindings =
      filtered.map(x => {
        val name = x.getCanonicalName().split('.').toList.map(_.replace("$", ""))
        val typ = typeSelect(name)
/*
        val term = termSelect(name)

        if (x.getCanonicalName().endsWith("$")) {
          q"""
          (classOf[$typ],
            (
            ((a: Any) => {
              println("custom object serialization!")
              write(${x.getCanonicalName()}).getBytes
            }),
            ((a: Array[Byte]) => {
              println("custom object deserialization!")
              if (new String(a) == ${x.getCanonicalName()})
                term
              else
                throw new Exception("cannot deserialize")
            })
            )
          )"""
        }
        else {
*/
          try {
            q"""
            (classOf[$typ],
              (
              ((a: Any) => {
                import upickle.default._
                println("upickle is writing!")
                write(a.asInstanceOf[$typ]).getBytes
              }),
              ((a: Array[Byte]) => {
                import upickle.default._
                println("upickle is reading!")
                read[$typ](new String(a))
              })
              )
            )"""
          } catch {
            case _ : Throwable =>
              println("error with "+x)
              q"""
              (classOf[$typ],
                (
                ((a: Any) => {
                  throw new Exception("not serializable")
                  Array[Byte]()
                }),
                ((a: Array[Byte]) => {
                  throw new Exception("not deserializable")
                  null
                })
                )
              )"""
          }

//        }
      })

    //println("show -> "+show(q"Map(..$bindings)"))

    c.Expr[Map[Class[_], (((Any) => Array[Byte]),(Array[Byte] => Any))]](
      q"Map(..$bindings)"
    )
  }
}
