package controllers

import java.text.DateFormat

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import models._
import dal._
import java.sql.Date
import play.api.libs.json._
import scala.concurrent.{ ExecutionContext, Future }
import controllers.Authentication

import javax.inject._

class ExamController @Inject() (repo: ExamRepository, oauth: Authentication, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport {
  val addExamForm: Form[CreateExamForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "level" -> number.verifying(min(1), max(10)),
      "date" -> sqlDate
    )(CreateExamForm.apply)(CreateExamForm.unapply)
  }
  val editExamForm: Form[EditExamForm] = Form {
    mapping(
      "id" -> longNumber(),
      "name" -> nonEmptyText,
      "level" -> number.verifying(min(1), max(10)),
      "date" -> sqlDate
    )(EditExamForm.apply)(EditExamForm.unapply)
  }

  def index = AuthAction { implicit request =>
    Ok(views.html.index(addExamForm))
  }

  def addExam = AuthAction.async { implicit request =>
    addExamForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      exam => {
        repo.create(exam.name, exam.level, exam.date).map { _ =>
          Redirect(routes.ExamController.index)
        }
      }
    )
  }

  def getExams = AuthAction.async { implicit request =>
    repo.list().map { exams =>
      Ok(Json.toJson(exams))
    }
  }

  def editExam(id: Long) = AuthAction.async { implicit request =>
    repo.getById(id).map(exams =>
      if (exams.nonEmpty)
        Ok(views.html.editExam(editExamForm.bind(Map(
          "id" -> exams.head.id.toString,
          "name" -> exams.head.name,
          "level" -> exams.head.level.toString,
          "date" -> exams.head.date.toString))))
      else
        Redirect(routes.ExamController.index)
    )
  }

  def saveExam = Action.async { implicit request =>
    editExamForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.editExam(errorForm)))
      },
      exam => {
        repo.save(exam.id, exam.name, exam.level, exam.date).map { _ =>
          Redirect(routes.ExamController.index)
        }
      }
    )
  }

  def deleteExam(id: Long) = Action.async { implicit request =>
    repo.delete(id).map(exams =>
      Redirect(routes.ExamController.index)
    )
  }
  
}

case class CreateExamForm(name: String, level: Int, date: Date)
case class EditExamForm(id: Long, name: String, level: Int, date: Date)
