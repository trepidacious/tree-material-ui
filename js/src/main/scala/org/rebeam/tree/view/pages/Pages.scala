package org.rebeam.tree.view.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.tree.Delta
import org.rebeam.tree.view.{Action, Cursor}

import scala.reflect.ClassTag

case class SetPageAction[P](ctl: RouterCtl[P], p: P) extends Action {
  def callback: Callback = ctl.set(p)
}

/**
  * A current page, and a RouterCtl, e.g. to navigate to new pages
  * @param current  The currently displayed page
  * @param ctl      The RouterCtl
  * @tparam P       The type of page we can route to
  * @tparam L       The type of current page
  */
case class Pages[+L, P](current: L, ctl: RouterCtl[P]) {
  def set(target: P): Action = SetPageAction(ctl, target)
  def modify(f: L => P): Action = set(f(current))
  def withCurrent[M](m: M): Pages[M, P] = copy(current = m)
}

object Pages {

  type CursorPages[U, A, D <: Delta[U, A], L, P] = Cursor[U, A, D, Pages[L, P]]

  implicit class CursorPagesEnriched[U, A, D <: Delta[U, A], L, P](cursor: Cursor[U, A, D, Pages[L, P]]) {

    class PageZoomer[M <: L] {
      def apply()(implicit ct: ClassTag[M]): Option[Cursor[U, A, D, Pages[M, P]]]
      = zoomPage(ct.unapply)
    }

    def zoomPageCT[M <: L] = new PageZoomer[M]

    def zoomPagePF[M <: L](toNewPage: PartialFunction[L, M]): Option[Cursor[U, A, D, Pages[M, P]]] =
      zoomPage(toNewPage.lift)

    def zoomPageEqual[M <: L](m: M): Option[Cursor[U, A, D, Pages[M, P]]] =
      zoomPage(l => if (l == m) Some(m) else None)

    def zoomPage[M <: L](toNewPage: L => Option[M]): Option[Cursor[U, A, D, Pages[M, P]]] = {
      val l = cursor.location.current
      toNewPage(l).map(m => cursor.move(cursor.location.withCurrent(m)))
    }

    def zoomModelAndPage[B, M, E <: Delta[U, B]](toNewCursor: L => Option[(Cursor[U, B, E, Pages[L, P]], M)]): Option[Cursor[U, B, E, Pages[M, P]]] = {
      val l = cursor.location.current
      toNewCursor(l).map{
        case (newCursor, e) => newCursor.move(cursor.location.withCurrent(e))
      }
    }

    class ModelAndPageZoomer[B, M <: L, E <: Delta[U, B]] {
      def apply(zoomWithM: M => Option[Cursor[U, B, E, Pages[L, P]]])(implicit ct: ClassTag[M]): Option[Cursor[U, B, E, Pages[M, P]]]
      = zoomModelAndPage[B, M, E](l => for (m <- ct.unapply(l); newCursor <- zoomWithM(m)) yield (newCursor, m))
    }

    /**
      * Zoom to a new model and page
      * @tparam B The new model type
      * @tparam M The new page type
      * @tparam E The delta type for the new model
      * @return ModelAndPageZoomer - call apply with a function to use the new page to zoom to the new model
      */
    def zoomModelAndPageCT[B, M <: L, E <: Delta[U, B]] = new ModelAndPageZoomer[B, M, E]
  }
}

