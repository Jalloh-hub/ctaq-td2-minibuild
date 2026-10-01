# TD2 — minibuild : réponses rédigées

## Question 2

> À quoi servent respectivement JUnit, Hamcrest, Mockito et JaCoCo ? Lequel n'est pas une bibliothèque de test à proprement parler ?

D'après le cours *Outils de test* :

- **JUnit** : sert à **écrire et exécuter les tests**. C'est le framework de référence pour les tests en Java : une classe de tests, des méthodes annotées `@Test`, et chaque test suit le schéma *préparer, exécuter le composant testé, vérifier* (assertions `assertEquals`, `assertThrows`…). Il gère aussi la *fixture*, c'est-à-dire le contexte dans lequel s'exécute un test (`@BeforeEach`).
- **Hamcrest** : sert à **écrire des assertions lisibles**. C'est une bibliothèque de *matchers* utilisés avec `assertThat(valeur, matcher)` : l'assertion décrit une propriété attendue (collections, chaînes…) plutôt qu'une égalité stricte, avec des messages d'échec explicites.
- **Mockito** : sert à **gérer les doublures de test**. Une doublure est un objet de substitution utilisé à la place d'un objet réel : elle remplace un collaborateur pour isoler le composant testé. `mock(...)` crée la doublure ; avec `when(...).thenReturn(...)` elle joue le rôle de *Stub*, avec `verify(...)` après l'exécution celui de *Spy*.
- **JaCoCo** : sert à **mesurer la couverture du code par les tests**, c'est-à-dire ce que les tests exécutent : instructions (C0), branches (C1), lignes, méthodes, classes.

**JaCoCo n'est pas une bibliothèque de test à proprement parler** : il ne sert ni à écrire, ni à exécuter, ni à vérifier un test, il mesure seulement ce que les tests exécutent. C'est d'ailleurs un plugin du build (`jacoco`) et non une dépendance de test. Comme le rappelle le cours, « 100 % de couverture ne garantit pas l'absence de défauts : un code exécuté n'est pas pour autant correctement vérifié ».
