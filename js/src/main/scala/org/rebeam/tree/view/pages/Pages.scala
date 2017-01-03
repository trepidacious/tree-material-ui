package org.rebeam.tree.view.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl

/**
  * A current page, and a RouterCtl, e.g. to navigate to new pages
  * @param current  The currently displayed page
  * @param ctl      The RouterCtl
  * @tparam P       The type of page
  */
case class Pages[P](current: P, ctl: RouterCtl[P]) {
  def set(target: P): Callback = ctl.set(target)
}

