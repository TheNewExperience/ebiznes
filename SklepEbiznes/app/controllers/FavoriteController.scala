package controllers

import javax.inject.{Inject, Singleton}
import models.{ Favorite,FavoriteRepository,Product, ProductRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import play.api.libs.json._
import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FavoriteController @Inject()(userRepo: UserRepository, favoriteRepo: FavoriteRepository, productRepo:ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val favoriteForm: Form[CreateFavoriteForm] = Form {
    mapping(

      "user_id" -> longNumber,


    )(CreateFavoriteForm.apply)(CreateFavoriteForm.unapply)
  }
  val updateFavoriteForm: Form[UpdateFavoriteForm] = Form {
    mapping(
      "id" -> longNumber,
      "user_id" -> longNumber,
    )(UpdateFavoriteForm.apply)(UpdateFavoriteForm.unapply)
  }

  def getFavorites: Action[AnyContent] = Action.async { implicit request =>
    val favorites = favoriteRepo.list()
    favorites.map(favorites => Ok(views.html.favorites(favorites)))
  }

  def getFavorite(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val favorite = favoriteRepo.getByIdOption(id)
    favorite.map(favorite => favorite match {
      case Some(f) => Ok(views.html.favorite(f))
      case None => Redirect(routes.FavoriteController.getFavorites())
    })
  }

  def updateFavorite(id: Long): Action[AnyContent] = Action.async { implicit reqest: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }

    val favorite = favoriteRepo.getById(id)
    favorite.map(favorite => {
      val favoriteForm = updateFavoriteForm.fill(UpdateFavoriteForm(favorite.id,favorite.user_id))
        Ok(views.html.favoriteupdate(favoriteForm, us))

    })
  }

  def updateFavoriteHandle = Action.async { implicit reqest =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }

    updateFavoriteForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.favoriteupdate(errorForm,us))
        )
      },
      favorite => {
        favoriteRepo.update(favorite.id, Favorite(favorite.id,favorite.user_id)).map { _ =>
          Redirect(routes.FavoriteController.updateFavorite(favorite.id)).flashing("success" -> "favorite updated")
        }
      }
    )
  }


  def addFavorite: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>

    val users = userRepo.list()
      users.map(us=>Ok(views.html.favoriteadd(favoriteForm,us)))

  }

  def addFavoriteHandle = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }


    favoriteForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.favoriteadd(errorForm,us))
        )
      },
      favorite => {
        favoriteRepo.create(favorite.user_id).map { _ =>
          Redirect(routes.FavoriteController.addFavorite()).flashing("success" -> "favorite.created")
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    favoriteRepo.delete(id)
    Redirect("/favorites")
  }


    //JSONS

  def getFavoritesJson() = Action.async{implicit request  =>
    val favorites = favoriteRepo.list()
    favorites.map(fav => Ok(Json.toJson(fav.toArray)))
  }


  def getFavoriteJson(id: Long) = Action.async({ implicit  request =>
    val favorite = favoriteRepo.getById(id)
    favorite.map(fav => Ok(Json.toJson(fav)))

  })


  def addFavoriteJson() = Action.async { implicit  request =>
    favoriteForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      fav => {
        favoriteRepo.create(fav.user_id).map { fav =>
          Ok(Json.toJson(fav))
        }
      }
    )

  }

  def updateFavoriteJson(id: Long) = Action.async { implicit  request =>

    updateFavoriteForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      fav => {
        favoriteRepo.update(id, Favorite(id, fav.user_id)).map { _ =>
          Ok(Json.toJson(Favorite(id, fav.user_id)))
        }
      }
    )
  }

  def deleteFavoriteJson(id:Long) = Action { implicit  request =>

    favoriteRepo.delete(id)
    Ok("Deleted product.")
  }

}

case class CreateFavoriteForm(user_id:Long)

case class UpdateFavoriteForm(id: Long,user_id:Long)