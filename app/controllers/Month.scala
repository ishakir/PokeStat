package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object Month extends REST {

  val tableName: String = "months"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number"    -> validateInt,
    "year_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, number: Int, year_id: Int) => {
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