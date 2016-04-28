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
    
    for (el <- elements){
      val url = el.attr("href")
      if(url.contains("tous-les-rayons")) {
      //  println (el)
      }
        
    }
      
    
    (for {
      el <- elements
      url = el.attr("href")
      childNode = el.childNode(0)
      if( url.contains("tous-les-rayons") && !el.children.isEmpty() && !childNode.toString.contains("<img")) 
    }yield url -> childNode.childNode(0).toString ).toMap
  }
  
  def listProductsInAPage(element: Element) : List[Product] = {
    List.empty
  }
  
}