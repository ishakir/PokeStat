package utils

import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}
import utils.controllers.TierMonthInfo

object Resource {

  private val successMetadataStructure: JsValue = {
    JsObject(
      "status" -> JsString("success") ::
      Nil
    )
  }
  
  def successStructureWithData(data: JsValue): JsValue = {
    JsObject(
      "metadata" -> successMetadataStructure ::
      "data" -> data ::
      Nil
    )
  }

  val successStructureWithoutData: JsValue = {
    JsObject(
      "metadata" -> successMetadataStructure ::
      Nil
    )
  }

  def errorStructure(messages: Seq[String]): JsValue = {
    val messagesJs = messages.map(str => JsString(str))
    JsObject(
      "metadata" -> JsObject(
        "status"  -> JsString("error") ::
        "messages" -> JsArray(messagesJs) ::
        Nil
      ) :: Nil
    )
  }

  def tierMonthsInfosToJson[I <: TierMonthInfo](infos: Seq[I], additionalTransformation: Option[I => JsObject]) = {
    Json.toJson(
      infos.groupBy(info => info.generation.toString).mapValues ( generationInfos => 
        Json.toJson(
          generationInfos.groupBy(genInfo => genInfo.tier).mapValues( tierInfos => 
            Json.toJson(
              tierInfos.groupBy(tierInfo => tierInfo.rating.toString).mapValues( ratingInfos =>
                if(additionalTransformation.isEmpty) Json.toJson(ratingInfos.map(formatDate))
                else Json.toJson(ratingInfos.map(monthInfo => formatDate(monthInfo) -> additionalTransformation.get(monthInfo)).toMap)
              )
            )
          )
        )
      )
    )
  }

  private def formatDate[I <: TierMonthInfo](info: I) = info.month + "/" + info.year

}