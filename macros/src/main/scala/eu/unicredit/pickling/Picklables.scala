package akka.remote

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

    //Let see how we can do that....
    val filtered = comps.filterNot( x =>
      x.getName.startsWith("sbt") ||
      x.getName.startsWith("scala.tools") ||
      x.getName.startsWith("scala.reflect") ||
      x.getName.startsWith("xsbt") ||
      x.getName.startsWith("javax.swing") ||
      x.getName.startsWith("javax.security") ||
      x.getName.startsWith("javax.xml") ||
      x.getName.startsWith("java.swing") ||
      x.getName.startsWith("java.awt") ||
      x.getName.startsWith("java.applet") ||
      x.getName.startsWith("sun.swing") ||
      x.getName.startsWith("sun.awt") ||
      x.getName.startsWith("sun.rmi") ||
      x.getName.startsWith("sun.security") ||
      x.getName.startsWith("com.sun.corba") ||
      x.getName.startsWith("com.sun.java.swing") ||
      x.getName.startsWith("org.omg") ||
      x.getName.startsWith("scala.xml") ||
      x.getName.startsWith("ch.epfl.lamp") ||
      x.getName.startsWith("com.sun.org") ||
      x.getName.startsWith("upickle") ||
      x.getName.startsWith("jdk") ||
      x.getName.startsWith("javax.management") ||
      x.getName.startsWith("javax.print") ||
      x.getName.startsWith("org.jboss") ||
      //here we become more serious... probably a loop...
      (try {
        x.getCanonicalName == "akka.remote.transport.AkkaProtocolTransport.AssociateUnderlyingRefuseUid$" ||
        x.getCanonicalName == "akka.remote.transport.AkkaProtocolTransport.AssociateUnderlyingRefuseUid"
      }
      catch {case _ : Throwable => true})
    )

    var total = filtered.size

    var ok = 0
    var failures = 0

    val bindings =
      filtered/*.par -> produces horrific output and loop */.map(x => {
        try {

          val name = x.getCanonicalName().split('.').toList.map(_.replace("$", ""))
          val typ = typeSelect(name)


          println("checking "+x.getCanonicalName()+ " -> "+total)
          val ast =
            if (x.getCanonicalName().endsWith("$")) {
              val term = termSelect(name)

              q"""
              ($term.getClass,
                (
                ((a: Any) => {
                  import upickle._
                  import upickle.default._
                  write($term).getBytes
                }),
                ((a: Array[Byte]) => {
                  import upickle._
                  import upickle.default._
                  $term
                })
                )
              )"""
            } else {
                q"""
                (classOf[$typ],
                  (
                  ((a: Any) => {
                    import upickle._
                    import upickle.default._
                    write(a.asInstanceOf[$typ]).getBytes
                  }),
                  ((a: Array[Byte]) => {
                    import upickle._
                    import upickle.default._
                    read[$typ](new String(a))
                  })
                  )
                )"""
            }

          //Important Check!
          c.typecheck(ast)

          println("is picklable -> "+x)
          ok += 1

          total -= 1
          Some(ast)
        } catch {
          case _ : Throwable =>
            //print("error with "+x+" ")
            failures += 1
            total -= 1
            println(" " +total+ " " + x)
            None
        }
      }).filter(_.isDefined).map(_.get).toList

      println("**************")
      println("**************")
      println("RESULTS:")
      println("ok -> "+ok)
      println("failures -> "+failures)
      println("**************")
      println("**************")

    c.Expr[Map[Class[_], (((Any) => Array[Byte]),(Array[Byte] => Any))]](
      try {
        q"Seq(..$bindings).toMap"
      } catch {
        case err: Throwable =>
          println("error in code generation")
          q"Map()"
      }
    )
  }
}
