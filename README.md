# MozartGame

Projet dans le cadre de l'UE Paradigmes de Programmation Concurrente à Sorbonne Université.

Ce projet est une implémentation du Jeu de Mozart (MozartGame) dans un système distribué.

## Exécution
Nécessite l'installation de [sbt](https://www.scala-sbt.org/download.html).

Dans votre terminal :
```sh
sbt
run 0 # Pour lancer le chef d'orchestre
```
Dans 3 autres terminaux :
```sh
sbt
run i # avec i ϵ [1,3]
```
