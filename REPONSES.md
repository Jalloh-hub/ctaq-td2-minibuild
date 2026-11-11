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


## Question 15

> La doublure de `ILineReader` n'ouvre aucun fichier. Quels défauts ce test unitaire est-il par construction incapable de détecter ?

Dans les tests de la question 14, la doublure est un **bouchon** (un *Stub* au sens du cours Outils) : elle renvoie des lignes programmées à l'avance, déjà découpées et propres. Par construction, le test ne voit que ce que la doublure lui donne. Il ne peut donc pas détecter les défauts qui tiennent **au vrai fichier et au vrai lecteur** :

- **l'encodage** : un fichier en ISO-8859-1 lu comme de l'UTF-8 (ou l'inverse) donne des caractères faux dans les noms ;
- **les fins de ligne** : un fichier écrit sous Windows (`\r\n`), ou dont la dernière ligne n'a pas de retour à la ligne ;
- **le fichier absent, illisible ou sans droits** : la `IOException` levée par le vrai lecteur, que la doublure ne lève jamais ;
- **le contrat réel de `readLine`** : le parseur suppose que `readLine` renvoie `null` en fin de fichier. La doublure respecte cette hypothèse parce que nous l'avons programmée ainsi, mais rien ne vérifie que la vraie réalisation (`BufferedLineReader`) la respecte aussi.

Ce dernier point est le plus important : chaque composant est testé avec **sa propre hypothèse** sur l'autre, et personne ne vérifie que les deux hypothèses concordent. C'est justement l'objet des **tests d'intégration**, qui testent les interfaces entre composants réels (cours C2), puis des **tests de validation** avec un vrai fichier sur disque (question 32). Le test unitaire localise bien un défaut du parseur, mais il ne dit rien de son assemblage avec le lecteur.


## Question 16

> Écrire le bouchon de `IStorage` à la main, puis avec Mockito, et comparer. Le test de la règle de non-republication vérifie-t-il un état, une interaction, ou les deux ?

**Comparaison.** À la main, le bouchon est une classe d'une quinzaine de lignes (`StubStorage`) qui implémente `IStorage` : réutilisable, mais figée (une seule réponse programmée, et `put` ne fait rien). Avec Mockito, la doublure est décrite en deux lignes, directement dans le test : `mock(IStorage.class)` puis `when(storage.get(LIB_A)).thenReturn(Optional.of(ARTEFACT_A))`. Dans les deux cas, elle joue le rôle d'un **bouchon (Stub)** au sens du cours Outils : elle fournit des réponses programmées et ne vérifie rien.

Mockito apporte surtout une chose que le bouchon écrit à la main ne permet pas : **vérifier les interactions** avec `verify`, après l'exécution. La doublure joue alors le rôle d'un **Spy** (c'est le test qui vérifie, après coup). Avec la classe écrite à la main, il faudrait écrire, à la main aussi, un Spy qui enregistre ses appels.

**État, interaction, ou les deux ?**

- Écrit avec le bouchon, le test de la règle ne vérifie qu'**un état** : `publish` lève `AlreadyPublishedException`. Il ne verrait pas un code qui rangerait l'artefact dans le stockage **puis** lèverait l'exception.
- En ajoutant `verify(storage, never()).put(any(), any())`, il vérifie **les deux** : l'état (l'exception) et l'interaction (le stockage n'est jamais modifié). C'est le sens réel de la règle : un artefact déjà publié ne doit pas être remplacé.

Description du test : `[IStorage | get(a) ↦ Some(A), put ×0]sp ⊢ publish(A) ⇒ ↯ AlreadyPublishedException`.

Le cas nominal se teste de la même façon : `[IStorage | get(a) ↦ None, put(a, A) ×1]sp ⊢ publish(A)`, vérifié par `verify(storage).put(LIB_A, ARTEFACT_A)`. Ici, seule l'interaction montre que `publish` a fait son travail, puisque `publish` ne renvoie rien.


## Question 17

> Écrire un test orienté état sur `resolve({lib-a})` (lib-a → lib-b → lib-c), avec `assertEquals(Set.of(…), …)` puis avec `containsInAnyOrder`. Faire échouer les deux versions et comparer les messages.

Description du test : `[IRegistry | lookup(a) ↦ Some(A), lookup(b) ↦ Some(B), lookup(c) ↦ Some(C)]s ⊢ resolve({a}) ⇒ {a, b, c}`, où `A = Artifact(a, {b})`, `B = Artifact(b, {c})`, `C = Artifact(c, ∅)`.

Pour faire échouer les deux assertions, on a cassé volontairement le résolveur : il ne descendait que d'un niveau, et oubliait donc `lib-c`. Messages obtenus :

- avec `assertEquals(Set.of(a, b, c), resolved)` (JUnit seul) :

  ```
  expected: <[org.acme:lib-a:1.0.0, org.acme:lib-b:2.1.0, org.other:lib-c:3.0.0]>
   but was: <[org.acme:lib-b:2.1.0, org.acme:lib-a:1.0.0]>
  ```

- avec `assertThat(resolved, containsInAnyOrder(a, b, c))` (Hamcrest) :

  ```
  Expected: iterable with items [<org.acme:lib-a:1.0.0>, <org.acme:lib-b:2.1.0>, <org.other:lib-c:3.0.0>] in any order
       but: no item matches: <org.other:lib-c:3.0.0> in [<org.acme:lib-b:2.1.0>, <org.acme:lib-a:1.0.0>]
  ```

