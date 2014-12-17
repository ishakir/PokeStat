package controllers

import anorm.Row
import anorm.SQL
import anorm.SqlParser.get

import db.Queries

import play.api._
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.Json
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.mvc._

import utils.Resource

object Generation extends Controller {

  def create = Action { request =>
    request.body.asJson.map { json =>
      (json \ "number").asOpt[Int].map { number =>
        number match {
          case num if num < 1   => BadRequest(Resource.errorStructure("Generation number is too low, try 1 < number < 256"))
          case num if num > 255 => BadRequest(Resource.errorStructure("Generation number is too high, try 1 < number < 256"))
          case num              => {
            DB.withConnection { implicit c =>
              try {
                val result: Option[Long] = SQL(Queries.Generation.insert(num.toByte)).executeInsert()
                result match {
                  case Some(id) => {
                    formatUnique(SQL(Queries.Generation.getById(id.toInt))()) match {
                      case Some(json) => Ok(json)
                      case None       => InternalServerError(Resource.errorStructure("Create failed for an unknown reason"))
                    }
                  }
                  case None => InternalServerError(Resource.errorStructure("Create failed for an unknown reason"))
                } 
              } catch {
                case e: Exception => BadRequest(Resource.errorStructure(e.getMessage))
              }
            }
          }
        }
      }.getOrElse {
        BadRequest(Resource.errorStructure("Missing json object parameter 'number'"))
      }
    }.getOrElse {
      BadRequest(Resource.errorStructure("Expecting Json data, try setting the 'Content-Type' header to 'application/json'"))
    }
  }

  def update(id: Int) = Action { request =>
    request.body.asJson.map { json =>
      (json \ "number").asOpt[Int].map { number =>
        number match {
          case num if num < 1   => BadRequest(Resource.errorStructure("Generation number is too low, try 1 < number < 256"))
          case num if num > 255 => BadRequest(Resource.errorStructure("Generation number if too high, try 1 < number < 256"))
          case num              => {
            DB.withConnection { implicit c =>
              try {
                val result: Int = SQL(Queries.Generation.update(id, num.toByte)).executeUpdate()
                result match {
                  case 0   => NotFound(Resource.errorStructure("No generation found"))
                  case res => {
                    formatUnique(SQL(Queries.Generation.getById(id))()) match {
                      case Some(json) => Ok(json)
                      case None => NotFound(Resource.errorStructure("No generation found"))
                    }
                  }
                }
              } catch {
                case e: Exception => BadRequest(Resource.errorStructure(e.getMessage))
              }
            }
          }
        }
      }.getOrElse {
        BadRequest(Resource.errorStructure("Missing json object parameter 'number'"))
      }
    }.getOrElse {
      BadRequest(Resource.errorStructure("Expecting Json data, try setting the 'Content-Type' header to 'application/json"))
    }
  }

  def getWithParams(number: Option[Int]) = Action {
    val query = number match {
      case Some(num) => Queries.Generation.getByNumber(num.toByte)
      case None      => Queries.Generation.getAll()
    }
    DB.withConnection { implicit c =>
      Ok(formatMany(SQL(query)()))
    }
  }

  def getById(id: Int) = Action {
    DB.withConnection { implicit c =>
      formatUnique(SQL(Queries.Generation.getById(id))()) match {
        case Some(json) => Ok(json)
        case None => NotFound(Resource.errorStructure("No generation found"))
      }
    }
  }

  def delete(id: Int) = Action {
    DB.withConnection { implicit c =>
      val result: Int = SQL(Queries.Generation.deleteById(id)).executeUpdate()
      result match {
        case 0   => NotFound(Resource.errorStructure("No generation found"))
        case res => Ok(JsObject(
          "metadata" -> JsObject(
            "status" -> JsString("success") ::
            Nil
          ) ::
          Nil
        ))
      }
    }
  }

  private def formatUnique(rows: Seq[Row]): Option[JsValue] = {
    rows.toList match {
      case Nil         => None
      case List(value) => Some(Resource.successStructure(singleGeneration(value)))
      case _ => throw new IllegalStateException("Expecting 0 or 1 elements!")
    }
  }

  private def formatMany(rows: Seq[Row]): JsValue = {
    val result: Seq[JsValue] = rows.map { row =>
      singleGeneration(row)
    }
    Resource.successStructure(Json.toJson(result))
  }

  private def singleGeneration(generationRow: Row): JsValue = {
    generationRow match {
      case Row(id: Int, number: Byte) => {
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