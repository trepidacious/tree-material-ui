package org.rebeam.tree.demo

import chandu0101.scalajs.react.components.materialui._
import japgolly.scalajs.react.ScalaComponent
import org.scalajs.dom

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.timers._
import japgolly.scalajs.react.vdom.html_<^._

object ReactApp {

  @JSExportTopLevel("org.rebeam.tree.demo.ReactApp.main")
  def main(): Unit = {

    //Update body class to app-loaded, this will
    //fade out the loading screen
    dom.document.body.className = "app-loaded"

    //in 1.0s when it has faded out, remove loading node completely
    setTimeout(1000.0){
      val loadingNode = dom.document.getElementById("loading")
      dom.document.body.removeChild(loadingNode)
    }

    // We would load scala-css styles here if we had any
//    AppCSS.load()

    // Set up a theme based on default light theme, but replacing cyan with
    // blueGrey
    val baseTheme = Mui.Styles.LightRawTheme
    val theme = Mui.Styles.getMuiTheme(
      baseTheme.copy(palette =
        baseTheme.palette
          .copy(primary1Color = Mui.Styles.colors.blueGrey500)
          .copy(primary2Color = Mui.Styles.colors.blueGrey700)
          .copy(accent1Color = Mui.Styles.colors.deepOrangeA200)
//          .copy(primary1Color = Mui.Styles.colors.blue500)
//          .copy(primary2Color = Mui.Styles.colors.blue700)
//          .copy(accent1Color = Mui.Styles.colors.lightGreenA700)
      )
    )

    // Our top-level component, display pages based on URL, with app bar and navigation
    val router = DemoRoutes.router

    // Need to wrap our top-level router component in a theme for Material-UI to work
    val themedView = ScalaComponent.builder[Unit]("themedView").render(p =>
      MuiMuiThemeProvider(muiTheme = theme)(
        router()
      )
    ).build

    // Finally, render the themed top-level view to the predefined HTML div with id "container"
    themedView().renderIntoDOM(dom.document.getElementById("container"))
  }

}
