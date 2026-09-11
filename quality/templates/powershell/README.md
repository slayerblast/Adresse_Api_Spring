# Contrôles PowerShell

Ce guide ajoute un formatage automatique et une analyse des fichiers `.ps1` et
`.psm1`. Le contrôle indique le fichier, la ligne et la règle quand une
correction est nécessaire.

## Installation pas à pas

1. Ouvrez PowerShell et installez PSScriptAnalyzer une seule fois :

```powershell
Install-Module -Name PSScriptAnalyzer -Scope CurrentUser -Force
Get-Module -ListAvailable PSScriptAnalyzer
```

2. Copiez le dossier `quality` dans votre dépôt.
3. Copiez
	`quality/templates/powershell/PSScriptAnalyzerSettings.cool.psd1` vers
	`quality/PSScriptAnalyzerSettings.psd1`.
4. Depuis la racine du dépôt, lancez le contrôle sur le dossier qui contient
	vos scripts :

```powershell
.\quality\check-powershell.ps1 -Path .\scripts
```

La commande peut reformater les scripts. Les erreurs et avertissements sont
bloquants dans le profil cool.

## Tests Pester optionnels

Pour demander un test miroir pour chaque script placé sous `src`, installez
Pester et ajoutez `-RequireMirrorTests` :

```powershell
Install-Module -Name Pester -MinimumVersion 5.7.1 -Scope CurrentUser -Force -SkipPublisherCheck
.\quality\check-powershell.ps1 -Path . -RequireMirrorTests
```

## Profils de règles

| Paramètre | Cool | Strict |
|---|---|---|
| Sévérités bloquantes | `Error`, `Warning` | `Error`, `Warning`, `Information` |
| Règles exclues | `Write-Host`, `ShouldProcess`, variables globales | aucune |

Le profil cool convient à un projet existant ou à des scripts d'exploitation.
Le profil strict contrôle aussi les informations et n'exclut aucune règle.

Pour passer à strict, copiez `PSScriptAnalyzerSettings.strict.psd1` sur
`quality/PSScriptAnalyzerSettings.psd1`, corrigez les résultats, puis lancez de
nouveau la même commande.
