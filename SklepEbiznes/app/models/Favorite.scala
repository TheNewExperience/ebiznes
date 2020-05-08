package models

import play.api.libs.json.Json

case class Favorite(id: Long,productName: String, product_id: Long,user_id:Long)

object Favorite {
  implicit val favoriteFormat = Json.format[Favorite]
}

