import org.junit.runner._
import org.specs2.runner._
import play.api.test._
import scala.concurrent.duration._

import scala.concurrent.Await

@RunWith(classOf[JUnitRunner])
class ExamSpec extends PlaySpecification{
  "Exams" should {

    "should be empty at the beginning" in new WithApplication{
      val exams = route(FakeRequest(GET, "/exams").withSession(("user_id", "1234567890123456"))).get
      contentAsString(exams).length must beLessThan(5)
    }

    "should be added to list" in new WithApplication{
      val userid = "1234567890123456"
      val addExam = route(FakeRequest(POST, "/addexam")
        .withSession(("user_id", userid))
        .withFormUrlEncodedBody("name" -> "test1", "level" -> "4", "date" -> "2016-02-02")).get
      contentAsString(addExam) must beEmpty
      val exams = route(FakeRequest(GET, "/exams").withSession(("user_id", userid))).get
      contentAsString(exams) must contain("test1")
    }

    "should be visible only for proper user" in new WithApplication{
      val userid = "1234567890123456"
      val user2id = "1234567890123455"
      Await.result(route(FakeRequest(POST, "/addexam")
        .withSession(("user_id", userid))
        .withFormUrlEncodedBody("name" -> "test1", "level" -> "4", "date" -> "2016-02-02")).get, 1 second)
      Await.result(route(FakeRequest(POST, "/addexam")
        .withSession(("user_id", user2id))
        .withFormUrlEncodedBody("name" -> "test2", "level" -> "4", "date" -> "2016-02-02")).get, 1 second)

      var exams = route(FakeRequest(GET, "/exams")
        .withSession(("user_id", userid))).get
      contentAsString(exams) must contain("test1")
      contentAsString(exams) must not contain("test2")
      exams = route(FakeRequest(GET, "/exams")
        .withSession(("user_id", user2id))).get
      contentAsString(exams) must contain("test2")
      contentAsString(exams) must not contain("test1")
    }

    "should be editable" in new WithApplication{
      val userid = "1234567890123456"
      Await.result(route(FakeRequest(POST, "/addexam")
        .withSession(("user_id", userid))
        .withFormUrlEncodedBody("name" -> "test1", "level" -> "4", "date" -> "2016-02-02")).get, 1 second)

      var exams = route(FakeRequest(GET, "/exams")
        .withSession(("user_id", userid))).get
      val ids = (contentAsJson(exams) \\ "id").map(_.as[Int])

      contentAsString(route(FakeRequest(GET, "/editexam/" + ids.head.toString)
        .withSession(("user_id", userid))).get) must contain("test1")

      contentAsString(route(FakeRequest(POST, "/saveexam")
        .withFormUrlEncodedBody("id" -> ids.head.toString, "name" -> "test2", "level" -> "4", "date" -> "2016-02-02")
        .withSession(("user_id", userid))).get) must beEmpty
      exams = route(FakeRequest(GET, "/exams")
        .withSession(("user_id", userid))).get
      contentAsString(exams) must contain("test2")
      contentAsString(exams) must not contain("test1")
    }

    "should be possible to delete" in new WithApplication{
      val userid = "1234567890123456"
      Await.result(route(FakeRequest(POST, "/addexam")
        .withFormUrlEncodedBody("name" -> "test1", "level" -> "4", "date" -> "2016-02-02")
        .withSession(("user_id", userid))).get, 1 second)

      var exams = route(FakeRequest(GET, "/exams")
        .withSession(("user_id", userid))).get
      val ids = (contentAsJson(exams) \\ "id").map(_.as[Int])

      val deleteExam = route(FakeRequest(GET, "/deleteexam/" + ids.head.toString)
        .withSession(("user_id", userid))).get
      contentAsString(deleteExam) must beEmpty

      exams = route(FakeRequest(GET, "/exams")
        .withSession(("user_id", userid))).get
      contentAsString(exams).length must beLessThan(5)
    }
  }
}
