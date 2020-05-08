package controllers

import javax.inject.{Inject, Singleton}
import models.{ Favorite,FavoriteRepository,Product, ProductRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FavoriteController @Inject()(userRepo: UserRepository, favoriteRepo: FavoriteRepository, productRepo:ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val favoriteForm: Form[CreateFavoriteForm] = Form {
    mapping(
      "product_name" -> nonEmptyText,
      "product_id" -> longNumber,
      "user_id" -> longNumber,


    )(CreateFavoriteForm.apply)(CreateFavoriteForm.unapply)
  }
  val updateFavoriteForm: Form[UpdateFavoriteForm] = Form {
    mapping(
      "id" -> longNumber,
      "product_name" -> nonEmptyText,
      "product_id" -> longNumber,
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
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }

    val favorite = favoriteRepo.getById(id)
    favorite.map(favorite => {
      val favoriteForm = updateFavoriteForm.fill(UpdateFavoriteForm(favorite.id,favorite.productName,favorite.product_id,favorite.user_id))
        Ok(views.html.favoriteupdate(favoriteForm, prod,us))

    })
  }

  def updateFavoriteHandle = Action.async { implicit reqest =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    updateFavoriteForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.favoriteupdate(errorForm,prod,us))
        )
      },
      favorite => {
        favoriteRepo.update(favorite.id, Favorite(favorite.id,favorite.productName,favorite.product_id,favorite.user_id)).map { _ =>
          Redirect(routes.FavoriteController.updateFavorite(favorite.id)).flashing("success" -> "favorite updated")
        }
      }
    )
  }


  def addFavorite: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }
    val products = productRepo.list()

      products.map(prod=>Ok(views.html.favoriteadd(favoriteForm,  prod,us)))

  }

  def addFavoriteHandle = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }


    favoriteForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.favoriteadd(errorForm, prod,us))
        )
      },
      favorite => {
        favoriteRepo.create(favorite.productName,favorite.product_id,favorite.user_id).map { _ =>
          Redirect(routes.FavoriteController.addFavorite()).flashing("success" -> "favorite.created")
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    favoriteRepo.delete(id)
    Redirect("/favorites")
  }

}

case class CreateFavoriteForm(productName: String, product_id: Long,user_id:Long)

case class UpdateFavoriteForm(id: Long,productName: String, product_id: Long,user_id:Long)