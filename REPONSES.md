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
