package models
import play.api.libs.json._
case class Category(id_category: Int,category_name:String)

object Category {
  implicit val categoryFormat = Json.format[Category]
}
