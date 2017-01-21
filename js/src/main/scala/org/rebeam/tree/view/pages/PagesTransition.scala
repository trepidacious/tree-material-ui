package org.rebeam.tree.view.pages

sealed trait PagesTransition {
  def className: String
}
object PagesTransition {
  case object Left extends PagesTransition {
    def className = "left"
  }
  case object Right extends PagesTransition {
    def className = "right"
  }
  case object Down extends PagesTransition {
    def className = "down"
  }
  case object Up extends PagesTransition {
    def className = "up"
  }
}