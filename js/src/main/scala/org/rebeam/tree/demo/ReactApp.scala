package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react.ReactComponentB
import org.scalajs.dom
import org.rebeam.tree.view.MaterialSwatch

import scala.scalajs.js.JSApp
import scala.scalajs.js.timers._

object ReactApp extends JSApp {

  def main(): Unit = {

    //Update body class to app-loaded, this will
    //fade out the loading screen
    dom.document.body.className = "app-loaded"

    //in 1.0s when it has faded out, remove loading node completely
    setTimeout(1000.0){
      val loadingNode = dom.document.getElementById("loading")
      dom.document.body.removeChild(loadingNode)
    }

//    AppCSS.load()
//    AppRouter.router().render(dom.document.getElementById("container"))

    val mountNode = dom.document.getElementById("container")

//    val router = DemoRoutes.router
//    router() render mountNode

//    MaterialSwatch.AllFamiliesView() render mountNode

    val themedView = ReactComponentB[Unit]("themedView").render(p =>
      MuiMuiThemeProvider()(
//        DemoViews.addressView
        DemoViews.todoListView
      )
    ).build

    themedView() render mountNode
  }

}
