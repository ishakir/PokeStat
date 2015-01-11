package utils

import play.api.libs.json.Json
import play.api.libs.json.JsArray
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue

object Resource {

  private val successMetadataStructure: JsValue = {
    JsObject(
      "status" -> JsString("success") ::
      Nil
    )
  }
  
  def successStructureWithData(data: JsValue): JsValue = {
    JsObject(
      "metadata" -> successMetadataStructure ::
      "data" -> data ::
      Nil
    )
  }

  val successStructureWithoutData: JsValue = {
    JsObject(
      "metadata" -> successMetadataStructure ::
      Nil
    )
  }

  def errorStructure(messages: Seq[String]): JsValue = {
    val messagesJs = messages.map(str => JsString(str))
    JsObject(
      "metadata" -> JsObject(
        "status"  -> JsString("error") ::
        "messages" -> JsArray(messagesJs) ::
        Nil
      ) :: Nil
    )
  }
}