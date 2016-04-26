package org.scanny

  case class Product(marque: String, name: String, quantite: String, prix: String, prixKilo: String, imgLink: String){
    override def toString = s"Product {marque: $marque | name: $name | quantite: $quantite | prix: $prix | prixKilo: $prixKilo | imgLink: $imgLink}"
  }