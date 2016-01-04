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
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{

  /**
   * The mapping for the person form.
   */
  val examForm: Form[CreateExamForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "level" -> number.verifying(min(0), max(10)),
      "date" -> sqlDate
    )(CreateExamForm.apply)(CreateExamForm.unapply)
  }

  /**
   * The index action.
   */
  def index = Action {
    Ok(views.html.index(examForm))
  }

  /**
   * The add person action.
   *
   * This is asynchronous, since we're invoking the asynchronous methods on PersonRepository.
   */
  def addExam = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    examForm.bindFromRequest.fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the person creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      // There were no errors in the from, so create the person.
      exam => {
        repo.create(exam.name, exam.level, exam.date).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.ExamController.index)
        }
      }
    )
  }

  /**
   * A REST endpoint that gets all the people as JSON.
   */
  def getExams = Action.async {

  	repo.list().map { exams =>
      Ok(Json.toJson(exams))
    }
  }
}

/**
 * The create person form.
 *
 * Generally for forms, you should define separate objects to your models, since forms very often need to present data
 * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
 * that is generated once it's created.
 */
case class CreateExamForm(name: String, level: Int, date: Date)
