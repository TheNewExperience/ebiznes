package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, categoryRepo: CategoryRepository, manufacturerRepo:ManufacturerRepository)(implicit ec: ExecutionContext) {
 val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag,"product") {
    def id_product = column[Long]("id_product",O.PrimaryKey,O.AutoInc)
    def product_name = column[String]("product_name")
    def product_description = column[String]("product_description")
    def product_price = column[Double]("price")
    def category = column[Int]("id_category")
    def category_fk = foreignKey("category_fk",category, catTab)(_.id_category)
    def manufacturer = column[Int]("id_manufacturer")
    def manufacturer_fk = foreignKey("product_manufacturer_fk",manufacturer, manuTab)(_.id_manufacturer)
    def * = (id_product,product_name,product_description,product_price,category,manufacturer)<>((Product.apply _).tupled,Product.unapply)
  }
  import categoryRepo.CategoryTable
  import manufacturerRepo.ManufacturerTable

   val product = TableQuery[ProductTable]
   val catTab = TableQuery[CategoryTable]
   val manuTab = TableQuery[ManufacturerTable]

  def create(name: String, description: String, price: Double, category: Int,manufacturer: Int): Future[Product] = db.run {
    (product.map(p =>(p.product_name,p.product_description,p.product_price,p.category,p.manufacturer))
      returning( product.map(_.id_product))
      into {case((name,description,price,category,manufacturer),id) => Product(id,name,description,price,category,manufacturer)}
      )+=(name,description,price,category,manufacturer)
  }


  def list(): Future[Seq[Product]] = db.run {
    product.result
  }

  def getById(id: Long): Future[Product] = db.run {
    product.filter(_.id_product === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Product]] = db.run {
    product.filter(_.id_product === id).result.headOption
  }

  def update(id: Long, new_prod: Product): Future[Unit] = {
    val productToUpdate: Product = new_prod.copy(id)
    db.run {
      (product.filter(_.id_product === id).update(productToUpdate)).map(_ => ())
    }
  }

  def delete(id: Long): Future[Unit] = db.run {
    (product.filter(_.id_product === id).delete).map(_ => ())
  }

}


