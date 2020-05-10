package models

import play.api.libs.json.Json

case class FavoriteLine(id:Long, product_name:String, product_id: Long, favorite_id:Long)

object FavoriteLine {
  implicit val favoriteLineFormat = Json.format[FavoriteLine]
}

