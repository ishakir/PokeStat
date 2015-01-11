package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue

object Nature extends REST {

  val tableName: String = "natures"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "name" -> allStringsValidator
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Int, name: String) => {
        JsObject(
          "id"   -> JsNumber(id) ::
          "name" -> JsString(name) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!")
    }
  }

}