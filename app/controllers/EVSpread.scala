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
      case Row(id: Long, hp: Int, attack: Int, defence: Int, spa: Int, spd: Int, speed: Int) => {
        JsObject(
          "id"      -> JsNumber(id) ::
          "hp"      -> JsNumber(hp) ::
          "attack"  -> JsNumber(attack) ::
          "defence" -> JsNumber(defence) ::
          "spa"     -> JsNumber(spa) ::
          "spd"     -> JsNumber(spd) ::
          "speed"   -> JsNumber(speed) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!")
    }
  }

}