package org.scanny

import org.jsoup.nodes.Element

trait Analyzer {

  def getHostName: String

  def getHomePage: HomePage

  def getPromoLink(element: Element): String

  def getRayonList(element: Element): Map[String, String]

  def listProductsInAPage(element: Element): List[Product]
  
  
  case class HomePage (url: String, httpMethod : String, data: Map[String, String])

}