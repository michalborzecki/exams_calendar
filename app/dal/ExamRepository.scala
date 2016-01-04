package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import java.sql.Date

import models.Exam

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class ExamRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class ExamsTable(tag: Tag) extends Table[Exam](tag, "exams") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The age column */
    def level = column[Int]("level")

    /** Te date column */
    def date = column[Date]("date")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, name, level, date) <> ((Exam.apply _).tupled, Exam.unapply)
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val exams = TableQuery[ExamsTable]

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(name: String, level: Int, date: Date): Future[Exam] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (exams.map(p => (p.name, p.level, p.date))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning exams.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((row, id) => Exam(id, row._1, row._2, row._3))
    // And finally, insert the person into the database
    ) += (name, level, date)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Exam]] = db.run {
    exams.result
  }
}
