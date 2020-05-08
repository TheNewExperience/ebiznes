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

  class ManufacturerTable(tag: Tag) extends Table[Manufacturer](tag,"manufacturer") {
    def id_manufacturer = column[Int]("id_manufacturer",O.PrimaryKey,O.AutoInc)
    def name_manufacturer = column[String]("name_manufacturer")
    def * = (id_manufacturer,name_manufacturer)<>((Manufacturer.apply _).tupled,Manufacturer.unapply)
  }

  val manufacturer = TableQuery[ManufacturerTable]

  def create(name: String): Future[Manufacturer] = db.run {
    (manufacturer.map(m =>(m.name_manufacturer))
      returning( manufacturer.map(_.id_manufacturer))
      into ((name, id)=> Manufacturer(id,name))
      )+=(name)
  }


  def list(): Future[Seq[Manufacturer]] = db.run {
    manufacturer.result
  }

  def getById(id: Int): Future[Manufacturer] = db.run {
    manufacturer.filter(_.id_manufacturer === id).result.head
  }

  def getByIdOption(id: Int): Future[Option[Manufacturer]] = db.run {
    manufacturer.filter(_.id_manufacturer === id).result.headOption
  }

  def update(id: Int, new_mat: Manufacturer): Future[Unit] = {
    val manufacturerToUpdate: Manufacturer = new_mat.copy(id)
    db.run {
      (manufacturer.filter(_.id_manufacturer === id).update(manufacturerToUpdate)).map(_ => ())
    }
  }

  def delete(id: Int): Future[Unit] = db.run {
    (manufacturer.filter(_.id_manufacturer === id).delete).map(_ => ())
  }

}


