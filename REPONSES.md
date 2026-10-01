# TD2 — minibuild : réponses rédigées

## Question 2

> À quoi servent respectivement JUnit, Hamcrest, Mockito et JaCoCo ? Lequel n'est pas une bibliothèque de test à proprement parler ?

D'après le cours *Outils de test* :

- **JUnit** : sert à **écrire et exécuter les tests**. C'est le framework de référence pour les tests en Java : une classe de tests, des méthodes annotées `@Test`, et chaque test suit le schéma *préparer, exécuter le composant testé, vérifier* (assertions `assertEquals`, `assertThrows`…). Il gère aussi la *fixture*, c'est-à-dire le contexte dans lequel s'exécute un test (`@BeforeEach`).
- **Hamcrest** : sert à **écrire des assertions lisibles**. C'est une bibliothèque de *matchers* utilisés avec `assertThat(valeur, matcher)` : l'assertion décrit une propriété attendue (collections, chaînes…) plutôt qu'une égalité stricte, avec des messages d'échec explicites.
- **Mockito** : sert à **gérer les doublures de test**. Une doublure est un objet de substitution utilisé à la place d'un objet réel : elle remplace un collaborateur pour isoler le composant testé. `mock(...)` crée la doublure ; avec `when(...).thenReturn(...)` elle joue le rôle de *Stub*, avec `verify(...)` après l'exécution celui de *Spy*.
- **JaCoCo** : sert à **mesurer la couverture du code par les tests**, c'est-à-dire ce que les tests exécutent : instructions (C0), branches (C1), lignes, méthodes, classes.

**JaCoCo n'est pas une bibliothèque de test à proprement parler** : il ne sert ni à écrire, ni à exécuter, ni à vérifier un test, il mesure seulement ce que les tests exécutent. C'est d'ailleurs un plugin du build (`jacoco`) et non une dépendance de test. Comme le rappelle le cours, « 100 % de couverture ne garantit pas l'absence de défauts : un code exécuté n'est pas pour autant correctement vérifié ».


## Question 3

> Construire la task list initiale du projet minibuild à partir de la description du système ci-dessus.

La task list est tenue dans le fichier [TASKS.md](TASKS.md) et mise à jour à chaque étape : on coche la tâche réalisée et on ajoute celles découvertes en chemin.

Elle suit les couches du système, du bas vers le haut : d'abord la coordonnée `Gav` et les composants qui n'appellent personne (`InMemoryStorage`, `BufferedLineReader`), puis ceux qui ont des collaborateurs (`LineBasedPomParser`, `StorageBasedRegistry`, `AllVersionsResolver`, `BuildTool`), et enfin les tests d'intégration et de validation. La première tâche choisie est `Gav`, la brique la plus élémentaire, pour boucler rapidement un premier cycle red-green-refactor.



## Question 5

> Appliquer la technique Fake it : écrire le minimum nécessaire pour atteindre la green bar, c'est-à-dire une méthode parse qui renvoie une valeur constante. Comparer le test et le code : quelle information apparaît dans les deux ? Pourquoi est-ce le signe qu'il reste du travail ?

`Gav.parse` renvoie une constante : `new Gav("org.acme")`, sans regarder la chaîne reçue. Le test passe (green bar).

La valeur `"org.acme"` apparaît à la fois dans le test (c'est l'attendu de `assertEquals`) et dans le code (c'est la constante renvoyée par `parse`). C'est une **duplication** entre le test et le code.

C'est le signe qu'il reste du travail, car le code ne calcule rien : il recopie la réponse attendue par le test. Il ne fonctionne que pour cette donnée précise ; pour toute autre coordonnée, par exemple `org.other:lib-c:3.0.0`, il renverrait encore `org.acme`. Le test passe donc sans que le comportement soit réalisé : il est insuffisamment spécifié. Il faut éliminer cette duplication en faisant calculer le groupe à partir de la chaîne, ce que la triangulation (question 6) va forcer avec un second test.

