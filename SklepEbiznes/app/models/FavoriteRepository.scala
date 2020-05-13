package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FavoriteRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, UserRepo: UserRepository,  ProductRepo:ProductRepository)(implicit ec: ExecutionContext) {
  protected [models] val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  protected [models] class FavoriteTable(tag: Tag) extends Table[Favorite](tag,"favorites") {
    def id = column[Long]("id_favorite",O.PrimaryKey,O.AutoInc)
    def user_id = column[Long]("user_id")
    private def user_fk = foreignKey("user_id_fk",user_id, userTab)(_.id_user)

    def * = (id,user_id)<>((Favorite.apply _).tupled,Favorite.unapply)
  }
  import ProductRepo.ProductTable
  import UserRepo.UserTable

  protected [models] val favorite = TableQuery[FavoriteTable]
  private val prodTab = TableQuery[ProductTable]
  private val userTab = TableQuery[UserTable]

  def create( user_id: Long): Future[Favorite] = db.run {
    (favorite.map(f =>(f.user_id))
      returning( favorite.map(_.id))
      into {case((user_id),id) => Favorite(id,user_id)}
      )+=(user_id)
  }


  def list(): Future[Seq[Favorite]] = db.run {
    favorite.result
  }

  def getById(id: Long): Future[Favorite] = db.run {
    favorite.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Favorite]] = db.run {
    favorite.filter(_.id === id).result.headOption
  }

  def update(id: Long, new_favorite: Favorite): Future[Unit] = {
    val favoriteToUpdate: Favorite = new_favorite.copy(id)
    db.run {
      (favorite.filter(_.id === id).update(favoriteToUpdate)).map(_ => ())
    }
  }

  def delete(id: Long): Future[Unit] = db.run {
    (favorite.filter(_.id === id).delete).map(_ => ())
  }

}


