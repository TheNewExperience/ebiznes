package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FavoriteLineRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, FavoriteRepo:FavoriteRepository, ProductRepo:ProductRepository)(implicit ec: ExecutionContext) {
  protected [models]val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  protected [models] class FavoriteLineTable(tag: Tag) extends Table[FavoriteLine](tag,"favorite_line") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def product_name = column[String]("product_name")
    def product_id = column[Long]("product_id")
    private def product_fk = foreignKey("product_id_fk",product_id,productTab)(_.id_product)
    def favorite_id = column[Long]("favorite_id")
    private def favorite_id_fk = foreignKey("favorite_id_fk",favorite_id, favoriteTab)(_.id)
    def * = (id,product_name,product_id,favorite_id)<>((FavoriteLine.apply _).tupled,FavoriteLine.unapply)
  }
  import FavoriteRepo.FavoriteTable
  import ProductRepo.ProductTable

  protected [models] val favoriteLine = TableQuery[FavoriteLineTable]
  private val productTab = TableQuery[ProductTable]
  private val favoriteTab = TableQuery[FavoriteTable]

  def create( product_name:String,product_id: Long,favorite_id:Long): Future[FavoriteLine] = db.run {
    (favoriteLine.map(fl =>(fl.product_name,fl.product_id,fl.favorite_id))
      returning( favoriteLine.map(_.id))
      into {case((product_name,product_id,favorite_id),id) => FavoriteLine(id,product_name,product_id,favorite_id)}
      )+=(product_name,product_id,favorite_id)
  }


  def list(): Future[Seq[FavoriteLine]] = db.run {
    favoriteLine.result
  }

  def getById(id: Long): Future[FavoriteLine] = db.run {
    favoriteLine.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[FavoriteLine]] = db.run {
    favoriteLine.filter(_.id === id).result.headOption
  }

  def update(id: Long, new_fav_line: FavoriteLine): Future[Unit] = {
    val fav_line_to_update: FavoriteLine = new_fav_line.copy(id)
    db.run {
      (favoriteLine.filter(_.id === id).update(fav_line_to_update)).map(_ => ())
    }
  }

  def delete(id: Long): Future[Unit] = db.run {
    (favoriteLine.filter(_.id === id).delete).map(_ => ())
  }

}


