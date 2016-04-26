package org.scanny.app

import org.scanny.Scanner
import org.scanny.intermarche.IntermarcheAnalyzer
import org.apache.http.HttpHost

object ScannyApp extends App {
  
  Scanner.scan(new IntermarcheAnalyzer, Some(new HttpHost("proxy", 3128)))
  
}