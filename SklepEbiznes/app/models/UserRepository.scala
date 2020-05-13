package models
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
 val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag,"users") {
    def id_user = column[Long]("id_user",O.PrimaryKey,O.AutoInc)
    def email = column[String]("email")
    def password = column[String]("encrypted_password")
    def reset_question = column[String]("reset_question")
    def reset_answer = column[String]("reset_answer")
    def created_at = column[String]("created_at")
    def updated_at = column[String]("updated_at")
    def is_admin = column[Int]("is_admin")
    def * = (id_user,email,password,reset_question,reset_answer,created_at,updated_at,is_admin)<>((User.apply _).tupled,User.unapply)
  }


  val user = TableQuery[UserTable]

  def create(email: String, password: String, reset_question: String, reset_answer: String,created_at: String,updated_at:String,is_admin:Int): Future[User] = db.run {
    (user.map(u =>(u.email,u.password,u.reset_question,u.reset_answer,u.created_at,u.updated_at,u.is_admin))
      returning( user.map(_.id_user))
      into {case((email,password,reset_question,reset_answer,created_at,updated_at,is_admin),id) => User(id,email,password,reset_question,reset_answer,created_at,updated_at,is_admin)}
      )+=(email,password,reset_question,reset_answer,created_at,updated_at,is_admin)
  }


  def list(): Future[Seq[User]] = db.run {
    user.result
  }

  def getById(id: Long): Future[User] = db.run {
    user.filter(_.id_user === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[User]] = db.run {
    user.filter(_.id_user === id).result.headOption
  }

  def update(id: Long, new_user: User): Future[Unit] = {
    val userToUpdate: User = new_user.copy(id)
    db.run {
      (user.filter(_.id_user === id).update(userToUpdate)).map(_ => ())
    }
  }

  def delete(id: Long): Future[Unit] = db.run {
    (user.filter(_.id_user === id).delete).map(_ => ())
  }

}


