# Contrôles Java avec Maven

Ce guide ajoute quatre contrôles à un projet Java Maven : formatage du code,
règles Java, duplication de code et couverture de tests. Commencez avec le
profil **cool**; il permet d'adopter les contrôles sans bloquer un projet
existant pour tous ses anciens écarts.

## Installation pas à pas

1. Installez un JDK 21, puis ouvrez PowerShell et vérifiez :

```powershell
java -version
```

2. Placez-vous à la racine du projet. C'est le dossier qui contient `pom.xml`.
3. Vérifiez que le wrapper Maven existe :

```powershell
.\mvnw.cmd -v
```

S'il n'existe pas, installez Maven une seule fois, puis créez le wrapper :

```powershell
mvn -N wrapper:wrapper
```

4. Copiez le dossier `quality` dans votre dépôt.
5. Copiez le contenu de [pom-plugins-fragment.xml](pom-plugins-fragment.xml)
   dans le bloc `<build><plugins>` existant de votre `pom.xml`. Ne créez pas un
   second bloc `<plugins>`.
6. Copiez `checkstyle.cool.xml` à la racine du projet et renommez-le
   `checkstyle.xml`. Faites de même avec `pmd-ruleset.cool.xml`, renommé
   `pmd-ruleset.xml`.
7. Lancez le premier contrôle :

```powershell
Set-Location "C:\code\mon-projet"
.\quality\check-java.ps1 -ProjectPath . -FastOnly
```

Cette commande peut reformater le code. Elle lance aussi Checkstyle. Quand elle
est verte, lancez le contrôle complet avant un push :

```powershell
.\quality\check-java.ps1 -ProjectPath .
```

Le fragment utilise `src` pour le code et `test` pour les tests. Si votre
projet utilise `src/main/java` et `src/test/java`, adaptez ces chemins dans le
`pom.xml` avant de lancer le contrôle.

## Profils

Les deux profils utilisent `<violationSeverity>error</violationSeverity>` : un
avertissement est affiché mais ne bloque pas la compilation; une erreur bloque
la compilation. Checkstyle est l'outil qui décide des seuils de taille et de
complexité.

| Règle | Cool | Strict |
|---|---:|---:|
| Magic numbers | erreur | erreur |
| Taille de fichier | avertissement à 400 | erreur à 500, avertissement à 400 |
| Complexité cyclomatique | avertissement à 15 | erreur à 20, avertissement à 15 |
| Taille de méthode | avertissement à 100 | erreur à 200, avertissement à 100 |
| Expression booléenne | avertissement à 4 | erreur à 6, avertissement à 4 |
| Profondeur de `if` | avertissement à 3 | erreur à 5, avertissement à 3 |
| Profondeur de `try` | avertissement à 2 | avertissement à 2 |
| Couplage DAC | avertissement à 7 | erreur à 9, avertissement à 7 |
| Nombre de paramètres | avertissement à 5 | erreur à 7, avertissement à 5 |
| JaCoCo | 50 % | 80 % |

PMD cherche les défauts Java courants, par exemple une variable inutilisée ou
un bloc vide. Il ne redéfinit pas les seuils de complexité déjà contrôlés par
Checkstyle.

## Passer en strict

Quand les avertissements cool sont traités, remplacez `checkstyle.xml` et
`pmd-ruleset.xml` par les fichiers `strict` correspondants. Dans le `pom.xml`,
remplacez aussi le minimum JaCoCo `0.50` par `0.80`. Relancez ensuite le
contrôle complet.
