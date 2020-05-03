package models

import play.api.libs.json._

case class Product(id_product: Long, product_name: String, product_description: String, product_price: Double,category: Int,manufacturer: Int)

object Product {
 implicit val productFormat = Json.format[Product]
}

