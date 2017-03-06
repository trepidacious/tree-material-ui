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
  def modify(f: C => P): Callback = set(f(current))
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

    def zoomPageEqual[D <: C](d: D): Option[CursorP[M, Pages[D, P]]] =
      zoomPage(c => if (c == d) Some(d) else None)

    def zoomPage[D <: C](cToD: C => Option[D]): Option[CursorP[M, Pages[D, P]]] = {
      val c = cursor.p.current
      cToD(c).map(d => cursor.withP(cursor.p.withCurrent(d)))
    }

    def zoomModelAndPage[N, D](cToD: C => Option[(Cursor[N], D)]): Option[CursorP[N, Pages[D, P]]] = {
      val c = cursor.p.current
      cToD(c).map{case (cn, d) => cn.withP(cursor.p.withCurrent(d))}
    }

    class ModelAndPageZoomer[N, D <: C] {
      def apply(zoomWithD: D => Option[Cursor[N]])(implicit ct: ClassTag[D]): Option[CursorP[N, Pages[D, P]]]
      = zoomModelAndPage[N, D](c => for (d <- ct.unapply(c); cn <- zoomWithD(d)) yield (cn, d))
    }

    def zoomModelAndPageCT[N, D <: C] = new ModelAndPageZoomer[N, D]

  }
}

