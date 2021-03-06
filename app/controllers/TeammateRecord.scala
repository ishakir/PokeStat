package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsValue}

object TeammateRecord extends REST {

  val tableName: String = "teammate_records"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number"         -> validateFloat,
    "pokemon_id"     -> validateInt,
    "stat_record_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, number: Float, pokemon_id: Long, stat_record_id: Long) => {
        JsObject(
          "id"             -> JsNumber(id) ::
          "number"         -> JsNumber(number) ::
          "pokemon_id"     -> JsNumber(pokemon_id) ::
          "stat_record_id" -> JsNumber(stat_record_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}