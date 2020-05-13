package controllers

import javax.inject.{Inject, Singleton}
import models.{FavoriteLine, Favorite, FavoriteLineRepository, FavoriteRepository, Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

@Singleton
class FavoriteLineController @Inject()(favoriteLineRepo: FavoriteLineRepository, productRepo:ProductRepository,favoriteRepo:FavoriteRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val favLineForm: Form[CreateFavoriteLineForm] = Form {
    mapping(

      "product_name" -> nonEmptyText,
      "product_id" -> longNumber,
      "favorite_id" ->longNumber

    )(CreateFavoriteLineForm.apply)(CreateFavoriteLineForm.unapply)
  }
  val updateFavLineForm: Form[UpdateFavoriteLineForm] = Form {
    mapping(
      "id" -> longNumber,
      "product_name" -> nonEmptyText,
      "product_id" -> longNumber,
      "favorite_id" ->longNumber
    )(UpdateFavoriteLineForm.apply)(UpdateFavoriteLineForm.unapply)
  }

  def getFavLines: Action[AnyContent] = Action.async { implicit request =>
    val favLines = favoriteLineRepo.list()
    favLines.map(lines => Ok(views.html.favlines(lines)))
  }

  def getById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val favLine = favoriteLineRepo.getByIdOption(id)
    favLine.map(favLi => favLi match {
      case Some(fav) => Ok(views.html.favline(fav))
      case None => Redirect(routes.FavoriteLineController.getFavLines())
    })
  }

  def updateFavLine(id: Long): Action[AnyContent] = Action.async { implicit reqest: MessagesRequest[AnyContent] =>
    var fv: Seq[Favorite] = Seq[Favorite]()
    val favorite = favoriteRepo.list().onComplete {
      case Success(favorites) => fv = favorites
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    val favLine = favoriteLineRepo.getById(id)
    favLine.map(fLine => {
      val favLineForm = updateFavLineForm.fill(UpdateFavoriteLineForm(fLine.id,fLine.product_name,fLine.product_id,fLine.favorite_id))
      Ok(views.html.favlineupdate(favLineForm,  prod,fv))

    })
  }

  def updateFavLineHandle = Action.async { implicit reqest =>
    var fv: Seq[Favorite] = Seq[Favorite]()
    val favorite = favoriteRepo.list().onComplete {
      case Success(favorites) => fv = favorites
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    updateFavLineForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.favlineupdate(errorForm,prod,fv))
        )
      },
      favLine => {
        favoriteLineRepo.update(favLine.id,FavoriteLine(favLine.id,favLine.product_name,favLine.product_id,favLine.favorite_id)).map { _ =>
          Redirect(routes.FavoriteLineController.updateFavLine(favLine.id)).flashing("success" -> "fav line updated")
        }
      }
    )
  }


  def addFavLine: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var fv: Seq[Favorite] = Seq[Favorite]()
    val favorite = favoriteRepo.list().onComplete {
      case Success(favorites) => fv = favorites
      case Failure(_) => print("fail")
    }
    val products = productRepo.list()




      products.map(prod=>Ok(views.html.favlineadd(favLineForm, prod,fv)))

  }

  def addFavLineHandle = Action.async { implicit request =>
    var fv: Seq[Favorite] = Seq[Favorite]()
    val favorite = favoriteRepo.list().onComplete {
      case Success(favorites) => fv = favorites
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }

    favLineForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.favlineadd(errorForm,prod,fv))
        )
      },
      favLine => {
        favoriteLineRepo.create(favLine.product_name,favLine.product_id,favLine.favorite_id).map { _ =>
          Redirect(routes.FavoriteLineController.addFavLine()).flashing("success" -> "fav line.created")
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    favoriteLineRepo.delete(id)
    Redirect("/favlines")
  }

  //JSONS
  def getFavoriteLinesJson() = Action.async{implicit request  =>
    val favoriteL = favoriteRepo.list()
    favoriteL.map(favL => Ok(Json.toJson(favL.toArray)))
  }


  def getFavoriteLineJson(id: Long) = Action.async({ implicit  request =>
    val favoriteL = favoriteLineRepo.getById(id)
    favoriteL.map(fav => Ok(Json.toJson(fav)))

  })


  def addFavoriteLineJson() = Action.async { implicit  request =>
    favLineForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      favL => {
        favoriteLineRepo.create(favL.product_name,favL.product_id,favL.favorite_id).map { product =>
          Ok(Json.toJson(product))
        }
      }
    )

  }

  def updateFavoriteLineJson(id: Long) = Action.async { implicit  request =>

    updateFavLineForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      favL => {
        favoriteLineRepo.update(id, FavoriteLine(id,favL.product_name,favL.product_id,favL.favorite_id)).map { _ =>
          Ok(Json.toJson(FavoriteLine(id, favL.product_name,favL.product_id,favL.favorite_id)))
        }
      }
    )
  }

  def deleteFavoriteLineJson(id:Long) = Action { implicit  request =>

    favoriteLineRepo.delete(id)
    Ok("Deleted product.")
  }
}

case class CreateFavoriteLineForm( product_name:String, product_id: Long, favorite_id:Long)

case class UpdateFavoriteLineForm(id:Long, product_name:String, product_id: Long, favorite_id:Long)