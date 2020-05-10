package models

import play.api.libs.json.Json

case class Favorite(id: Long,user_id:Long)

object Favorite {
  implicit val favoriteFormat = Json.format[Favorite]
}

