package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object EVSpread extends REST {

  val tableName: String = "ev_spreads"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "hp"      -> validateByte,
    "attack"  -> validateByte,
    "defence" -> validateByte,
    "spa"     -> validateByte,
    "spd"     -> validateByte,
    "speed"   -> validateByte
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Int, number: Byte) => {
        JsObject(
          "id"      -> JsNumber(id) ::
          "hp"      -> JsNumber(number) ::
          "attack"  -> JsNumber(number) ::
          "defence" -> JsNumber(number) ::
          "spa"     -> JsNumber(number) ::
          "spd"     -> JsNumber(number) ::
          "speed"   -> JsNumber(number) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!")
    }
  }

}