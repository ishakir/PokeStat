package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsValue}

object Year extends REST {

  val tableName: String = "years"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number" -> validateShort
  )

  protected def single(generationRow: Row): JsValue = {
    generationRow match {
      case Row(id: Long, number: Long) => {
        JsObject(
          "id" -> JsNumber(id) ::
          "number" -> JsNumber(number) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!")
    }
  }

}