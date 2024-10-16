# Système de Planification de Tâches Distribuées

## Aperçu

Ce projet implémente un système de planification de tâches distribuées en utilisant **les sockets Java** et **RMI (Remote Method Invocation)**. Il permet de distribuer des tâches complexes comme **le calcul matriciel** et **le traitement d'images** entre plusieurs Slaves, avec une exécution parallèle pour optimiser les performances.

## Structure du Projet

1. **Client** : Envoie des tâches (calcul matriciel ou traitement d'image) au serveur et reçoit les résultats une fois terminées.
2. **Serveur** : Coordonne la répartition des tâches entre les workers et les slaves, et gère les communications avec les clients.
3. **Worker** : Intermédiaire entre le serveur et les slaves, chaque worker est chargé de la gestion des tâches d'un client et de leur distribution aux slaves.
4. **Slave** : Traite les sous-tâches assignées par le worker et renvoie les résultats.

## Exemple 
### Traitement d'image 
![image](https://github.com/user-attachments/assets/5fbee044-d544-41a9-ba61-32834b3f1a7e)
###  Calcule matriciel 
![image](https://github.com/user-attachments/assets/8b77d7c0-f4f7-4d49-a578-291fc82dd04f)


## Technologies

- **Java** : Langage principal utilisé pour construire le système.
- **Sockets** : Utilisés pour la communication client-serveur.
- **RMI** : Utilisé pour l'invocation de méthodes à distance entre le serveur et les workers.

## Auteurs

Ce projet a été réalisé par **Hajar EL KOUNI** et **Asma EL BALFYQY** dans le cadre du Master Spécialisé MQL & M2I.








