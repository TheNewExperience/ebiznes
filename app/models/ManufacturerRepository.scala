package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ManufacturerRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ManufaturerTable(tag: Tag) extends Table[Manufacturer](tag,"manufacturer") {
    def id_manufaturer = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def name_manufaturer = column[String]("name_manufacturer")
    def * = (id_manufaturer,name_manufaturer)<>((Manufacturer.apply _).tupled,Manufacturer.unapply)
  }

  val manufacturer = TableQuery[ManufaturerTable]

  def create(name: String): Future[Manufacturer] = db.run {
    (manufacturer.map(m =>(m.name_manufaturer))
      returning( manufacturer.map(_.id_manufaturer))
      into ((name, id)=> Manufacturer(id,name))
      )+=(name)
  }


  def list(): Future[Seq[Manufacturer]] = db.run {
    manufacturer.result
  }

  def getById(id: Int): Future[Manufacturer] = db.run {
    manufacturer.filter(_.id_manufaturer === id).result.head
  }

  def getByIdOption(id: Int): Future[Option[Manufacturer]] = db.run {
    manufacturer.filter(_.id_manufaturer === id).result.headOption
  }

  def update(id: Int, new_cat: Manufacturer): Future[Unit] = {
    val manufaturerToUpdate: Manufacturer = new_cat.copy(id)
    db.run {
      (manufacturer.filter(_.id_manufaturer === id).update(manufaturerToUpdate)).map(_ => ())
    }
  }

  def delete(id: Int): Future[Unit] = db.run {
    (manufacturer.filter(_.id_manufaturer === id).delete).map(_ => ())
  }

}


