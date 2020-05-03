package controllers

import javax.inject.{Inject, Singleton}
import models.{Category, CategoryRepository, Manufacturer, ManufacturerRepository, Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.format.Formats._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ProductController @Inject()(productsRepo: ProductRepository, categoryRepo: CategoryRepository,manufacturerRepo: ManufacturerRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val productForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "price" -> of[Double],
      "category" -> number,
      "manufacturer" -> number,

    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }
  val updateProductForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "price" -> of[Double],
      "category" -> number,
      "manufacturer" -> number,
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def getProducts: Action[AnyContent] = Action.async { implicit request =>
    val products = productsRepo.list()
    products.map(products => Ok(views.html.products(products)))
  }

  def getProduct(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val product = productsRepo.getByIdOption(id)
    product.map(product => product match {
      case Some(p) => Ok(views.html.product(p))
      case None => Redirect(routes.ProductController.getProducts())
    })
  }

  def updateProduct(id: Long): Action[AnyContent] = Action.async { implicit reqest: MessagesRequest[AnyContent] =>
    var categ: Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }
    var manuf: Seq[Manufacturer] = Seq[Manufacturer]()
    val manufacturers = manufacturerRepo.list().onComplete {
      case Success(manu) => manuf = manu
      case Failure(_) => print("fail")
    }
    val product = productsRepo.getById(id)
    product.map(product => {
      val prodForm = updateProductForm.fill(UpdateProductForm(product.id_product, product.product_name, product.product_description, product.product_price, product.category, product.manufacturer))
      Ok(views.html.productupdate(prodForm, categ, manuf))

    })
  }

  def updateProductHandle = Action.async { implicit reqest =>
    var categ: Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }
    var manuf: Seq[Manufacturer] = Seq[Manufacturer]()
    val manufacturers = manufacturerRepo.list().onComplete {
      case Success(manu) => manuf = manu
      case Failure(_) => print("fail")
    }

    updateProductForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productupdate(errorForm, categ,manuf))
        )
      },
      product => {
        productsRepo.update(product.id, Product(product.id, product.name, product.description, product.price, product.category, product.manufacturer)).map { _ =>
          Redirect(routes.ProductController.updateProduct(product.id)).flashing("success" -> "product updated")
        }
      }
    )
  }


  def addProduct: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var categ: Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    val manufacturers = manufacturerRepo.list()

      manufacturers.map(manuf=>Ok(views.html.productadd(productForm, categ, manuf)))

  }

  def addProductHandle = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }
    var manuf: Seq[Manufacturer] = Seq[Manufacturer]()
    val manufacturers = manufacturerRepo.list().onComplete {
      case Success(manu) => manuf = manu
      case Failure(_) => print("fail")
    }

    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productadd(errorForm, categ, manuf))
        )
      },
      product => {
        productsRepo.create(product.name, product.description, product.price, product.category, product.manufacturer).map { _ =>
          Redirect(routes.ProductController.addProduct()).flashing("success" -> "product.created")
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    productsRepo.delete(id)
    Redirect("/products")
  }

}

case class CreateProductForm(name:String,description: String, price: Double, category: Int, manufacturer: Int)

case class UpdateProductForm(id: Long, name:String,description: String, price: Double, category: Int, manufacturer: Int)