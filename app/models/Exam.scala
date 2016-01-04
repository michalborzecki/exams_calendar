package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.sql.Date

case class Exam(id: Long, name: String, level: Int, date: Date)

object Exam {
  implicit val examFormat: Format[Exam] = (
    (__ \ "id").format[Long] and
    (__ \ "name").format[String] and
    (__ \ "level").format[Int] and
    (__ \ "date").format(Writes.sqlDateWrites("YYYY-MM-dd"))(Reads.sqlDateReads("YYYY-MM-dd"))
    )(Exam.apply, unlift(Exam.unapply))
}