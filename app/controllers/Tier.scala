package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

object Tier extends REST {

  val tableName: String = "tiers"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "name" -> allStringsValidator,
    "generation_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, name: String, generation_id: Long) => {
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