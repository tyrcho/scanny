package org.scanny.intermarche

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Try
import scala.util.control.NonFatal
import org.jsoup.nodes.Element
import org.scanny.Product
import grizzled.slf4j.Logging
import info.daviot.scraper.DataParser
import info.daviot.scraper.HttpClientUrlReader
import info.daviot.scraper.LinksParser
import info.daviot.soup.JsoupParser
import info.daviot.soup.JsoupScraper

object Demo extends App {
  val cacheFolder = "d:/temp"

  val hstdScraper = new IntermarcheScraper(
    Seq("https://drive.intermarche.com/Home?p=360"),
    cacheFolder)
  val data = Await.result(hstdScraper.collectedData, 1000.seconds)

  for {
    (id, products) <- data
  } {
    println(id)
    products.foreach(println)
  }
}

class IntermarcheScraper(
  val initial: Iterable[String],
  cacheFolder: String)
    extends JsoupScraper(
      IntermarcheDataParser,
      new IntermarcheLinksParser,
      cacheFolder,
      new HttpClientUrlReader)

object IntermarcheDataParser extends DataParser[String, List[Product]] with JsoupParser with Logging {
  import org.scanny.Product

  def extract(id: String, content: String): Future[Option[List[Product]]] =
    parseDocument(content) map { doc =>
      val products = for {
        div <- doc.select("div.content_vignettes").toList
        li <- div.select("li.vignette_produit_info")
        p <- parseProduct(li).toOption
      } yield p
      products match {
        case Nil => None
        case _   => Some(products)
      }
    }

  def parseProduct(li: Element) = (Try {
    val infos = li.select(".vignette_info").head
    val marque = infos.select(".js-marque").head.text
    val name = infos.select("p").head.text
    val qty = infos.select(".vignette_produit_quantite").headOption.map(_.text).getOrElse("")
    val priceDiv = li.select(".vignette_prix").head
    val price = priceDiv.select("p").get(0).text
    val priceKilo = priceDiv.select("p").get(1).text
    val imgUrl = li.select(".vignette_img").head.select("img").head.attr("src")
    Product(marque, name, qty, price, priceKilo, imgUrl)
  }).recoverWith {
    case NonFatal(e) =>
      warn(s"$e \nCould not parse \n $li")
      Failure(e)
  }
}

class IntermarcheLinksParser extends LinksParser[String] with JsoupParser {
  def extract(id: String, content: String): Future[List[String]] =
    parseDocument(content) map { doc =>
      for {
        nav <- doc.select(".nav_sous-menu").toList take 20
        a <- nav.select("a")
        href = a.attr("href")
        if href.startsWith("/Rayon")
      } yield s"https://drive.intermarche.com$href"
    }
}
 