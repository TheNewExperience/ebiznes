package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import models.{Category, CategoryRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CategoryController @Inject()(CategoryRepo: CategoryRepository, cc: MessagesControllerComponents)(implicit  ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val categoryForm: Form[CreateCategoryForm] = Form {
    mapping(
      "name" ->nonEmptyText
    )(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }

  val updateCategorForm: Form[UpdateCategoryForm] = Form  {
    mapping(
      "id" ->number ,
      "name" ->nonEmptyText,
    )(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }


  def index =  Action{
    Ok(views.html.index("My application is ready."))
  }

  def getCategories: Action[AnyContent] = Action.async {
    val categories = CategoryRepo.list()
    categories.map(categories => Ok(views.html.categories(categories)))
  }

  def getCategory(id: Int): Action[AnyContent] = Action.async {
    implicit request =>
      val category = CategoryRepo.getByIdOption(id)
      category.map(category => category match{
        case Some(cat) =>Ok(views.html.category(cat))
        case None => Redirect(routes.CategoryController.getCategories())
      })
  }

  def delete(id: Int):Action[AnyContent] = Action {
    CategoryRepo.delete(id)
    Redirect("/categories")
  }

  def addCategory: Action[AnyContent] = Action.async  {implicit request: MessagesRequest[AnyContent] =>
    categoryForm.bindFromRequest.fold(
      errorForm=>{
        Future.successful(
                BadRequest(views.html.categoryadd(errorForm))
              )
      },
      category => {
        CategoryRepo.create(category.name).map { _ =>
          Redirect(routes.CategoryController.addCategory()).flashing("success"->"category.created")
        }
      }
    )



  }



  def updateCategory(id: Int): Action[AnyContent] = Action.async { implicit  request: MessagesRequest[AnyContent] =>
   val category = CategoryRepo.getById(id)
    category.map(category => {
      val catForm =  updateCategorForm.fill(UpdateCategoryForm(category.id_category,category.category_name))
      Ok(views.html.categoryupdate(catForm))
    })

  }


  def updateCategoryHandle = Action.async{implicit request =>
    updateCategorForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.categoryupdate(errorForm))
        )
      },
      category=> {
        CategoryRepo.update(category.id, Category(category.id,category.name)).map { _ =>
          Redirect(routes.CategoryController.updateCategory(category.id)).flashing("success" -> "category updated")
        }
      }
    )
  }



}
case class CreateCategoryForm(name:String)

case class UpdateCategoryForm(id: Int, name:String)
