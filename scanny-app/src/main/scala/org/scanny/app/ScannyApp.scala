package org.scanny.app

import org.scanny.Scanner
import org.scanny.intermarche.IntermarcheAnalyzer
import org.apache.http.HttpHost
import org.scanny.carrefour.CarrefourAnalyzer

object ScannyApp extends App {
  
  Scanner.scan(new IntermarcheAnalyzer)
  Scanner.scan(new CarrefourAnalyzer)
  
}