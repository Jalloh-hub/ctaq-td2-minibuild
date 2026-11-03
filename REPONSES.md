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


## Question 7

> Au sens du TD1, ces deux coordonnées appartiennent-elles à la même classe d'équivalence ? Pourquoi la triangulation en demande-t-elle quand même deux ?

**Oui, elles appartiennent à la même classe d'équivalence.** `org.acme:lib-a:1.0.0` et `org.other:lib-c:3.0.0` sont deux coordonnées bien formées : trois parties non vides séparées par `:`. Ce sont deux représentants de la même classe valide de l'entrée de `Gav.parse`. D'après le cours (C3), les éléments d'une même classe ont le même comportement : « si le résultat est correct avec un élément, il l'est pour tous les éléments de la classe ». Pour le test boîte noire, un seul représentant suffirait donc.

**La triangulation en demande deux parce que cette hypothèse n'est pas encore vraie pour notre code.** Dire qu'un représentant vaut pour toute la classe suppose que le programme traite tous les éléments de la classe de la même façon. Or après la question 5, `parse` renvoie une constante : il donne le bon résultat pour `org.acme:lib-a:1.0.0` et un résultat faux pour toutes les autres coordonnées de la même classe. Un seul exemple peut toujours être satisfait par une constante ; deux exemples dont les résultats attendus diffèrent ne le peuvent plus, et obligent à écrire le vrai découpage de la chaîne.

Les deux techniques n'ont pas le même but. Les classes d'équivalence servent à **choisir peu de données de test** pour chercher des défauts dans un code supposé écrit de façon générale. La triangulation sert à **faire émerger ce code général** : le second test n'apporte pas une nouvelle classe, il rend la tricherie impossible.


## Question 10

> La signature fournie de `get` renvoie un `Optional` : pourquoi est-ce préférable à `null` ou à une exception pour une coordonnée absente ?

**Une coordonnée absente du stockage est un cas normal, pas une erreur.** Le résolveur interrogera souvent le stockage pour des coordonnées qui n'y sont pas encore. Ce cas fait partie du fonctionnement ordinaire et doit donc apparaître dans le résultat de `get`.

- **Contre `null`** : rien dans la signature `Artifact get(Gav)` ne prévient l'appelant que le résultat peut être absent. Il peut l'oublier, et l'erreur n'apparaît que plus loin, sous la forme d'une `NullPointerException`. Avec `Optional<Artifact>`, l'absence est écrite dans le **type** : l'appelant est obligé de traiter les deux cas (`isEmpty`, `orElse`, `orElseThrow`…) avant d'accéder à l'artefact.
- **Contre une exception** : une exception est réservée aux situations anormales. L'utiliser pour un cas attendu obligerait l'appelant à écrire un `try/catch` pour un résultat ordinaire.

C'est l'appelant qui décide si l'absence est une erreur dans son contexte. Par exemple, le résolveur lèvera plus tard une exception pour une dépendance introuvable (question 34). Le stockage, lui, se contente de répondre « présent » ou « absent ».


## Question 11

> Le TDD vous a-t-il aidé sur ce composant ? Pourquoi ?

