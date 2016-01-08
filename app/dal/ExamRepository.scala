package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import java.sql.Date

import models.Exam

import scala.concurrent.{ Future, ExecutionContext }

@Singleton
class ExamRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  private class ExamsTable(tag: Tag) extends Table[Exam](tag, "exams") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def level = column[Int]("level")
    def date = column[Date]("date")

    def * = (id, name, level, date) <> ((Exam.apply _).tupled, Exam.unapply)
  }

  private val exams = TableQuery[ExamsTable]

  def create(name: String, level: Int, date: Date): Future[Exam] = db.run {
    (exams.map(p => (p.name, p.level, p.date))
      returning exams.map(_.id)
      into ((row, id) => Exam(id, row._1, row._2, row._3))
    ) += (name, level, date)
  }

  def list(): Future[Seq[Exam]] = db.run {
    exams.result
  }

  def getById(id: Long): Future[Seq[Exam]] = db.run {
    exams.filter(_.id === id).result
  }

  def save(id: Long, name: String, level: Int, date: Date) : Future[Int] = db.run {
    exams.filter(_.id === id)
         .map(e => (e.name, e.level, e.date))
         .update((name, level, date))
  }
  def delete(id: Long) : Future[Int] = db.run {
    exams.filter(_.id === id).delete
  }
}
