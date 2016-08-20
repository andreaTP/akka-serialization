package unicredit

case class Prova(str: String, int: Int)

object GenMessage {

  var status = 0

  def generate = {
    import eu.unicredit.test._

    status += 1

    Test(
      timestamp = new java.util.Date().getTime,
      msg = s"pippo$status",
      anum =  scala.util.Random.nextDouble
    )
  }
}
