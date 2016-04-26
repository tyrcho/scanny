package org.scanny

import org.apache.http.client.ResponseHandler
import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.ResponseHandler
import org.apache.http.client.ClientProtocolException
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.config.CookieSpecs
import java.io.File
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.protocol.HttpClientContext
import java.io.PrintWriter
import org.jsoup.Jsoup
import java.util.ArrayList
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._
import org.apache.http.HttpHost

object Scanner {

  val globalConfig = RequestConfig.custom.setCookieSpec(CookieSpecs.BEST_MATCH).build
  val cookieStore = new BasicCookieStore();

  def scan(analyzer: Analyzer, proxy: Option[HttpHost] = None) = {

    val httpclient = {
      if (proxy.isDefined) HttpClients.custom.setProxy(proxy.get).setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build
      else HttpClients.custom.setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build
    }
    try {
      val homePage = processHttpRequest(httpclient, analyzer.getHomePage)

      val html = Jsoup.parse(homePage)

      val promoLink = analyzer.getPromoLink(html)

      val rayonLinks = analyzer.getRayonList(html)

      println(s"PromoLink https://drive.intermarche.com$promoLink")
      println(rayonLinks.mkString("\n"))
      println("----------------------------")

      displayProductForAPage(promoLink, "promo")

      displayProductForAPage(rayonLinks.head._1, rayonLinks.head._2)

      /*
    	for (rayon <- rayonLinks) {
      	displayProductForAPage(rayon._1, rayon._2)
    	}*/

    } finally {
      httpclient.close
    }

    def displayProductForAPage(rayonUrl: String, rayonName: String) = {
      val rayonHTML = processHttpRequest(httpclient, analyzer.getHostName + rayonUrl)
      val products = analyzer.listProductsInAPage(Jsoup.parse(rayonHTML))
      val nbProducts = products.size
      println(products.mkString(s"List products for rayon $rayonName (nb product : $nbProducts)", "\n", "---------------"))
    }

  }


  private def processHttpRequest(httpclient: CloseableHttpClient, url: String): String = {

    val httpget = new HttpGet(url)

    // Create a custom response handler
    val responseHandler = new ResponseHandler[String] {

      @Override
      def handleResponse(response: HttpResponse): String = {
        println(httpget)
        println(httpget.getAllHeaders.mkString("\n"))
        println("----------------------------------------")
        println(response.getStatusLine)
        println(response.getAllHeaders.mkString("\n"))

        println("Cookies:\n" + cookieStore.getCookies.toArray().mkString("\n"))

        val status = response.getStatusLine.getStatusCode
        if (status >= 200 && status < 300) {
          val entity = response.getEntity
          if (entity != null) EntityUtils.toString(entity) else null
        } else {
          throw new ClientProtocolException("Unexpected response status: " + status)
        }
      }

    };

    val context = HttpClientContext.create();
    context.setCookieStore(cookieStore);
    httpclient.execute(httpget, responseHandler, context)
  }
}

