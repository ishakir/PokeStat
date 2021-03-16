package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

object DBPokemon extends REST {

  val tableName: String = "pokemon"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "name" -> allStringsValidator
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, name: String) => {
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