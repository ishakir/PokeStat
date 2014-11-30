package controllers

import java.io.File

import play.api._
import play.api.libs.json.Json
import play.api.libs.json.JsArray
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.Play.current
import play.api.mvc._

import scala.io.Source

object Pokemon extends Controller {

  def usage(pokemon: String, tier: String) = Action {
    val statPokemonHash = allFiles(tier).mapValues(list => list.find {
      case jsArray: JsArray => jsArray(1) match {
        case string: JsString => string.value.equalsIgnoreCase(pokemon)
      }
    })
    val usageHash = statPokemonHash.mapValues {
      case Some(value) => value match {
        case array: JsArray => array(2) match {
          case string: JsString => string.value.dropRight(1).toDouble
        }
      }
      case None => 0
    }
    Ok(Json.toJson(usageHash))
  }

  def all(tier: String) = Action {
    val allPokemon = allFiles(tier).values.flatten.map {
      case array: JsArray => array(1) match {
        case string: JsString => string.value
      }
    }.toList.distinct
    Ok(Json.toJson(allPokemon))
  }

  private def allFiles(tier: String): Map[String, List[JsValue]] = {
    val projectRoot = Play.application.path.getAbsolutePath
    val statFiles = new File(projectRoot + "/data/"+tier+"/usage").listFiles
    val statFileHash = statFiles.map (file => file.getName.split('.').head -> file).toMap
    val statStringHash = statFileHash.mapValues(file => Source.fromFile(file.getAbsolutePath).mkString)
    val statJsonHash = statStringHash.mapValues(string => Json.parse(string))
    statJsonHash.mapValues(jsValue => jsValue.as[List[JsValue]])
  }

}