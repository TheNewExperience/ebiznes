package controllers

import java.security.Timestamp
import java.util.Calendar

import javax.inject.{Inject, Singleton}
import models.{User, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.format.Formats._
import play.api.libs.json._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}


import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserController @Inject()( userRepo: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val userForm: Form[CreateUserForm] = Form {
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "reset_question" -> nonEmptyText,
      "reset_answer" -> nonEmptyText,
      "created_at" -> ignored(Calendar.getInstance.getTime.toString),
      "updated_at" ->ignored(Calendar.getInstance.getTime.toString),
      "is admin" -> ignored(0)


    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }
  val updateUserForm: Form[UpdateUserForm] = Form {
    mapping(
      "id" -> longNumber,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "reset_question" -> nonEmptyText,
      "reset_answer" -> nonEmptyText,
      "created_at" ->nonEmptyText,
      "updated_at" ->ignored(Calendar.getInstance.getTime.toString),
      "is admin" -> number(max = 1, min = 0)


    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }

  def getUsers: Action[AnyContent] = Action.async { implicit request =>
    val users = userRepo.list()
    users.map(users => Ok(views.html.users(users)))
  }

  def getUser(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val user = userRepo.getByIdOption(id)
    user.map(user => user match {
      case Some(u) => Ok(views.html.user(u))
      case None => Redirect(routes.UserController.getUsers())
    })
  }

  def updateUser(id: Long): Action[AnyContent] = Action.async { implicit reqest: MessagesRequest[AnyContent] =>

    val user = userRepo.getById(id)
    user.map(user => {
      val userForm = updateUserForm.fill(UpdateUserForm(user.id_user, user.email, user.password,user.reset_question,user.reset_answer,user.created_at,user.updated_at,user.is_admin))
      Ok(views.html.userupdate(userForm))

    })
  }

  def updateUserHandle = Action.async { implicit reqest =>
    updateUserForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userupdate(errorForm))
        )
      },
      user=> {
        userRepo.update(user.id_user,User(user.id_user, user.email,user.password,user.reset_question,user.reset_answer,user.created_at,user.updated_at,user.is_admin)).map { _ =>
          Redirect(routes.UserController.updateUser(user.id_user)).flashing("success" -> "user updated")
        }
      }
    )
  }


  def addUser: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.useradd(errorForm))
        )
      },
      user => {
        userRepo.create(user.email, user.password, user.reset_question, user.reset_answer, user.created_at, user.updated_at, user.is_admin).map { _ =>
          Redirect(routes.UserController.addUser()).flashing("success" -> "user.created")
        }
      }
    )
  }
  def delete(id: Long): Action[AnyContent] = Action {
    userRepo.delete(id)
    Redirect("/users")
  }

//  JSONS

  def getUsersJson() = Action.async{implicit request  =>
    val users = userRepo.list()
    users.map(user => Ok(Json.toJson(user.toArray)))
  }



  def getUserJson(id: Long) = Action.async({ implicit  request =>
    val user = userRepo.getById(id)
    user.map(user => Ok(Json.toJson(user)))

  })


  def addUserJson() = Action.async { implicit  request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      user => {
        userRepo.create(user.email,user.password,user.reset_question,user.reset_answer,user.created_at,user.updated_at,user.is_admin).map { user =>
          Ok(Json.toJson(user))
        }
      }
    )

  }

  def updateUserJson(id: Long) = Action.async { implicit  request =>

    updateUserForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      user => {
        userRepo.update(id, User(id,user.email,user.password,user.reset_question,user.reset_answer,user.created_at,user.updated_at,user.is_admin)).map { _ =>
          Ok(Json.toJson(User(id,user.email,user.password,user.reset_question,user.reset_answer,user.created_at,user.updated_at,user.is_admin)))
        }
      }
    )
  }

  def deleteUserJson(id:Long) = Action { implicit  request =>

    userRepo.delete(id)
    Ok("Deleted product.")
  }

}

case class CreateUserForm(email: String, password: String, reset_question: String, reset_answer: String, created_at: String,updated_at: String,is_admin:Int)

case class UpdateUserForm( id_user: Long, email: String, password: String, reset_question: String, reset_answer: String,created_at: String,updated_at: String,is_admin:Int)