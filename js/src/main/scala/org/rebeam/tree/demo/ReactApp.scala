package org.rebeam.tree.demo

import org.scalajs.dom

import org.rebeam.tree.view.MaterialSwatch

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.Dynamic.{global => g}

object ReactApp extends JSApp {

  def main(): Unit = {

    // remove waiting page stuff
    if (!js.isUndefined(g.loadingElement)) {
      g.loadingElement

      dom.document.getElementsByClassName("pg-loading-screen")

      g.document.body.removeChild(g.loadingElement)
      g.loadingElement = js.undefined
      dom.document.body.className.replace("pg-loading", "")
      dom.document.body.className += " pg-loaded"
    }

//    AppCSS.load()
//    AppRouter.router().render(dom.document.getElementById("container"))


    val mountNode = dom.document.getElementById("container")

//    val router = DemoRoutes.router
//    router() render mountNode

    MaterialSwatch.AllFamiliesView() render mountNode

//    ReactDOM.render(DemoViews.todoListView, mountNode)
  }

}
