# minibuild — task list

[X] 1- mettre en place le projet Gradle (JUnit 5, Hamcrest, Mockito, JaCoCo)
[ ] 2- Gav : construire une coordonnée à partir d'une chaîne "group:artifact:version" (groupe, artefact, version)
[ ] 3- Gav : lever une exception pour une coordonnée mal formée
[ ] 4- InMemoryStorage : put puis get renvoie l'artefact associé à la coordonnée
[ ] 5- InMemoryStorage : get d'une coordonnée absente renvoie un Optional vide
[ ] 6- BufferedLineReader : lire ligne à ligne en déléguant à un BufferedReader
[ ] 7- LineBasedPomParser : lire la ligne "project" (nom du projet)
[ ] 8- LineBasedPomParser : lire les lignes "dependency" (dépendances directes)
[ ] 9- LineBasedPomParser : ignorer les lignes vides
[ ] 10- LineBasedPomParser : refuser un fichier mal formé
[ ] 11- StorageBasedRegistry : publier un artefact puis le retrouver (publish, lookup)
[ ] 12- StorageBasedRegistry : refuser de republier une coordonnée déjà publiée (AlreadyPublishedException)
[ ] 13- AllVersionsResolver : résoudre un artefact sans dépendance
[ ] 14- AllVersionsResolver : calculer la fermeture transitive des dépendances
[ ] 15- AllVersionsResolver : garder toutes les versions d'un même artefact (pas de résolution de conflit)
[ ] 16- BuildTool : lire le fichier de build puis résoudre ses dépendances
[ ] 17- tests d'intégration : assembler les composants (descendant puis ascendant)
[ ] 18- tests de validation : système complet, vrai fichier de build en entrée
