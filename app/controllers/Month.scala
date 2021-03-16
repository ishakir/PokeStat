package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsValue}

object Month extends REST {

  val tableName: String = "months"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number"    -> validateInt,
    "year_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, number: Long, year_id: Long) => {
        JsObject(
          "id"      -> JsNumber(id) ::
          "number"  -> JsNumber(number) ::
          "year_id" -> JsNumber(year_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}