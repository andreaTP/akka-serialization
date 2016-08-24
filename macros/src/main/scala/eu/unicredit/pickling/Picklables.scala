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

    //Let see how we can do that....
    val filtered = comps/*.filterNot( x =>
      x.getName.startsWith("sbt") ||
      x.getName.startsWith("scala.tools") ||
      x.getName.startsWith("scala.reflect") ||
      x.getName.startsWith("xsbt") ||
      x.getName.startsWith("javax.swing") ||
      x.getName.startsWith("org.omg") ||
      x.getName.startsWith("scala.xml") ||
      x.getName.startsWith("ch.epfl.lamp") ||
      x.getName.startsWith("com.sun.org.apache.bcel") ||
      x.getName.reverse.drop(1).contains("$")
    )*/.filter(x =>
      ((
       x.getName == "java.lang.String" ||
       x.getName.startsWith("unicredit") ||
       x.getName.startsWith("eu.unicredit")/* ||
       x.getName.startsWith("akka") && !(
       x.getName.reverse.drop(1).contains('$') ||
       x.getName.startsWith("upickle.default") ||
       x.getName.startsWith("akka.dispatch") ||
       x.getName.startsWith("akka.remote") ||
       {  //need a better accesibility thing
         try {x.newInstance(); true} catch {case _ : Throwable => false}
       }
     )*/
     )
       //x.getName == "eu.unicredit.test.Test"
     )
   )//.take(10)

   println("--> "+comps.filter(_.getName.startsWith("eu.unicredit")))

    println("comps are \n"+filtered.mkString("\n"))
    //println("comps are \n"+filtered.size)

    val bindings =
      filtered.map(x => {
        try {

        val name = x.getCanonicalName().split('.').toList.map(_.replace("$", ""))
        val typ = typeSelect(name)
        val term = termSelect(name)

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

            val str =
            q"""
            Some(
            (classOf[$typ],
              (
              ((a: Any) => {
                import upickle._
                import upickle.default._
                println("upickle is writing!")
                write(a.asInstanceOf[$typ]).getBytes
              }),
              ((a: Array[Byte]) => {
                import upickle._
                import upickle.default._
                println("upickle is reading!")
                read[$typ](new String(a))
              })
              )
            )
            )"""

            c.Expr[(((Any) => Array[Byte]),(Array[Byte] => Any))](
              q"""
              $term
              $str
              """
            )

            str
          } catch {
            case _ : Throwable =>
              println("error with "+x)
              //throw new Exception("mine ;-)")
              q"""None"""
              /*
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
              )"""*/
          }

//        }
      })

    //println("show -> "+show(q"Map(..$bindings)"))

    c.Expr[Map[Class[_], (((Any) => Array[Byte]),(Array[Byte] => Any))]](
      q"Seq(..$bindings).flatten.toMap"
    )
  }
}
