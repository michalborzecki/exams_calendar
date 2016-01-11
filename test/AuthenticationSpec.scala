import controllers.{AuthAction, Authentication}
import org.specs2.mutable._
import play.api.mvc._
import play.api.test._
import org.specs2.mock._
import scala.concurrent.Future

/**
  * Created by Piotr on 10.01.2016.
  */
class AuthenticationSpec extends PlaySpecification with Results with Mockito {
  class TestAuthentication() extends Authentication


  "Authentication Tests" should {
    "Is User Authenticated" in new WithApplication {
      val controller = new TestAuthentication

      var request = FakeRequest("GET", "").withSession(("user_id", "1234567"))
      controller.isAuthenticated(request) must beTrue

      request = FakeRequest("GET", "")
      controller.isAuthenticated(request) must beFalse

      request = FakeRequest("GET", "").withSession(("user_id", ""))
      controller.isAuthenticated(request) must beFalse
    }

    "AuthAction Check Authentication" in new WithApplication {
      val controller = mock[Authentication]
      val request = FakeRequest("GET", "")
      controller.isAuthenticated(request).returns(true)

      object TestAuthAction extends AuthAction {
        def oauth: Authentication = controller
      }

      val block = mock[(Request[AnyContent]) => Future[Result]]
      val futureResult = mock[Future[Result]]
      block.apply(request).returns(futureResult)

      TestAuthAction.invokeBlock(request, block) must equalTo(futureResult)

      controller.isAuthenticated(request).returns(false)


      TestAuthAction.invokeBlock(request, block) must not equalTo(futureResult)

    }
  }

}
