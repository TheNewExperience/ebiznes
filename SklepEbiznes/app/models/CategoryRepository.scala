package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class CategoryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CategoryTable(tag: Tag) extends Table[Category](tag, "category") {
    def id_category = column[Int]("id_category", O.PrimaryKey, O.AutoInc)

    def name = column[String]("category_name")

    def * = (id_category, name) <> ((Category.apply _).tupled, Category.unapply)

  }

  val category = TableQuery[CategoryTable]

  def create(c_name: String): Future[Category] = db.run {
    (category.map(c => (c.name))
      returning category.map(_.id_category)
      into ((name, id) => Category(id, name))
      ) += c_name


  }

  def list(): Future[Seq[Category]] = db.run {
    category.result
  }

  def getById(id: Int): Future[Category] = db.run {
    category.filter(_.id_category === id).result.head
  }

  def getByIdOption(id: Int): Future[Option[Category]] = db.run {
    category.filter(_.id_category === id).result.headOption
  }

  def update(id: Int, new_cat: Category): Future[Unit] = {
    val categoryToUpdate: Category = new_cat.copy(id)
    db.run {
      (category.filter(_.id_category === id).update(categoryToUpdate)).map(_ => ())
    }
  }

  def delete(id: Int): Future[Unit] = db.run {
    (category.filter(_.id_category === id).delete).map(_ => ())
  }

}


