# minibuild — task list

[X] 1- mettre en place le projet Gradle (JUnit 5, Hamcrest, Mockito, JaCoCo)
[X] 2- Gav : construire une coordonnée à partir d'une chaîne "group:artifact:version" (groupe, artefact, version)

[X] 3- Gav : lever une exception pour une coordonnée mal formée
    [X] 3a- chaîne vide, trop peu / trop de parties
    [X] 3b- partie vide ou faite d'espaces
    [X] 3c- null

[X] 4- InMemoryStorage : put puis get renvoie l'artefact associé à la coordonnée
[X] 5- InMemoryStorage : get d'une coordonnée absente renvoie un Optional vide
[X] 6- BufferedLineReader : lire ligne à ligne en déléguant à un BufferedReader
[X] 7- LineBasedPomParser : lire la ligne "project" (nom du projet)
[X] 8- LineBasedPomParser : lire les lignes "dependency" (dépendances directes)
[X] 9- LineBasedPomParser : ignorer les lignes vides
[X] 10- LineBasedPomParser : refuser un fichier mal formé
    [X] 10a- fichier vide
    [X] 10b- ligne inconnue, coordonnée invalide, mot-clé sans valeur
    [X] 10c- première ligne qui n'est pas « project »
[X] 11- StorageBasedRegistry : publier un artefact puis le retrouver (publish, lookup)
[X] 12- StorageBasedRegistry : refuser de republier une coordonnée déjà publiée (AlreadyPublishedException)
[X] 13- AllVersionsResolver : résoudre un artefact sans dépendance
[X] 14- AllVersionsResolver : calculer la fermeture transitive des dépendances
[ ] 15- AllVersionsResolver : garder toutes les versions d'un même artefact (pas de résolution de conflit)
[X] 19- AllVersionsResolver : cycle dans les dépendances (tâche découverte en question 19)
[ ] 16- BuildTool : lire le fichier de build puis résoudre ses dépendances
[ ] 17- tests d'intégration : assembler les composants (descendant puis ascendant)
[ ] 18- tests de validation : système complet, vrai fichier de build en entrée
