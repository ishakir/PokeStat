package controllers

import java.io.File

import play.api._
import play.api.libs.json.Json
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
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

  def allNew(generation: String, tier: String, year: String, month: String, rating: String) = Action {
    val parsedJson = getChaosStructure(generation, tier, year, month, rating)
    val pokemonJson = parsedJson \ "data"
    val pokemonData = pokemonJson match {
      case obj: JsObject => obj.value
    }
    val usageOnly = pokemonData.mapValues {
      case obj: JsObject => (obj \ "Raw count") match {
        case num: JsNumber => num.value
      } 
    }
    val array = usageOnly.toArray
    val sortedArray = array.sortWith((tuple1, tuple2) => {
      tuple1._2 > tuple2._2
    }).map(_._1)
    Ok(Json.toJson(sortedArray))
  }

  def teammates(pokemon: String, generation: String, tier: String, year: String, month: String, rating: String) = Action {
    val parsedJson = getChaosStructure(generation, tier, year, month, rating)
    val teammatesJson = parsedJson \ "data" \ pokemon \ "Teammates"
    val teammates = teammatesJson match {
      case obj: JsObject => obj.value
    }
    val map = teammates.mapValues({
      case num: JsNumber => num.value
    })
    val array = map.toArray
    val sortedArray = array.sortWith((tuple1, tuple2) => {
      tuple1._2.abs > tuple2._2.abs
    }).map(tuple => tuple._1)
    Ok(Json.toJson(sortedArray))
  }

  private def getChaosStructure(generation: String, tier: String, year: String, month: String, rating: String) = {
    val projectRoot = Play.application.path.getAbsolutePath
    val file = new File(projectRoot+"/data/chaos/"+generation+"/"+tier+"/"+year+"/"+month+"/"+rating+".json")
    val fileAsString = Source.fromFile(file.getAbsolutePath).mkString
    Json.parse(fileAsString)
  }

  private def allFiles(tier: String): Map[String, List[JsValue]] = {
    val projectRoot = Play.application.path.getAbsolutePath
    val statFiles = new File(projectRoot + "/old/data/"+tier+"/usage").listFiles
    val statFileHash = statFiles.map (file => file.getName.split('.').head -> file).toMap
    val statStringHash = statFileHash.mapValues(file => Source.fromFile(file.getAbsolutePath).mkString)
    val statJsonHash = statStringHash.mapValues(string => Json.parse(string))
    statJsonHash.mapValues(jsValue => jsValue.as[List[JsValue]])
  }

}