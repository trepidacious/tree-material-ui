package org.rebeam.tree.view.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import org.rebeam.tree.view.{Cursor, CursorP}

import scala.reflect.ClassTag

/**
  * A current page, and a RouterCtl, e.g. to navigate to new pages
  * @param current  The currently displayed page
  * @param ctl      The RouterCtl
  * @tparam P       The type of page we can route to
  * @tparam C       The type of current page
  */
case class Pages[+C, P](current: C, ctl: RouterCtl[P]) {
  def set(target: P): Callback = ctl.set(target)
  def withCurrent[D](d: D): Pages[D, P] = copy(current = d)
}

object Pages {

  type CursorPages[M, C, P] = CursorP[M, Pages[C, P]]

  implicit class CursorPagesEnriched[M, C, P](cursor: CursorP[M, Pages[C, P]]) {

    class PageZoomer[D <: C] {
      def apply()(implicit ct: ClassTag[D]): Option[CursorP[M, Pages[D, P]]]
      = zoomPage(ct.unapply)
    }

    def zoomPageCT[D <: C] = new PageZoomer[D]

    def zoomPagePF[D <: C](cToD: PartialFunction[C, D]): Option[CursorP[M, Pages[D, P]]] =
      zoomPage(cToD.lift)

    def zoomPage[D <: C](cToD: C => Option[D]): Option[CursorP[M, Pages[D, P]]] = {
      val c = cursor.p.current
      cToD(c).map(d => cursor.withP(cursor.p.withCurrent(d)))
    }

    def zoom[N, D](cToD: C => Option[(D, Cursor[N])]): Option[CursorP[N, Pages[D, P]]] = {
      val c = cursor.p.current
      cToD(c).map{case (d, cn) => cn.withP(cursor.p.withCurrent(d))}
    }

    class Zoomer[N, D <: C] {
      def apply(zoomWithD: D => Option[Cursor[N]])(implicit ct: ClassTag[D]): Option[CursorP[N, Pages[D, P]]]
      = zoom[N, D](c => for (d <- ct.unapply(c); cn <- zoomWithD(d)) yield (d, cn))
    }

    def zoomCT[N, D <: C] = new Zoomer[N, D]

  }
}

