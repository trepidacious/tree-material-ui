package org.rebeam.tree.view.pages

trait PagesToTransition[P] {
  def apply(from: P, to: P): PagesTransition
}
