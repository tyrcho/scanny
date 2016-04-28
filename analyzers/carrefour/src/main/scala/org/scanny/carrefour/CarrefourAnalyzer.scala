package org.scanny.carrefour

import org.scanny.Analyzer
import org.jsoup.nodes.Element
import java.util.ArrayList
import org.scanny.Product
import scala.collection.JavaConversions._
import org.jsoup.Jsoup

class CarrefourAnalyzer extends Analyzer {
  
  def getHomePage = HomePage("http://courses.carrefour.fr/drive/accueil.body.overlayers.storeoverlayer.drivepickingform/33", "POST", Map("t:formdata" -> "H4sIAAAAAAAAAFvzloEVAN3OqfcEAAAA"))
  
  def getHostName : String = "https://courses.carrefour.fr"
  
  def getPromoLink(element: Element) : String = {
    ""
  }
  
  def getRayonList(element: Element) : Map[String, String] = {
    val elements = element.select("a.page").asInstanceOf[ArrayList[Element]].toList
    
    (for {
      el <- elements
      url = el.attr("href")
      childNode = el.childNode(0)
      if( url.contains("tous-les-rayons") && !el.children.isEmpty() && !childNode.toString.contains("<img")) 
    }yield url -> childNode.childNode(0).toString ).toMap
  }
  
  def listProductsInAPage(element: Element) : List[Product] = {
    
    val elements = element.select("div.productWrap").asInstanceOf[ArrayList[Element]].toList
    for {
      el <- elements
      brand = if(!el.select("div.brand").first.childNodes.isEmpty && 
          !el.select("div.brand").first.childNode(0).childNodes.isEmpty ) 
              el.select("div.brand").first.childNode(0).childNode(0).attr("src") else "no brand"
      nameElement = el.select("h3.heading").first.childNode(0).childNode(0)
      name = if(!nameElement.childNodes.isEmpty) nameElement.childNode(0).toString else nameElement.toString
      img = el.select(".graphic").first.childNode(0).attr("href")
      quantite = if (el.select(".unit").first != null) el.select(".unit").first.text else "no quantite"
      priceElement = el.select(".price").first
      
      price = if(priceElement!=null) {
        if (priceElement.select(".detail").first != null) priceElement.select(".detail").first.text else "no price"
      } else "no price"
      
      unitPriceElement = el.select(".unitPrice").first  
      unitPrice = if(unitPriceElement!=null) {
        if (unitPriceElement.select(".detail").first != null) unitPriceElement.select(".detail").first.text else "no price"
      }  else "no price"
      //println(s"brand: $brand, name: $name, img: $img, quantite: $quantite, price: $price")
      
    } yield Product(brand, name, quantite, price, unitPrice, img)
    
  }
  
}