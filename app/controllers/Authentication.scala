package controllers

import play.api.libs.ws
import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.{Await, Future}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
/**
  * Created by Piotr on 07.01.2016.
  */
class Authentication extends Controller{
  def isAuthenticated[A](request: Request[A]): Boolean ={
    request.session.get("user_id").map {
      user => true
    }.getOrElse {
      false
    }
  }

  def authenticate = Action {
    Redirect(FacebookApi.oauth_url)
  }

  def validateAuthentication(code: String) = Action.async { implicit request =>

    val access_token = WS.url(FacebookApi.get_token_url(code)).get().map { response =>
      response.json.\("access_token").as[String]
    }

    val response = access_token.map { access_token =>
      WS.url(FacebookApi.confirmation_url(access_token)).get().map { response =>
        val user_id = response.json.\("data").\("user_id").as[String]
        Redirect(routes.ExamController.index).withSession(
          request.session + ("user_id" -> user_id)
        )
      }
    }

    Await.result(response, Duration(3000, "millis"))

  }

}

object AuthAction extends ActionBuilder[Request] with play.api.mvc.Results{
  val oauth = new Authentication
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    if (oauth isAuthenticated request) block(request)
    else Future { Redirect(routes.Authentication.authenticate()) }
  }
}
