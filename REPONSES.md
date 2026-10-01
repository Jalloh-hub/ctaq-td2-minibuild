# TD2 — minibuild : réponses rédigées

## Question 2

> À quoi servent respectivement JUnit, Hamcrest, Mockito et JaCoCo ? Lequel n'est pas une bibliothèque de test à proprement parler ?

| Outil | Rôle |
|---|---|
| **JUnit 5** | Framework xUnit : on y **écrit** les tests (`@Test`, fixture `@BeforeEach`…), on les **exécute** (moteur JUnit Platform, lancé par `./gradlew test`) et on obtient le verdict (assertions `assertEquals`, `assertThrows`…). |
| **Hamcrest** | Bibliothèque de **matchers** composables (`is`, `hasSize`, `containsInAnyOrder`, `not`…) utilisés avec `assertThat(valeur, matcher)` : des assertions plus lisibles et des messages d'échec plus explicites. Elle n'exécute pas de tests, elle sert à les vérifier. |
| **Mockito** | Bibliothèque de **doublures de test** : `mock(...)` crée un objet qui remplace un collaborateur du composant testé, pour l'isoler. Avec `when(...).thenReturn(...)` la doublure joue le rôle de *stub* ; avec `verify(...)` après l'exécution, celui de *spy* (vérification d'interaction). |
| **JaCoCo** | Outil de **mesure de la couverture** du code Java par les tests (instructions, branches, lignes, méthodes…) : il instrumente le bytecode pendant l'exécution des tests, puis produit un rapport (`./gradlew jacocoTestReport`). |

**JaCoCo n'est pas une bibliothèque de test à proprement parler.** On ne l'utilise pas dans le code des tests : il ne sert ni à écrire, ni à exécuter, ni à vérifier un test. C'est un outil de mesure, branché sur le build via un plugin Gradle et non comme une dépendance `testImplementation`, qui observe ce que les tests exécutent. Il évalue la qualité de la suite de tests, pas celle du code testé : 100 % de couverture ne garantit pas l'absence de défauts.
