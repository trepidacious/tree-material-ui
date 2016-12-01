package org.rebeam.tree.demo

import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import org.rebeam.tree.view.Cursor._
import DemoData._
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import org.rebeam.tree.demo.DemoData.Priority._
import org.rebeam.tree.sync.Sync.ModelIdGen

import scala.scalajs.js

object DemoViews {

  val streetView = cursorView[Street]("StreetView") { c =>
    <.div(
      <.p("Blah"),
      intView(c.zoomN(Street.number).label("Number")),
      textView(c.zoomN(Street.name).label("Name")),
      doubleView(c.zoomN(Street.temperature).label("Temperature")),
      raisedButton("Number multiple", primary = true){
        c.act(StreetAction.NumberMultiple(10): StreetAction)
      },
      raisedButton("Capitalise", secondary = true){
        c.act(StreetAction.Capitalise: StreetAction)
      }
    )
  }

  val noAddress = <.div(
    <.h3("Address"),
    spinner()
  )
//  val addressView = WSRootComponent[Address](noAddress, "api/address") {
//    addressCursor => {
//      val streetCursor = addressCursor.zoomN(Address.street)
//      <.div(
//        <.h3("Address"),
//        streetView(streetCursor)
//      )
//    }
//  }

  implicit val addressIdGen = new ModelIdGen[Address] {
    def genId(a: Address) = None
  }
  val addressView = ServerRootComponent[Address](noAddress, "api/address") {
    addressCursor => {
      val streetCursor = addressCursor.zoomN(Address.street)
      <.div(
        <.h3("Address"),
        streetView(streetCursor)
      )
    }
  }


  val homeView = staticView("home")(
    <.div (
      ^.margin := "24px",
      <.h3("Home"),
//      ReactGridLayoutDemo.c()
      SortableContainerDemo.c()
//        WidthProviderDemo.widthBeforeMountHelloView(
//          WidthProviderDemo.HelloData("Higher", "Order Component")
//        )
    )
  )

  val todoView = cursorView[Todo]("TodoView") { c =>
//    def tdText(xs: TagMod*) = <.td(^.cls := "mdl-data-table__cell--non-numeric")(xs)
//
//    def tdPriority(p: Priority) =
//      <.td(
//        ^.classSet1(
//          "mdl-data-table__cell--priority",
//          "mdl-data-table__cell--priority-high" -> (p == High),
//          "mdl-data-table__cell--priority-medium" -> (p == Medium),
//          "mdl-data-table__cell--priority-low" -> (p == Low)
//        ),
//        ^.onClick --> c.act(TodoAction.CyclePriority: TodoAction),
//        <.i(
//          ^.cls := "material-icons",
//          p match {
//            case High => "star"
//            case Medium => "star_half"
//            case Low => "star_border"
//          }
//        )
//      )

    val t = c.model
    val icon  = t.priority match {
      case High => Mui.SvgIcons.ToggleStar(color = Mui.Styles.colors.amber700)()
      case Medium => Mui.SvgIcons.ToggleStarHalf(color = Mui.Styles.colors.blue300)()
      case Low => Mui.SvgIcons.ToggleStarBorder(color = Mui.Styles.colors.grey300)()
    }

    MuiTableRow(key = t.id.toString)(
      MuiTableRowColumn(style = js.Dynamic.literal("width" -> "40px"))(
        booleanViewUnlabelled(c.zoomN(Todo.completed))
      ),
      MuiTableRowColumn(style = js.Dynamic.literal("width" -> "40px"))(
        "#" + t.id
      ),
      MuiTableRowColumn(style = js.Dynamic.literal("width" -> "100%"))(
        textViewPlainLabel(c.zoomN(Todo.name).label("Name"))
      ),
      MuiTableRowColumn(
        style = js.Dynamic.literal("width" -> "40px")
      )(
        MuiIconButton(
          onTouchTap = touch(c.act(TodoAction.CyclePriority: TodoAction))
        )(icon)
      )
    )
  }
  val todoListTableView = cursorView[TodoList]("TodoListTableView") { c =>
    MuiTable(
      selectable = false
    )(
      MuiTableHeader(displaySelectAll = false, adjustForCheckbox = false, enableSelectAll = false)(
        MuiTableRow()(
          MuiTableHeaderColumn(tooltip = "Tick when item is done", style = js.Dynamic.literal("width" -> "40px"))("Done?"),
          MuiTableHeaderColumn(tooltip = "Permanent identifier for the item", style = js.Dynamic.literal("width" -> "40px"))("Id"),
          MuiTableHeaderColumn(tooltip = "Name of item", style = js.Dynamic.literal("width" -> "100%"))("Name"),
          MuiTableHeaderColumn(tooltip = "Priority of item", style = js.Dynamic.literal("width" -> "40px"))("Priority")
        )
      ),
      MuiTableBody(
        showRowHover = true,
        stripedRows = false
      )(
        //zoomAllI produces a list of cursors by position in list,
        //then we view each one. This is easy, but fragile since we
        //don't know that items will stay at the same index in the list
        //        itemsCursor.zoomAllI.map(
        //          todoCursor => todoView.withKey(todoCursor.model.id)(todoCursor)
        //        )

        //zoomAllMatches accepts a function from items to a predicate finding
        //them, and produces a list of cursors for the items.
        //This is more robust, since a delta generated by the cursor will use
        //FindTodoById and so will find the same Todo in an updated list (or
        //do nothing if there is no matching Todo).
        c.zoomN(TodoList.items).zoomAllMatches(todo => FindTodoById(todo.id)).map(todoView(_))
      )
    )
  }

  val noTodoList = <.div(
    <.h3("Todo List"),
    spinner()
  )
  val todoListView = WSRootComponent[TodoList](noTodoList, "api/todolist") {
    c => {
      <.div(
        ^.margin := "24px",
        <.h3("Todo List"),
        textView(c.zoomN(TodoList.name).label("Name")),
        textView(c.zoomN(TodoList.email).label("Email")),
        todoListTableView(c)
      )
    }
  }

}
