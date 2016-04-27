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
import org.apache.http.client.methods.HttpPost
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.RedirectStrategy
import org.apache.http.HttpRequest
import org.apache.http.protocol.HttpContext
import org.apache.http.ProtocolException
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.DefaultRedirectStrategy

object Scanner {

  val globalConfig = RequestConfig.custom.setCookieSpec(CookieSpecs.BEST_MATCH).build
  val cookieStore = new BasicCookieStore();

  def scan(analyzer: Analyzer, proxy: Option[HttpHost] = None) = {

    val httpclient = {
      if (proxy.isDefined) HttpClients.custom.setRedirectStrategy(new AllowPOSTRedirection()).setProxy(proxy.get).setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build
      else HttpClients.custom.setRedirectStrategy(new AllowPOSTRedirection()).setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build
    }
    try {

      val httpRequest = getHttpRequest(analyzer)

      val homePage = processHttpRequest(httpclient, httpRequest)

      println(homePage)

      val html = Jsoup.parse(homePage)

      val promoLink = analyzer.getPromoLink(html)

      val rayonLinks = analyzer.getRayonList(html)

      println(s"PromoLink https://drive.intermarche.com$promoLink")
      println(rayonLinks.mkString("\n"))
      println("----------------------------")

      displayProductForAPage(promoLink, "promo")

      if(!rayonLinks.isEmpty){
        displayProductForAPage(rayonLinks.head._1, rayonLinks.head._2)
      }
      

      /*
    	for (rayon <- rayonLinks) {
      	displayProductForAPage(rayon._1, rayon._2)
    	}*/

    } finally {
      httpclient.close
    }

    def displayProductForAPage(rayonUrl: String, rayonName: String) = {
      val rayonHTML = processHttpRequest(httpclient, new HttpGet(analyzer.getHostName + rayonUrl))
      val products = analyzer.listProductsInAPage(Jsoup.parse(rayonHTML))
      val nbProducts = products.size
      println(products.mkString(s"List products for rayon $rayonName (nb product : $nbProducts)", "\n", "---------------"))
    }

  }

  
  private def getHttpRequest(analyzer: Analyzer) = {
      
      val homepage = analyzer.getHomePage
      val httpRequest = if(homepage.httpMethod == "GET") new HttpGet(homepage.url) else new HttpPost(homepage.url)

      httpRequest match {
       case p: HttpPost => {
         val nvps = for {
           d <- homepage.data.toList
         } yield new BasicNameValuePair(d._1, d._2)
         p.setEntity(new UrlEncodedFormEntity(nvps))
       }
       case _ => 
      }

      httpRequest
  }
  
  private def processHttpRequest(httpclient: CloseableHttpClient, httpRequest: HttpUriRequest): String = {

    // Create a custom response handler
    val responseHandler = new ResponseHandler[String] {

      @Override
      def handleResponse(response: HttpResponse): String = {
        println(httpRequest)
        println(httpRequest.getAllHeaders.mkString("\n"))
        println("----------------------------------------")
        println(response.getStatusLine)
        println(response.getAllHeaders.mkString("\n"))

        println("Cookies:\n" + cookieStore.getCookies.toArray().mkString("\n"))

        val status = response.getStatusLine.getStatusCode
        if (status >= 200 && status <= 302) {
          val entity = response.getEntity
          if (entity != null) EntityUtils.toString(entity) else null
        } else {
          throw new ClientProtocolException("Unexpected response status: " + status)
        }
      }

    };

    val context = HttpClientContext.create();
    context.setCookieStore(cookieStore);
    httpclient.execute(httpRequest, responseHandler, context)
  }
}


class AllowPOSTRedirection extends DefaultRedirectStrategy {
  
  override def isRedirectable(method : String) = method.equals("GET") || method.equals("POST")
  
  
}