**Comparaison.** `assertEquals` affiche les deux ensembles en entier, dans l'ordre arbitraire d'un `Set` : c'est à nous de les comparer élément par élément pour trouver ce qui manque. Le message de Hamcrest le dit directement : « no item matches: lib-c », c'est-à-dire **quel élément manque**. Sur un `Set`, `assertEquals` ne dépend déjà pas de l'ordre (l'égalité de deux `Set` ignore l'ordre) : l'apport de Hamcrest est ici **la clarté du message d'échec**, comme l'annonce le cours Outils (« des messages d'échec explicites »).


## Question 18

> Écrire un test orienté interaction sur le même scénario : vérifier avec `verify` que `AllVersionsResolver` interroge le registre pour chacune des trois coordonnées, et une seule fois pour chacune. Quelle propriété du résolveur ce second test capture-t-il, que le premier ne capture pas ?

Description du test : `[IRegistry | lookup(a) ↦ Some(A), lookup(b) ↦ Some(B), lookup(c) ↦ Some(C)]sp ⊢ resolve({a}) ⇒ ∀ g ∈ {a, b, c} · lookup(g) ×1`, et aucun autre appel au registre. La doublure est la même qu'à la question 17, mais elle joue cette fois le rôle d'un **Spy** : on vérifie après coup, avec `verify(registry, times(1)).lookup(…)` et `verifyNoMoreInteractions(registry)`, les appels qu'elle a reçus.

**Propriété capturée : le résolveur n'interroge pas plusieurs fois le registre pour une même coordonnée** (pas de travail redondant). Le test d'état de la question 17 ne peut pas la voir : qu'on interroge le registre une ou deux fois pour la même coordonnée, l'ensemble renvoyé est le même, puisqu'un `Set` ignore les doublons. Seule l'observation des appels (les sorties indirectes du résolveur vers le registre) la révèle.

**Remarque : ce test est vert d'emblée.** Sur la chaîne a → b → c, la version naïve interroge déjà chaque coordonnée une seule fois. Le test ne devient discriminant que sur un **losange** (a → b, a → c, b → d, c → d), où la version naïve interroge deux fois d (une fois en passant par b, une fois par c). Un test qui passe dès qu'on l'écrit n'a jamais été vu rouge : il doit faire douter de son pouvoir de détection. On ajoutera le cas du losange à la question 19.


## Question 19

> Le cas d'un cycle figure-t-il dans votre task list ? Ajouter un cycle (lib-c dépend de lib-a), écrire le test et constater l'échec (que se passe-t-il ?), puis corriger `AllVersionsResolver`. Le test d'interaction de la question 18 vous aide-t-il à diagnostiquer plus vite que le test d'état ?

**Le cycle n'était pas dans la task list** : on l'a ajouté (tâche 19, « découverte en question 19 »). Description du test : `[IRegistry | lookup(a) ↦ Some(A), lookup(b) ↦ Some(B), lookup(c) ↦ Some(C')]s ⊢ resolve({a}) ⇒ {a, b, c}`, où `C' = Artifact(c, {a})`.

**Ce qui se passe** : la version récursive naïve ne termine pas. `resolve({a})` appelle `resolve({b})`, qui appelle `resolve({c})`, qui rappelle `resolve({a})`… chaque appel ajoute un étage sur la pile, jusqu'à la `StackOverflowError`. Une version itérative (boucle `while`) sans mémoire des coordonnées déjà vues ne planterait même pas : elle **bouclerait indéfiniment**. D'où l'annotation `@Timeout(2)` sur le test, qui le fait échouer au lieu de bloquer toute la suite (cours Outils : `@Timeout` sert à « faire échouer un test trop long (boucle infinie, attente bloquée) »).

**Correction** : un parcours itératif avec l'**ensemble des coordonnées déjà vues**. On traite une file de coordonnées ; une coordonnée n'est demandée au registre que la première fois qu'on la rencontre (`if (resolues.add(gav))`). Comme le nombre de coordonnées est fini et que chacune n'est traitée qu'une fois, la terminaison est garantie, même en présence de cycles.

**Le test d'interaction aide-t-il à diagnostiquer ?** **Non, pas ici.** Sur le scénario du cycle, le test d'état et le test d'interaction échouent de la même façon : le programme plante (ou bouclerait) **avant** d'atteindre les assertions et les `verify`. Ni l'un ni l'autre ne dit pourquoi : c'est la **pile d'appels** de l'erreur (`AllVersionsResolver.resolve` répété des milliers de fois) qui oriente le diagnostic vers la récursion sans fin.

**En complément, le losange annoncé à la question 18.** On a ajouté le test `resolveNeRedemandePasUneCoordonneeDejaVue` : a → b, a → c, b → d, c → d, avec `verify(registry, times(1)).lookup(d)`. Lancé sur la version naïve, il échoue (« Wanted 1 time … But was 2 times ») : d était demandé une fois en passant par b et une fois en passant par c. Sur la version corrigée, il passe. C'est ce test d'interaction, et non le test d'état (le résultat {a, b, c, d} est le même dans les deux versions), qui distingue les deux résolveurs.

## Question 20

> Un test d'interaction est plus précis mais aussi plus fragile : il se casse lors de refactorings qui ne changent pourtant pas le comportement observable. Donner un exemple concret sur `AllVersionsResolver`. Quelle règle en tirer sur l'usage de `verify` ?

**Exemples concrets.** Le comportement observable du résolveur, c'est l'ensemble de coordonnées qu'il renvoie. Les refactorings suivants ne le changent pas, mais cassent les tests des questions 18 et 19, qui exigent `verify(registry, times(1)).lookup(…)` pour chaque coordonnée et `verifyNoMoreInteractions(registry)` :

- **interroger le registre par lots** : on ajoute à `IRegistry` une méthode `lookupAll(Set<Gav>)`, et le résolveur demande toutes les coordonnées d'un niveau en un seul appel. Le résultat est identique, mais `lookup` n'est plus appelé du tout : les `verify(…, times(1)).lookup(…)` échouent ;
- **ajouter un cache devant le registre** : un résolveur qui garde en mémoire les artefacts déjà demandés d'un appel de `resolve` à l'autre n'interroge plus le registre la deuxième fois. Le résultat ne change pas, mais le nombre d'appels, si ;
- **ajouter un appel inoffensif** : par exemple vérifier d'abord que chaque dépendance directe existe (`lookup` en plus), ou consulter le registre pour journaliser. Le résultat est le même, mais `times(1)` et surtout `verifyNoMoreInteractions` cassent, puisqu'ils interdisent tout appel non prévu.

Dans chaque cas, le test échoue alors que le résolveur fonctionne toujours : c'est un faux positif, qui coûte du temps et finit par faire perdre confiance dans les tests.

**Règle à en tirer : ne vérifier par `verify` que les interactions qui font partie du contrat du composant** ; pour tout le reste, préférer la vérification d'état (le résultat renvoyé). Exemples d'interactions qui sont bien dans le contrat :

- le registre ne doit **jamais** modifier le stockage quand il refuse une republication (question 16, `verify(storage, never()).put(…)`) : c'est la règle métier elle-même ;
- le registre doit ranger un nouvel artefact (`verify(storage).put(…)`), car `publish` ne renvoie rien et n'a pas d'autre effet observable ;
- le résolveur ne doit **jamais** publier (`never()).publish(…)`).

En revanche, « interroger le registre exactement une fois par coordonnée » relève plutôt d'un choix d'implémentation (une exigence de performance). Il est utile de le tester quand on veut justement garantir cette propriété, comme pour le losange de la question 19, mais en sachant que ce test devra évoluer avec l'implémentation. C'est ce que dit le cours Outils : la vérification d'état observe ce que le composant **produit**, la vérification d'interaction **comment** il collabore, et seule la première est indépendante de la façon dont il est écrit.

## Question 22

> Reprendre le graphe d'appel de minibuild. Pour chaque composant, indiquer qui sont ses appelants et ses appelés. Quels composants n'ont aucun appelant ? Aucun appelé ?

Graphe d'appel (une flèche va de l'appelant vers l'appelé) :

```
                         BuildTool
                       /           \
        LineBasedPomParser       AllVersionsResolver
                |                         |
        BufferedLineReader       StorageBasedRegistry
                                          |
                                   InMemoryStorage
```

| Composant | Appelants | Appelés |
|---|---|---|
| `BuildTool` | — | `LineBasedPomParser`, `AllVersionsResolver` |
| `LineBasedPomParser` | `BuildTool` | `BufferedLineReader` |
| `AllVersionsResolver` | `BuildTool` | `StorageBasedRegistry` |
| `StorageBasedRegistry` | `AllVersionsResolver` | `InMemoryStorage` |
| `InMemoryStorage` | `StorageBasedRegistry` | — |
| `BufferedLineReader` | `LineBasedPomParser` | — |

**Aucun appelant : `BuildTool`** (le sommet du graphe, c'est lui qu'on lance). **Aucun appelé : `InMemoryStorage` et `BufferedLineReader`** (les feuilles, qui ne dépendent d'aucun autre composant de minibuild).

Le graphe est un **arbre** : chaque composant, sauf `BuildTool`, a un seul appelant. Il a deux branches : la branche de lecture (`BuildTool` → parseur → lecteur de lignes) et la branche de résolution (`BuildTool` → résolveur → registre → stockage), plus longue d'un niveau.

Conséquence pour l'intégration (cours C2, diapositives 24-25) : un composant sans appelant n'aura jamais besoin d'un **pilote** une fois intégré lui-même ; un composant sans appelé n'aura jamais besoin de **bouchon**. Le **pilote** remplace un appelant non encore intégré, le **bouchon** remplace un appelé non encore intégré.

## Question 23

> Pour chaque composant, calculer sa hauteur (longueur du plus long chemin vers un composant sans appelé) et sa profondeur (longueur du plus long chemin depuis un composant sans appelant). Que remarquez-vous ?

Une longueur de chemin se compte en **nombre d'arcs** (d'appels) : un composant sans appelé a une hauteur 0, un composant sans appelant a une profondeur 0. Les chemins suivent le graphe de la question 22.

| Composant | Hauteur | Profondeur |
|---|---|---|
| `BuildTool` | 3 (→ résolveur → registre → stockage) | 0 |
| `LineBasedPomParser` | 1 (→ lecteur) | 1 (`BuildTool` →) |
| `AllVersionsResolver` | 2 (→ registre → stockage) | 1 (`BuildTool` →) |
| `StorageBasedRegistry` | 1 (→ stockage) | 2 (`BuildTool` → résolveur →) |
| `InMemoryStorage` | 0 | 3 (`BuildTool` → résolveur → registre →) |
| `BufferedLineReader` | 0 | 2 (`BuildTool` → parseur →) |

La hauteur de `BuildTool` est 3 et non 2 : on prend le **plus long** chemin, celui de la branche de résolution.

**Remarque : le graphe n'est pas équilibré.** Hauteur + profondeur vaut 3 sur la branche de résolution (`BuildTool`, `AllVersionsResolver`, `StorageBasedRegistry`, `InMemoryStorage`), mais seulement 2 pour `LineBasedPomParser` et `BufferedLineReader` : la branche de lecture est plus courte d'un niveau. Les niveaux ne regroupent donc **pas les mêmes composants** selon qu'on compte depuis le haut ou depuis le bas :

- **par profondeur** (ordre **descendant**, on part du sommet) : le parseur est au niveau du résolveur (1), le lecteur au niveau du registre (2) ;
- **par hauteur** (ordre **ascendant**, on part des feuilles) : le lecteur est au niveau du stockage (0, à intégrer en premier), le parseur au niveau du registre (1).

Conséquence : l'ordre d'intégration descendant (question 24) se construira niveau par niveau de profondeur, et l'ordre ascendant (partie suivante) niveau par niveau de hauteur, et ces deux découpages ne sont pas le miroir l'un de l'autre.

## Question 24

> Proposer un ordre d'intégration descendant pour minibuild. Pour chaque étape, indiquer les composants ajoutés, les interfaces testées, les bouchons nécessaires et les pilotes nécessaires.

On suit la démarche incrémentale du cours (C2, diapositive 40) : on part du sommet du graphe (`BuildTool`, profondeur 0) et, à chaque étape, on **ajoute** un composant à ceux déjà intégrés (l'intégration est **cumulative**). Chaque étape est caractérisée par l'interface qu'elle permet de tester. Les composants appelés qui ne sont pas encore intégrés sont remplacés par des **bouchons**.

| Étape | Composants ajoutés | Interface testée | Bouchons | Pilotes |
|---|---|---|---|---|
| 1 | `BuildTool`, `LineBasedPomParser` | `BuildTool`–`IPomParser` | lecteur de lignes (`ILineReader`), résolveur (`IResolver`) | — |
| 2 | `AllVersionsResolver` | `BuildTool`–`IResolver` | lecteur de lignes, registre (`IRegistry`) | — |
| 3 | `BufferedLineReader` | `LineBasedPomParser`–`ILineReader` | registre | — |
| 4 | `StorageBasedRegistry` | `AllVersionsResolver`–`IRegistry` | stockage (`IStorage`) | — |
| 5 | `InMemoryStorage` | `StorageBasedRegistry`–`IStorage` | — | — |

- **Aucun pilote** : en descendant, le composant le plus haut intégré est toujours `BuildTool`, qui n'a pas d'appelant (question 22). Le test l'appelle directement.
- **Les bouchons disparaissent un à un** : à chaque étape, le composant ajouté remplace le bouchon qui le simulait, mais il peut en faire apparaître un nouveau pour ses propres appelés (à l'étape 2, le résolveur réel remplace le bouchon du résolveur, mais il faut désormais un bouchon du registre).
- **Les étapes 2 et 3 peuvent être permutées** : elles concernent deux branches indépendantes du graphe.
- On aurait aussi pu intégrer « niveau par niveau » selon la profondeur (question 23) : {`BuildTool`}, puis {parseur, résolveur}, puis {lecteur, registre}, puis {stockage}. Mais la première étape ne teste alors **aucune interface** (`BuildTool` seul avec deux bouchons, c'est le test unitaire de la question 21) ; la démarche du cours commence donc par une **paire** appelant–appelé.

## Question 25

> Implémenter l'étape 1 de votre ordre descendant : les composants ajoutés sont réels, les autres sont simulés. Le test porte sur l'interface entre les composants ajoutés, pas sur leur logique interne, déjà couverte par les tests unitaires de la partie 3. En quoi ce test diffère-t-il du test unitaire de `BuildTool` (question 21) ?

Code : `TopDownIntegrationTest` (tag `td2-q25`). `BuildTool` et `LineBasedPomParser` sont **réels** ; le lecteur de lignes et le résolveur sont des **bouchons** ; il n'y a pas de pilote, le test appelle directement `BuildTool`.

- `[ILineReader | readLine ↦ ⟨"project mon-app", "dependency a", "dependency b", ⊥⟩]s, [IResolver | resolve({a, b}) ↦ {a, b}, resolve({a, b}) ×1]sp ⊢ build(reader) ⇒ {a, b}`
- `[ILineReader | readLine ↦ ⟨⊥⟩]s, [IResolver | resolve ×0]sp ⊢ build(reader) ⇒ ↯ InvalidBuildFileException`

**Différence avec le test unitaire de la question 21 : le parseur est réel.** À la question 21, le parseur était un bouchon qui renvoyait **toujours** un `Project` bien formé : le test vérifiait seulement que `BuildTool` enchaîne deux appels, avec une hypothèse sur ce que renvoie `parse` que personne ne contrôlait. Ici, on vérifie l'**interface** `BuildTool`–`IPomParser` telle qu'elle est réellement réalisée :

- `BuildTool` comprend le `Project` **produit par le vrai parseur** : les dépendances lues dans le fichier arrivent bien au résolveur ;
- quand le parseur rencontre un fichier vide, son choix (lever `InvalidBuildFileException`, question 14.1) **remonte** à travers `BuildTool` sans que le résolveur soit sollicité. C'est exactement le défaut d'interface décrit en introduction de la partie 4 : si le parseur avait renvoyé `null`, les deux tests unitaires seraient verts, et c'est ce test d'intégration qui échouerait (`NullPointerException`).

Le test ne revérifie pas la logique interne du parseur (lignes vides, coordonnées mal formées…), déjà couverte par `LineBasedPomParserTest`.

## Question 26

> Poursuivre l'intégration descendante jusqu'à ce que tous les composants soient réels. À chaque étape, conserver les tests des étapes précédentes : ils doivent continuer à passer.

Code : `TopDownIntegrationTest`, un test par étape (tags `td2-q26-etape2` à `td2-q26-etape5`) ; les tests des étapes précédentes sont conservés et passent toujours.

| Étape | Composant réel ajouté | Interface testée | Bouchons restants | Scénario |
|---|---|---|---|---|
| 2 | `AllVersionsResolver` | `BuildTool`–`IResolver` | lecteur, registre | `[ILineReader \| readLine ↦ ⟨"project mon-app", "dependency a", ⊥⟩]s, [IRegistry \| lookup(a) ↦ Some(A), lookup(c) ↦ Some(C)]s ⊢ build(reader) ⇒ {a, c}` |
| 3 | `BufferedLineReader` | `LineBasedPomParser`–`ILineReader` | registre | même registre ; vrai texte `"project mon-app\n\ndependency a\n"` (avec une ligne vide) lu par un `StringReader` |
| 4 | `StorageBasedRegistry` | `AllVersionsResolver`–`IRegistry` | stockage | `[IStorage \| get(a) ↦ Some(A), get(c) ↦ Some(C)]s ⊢ build(…) ⇒ {a, c}` |
| 5 | `InMemoryStorage` | `StorageBasedRegistry`–`IStorage` | aucun | A et C publiés par `publish` dans le vrai registre, puis `build(…) ⇒ {a, c}` |

avec `A = Artifact(a, {c})` et `C = Artifact(c, ∅)`. À chaque étape, le composant ajouté remplace le bouchon qui le simulait ; le bouchon descend d'un niveau dans le graphe, jusqu'à disparaître à l'étape 5, où le système est entièrement assemblé.

## Question 27

> Le cours (C2, diapositive 35) indique que la stratégie descendante détecte tôt les défauts de structure, mais que la logique des bouchons doit être élaborée. Illustrer ces deux points sur votre intégration de minibuild.

Le cours (C2, diapositive 35) : la stratégie incrémentale descendante consiste à « regrouper et intégrer d'abord les composants de plus haut niveau et simuler les composants de plus bas niveau ». Avantage : « les défauts de structure sont détectés très tôt ». Inconvénient : « la logique des souches doit être élaborée. Elles doivent retourner au composant à tester des valeurs qui correspondent aux différents contextes d'appels ».

**Défauts de structure détectés tôt.** Dès l'**étape 1**, avant même qu'existent un registre ou un stockage réels, on vérifie l'enchaînement `BuildTool` → parseur → résolveur :

- les dépendances lues dans le fichier sont bien **transmises** au résolveur (`verify(resolver).resolve({a, b})`) ;
- un fichier vide fait **remonter** `InvalidBuildFileException` à travers `BuildTool` sans que le résolveur soit sollicité.

C'est précisément le défaut d'interface décrit en introduction de la partie 4 : si le parseur avait renvoyé `null` pour un fichier vide, le test unitaire de `BuildTool` (bouchon qui renvoie toujours un `Project`) serait resté vert, mais l'étape 1 aurait échoué par une `NullPointerException`. L'architecture générale (qui appelle qui, avec quelles données) est donc validée dès les premières étapes.

**La logique des bouchons doit être élaborée.** Aux étapes 2 à 4, le bouchon du registre (puis celui du stockage) ne peut pas renvoyer n'importe quoi : il doit simuler un registre **cohérent**, c'est-à-dire renvoyer des artefacts dont les dépendances existent elles aussi. Dans nos tests, `A = Artifact(a, {c})` oblige le bouchon à répondre aussi à `lookup(c)` (puis `get(c)`) avec `C = Artifact(c, ∅)`. Un bouchon incohérent fait échouer ou réussir le test pour de **mauvaises raisons** :

- si on oublie `lookup(c)`, Mockito renvoie par défaut un `Optional` vide, comme si `c` n'avait jamais été publiée. Notre résolveur garde alors `c` dans le résultat sans explorer ses dépendances : le test passe quand même, mais il valide un registre où `a` dépend d'un artefact **inexistant**, situation qu'un vrai registre bien alimenté ne présente pas. Si `c` avait elle-même des dépendances, elles disparaîtraient du résultat et le test échouerait à cause du bouchon, et non du code ;
- si le bouchon renvoie pour `lookup(a)` un artefact dont la coordonnée n'est pas `a`, ou un graphe qu'aucun vrai registre ne pourrait contenir, le test vérifie une situation qui n'arrivera jamais.

Plus on descend, plus le bouchon doit reproduire de logique : à l'étape 4, le bouchon du stockage doit renvoyer exactement ce que le vrai registre y aurait rangé par `publish`. Ce n'est qu'à l'étape 5, sans bouchon, que les artefacts sont mis en place par la vraie porte d'entrée (`publish`), ce qui garantit leur cohérence.

## Question 28

> Proposer un ordre d'intégration ascendant pour minibuild, sous la même forme qu'en question 24.

Le cours (C2, diapositive 36) : la stratégie incrémentale ascendante consiste à « regrouper et intégrer les composants de plus bas niveau d'abord » ; ce sont alors les **pilotes** qui sont simulés. On part des feuilles du graphe (hauteur 0, question 23) et on remonte, en intégrant chaque fois une **paire** appelant–appelé (même démarche cumulative qu'en question 24). Un composant n'est ajouté que lorsque tous ses appelés sont déjà réels : **aucun bouchon** n'est nécessaire.

| Étape | Composants ajoutés | Interface testée | Bouchons | Pilotes |
|---|---|---|---|---|
| 1 | `StorageBasedRegistry`, `InMemoryStorage` | `StorageBasedRegistry`–`IStorage` | — | à la place du résolveur (appelant du registre) |
| 2 | `LineBasedPomParser`, `BufferedLineReader` | `LineBasedPomParser`–`ILineReader` | — | à la place de `BuildTool` (appelant du parseur) |
| 3 | `AllVersionsResolver` | `AllVersionsResolver`–`IRegistry` | — | à la place de `BuildTool` (appelant du résolveur) |
| 4 | `BuildTool` | `BuildTool`–`IPomParser`, `BuildTool`–`IResolver` | — | — |

- **Aucun bouchon** : à chaque étape, les composants appelés par ceux qu'on ajoute sont déjà intégrés et réels.
- **Un pilote par étape, sauf la dernière** : le pilote joue l'appelant non encore intégré ; dans les tests, c'est la **classe de test elle-même** qui joue ce rôle (elle appelle le composant le plus haut de l'assemblage, lui fournit les jeux d'essai et recueille les résultats). À l'étape 4, `BuildTool` n'a pas d'appelant (question 22) : plus de pilote.
- **Les étapes 1 et 2 peuvent être permutées** : elles concernent deux branches indépendantes du graphe.
- Ce n'est pas le miroir de l'ordre descendant (question 23) : le lecteur (hauteur 0) est intégré dès le début avec son parseur, alors qu'en descendant il n'arrivait qu'à l'étape 3 ; et `BuildTool` teste ses **deux** interfaces à la dernière étape, alors qu'en descendant elles étaient testées aux étapes 1 et 2.

## Question 29

> Implémenter l'étape 1 de votre ordre ascendant, avec un pilote qui fournit les jeux d'essai.

Code : `BottomUpIntegrationTest` (tag `td2-q29`). `StorageBasedRegistry` et `InMemoryStorage` sont réels ; il n'y a **aucun bouchon** (le stockage n'appelle rien). **La classe de test est le pilote** : elle joue le rôle du résolveur, l'appelant non encore intégré, et, comme le dit le cours (C2, diapositives 24-25), elle « fournit les jeux d'essai au composant à tester et en recueille les résultats ». Trois jeux d'essai :

1. `⊢ publish(A) ; lookup(a) ⇒ Some(A)` : ce qui est publié se retrouve, avec ses dépendances ;
2. `⊢ lookup(c) ⇒ None` : ce qui n'a jamais été publié est absent ;
3. `⊢ publish(A) ; publish(A') ⇒ ↯ AlreadyPublishedException ; lookup(a) ⇒ Some(A)` : la republication est refusée **et l'original n'est pas écrasé**.

Ce que l'intégration apporte par rapport au test unitaire du registre (question 16) : là, le stockage était un bouchon et l'on vérifiait par `verify(storage, never()).put(…)` que le registre **n'appelait pas** `put` en cas de republication. Ici, le vrai stockage est utilisé et on vérifie l'**état** : après le refus, `lookup` renvoie toujours l'artefact d'origine. C'est l'accord entre le registre et le stockage (ce que `put` range, `get` le rend ; `get` d'une coordonnée absente renvoie un `Optional` vide) qui est testé.

## Question 30

> Comparer les deux stratégies sur ce système : nombre de pilotes, nombre de bouchons, moment où chaque défaut serait détecté. Laquelle recommanderiez-vous ici, et pourquoi ?

D'après les tableaux des questions 24 et 28 :

| Critère | Descendante (Q24) | Ascendante (Q28) |
|---|---|---|
| Nombre de pilotes | **0** (le test appelle toujours `BuildTool`, sans appelant) | **3** (étapes 1 à 3 ; aucun à l'étape 4) |
| Nombre de bouchons | **4** composants simulés à un moment ou un autre : lecteur, résolveur, registre, stockage | **0** |
| Défauts de structure (enchaînement `BuildTool` → parseur → résolveur, données transmises, exception d'un fichier vide) | détectés **tôt** (étape 1) | détectés **tard** (étape 4, la dernière) |
| Défauts des interfaces de bas niveau (registre–stockage, parseur–lecteur) | détectés **tard** (étapes 3 et 5) | détectés **tôt** (étapes 1 et 2) |

On retrouve les arguments du cours (C2, diapositives 35 et 36) :

- **descendante** : « les défauts de structure sont détectés très tôt », « moins de pilotes à mettre en œuvre », et les mêmes jeux d'essai se réutilisent d'une étape à l'autre (le scénario a → c sert aux étapes 2 à 5) ; mais « la logique des souches doit être élaborée » (question 27) et les composants de bas niveau ne sont testés qu'à la fin ;
- **ascendante** : « facilité de localisation des défauts du composant intégré » et « données de test plus faciles à définir » (pas de bouchon à rendre cohérent : on publie de vrais artefacts) ; mais les défauts de haut niveau arrivent en dernier, et les jeux d'essai des pilotes « ne sont réutilisables ni pour les étapes suivantes d'intégration ni en validation car trop spécifiques » (les tests de la question 29 ne parlent que de `publish`/`lookup`).

**Recommandation pour minibuild : la stratégie descendante.**

- Le **risque principal** est dans l'enchaînement de haut niveau (`BuildTool`, résolveur : transmission des dépendances, propagation des erreurs, fermeture transitive), que la descendante teste dès l'étape 1 ;
- les **feuilles sont simples** (un stockage en mémoire, un adaptateur sur `BufferedReader`) et déjà bien couvertes par leurs tests unitaires : les tester tard est peu risqué ;
- avec **Mockito**, les bouchons sont peu coûteux à écrire (`when(…).thenReturn(…)`), ce qui atténue le principal inconvénient de la descendante ;
- à l'inverse, les **pilotes** de l'ascendante sont des tests écrits pour une seule étape, non réutilisables ensuite.

## Question 31

> Une troisième voie, présentée dans le cours (C2, diapositive 32), consiste à tout assembler d'un coup (big bang). Quel est son coût en pilotes et bouchons ? Pourquoi est-elle malgré tout déconseillée ?

Le cours (C2, diapositive 32) : dans la stratégie du big-bang, « tous les composants sont assemblés en même temps et le logiciel est testé dans son intégralité ».

**Coût apparent nul : aucun pilote, aucun bouchon.** On assemble directement les six composants réels de minibuild et on lance `build` sur un fichier : c'est exactement le test de l'étape 5 de la question 26, mais sans être passé par les étapes 1 à 4.

**Pourquoi elle est déconseillée** (« le chaos est garanti », C2 diapositive 32) :

- **« Les erreurs sont très difficiles à localiser »** : si `build` ne renvoie pas `{a, c}`, rien n'indique quelle interface est en cause : `BuildTool`–parseur, parseur–lecteur (une ligne vide mal traitée ?), `BuildTool`–résolveur, résolveur–registre (un `Optional` mal lu ?) ou registre–stockage. Avec l'intégration incrémentale, chaque étape n'ajoute qu'une interface : le défaut est dans celle-là ;
- **« Les tests ne peuvent être préparés à l'avance »** : on attend que tous les composants soient terminés pour commencer à tester les interfaces ;
- **« Difficulté de planification »** : on ne sait pas combien de temps prendra la mise au point de l'assemblage ;
- **« Risque d'entrer dans une boucle sans fin de correction/apparition d'erreurs »** : corriger un défaut sans savoir précisément d'où il vient peut en faire apparaître d'autres, que l'on découvre seulement en relançant tout le système.

Les stratégies incrémentales en sont l'« antithèse » (C2, diapositive 34) : « le logiciel est construit et testé par petits morceaux. Les erreurs sont donc plus faciles à localiser et à corriger » et « les interfaces sont testées plus complètement et précisément ». Le coût des pilotes et bouchons est le prix de cette localisation.

## Question 32

> Écrire un test de validation de bout en bout : un vrai fichier de build sur disque, dans les ressources de test ; un vrai `BufferedLineReader` sur ce fichier ; un registre réel, pré-alimenté avec les artefacts et leurs dépendances ; vérification de l'ensemble des dépendances résolues avec Hamcrest.

Code : `ValidationTest` et le fichier `src/test/resources/validation/mon-app.build` (tag `td2-q32`).

- **Le fichier de build** est celui de l'exemple de la partie 1 (`project mon-app`, `dependency org.acme:lib-a:1.0.0`, `dependency org.acme:lib-b:2.1.0`). Placé dans `src/test/resources`, il est copié par Gradle dans le classpath de test et retrouvé par `getResource("/validation/mon-app.build")` ;
- il est lu par un **vrai `BufferedLineReader`** ouvert sur le fichier (`Files.newBufferedReader`, fermé par un `try`-with-resources) ;
- le **registre réel** (`StorageBasedRegistry` sur `InMemoryStorage`) est pré-alimenté par `publish` dans `@BeforeEach`, avec le graphe de la partie 1 : `lib-a` dépend de `lib-c`, `lib-b` et `lib-c` n'ont pas de dépendance ;
- **aucune doublure** : tous les composants sont réels ;
- vérification avec Hamcrest : `assertThat(resolues, containsInAnyOrder(LIB_A, LIB_B, LIB_C))`. On attend la **fermeture transitive** de la spécification (partie 1) : les deux dépendances directes et `lib-c`, dépendance indirecte absente du fichier. `containsInAnyOrder` exprime qu'on attend exactement ces trois éléments, sans imposer d'ordre (un `Set` n'en a pas).

## Question 33

> À la fin de l'intégration descendante (question 26), tous les composants sont réels. En quoi votre dernier test d'intégration diffère-t-il de ce test de validation ?

Les deux tests assemblent les six composants réels, sans doublure, et vérifient le même genre de résultat. Ce qui les distingue n'est pas le code exécuté, mais **ce que chacun cherche à vérifier et d'où il vient** (cours C2) :

| | Dernier test d'intégration (Q26, étape 5) | Test de validation (Q32) |
|---|---|---|
| **Conçu à partir de** | l'**architecture** : « documentation d'architecture, documents de conception globale » (C2, diapositive 41) ; l'étape 5 existe parce que le graphe d'appel a une interface registre–stockage | la **spécification** : « les spécifications fonctionnelles, le manuel utilisateur, les cas d'utilisation » (C2, diapositive 45) ; ici l'exemple de la partie 1 et sa fermeture transitive |
| **Ce qu'il vérifie** | une **interface** précise (`StorageBasedRegistry`–`IStorage`), la dernière ajoutée | un **comportement du système** attendu par l'utilisateur : « tout comportement non conforme au comportement décrit dans les documents d'entrée » (C2, diapositive 45) |
| **Point de vue** | le test **connaît l'architecture** : les composants, leurs interfaces, l'ordre d'intégration | **boîte noire** : « tests de validation et/ou système traitent le comportement du logiciel complet » (C2, diapositive 44) |
| **Entrée** | un texte **en mémoire** (`StringReader`), choisi pour exercer l'interface | un **vrai fichier sur disque**, l'interface externe du système, dans un environnement « le plus proche possible de l'environnement final » (C2, diapositive 44) |
| **Scénario** | minimal (a → c), le même qu'aux étapes 2 à 4 | un **scénario d'usage** complet : le projet de la partie 1, deux dépendances directes, une transitive |
| **En cas d'échec** | le défaut est dans l'interface ajoutée à cette étape | le système ne fait pas ce que dit la spécification, sans indiquer où |

Autrement dit, le test d'intégration répond à « **les composants se comprennent-ils ?** » (vérification par rapport à la conception), le test de validation à « **le système fait-il ce qu'on attend de lui ?** » (vérification par rapport à la spécification). C'est la correspondance du cycle en V : tests d'intégration en face de la conception globale, tests de validation en face des spécifications fonctionnelles (C2, diapositives 4 et 13).

## Question 34

> Ajouter deux tests de validation supplémentaires : un fichier de build référençant une dépendance absente du registre, et un fichier syntaxiquement invalide. Que doit faire le système dans chaque cas ? La spécification du sujet répond-elle à cette question ?

Code : tags `td2-q33-red` (tests de validation), `td2-q33-unit-red` (exception et test unitaire du résolveur), `td2-q33-green` (correction du résolveur).

- `dependance-absente.build` : le projet dépend de `org.acme:lib-x:9.9.9`, jamais publiée ⇒ `↯ UnresolvedDependencyException` ;
- `invalide.build` : la ligne `dependency org.acme:lib-a` n'a que deux parties ⇒ `↯ InvalidBuildFileException`.

**La spécification ne répond qu'à moitié.** Pour le fichier invalide, la question 14 avait déjà fixé le comportement : le parseur lève `InvalidBuildFileException` ; le test de validation est vert d'emblée. Pour la dépendance absente, **le sujet ne dit rien** : la partie 1 décrit la fermeture transitive en supposant que tout est publié. Notre résolveur ignorait silencieusement une coordonnée introuvable (`ifPresent`) et renvoyait une fermeture **incomplète** sans le signaler.

**Choix fait : échouer explicitement**, en levant `UnresolvedDependencyException` (non vérifiée, car l'interface fournie `IResolver` ne déclare aucune exception). Une fermeture silencieusement incomplète ferait échouer le build **plus tard, loin de la cause** (à la compilation ou à l'exécution du projet, faute de la bibliothèque). C'est aussi le comportement de Maven et de Gradle : le build échoue sur une dépendance introuvable. (Une alternative aux exceptions serait de renvoyer un `Either<Erreur, Set<Gav>>`, comme avec vavr ; ici, le type de retour est fixé par l'interface `IResolver`.)

**Démarche suivie** : le test de validation révèle un **trou de la spécification** → nouvelle tâche dans la task list (tâche 20) → **test unitaire** du résolveur, qui localise le défaut dans un composant précis → correction :

```java
Artifact artefact = registry.lookup(gav)
        .orElseThrow(() -> new UnresolvedDependencyException(gav));
aTraiter.addAll(artefact.dependencies());
```

**Conséquence sur la question 27** : un bouchon de registre qui oublierait de répondre pour une dépendance (`lookup(c)` non programmé, donc `Optional` vide) ne fait plus passer le test pour de mauvaises raisons : le résolveur lève maintenant `UnresolvedDependencyException`, et le bouchon incohérent est détecté.

## Question 35

> Un test de validation qui échoue vous indique-t-il quel composant est fautif ? Comparer avec un test unitaire et un test d'intégration qui échouent. Qu'en déduisez-vous sur la complémentarité des trois niveaux ?

**Non.** Un test de validation qui échoue dit que **le système ne répond pas à la spécification**, pas **où** est le défaut. C'est un test « boîte noire » sur le logiciel complet (C2, diapositive 44) : il voit l'entrée (le fichier de build) et la sortie (l'ensemble résolu ou l'exception), pas les composants traversés. Exemple vécu à la question 34 : le test « dépendance absente » échouait (aucune exception levée), mais il aurait pu s'agir du parseur (dépendance mal lue), du résolveur (coordonnée ignorée), du registre ou du stockage (artefact mal rangé). Il a fallu écrire un test unitaire du résolveur pour localiser le défaut.

| Niveau qui échoue | Ce qu'il indique | Localisation |
|---|---|---|
| **Unitaire** | un composant ne fait pas ce que dit **sa conception** | **un composant** (tous ses collaborateurs sont doublés : le défaut ne peut être que chez lui) |
| **Intégration** | deux composants ne s'accordent pas sur **leur interface** | **une interface**, celle ajoutée à l'étape qui échoue |
| **Validation** | le système ne fait pas ce que dit **la spécification** | **aucune** : seulement « le système est faux » |

**Complémentarité des trois niveaux.** Chaque niveau répond à une question différente, et aucun ne remplace les autres :

- la **validation** dit **si** le logiciel est faux **du point de vue du client** : c'est le seul niveau qui confronte le système à la spécification, et le seul qui révèle un **trou de la spécification** (question 34) ;
- l'**intégration** et l'**unitaire** disent **où** : à quelle interface, puis dans quel composant ;
- inversement, des tests unitaires et d'intégration tous verts ne garantissent pas que le système fait ce que le client attend : ils vérifient la conception, pas la spécification.

C'est la structure du cycle en V (C2, diapositives 4 et 13) : chaque niveau de test fait face à un niveau de conception (spécification ↔ validation, conception globale ↔ intégration, conception détaillée ↔ unitaire). En pratique, un échec en validation se **descend** : on écrit un test d'intégration ou unitaire qui reproduit le défaut au plus près de sa cause, on corrige, et les trois niveaux repassent au vert.

## Question 36

> Le cours (C1, diapositive 26, « Qui fait quoi ? ») recommande que les tests de validation soient conduits par une équipe indépendante. Pourquoi ? Que recommande-t-il pour les tests d'intégration et pour les tests unitaires, et pourquoi cette différence ? Dans ce TD, vous avez écrit seul les trois niveaux de test : qu'est-ce que cela change ?

**Ce que recommande le cours (C1, « Qui fait quoi ? »)** :

| Niveau | Qui | Force de la recommandation | Pourquoi (C1) |
|---|---|---|---|
| **Unitaires** | les **développeurs** | « ne peuvent être conduits que par » | ce sont des tests « **boîte blanche** » : il faut connaître le code ; « le taux de couverture est mesuré durant cette activité » |
| **Intégration** | une **équipe indépendante** | « **devraient** » | « cela oblige les architectes/concepteurs et les développeurs à **documenter les interfaces** » ; « la gestion des interfaces logicielles et matérielles **n'est pas du ressort des développeurs** » |
| **Validation** | une **équipe indépendante** | « **doivent** » | ils sont conduits avec une « **vue client (i.e. métier)** » ; ce sont des tests « **boîte noire** (ils ne sont pas structurels) » ; éventuellement par le client lui-même (version bêta) |

**Pourquoi l'indépendance pour la validation.** La validation confronte le système à la **spécification**, du point de vue du client. Une équipe indépendante :

- lit la spécification **sans connaître le code** : elle teste ce que le système doit faire, pas ce qu'il fait ;
- **ne partage pas les hypothèses** des développeurs, ni leurs **erreurs de compréhension** de la spécification : si le développeur a mal compris une exigence, il écrira un code faux **et** un test faux, cohérents entre eux, et le test passera ;
- échappe à la « psychologie des développeurs/testeurs » (C1, diapositive 27) : les tests sont souvent construits « avec pour objectif (souvent inconscient) que le système est exempt de défauts ». Celui qui a écrit le code cherche, sans le vouloir, à montrer qu'il marche.

**Pourquoi cette différence entre les niveaux.** Plus on monte dans le cycle en V, plus le test porte sur ce que **d'autres** ont exprimé (les interfaces décidées par les architectes, puis les besoins du client) et moins il a besoin de connaître le code. Le test unitaire, lui, est structurel : seul le développeur connaît assez le code pour l'écrire (et en TDD, il l'écrit même avant le code). L'intégration est intermédiaire (« devraient ») : l'indépendance y est surtout un moyen de forcer la documentation des interfaces.

**Ce que cela change dans ce TD.** Nous avons écrit seuls les trois niveaux, et nos tests de validation sont donc fondés sur **notre propre lecture du sujet** :

- **à la question 34**, faute de spécification, nous avons nous-mêmes **choisi** le comportement (échouer sur une dépendance absente) puis écrit le test qui le vérifie. Une équipe indépendante aurait pu faire un **autre choix** (ignorer la dépendance, ou l'ajouter sans ses dépendances) : le désaccord aurait **révélé l'ambiguïté** de la spécification et forcé à la faire trancher par le client ;
- nos tests de validation partagent nos **hypothèses** : par exemple, le format de fichier (`project` sur la première ligne, lignes vides ignorées) est celui que nous avons implémenté ; personne d'autre n'a vérifié que c'est bien ce que le client attend ;
- les tests de validation ressemblent donc davantage à des tests **d'acceptation par le développeur** qu'à une vraie validation. Ils restent utiles (ils ont révélé le trou de la question 34), mais ils ne donnent pas la même garantie qu'une validation indépendante.

## Question 37

> Avant de lancer la mesure : quel taux de couverture attendez-vous, sachant que l'essentiel du code a été écrit en TDD ? Justifier.

JaCoCo mesure notamment la couverture des **instructions** (critère **C0** : « tester toutes les instructions au moins une fois ») et des **branches** des `if`/`switch` (critère **C1** : « tester toutes les décisions possibles au moins une fois ») (C4, diapositive 19 ; cours Outils, « JaCoCo : mesurer la couverture »).

**Prédiction : proche de 100 % en instructions et en branches**, mais pas forcément 100 %.

**Pourquoi proche de 100 %.** En TDD, **aucune ligne de production n'est écrite sans un test rouge qui l'exige** : on écrit le test, puis le code **minimal** qui le fait passer (Fake it, puis triangulation). Chaque instruction et chaque branche a donc été introduite pour faire passer un test, qui l'exécute. Par exemple :

- les deux branches de `Gav.parse` (mauvais nombre de parties, partie vide) viennent des tests de la question 9 ;
- chaque `throw` de `LineBasedPomParser` vient d'un cycle de la question 14 (fichier vide, ligne inconnue, coordonnée invalide…) ;
- la branche `if (resolues.add(gav))` du résolveur est exercée dans ses deux sens par le test du cycle et celui du losange (question 19) ;
- le `orElseThrow` du résolveur vient du test unitaire de la question 34.

Les tests d'intégration et de validation ré-exécutent ensuite ce même code : ils n'ajoutent pas de couverture, mais ne peuvent pas en retirer.

**Pourquoi pas forcément 100 %.** Ce qui **n'a pas été écrit en TDD** peut rester non couvert :

- **`App.java`**, la classe `main` (« Hello World! ») engendrée par `gradle init` à la question 1 : aucun test ne l'appelle, elle devrait apparaître à **0 %** ;
- les **records** `Artifact` et `Project` et les **exceptions**, écrits sans test dédié (question 10 : on ne teste pas un agrégat de données) : on s'attend à ce qu'ils soient couverts **par leur usage** dans les autres tests, mais un constructeur ou un accesseur jamais utilisé resterait non couvert ;
- d'éventuelles **branches défensives** (vérifications de `null`, cas « impossibles ») qu'aucun test n'a exigées.

Enfin, même 100 % ne prouverait pas l'absence de défauts : « un code exécuté n'est pas pour autant correctement vérifié » (cours Outils). La couverture mesure ce que les tests **exécutent**, pas ce qu'ils **vérifient**.

## Question 38

> Lancer la mesure et ouvrir le rapport HTML. Comparer la couverture obtenue avec votre prédiction. Identifier les portions non couvertes et compléter les tests si nécessaire.

Mesure : `./gradlew test jacocoTestReport`, rapport HTML dans `build/reports/jacoco/test/html/index.html`.

**Première mesure** (48 tests verts) :

| Compteur | Couverture |
|---|---|
| Instructions (C0) | **98 %** (7 manquées sur 367) |
| Branches (C1) | **100 %** (0 manquée sur 24) |
| Lignes | 81 / 84 |
| Méthodes | 27 / 29 |
| Classes | 12 / 13 |

**Comparaison avec la prédiction (question 37) : conforme.** Toutes les classes écrites en TDD sont à **100 %** en instructions et en branches. Les records `Artifact` et `Project` et les trois exceptions sont aussi à 100 %, **sans test dédié** : ils sont couverts par leur usage dans les autres tests. **La seule portion non couverte est `App`** (0 %, 7 instructions, 2 méthodes : le constructeur implicite et `main`), exactement comme prévu.

**Que faire de `App` ?** C'est la classe `main` (« Hello World! ») engendrée par `gradle init` à la question 1, avec le plugin `application` qui la désigne comme point d'entrée. Elle ne fait **rien** de minibuild, aucune tâche de la task list ne la concerne, et aucun test ne l'a exigée. Écrire un test pour la couvrir serait **tester un code sans intérêt** dans le seul but de faire monter un chiffre. La bonne correction est de **la supprimer** (YAGNI, cours MAPC : du code qu'aucun besoin ne justifie) : suppression de `App.java` et remplacement du plugin `application` par le plugin `java` dans `build.gradle.kts` (le corrigé n'a pas non plus de classe `App`).

**Seconde mesure**, après suppression (48 tests verts) :

| Compteur | Couverture |
|---|---|
| Instructions (C0) | **100 %** (360 / 360) |
| Branches (C1) | **100 %** (24 / 24) |
| Lignes | 81 / 81 |
| Méthodes | 27 / 27 |
| Classes | 12 / 12 |

On retrouve les chiffres du code de référence (360/360 instructions, 24/24 branches). **Aucun test n'a eu besoin d'être ajouté** : c'est la conséquence attendue du TDD.

**Limite** : 100 % en C0 et C1 signifie que chaque instruction et chaque branche a été **exécutée** au moins une fois, pas qu'elle a été **vérifiée**. Un test sans assertion qui appellerait `build` couvrirait tout autant de code. Par exemple, `Artifact` et `Project` sont couverts sans qu'aucun test ne vérifie spécifiquement leur copie défensive. « 100 % de couverture ne garantit pas l'absence de défauts : un code exécuté n'est pas pour autant correctement vérifié » (cours Outils).

## Question 39

> Comparer la couverture apportée par chaque niveau de test en exécutant séparément les tests unitaires, d'intégration et de validation (piste : étiqueter les tests avec l'annotation JUnit `@Tag` et filtrer dans Gradle).

Code : tag `td2-q39`.

- **Étiquettes JUnit** : chaque classe de test porte `@Tag("unit")` (les 7 classes de la partie 2 et 3), `@Tag("integration")` (`TopDownIntegrationTest`, `BottomUpIntegrationTest`) ou `@Tag("validation")` (`ValidationTest`). `SmokeTest` (vérification de l'outillage, question 1) n'est d'aucun niveau et n'a pas d'étiquette.
- **Tâches Gradle** : `unitTest`, `integrationTest` et `validationTest` (`build.gradle.kts`), chacune de type `Test` filtrée par `useJUnitPlatform { includeTags(niveau) }`, et suivie (`finalizedBy`) d'un rapport JaCoCo propre (`build/reports/jacoco/<niveau>`). La tâche `test` habituelle exécute toujours tous les tests.

```
./gradlew unitTest integrationTest validationTest
```

| Tests exécutés | Nombre | Instructions (C0) | Branches (C1) |
|---|---|---|---|
| unitaires | 33 | **100 %** (360 / 360) | **100 %** (24 / 24) |
| intégration | 9 | **88,1 %** (317 / 360) | **79,2 %** (19 / 24) |
| validation | 3 | **90,6 %** (326 / 360) | **70,8 %** (17 / 24) |
| tous (`test`) | 48 | 100 % | 100 % |

**Ce que l'intégration et la validation ne couvrent pas** (d'après les rapports) :

- **les cas d'erreur fins**, que seuls les tests unitaires exercent : les coordonnées mal formées de `Gav.parse` (mauvais nombre de parties, partie vide), les différentes erreurs du parseur (ligne inconnue, mot-clé sans valeur, première ligne qui n'est pas `project`), et pour la validation la **non-republication** (`AlreadyPublishedException` et sa branche dans `StorageBasedRegistry` : les tests de validation ne publient jamais deux fois) ;
- **certaines branches du résolveur** : le cas « coordonnée déjà vue » (cycle, losange de la question 19) n'est exercé que par les tests unitaires ; l'intégration ne passe pas non plus par la dépendance absente (`UnresolvedDependencyException`), vue seulement en validation et en unitaire.

**Interprétation.** Les tests unitaires couvrent **tout** à eux seuls : en TDD, chaque instruction a été écrite pour un test unitaire. Les tests d'intégration et de validation couvrent beaucoup de code (près de 90 % des instructions) avec **très peu de tests** (9 et 3), parce que chacun traverse toute la chaîne de composants ; mais ils suivent surtout le **chemin nominal** et laissent de côté la plupart des branches d'erreur (moins de 80 % des branches). Le taux de **branches** est plus révélateur que le taux d'instructions de ce qu'un niveau teste vraiment.

Ce n'est pas un défaut des tests d'intégration et de validation : leur rôle n'est pas de couvrir le code, mais de vérifier les **interfaces** et la **conformité à la spécification** (questions 33 et 35). À l'inverse, ces niveaux apportent ce que la couverture ne mesure pas : la couverture « tous niveaux » n'est pas plus élevée que celle des seuls tests unitaires (100 % dans les deux cas), et pourtant l'intégration et la validation vérifient des choses que les unitaires ne peuvent pas voir : l'intégration aurait détecté le défaut d'interface décrit en introduction de la partie 4 (parseur qui renvoie `null`), et la validation a révélé le trou de spécification de la question 34. **La couverture ne mesure pas la valeur d'un niveau de test.**

## Question 40

> Un taux de couverture élevé garantit-il l'absence de défauts ? JaCoCo mesure notamment la couverture des instructions et celle des branches : à quels critères du cours (C4, diapositive 19) correspondent-elles ? Construire sur minibuild un défaut réel non détecté malgré 100 % de couverture des instructions. Résiste-t-il à 100 % de couverture des branches ?

**Non, un taux de couverture élevé ne garantit pas l'absence de défauts.** La couverture mesure ce que les tests **exécutent**, pas ce qu'ils **vérifient** ni les situations qu'ils n'ont pas imaginées : « 100 % de couverture ne garantit pas l'absence de défauts : un code exécuté n'est pas pour autant correctement vérifié » (cours Outils).

**Correspondance avec les critères du cours (C4, diapositive 19)** :

- couverture des **instructions** de JaCoCo = critère **C0** : « tester toutes les instructions (nœuds) au moins une fois » ;
- couverture des **branches** de JaCoCo = critère **C1** : « tester toutes les décisions possibles au moins une fois (ou toutes les branches) ».

**Un défaut réel non détecté malgré 100 % de couverture.** À l'étape `td2-q18`, `AllVersionsResolver` était la version récursive naïve :

```java
public Set<Gav> resolve(Set<Gav> directDependencies) {
    Set<Gav> resolues = new HashSet<>();
    for (Gav gav : directDependencies) {
        resolues.add(gav);
        registry.lookup(gav)
                .ifPresent(artefact -> resolues.addAll(resolve(artefact.dependencies())));
    }
    return resolues;
}
```

Mesuré avec JaCoCo sur cette version, avec les seuls tests de `AllVersionsResolverTest` à ce moment-là (questions 17 et 18, chaîne a → b → c) : **100 % des instructions (45 / 45) et 100 % des branches (2 / 2)** de la classe. Tous les tests sont verts. Pourtant, la classe contient **deux défauts réels** :

- **elle ne termine pas en présence d'un cycle** (a → b → c → a) : chaque appel relance `resolve` sur une coordonnée déjà traitée, jusqu'à la `StackOverflowError` (question 19) ;
- **elle interroge plusieurs fois une même coordonnée** dans un losange (a → b, a → c, b → d, c → d : `lookup(d)` est appelé deux fois).

**Le défaut résiste-t-il à 100 % des branches ? Oui.** Les deux branches (entrer ou non dans la boucle `for`, c'est-à-dire avoir ou non une dépendance à traiter) sont bien exécutées par la chaîne a → b → c. Le défaut ne tient pas à une **branche non exécutée**, mais à un **chemin** : revenir sur une coordonnée déjà visitée. Ni C0 ni C1 n'exigent ce chemin ; il faudrait un critère plus fort portant sur les chemins ou sur le nombre de passages dans les boucles et les appels récursifs (critères C-i, « passer i fois dans chaque boucle », ou couverture des chemins, C4), que JaCoCo ne mesure pas.

**Ce que montre la question 19** : il a fallu **un test de plus** (le cycle, puis le losange), pas une couverture plus élevée, pour trouver le défaut. Après correction, la couverture est restée à 100 % : elle n'a ni annoncé le défaut, ni mesuré sa correction. La couverture est un **indicateur des parties non testées** (ce qu'on n'a pas exécuté est sûrement mal testé), pas une preuve que ce qui est exécuté est correct. Les cas de test se conçoivent à partir de la spécification et des risques (classes d'équivalence, valeurs limites, cycles), la couverture sert ensuite à repérer ce qu'on a oublié.
