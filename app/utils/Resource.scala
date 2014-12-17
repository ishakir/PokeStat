package utils

import play.api.libs.json.Json
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue

object Resource {
  def successStructure(data: JsValue): JsValue = {
    JsObject(
      "metadata" -> JsObject(
        "status" -> JsString("success") ::
        Nil
      ) ::
      "data" -> data ::
      Nil
    )
  }

  def errorStructure(message: String): JsValue = {
    JsObject(
      "metadata" -> JsObject(
        "status"  -> JsString("error") ::
        "message" -> JsString(message) ::
        Nil
      ) :: Nil
    )
  }
}