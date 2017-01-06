package org.rebeam.tree.demo

import japgolly.scalajs.react.vdom.prefix_<^._
import org.rebeam.tree.view.View._
import org.rebeam.tree.view._
import org.rebeam.tree.view.Cursor._
import DemoData._
import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react._
import org.rebeam.tree.demo.DemoData.Priority._
import org.rebeam.tree.demo.DemoRoutes._
import org.rebeam.tree.view.pages.Pages
import org.rebeam.tree.view.sortable.{SortableContainer, SortableElement, SortableView}

import scala.scalajs.js
import Mui.Styles.colors

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

    val t = c.model
    val icon  = t.priority match {
      case High => Mui.SvgIcons.ToggleStar(color = Mui.Styles.colors.amber700)()
      case Medium => Mui.SvgIcons.ToggleStarHalf(color = Mui.Styles.colors.blue300)()
      case Low => Mui.SvgIcons.ToggleStarBorder(color = Mui.Styles.colors.grey300)()
    }

    MuiTableRow(key = t.id.toString, style = js.Dynamic.literal("border-bottom" -> "0px"))(
      MuiTableRowColumn(style = js.Dynamic.literal("width" -> "40px"))(
        booleanViewUnlabelled(c.zoomN(Todo.completed))
      ),
      MuiTableRowColumn(style = js.Dynamic.literal("width" -> "40px"))(
        "#" + t.id.value
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
    ^.margin := "24px",
    <.h3("Todo List"),
    spinner()
  )
  val todoListView = ServerRootComponent[TodoList](noTodoList, "api/todolist") {
    c => {
      <.div(
        ^.margin := "24px",
        <.h3("Todo List"),
        textView(c.zoomN(TodoList.name).label("Name")),
//        textView(c.zoomN(TodoList.email).label("Email")),
        todoListTableView(c)
      )
    }
  }

  val noTodoProject = <.div(
    <.h3("Todo Project"),
    spinner()
  )

  val todoListSummaryView = SortableElement.wrap(
    ReactComponentB[(TodoList, Pages[TodoPage])]("todoListSummaryView")
      .render(d => {
        val list = d.props._1
        val toList = d.props._2.set(TodoProjectListPage(list.id))
        <.div(
          ^.onClick --> toList,
          ^.cursor := "pointer",
          ^.className := "react-sortable-item",
          <.div(
            ^.display := "flex",
            MuiAvatar(
              style = js.Dynamic.literal("margin-right" -> "16px"),
              color = colors.white,
              backgroundColor = list.color
            )(s"${list.id.value}": ReactNode),
            <.div(
//              ^.display := "flex",
              <.span(s"${list.name}"),
              <.br,
              <.span(
                ^.color := "rgba(0, 0, 0, 0.541176)",
                s"${list.items.size} item${if (list.items.size == 1) "" else "s"}"
              )
            )
          ),
//          MuiIconButton(
//            onTouchTap = touch(toList)
//          )(Mui.SvgIcons.NavigationChevronRight()()),
          SortableView.handle
        )
      })
      .build
  )

  // Equivalent of the `({items}) =>` lambda passed to SortableContainer in original demo
  val todoProjectListView = SortableContainer.wrap(
    ReactComponentB[CursorP[TodoProject, Pages[TodoPage]]]("todoProjectListView")
    .render(d => {
      val project = d.props.model
      <.div(
        ^.className := "react-sortable-list",
        project.lists.zipWithIndex.map {
          case (list, index) => todoListSummaryView(SortableElement.Props(index = index))((list, d.props.p))
        }
      )
    })
    .build
  )

  // This combines and stores the url and renderer, and will then produce a new element per page. This avoids
  // changing state when changing pages, so we keep the same websocket etc.
  val todoProjectViewFactory = ServerRootComponent.factory[TodoProject, Pages[TodoPage]](noTodoProject, "api/todoproject") {
    cp => {
      <.div(
        cp.p.current match {

          case TodoProjectPage => <.div(
            <.h2("Todo project"),
            textView(cp.zoomN(TodoProject.name).label("Name")),
//            cp.model.lists.map(l =>
//              <.p(raisedButton(s"List ${l.id}, ${l.name} >", primary = true)(cp.p.set(TodoProjectListPage(l.id))))
//            )
            MuiSubheader()("Lists"),
            todoProjectListView(
              SortableContainer.Props(
                onSortEnd = p => cp.zoomN(TodoProject.lists).set(p.updatedList(cp.model.lists)),
                useDragHandle = true,
                helperClass = "react-sortable-handler"
              )
            )(cp)
          )

          case TodoProjectListPage(listId) =>
            val listCursor = cp.zoomN(TodoProject.lists).zoomMatch(FindTodoListById(listId))

            val listNameView = listCursor
              .map[TagMod](c => textView(c.zoomN(TodoList.name).label("Name")))
              .getOrElse(<.div("List not found"))

            <.div(
              <.h2("Todo project"),
              textView(cp.zoomN(TodoProject.name).label("Name")),
              <.h3(s"Todo list $listId"),
              listNameView

//              listCursor.map(c => textView(c.zoomN(TodoList.name).label("Name"))).getOrElse(<.div("List not found"))
    //            textView(cap.cursor.zoomN(TodoProject.lists).zoomMatch(FindTodoListById(TodoListId(listId))).label("Name"))
            )

          case TodoProjectListItemPage(listId, todoId) => <.div(
            <.h2("Todo project"),
            textView(cp.zoomN(TodoProject.name).label("Name")),
            <.h3(s"Todo list $listId"),
            <.h3(s"Todo item $todoId")
          )
        }
      )
    }
  }

}

