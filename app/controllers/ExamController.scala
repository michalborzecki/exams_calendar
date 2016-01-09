package controllers


import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import dal._
import java.sql.Date
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

import javax.inject._

class ExamController @Inject() (repo: ExamRepository, val messagesApi: MessagesApi)
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
        repo.create(request.session.get("user_id").get,
          exam.name, exam.level, exam.date).map { _ =>
          Redirect(routes.ExamController.index)
        }
      }
    )
  }

  def getExams = AuthAction.async { implicit request =>
    repo.listForUser(request.session.get("user_id").get).map { exams =>
      Ok(Json.toJson(exams))
    }
  }

  def editExam(id: Long) = AuthAction.async { implicit request =>
    repo.getById(id).map(exams =>
      if (exams.isEmpty || exams.head.userid != request.session.get("user_id").get)
        Redirect(routes.ExamController.index)
      else
        Ok(views.html.editExam(editExamForm.bind(Map(
          "id" -> exams.head.id.toString,
          "name" -> exams.head.name,
          "level" -> exams.head.level.toString,
          "date" -> exams.head.date.toString))))
    )
  }

  def saveExam = AuthAction.async { implicit request =>
    editExamForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.editExam(errorForm)))
      },
      exam => {
        val oldExam = repo.getById(exam.id)
        if (Await.result(oldExam, 1 second).head.userid == request.session.get("user_id").get)
          repo.save(exam.id, exam.name, exam.level, exam.date).map { _ =>
            Redirect(routes.ExamController.index)
          }
        else
          Future { Redirect(routes.ExamController.index) }
      }
    )
  }

  def deleteExam(id: Long) = AuthAction.async { implicit request =>
    val oldExam = repo.getById(id)
    if (Await.result(oldExam, 1 second).head.userid == request.session.get("user_id").get)
      repo.delete(id).map(exams =>
        Redirect(routes.ExamController.index)
      )
    else
      Future { Redirect(routes.ExamController.index) }
  }
}

case class CreateExamForm(name: String, level: Int, date: Date)
case class EditExamForm(id: Long, name: String, level: Int, date: Date)
