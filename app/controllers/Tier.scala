package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue

object Tier extends REST {

  val tableName: String = "tiers"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "name" -> allStringsValidator,
    "generation_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, name: String, generation_id: Int) => {
        JsObject(
          "id" -> JsNumber(id) ::
          "name" -> JsString(name) ::
          "generation_id" -> JsNumber(generation_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!" + row) 
    }
  }

}