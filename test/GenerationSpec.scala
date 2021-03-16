import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.{JsNull, JsNumber, JsObject, JsValue}
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent._

@RunWith(classOf[JUnitRunner])
class GenerationSpec extends Specification {

  val applicationJson = FakeHeaders(
    Seq("Content-Type" -> Seq("application/json"))
  )

  val routeBase = "/api/generations"

  def fakePostRequestWithNumber(number: Int) = fakePostRequest(
    JsObject(
      "number" -> JsNumber(number) ::
      Nil
    )
  )

  def fakePostRequest(json: JsValue) = FakeRequest(
    POST,
    routeBase,
    applicationJson,
    json
  )

  def fakePutRequestWithNumber(number: Int, id: Int) = fakePutRequest(
    JsObject(
      "number" -> JsNumber(number) ::
      Nil
    ),
    id
  )

  def fakePutRequest(json: JsValue, id: Int) = FakeRequest(
    PUT,
    routeBase + "/" + id,
    applicationJson,
    json
  )

  val fakeGetRequest = FakeRequest(
    GET,
    routeBase
  )

  def fakeGetQuery(number: Int) = FakeRequest(
    GET,
    routeBase + "?number=" + number
  )

  def fakeGetByIdRequest(number: Int) = FakeRequest(
    GET,
    routeBase + "/" + number
  )

  def fakeDeleteRequest(number: Int) = FakeRequest(
    DELETE,
    routeBase + "/" + number
  )

  def sendRequest(responseOption: Option[Future[play.api.mvc.Result]], expectedResponse: Int, action: JsValue => Unit) {
    responseOption match {
      case Some(response) => {
        status(response) must equalTo(expectedResponse)
        val jsonContent = contentAsJson(response)
        action(jsonContent)
      }
      case None => 1 must equalTo(2)
    }
  }

  def sendJsonRequest(request: FakeRequest[JsValue], expectedResponse: Int, action: JsValue => Unit) {
    sendRequest(route(request), expectedResponse, action)
  }

  def sendEmptyRequest(request: FakeRequest[play.api.mvc.AnyContentAsEmpty.type], expectedResponse: Int, action: JsValue => Unit) {
    sendRequest(route(request), expectedResponse, action)
  }

