package org.scanny.intermarche

import org.scanny.Analyzer
import org.jsoup.nodes.Element
import java.util.ArrayList
import scala.collection.JavaConversions._
import org.jsoup.Jsoup
import org.scanny.Product

class IntermarcheAnalyzer extends Analyzer {
  
  def getHostName = "https://drive.intermarche.com"
  
  def getHomePage : String = "https://drive.intermarche.com/Home?p=360"
  
  def getPromoLink(element: Element) : String = element.select(".js-voir_promotion").first.attr("href")
    
  
  def getRayonList(element: Element) : Map[String, String] = {
    val rayons = element.select(".nav_sous-menu").asInstanceOf[ArrayList[Element]].toList
    (for {
      rayon <- rayons
      link <- rayon.select("a").asInstanceOf[ArrayList[Element]].toList
      if (link.attr("href").startsWith("/Rayon"))
    } yield link.attr("href") -> link.text).toMap
  }
  
  def listProductsInAPage(element: Element) : List[Product] = {
    val div = element.select("div.content_vignettes")
    val ul = div.select("li.vignette_produit_info").asInstanceOf[ArrayList[Element]].toList
    for {
      li <- ul
      element = Jsoup.parse(li.html)
    } yield getProductFromElement(element)

  }
  
  
  private def getProductFromElement(element: Element) = {
    val vignetteInfos = element.select(".vignette_info").first.children()
    val vignette_prix = element.select(".vignette_prix").first
    val vignette_img = element.select(".vignette_img").first.children()

    val marque = vignetteInfos.get(0).childNode(0)
    val name = vignetteInfos.get(1).childNode(0)
    val quantite = if (!vignetteInfos.get(2).childNode(1).childNodes().isEmpty) vignetteInfos.get(2).childNode(1).childNode(0); else ""
    val prix = vignette_prix.child(1).childNode(0)
    val prixKilo = vignette_prix.child(2).childNode(0)
    val imgLink = vignette_img.get(0).attr("src")

    Product(marque.toString, name.toString, quantite.toString, prix.toString, prixKilo.toString, imgLink)

  }
  
  
}