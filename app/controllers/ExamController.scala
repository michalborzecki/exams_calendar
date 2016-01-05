package controllers

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

import javax.inject._

class ExamController @Inject() (repo: ExamRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport {
  val examForm: Form[CreateExamForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "level" -> number.verifying(min(1), max(10)),
      "date" -> sqlDate
    )(CreateExamForm.apply)(CreateExamForm.unapply)
  }

  def index = Action {
    Ok(views.html.index(examForm))
  }

  def addExam = Action.async { implicit request =>
    examForm.bindFromRequest.fold(
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

  def getExams = Action.async {
  	repo.list().map { exams =>
      Ok(Json.toJson(exams))
    }
  }
}

case class CreateExamForm(name: String, level: Int, date: Date)