  "Generation POST" should {

    "Reject if JSON structure isn't there" in new WithApplication {
      sendJsonRequest(
        fakePostRequest(JsNull),
        400,
        jsonContent => {
          (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
          (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("Missing")
        }
      )
    }

    "Reject if JSON key number is missing" in new WithApplication {
      sendJsonRequest(
        fakePostRequest(
          JsObject(
            "hello" -> JsNull ::
            Nil
          )
        ),
        400,
        jsonContent => {
          (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
          (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("Missing")
        }
      )
    }

    "Succesfully create" in new WithApplication {
      sendJsonRequest(
        fakePostRequestWithNumber(1),
        200,
        jsonContent => {
          (jsonContent \ "metadata" \ "status").as[String] must equalTo("success")
          (jsonContent \ "data" \ "number").as[Int] must equalTo(1)
        }
      )
    }

    "Reject create if generation with number already exists" in new WithApplication {
      val fakePostRequestOne = fakePostRequestWithNumber(1)
      
      sendJsonRequest(
        fakePostRequestOne,
        200,
        jsonContent => sendJsonRequest(
          fakePostRequestOne,
          400,
          jsonContent => {
            (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
            (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("violation")
          }
        )
      )
    }
  }

  "Generation GET" should {
    "return all generations" in new WithApplication {
      val fakePostRequestOne = fakePostRequestWithNumber(1)
      val fakePostRequestTwo = fakePostRequestWithNumber(2)

      sendJsonRequest(
        fakePostRequestOne,
        200,
        jsonContent => sendJsonRequest(
          fakePostRequestTwo,
          200,
          jsonContent => sendEmptyRequest(
            fakeGetRequest,
            200,
            jsonContent => {
              (jsonContent \ "metadata" \ "status").as[String] must equalTo("success")
              val data = (jsonContent \ "data").as[List[JsValue]]
              data.size must equalTo(2)
              (data.head \ "number").as[Int] must equalTo(1)
            }
          ) 
        )
      )
    }

    "only return a generation with a particular number" in new WithApplication {
      val fakePostRequestOne = fakePostRequestWithNumber(1)
      val fakePostRequestTwo = fakePostRequestWithNumber(2)

      sendJsonRequest(
        fakePostRequestOne,
        200,
        jsonContent => sendJsonRequest(
          fakePostRequestTwo,
          200,
          jsonContent => sendEmptyRequest(
            fakeGetQuery(1),
            200,
            jsonContent => {
              (jsonContent \ "metadata" \ "status").as[String] must equalTo("success")
              val data = (jsonContent \ "data").as[List[JsValue]]
              data.size must equalTo(1)
              (data.head \ "number").as[Int] must equalTo(1)
            }
          ) 
        )
      )
    }
  }

  "Generation GET by Id" should {
    "fail if generation with id doesn't exist" in new WithApplication {
      sendEmptyRequest(
        fakeGetByIdRequest(1),
        404,
        jsonContent => {
          (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
          (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("found")
        }
      )
    }

    "return a particular generation" in new WithApplication {
      sendJsonRequest(
        fakePostRequestWithNumber(1),
        200,
        jsonContent => sendEmptyRequest(
          fakeGetByIdRequest((jsonContent \ "data" \ "id").as[Int]),
          200,
          jsonContent => {
            (jsonContent \ "metadata" \ "status").as[String] must equalTo("success")
            (jsonContent \ "data" \ "number").as[Int] must equalTo(1)
          }
        )
      )
    }
  }

  "Generation DELETE" should {
    "fail if generation with id doesn't exist" in new WithApplication {
      sendEmptyRequest(
        fakeDeleteRequest(1),
        404,
        jsonContent => {
          (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
          (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("found")
        }
      )
    }

    "Succesfully delete" in new WithApplication {
      sendJsonRequest(
        fakePostRequestWithNumber(1),
        200,
        jsonContent => {
          val id = (jsonContent \ "data" \ "id").as[Int]
          sendEmptyRequest(
            fakeDeleteRequest(id),
            200,
            jsonContent => {
              (jsonContent \ "metadata" \ "status").as[String] must equalTo("success")
              sendEmptyRequest(
                fakeGetByIdRequest(id),
                404,
                jsonContent => ()
              )
            }
          )
        }
      )
    }
  }

  "Generation PUT" should {
    "fail if such a generation doesnt exist" in new WithApplication {
      sendJsonRequest(
        fakePutRequestWithNumber(1, 1),
        404,
        jsonContent => {
          (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
          (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("found")
        }
      )
    }

    "Succesfully update" in new WithApplication {
      sendJsonRequest(
        fakePostRequestWithNumber(1),
        200,
        jsonContent => sendJsonRequest(
          fakePutRequestWithNumber(2, (jsonContent \ "data" \ "id").as[Int]),
          200,
          jsonContent => {
            (jsonContent \ "metadata" \ "status").as[String] must equalTo("success")
            (jsonContent \ "data" \ "number").as[Int] must equalTo(2)
          }
        )
      )
    }

    // Ones below need editing
    "Reject update if generation with number already exists" in new WithApplication {
      val fakePostRequestOne = fakePostRequestWithNumber(1)
      val fakePostRequestTwo = fakePostRequestWithNumber(2)
      sendJsonRequest(
        fakePostRequestOne,
        200,
        jsonContent => sendJsonRequest(
          fakePostRequestTwo,
          200,
          jsonContent => sendJsonRequest(
            fakePutRequestWithNumber(1, (jsonContent \ "data" \ "id").as[Int]),
            400,
            jsonContent => {
              (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
              (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("violation")
            }
          )
        )
      )
    }

    "Reject if JSON structure isn't there" in new WithApplication {
      sendJsonRequest(
        fakePutRequest(JsNull, 1),
        400,
        jsonContent => {
          (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
          (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("nothing")
        }
      )
    }

    "Reject if JSON key number is missing" in new WithApplication {
      sendJsonRequest(
        fakePutRequest(
          JsObject(
            "hello" -> JsNull ::
            Nil
          ), 
          1
        ),
        400,
        jsonContent => {
          (jsonContent \ "metadata" \ "status").as[String] must equalTo("error")
          (jsonContent \ "metadata" \ "messages")(0).as[String] must contain("nothing")
        }
      )
    }
  }
}
