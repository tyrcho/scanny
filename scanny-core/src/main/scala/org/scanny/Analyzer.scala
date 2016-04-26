package org.scanny

import org.jsoup.nodes.Element

trait Analyzer {

  def getHostName: String

  def getHomePage: String

  def getPromoLink(element: Element): String

  def getRayonList(element: Element): Map[String, String]

  def listProductsInAPage(element: Element): List[Product]

}