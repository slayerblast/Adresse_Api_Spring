# Contrôles de qualité

Ce dossier contient des scripts pour vérifier automatiquement un projet Java,
PowerShell ou les deux. Ils servent à repérer tôt les erreurs simples, le code
trop complexe et les secrets ajoutés par erreur.

## Par où commencer

Choisissez le guide adapté à votre projet :

| Votre projet contient | Guide à suivre |
|---|---|
| Java et un fichier `pom.xml` | [Java](templates/java/README.md) |
| Des fichiers `.ps1` ou `.psm1` | [PowerShell](templates/powershell/README.md) |
| Java et PowerShell | Les deux guides, dans cet ordre : Java puis PowerShell |

## Les scripts

| Script | Rôle | Quand le lancer |
|---|---|---|
| [check-java.ps1](check-java.ps1) | Formate le code Java et lance les contrôles Maven | Avant un push ou dans la CI |
| [check-powershell.ps1](check-powershell.ps1) | Formate et analyse les scripts PowerShell | Avant un push ou dans la CI |
| [check-secrets.ps1](check-secrets.ps1) | Cherche des mots de passe, clés et jetons commis par erreur | Avant un push ou dans la CI |

## Deux niveaux de règles

- **cool** : point de départ conseillé pour un projet existant. Les défauts
  évidents bloquent; les règles de structure produisent des avertissements.
- **strict** : à utiliser quand le projet est stabilisé. Les seuils hauts de
  structure deviennent bloquants.

Ne passez pas à strict tant que les avertissements cool ne sont pas compris et
traités. Le détail des seuils est dans les guides Java et PowerShell.

## Commandes utiles

Depuis la racine du dépôt :

```powershell
# Java : contrôle rapide, puis contrôle complet
.\quality\check-java.ps1 -ProjectPath . -FastOnly
.\quality\check-java.ps1 -ProjectPath .

# PowerShell : contrôle du dossier scripts
.\quality\check-powershell.ps1 -Path .\scripts

# Secrets : contrôle de tout le dépôt
.\quality\check-secrets.ps1
```

Les scripts affichent `passed` quand le contrôle est réussi. En cas d'échec,
ils affichent le fichier, la ligne et la règle à corriger.

