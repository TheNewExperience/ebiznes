package models

import play.api.libs.json._

case class Manufacturer(id_manufacturer: Int, name_manufacturer:String)

object Manufacturer{
  implicit val manufacturerFormat =Json.format[Manufacturer]
}