**Très peu.** `BufferedLineReader` ne contient aucune logique à nous : sa méthode `readLine` se contente d'appeler celle d'un `BufferedReader` (délégation d'une ligne). Le TDD aide quand le code se construit pas à pas : un test qui échoue, puis le code qui le fait passer, puis un autre test qui force à généraliser (Fake it, triangulation, comme pour `Gav`). Ici, il n'y a rien à faire émerger : la première version écrite est déjà la version finale, et aucun second test ne ferait évoluer le code.

Le test garde quand même une utilité : il vérifie que le **branchement** sur `BufferedReader` est correct (lignes lues dans l'ordre, `null` en fin de texte, comme le contrat de `ILineReader` l'annonce). Il le fait sans fichier, grâce à un `StringReader`, qui est un vrai `Reader` lisant une chaîne en mémoire : le test reste rapide et indépendant du disque.

## Question 12

> Réécrire les assertions de `GavTest` avec Hamcrest. Faire échouer volontairement une assertion dans chacune des deux versions (`assertEquals` et `assertThat`) et comparer les messages d'échec.

**Réécriture.** Dans `parseDecoupeLesTroisChamps`, chaque `assertEquals(attendu, obtenu)` devient `assertThat(obtenu, is(attendu))`, et une assertion utilisant `startsWith` vérifie la forme textuelle de la coordonnée (`toString()` ajouté à `Gav`, qui reconstruit `group:artifact:version`). Avec Hamcrest, l'assertion se lit comme une phrase (« le groupe est … ») et la valeur obtenue vient en premier ; avec JUnit, l'ordre `attendu, obtenu` est facile à inverser par erreur.

**Échec volontaire.** On remplace temporairement l'attendu par `"org.acme"` ; le test échoue pour la ligne `org.other:lib-c:3.0.0` du fichier CSV. Messages observés :

| Version | Message d'échec |
|---|---|
| JUnit `assertEquals("org.acme", gav.group())` | `expected: <org.acme> but was: <org.other>` |
| Hamcrest `assertThat(gav.group(), is("org.acme"))` | `Expected: is "org.acme"` / `but: was "org.other"` |

**Comparaison.** Sur une égalité simple, la différence est mince : les deux messages donnent l'attendu et l'obtenu. Le message Hamcrest est construit à partir du matcher lui-même (« is "org.acme" ») : il décrit la propriété attendue et pas seulement une valeur. La différence devient nette avec les matchers composables sur les collections (`containsInAnyOrder`, `hasSize`, `hasItem`), où Hamcrest indique quel élément manque ou est en trop (question 17).

## Question 13

> Dans le cours (C2, diapositive 25), un bouchon « simule la logique de comportement » d'un composant appelé. En quoi la vérification par `verify` va-t-elle au-delà de ce rôle ? Formuler la différence entre test orienté état et test orienté interaction.

**Le bouchon du cours ne fait que fournir des réponses.** D'après le cours (C2, diapositive 25, « Pilotes et bouchons (2/2) ») : « Les souches / bouchons simuleront la logique de comportement. En général elles se contenteront de retourner une valeur fixe conforme aux paramètres passés. » Le bouchon sert donc seulement à alimenter le composant testé : il remplace le composant appelé pour que le test puisse s'exécuter, mais il ne vérifie rien. C'est le rôle de `when(...).thenReturn(...)` avec Mockito (stub, au sens de Meszaros).

**`verify` va au-delà : il observe ce que le composant testé fait à son collaborateur.** Avec `verify(registry).lookup(a)` ou `verify(registry, never()).publish(any())`, la doublure enregistre les appels qu'elle reçoit et le test les contrôle après l'exécution : quelles méthodes ont été appelées, avec quels arguments, combien de fois. Ces appels sont des **sorties indirectes** du composant : elles n'apparaissent pas dans la valeur qu'il renvoie. La doublure ne sert plus seulement à faire tourner le test, elle devient un moyen d'observation (spy, au sens de Meszaros).

**Deux façons de vérifier un composant :**

- **Test orienté état** : on vérifie le **résultat** de l'appel, c'est-à-dire la valeur renvoyée ou l'état final du système, sans regarder comment il a été obtenu. Exemple : après `put(LIB_A, ARTEFACT_A)`, `get(LIB_A)` renvoie `Optional.of(ARTEFACT_A)` (question 10).
- **Test orienté interaction** : on vérifie les **appels faits aux collaborateurs** (lesquels, avec quels arguments, combien de fois). Exemple : en résolvant `{a}`, le résolveur appelle `lookup(a)` exactement une fois et n'appelle jamais `publish`.

Le premier teste *ce que* le composant produit, le second *comment* il collabore avec les autres. Un test orienté interaction est plus lié à l'implémentation : il peut échouer après un refactoring qui ne change pas le résultat. On le réserve donc aux cas où l'appel lui-même fait partie du comportement attendu (par exemple, ne jamais republier, ou ne pas interroger deux fois le registre pour la même coordonnée).
