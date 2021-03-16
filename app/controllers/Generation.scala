package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsValue}

object Generation extends REST {

  val tableName: String = "generations"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number" -> validateByte
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, number: Long) => {
        JsObject(
          "id"     -> JsNumber(id) ::
          "number" -> JsNumber(number) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!" + row)
    }
  }

}
