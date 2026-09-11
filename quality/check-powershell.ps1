#!/usr/bin/env pwsh
<#
.SYNOPSIS
    L0 autofix (format) + L1 validation (lint, optional mirror-test check) for PowerShell files, using PSScriptAnalyzer.
.DESCRIPTION
    Self-contained: settings file is resolved next to this script, so the whole
    `quality/` folder can be copied into any repo and run as-is.
.PARAMETER Path
    Root folder to scan recursively. Ignored when -Files is provided.
.PARAMETER Files
    Explicit list of .ps1/.psm1 files to check (e.g. staged files from a git hook).
.PARAMETER RequireMirrorTests
    If set, any target file under a `src/` folder must have a matching
    `tests/<same-subpath>.Tests.ps1` (Pester) file, or the check fails.
#>
param(
    [string]$Path,
    [string[]]$Files,
    [switch]$RequireMirrorTests
)

$ErrorActionPreference = 'Stop'
$settings = Join-Path $PSScriptRoot 'PSScriptAnalyzerSettings.psd1'

if (-not (Get-Module -ListAvailable PSScriptAnalyzer)) {
    Write-Error "PSScriptAnalyzer module not installed. Run: Install-Module PSScriptAnalyzer -Scope CurrentUser"
    exit 1
}

if ($Files) {
    $targets = $Files | Where-Object { $_ -match '\.ps[m]?1$' -and (Test-Path $_) }
} elseif ($Path) {
    $targets = Get-ChildItem -Recurse -Path $Path -Include *.ps1, *.psm1 -File | Select-Object -ExpandProperty FullName
} else {
    Write-Error "Provide either -Path or -Files."
    exit 1
}

if (-not $targets) { exit 0 }

# L0: autofix, silent, never blocks (format, then cmdlet aliases/quotes via -Fix)
foreach ($file in $targets) {
    $content = Get-Content -Path $file -Raw
    $formatted = Invoke-Formatter -ScriptDefinition $content -Settings $settings
    if ($formatted -ne $content) {
        Set-Content -Path $file -Value $formatted -NoNewline
    }
    Invoke-ScriptAnalyzer -Path $file -Settings $settings -Fix | Out-Null
}

# L1: validation, blocking
$issues = foreach ($file in $targets) {
    Invoke-ScriptAnalyzer -Path $file -Settings $settings
}
if ($issues) {
    $issues | Format-Table -Property ScriptName, Line, RuleName, Message -AutoSize
    Write-Error "PSScriptAnalyzer: $($issues.Count) violation(s) found."
    exit 1
}

# L1: mirror-test check (src/<path>.ps1 -> tests/<path>.Tests.ps1), opt-in
if ($RequireMirrorTests) {
    $srcMarker = [IO.Path]::DirectorySeparatorChar + 'src' + [IO.Path]::DirectorySeparatorChar
    $testsMarker = [IO.Path]::DirectorySeparatorChar + 'tests' + [IO.Path]::DirectorySeparatorChar
    $missing = foreach ($file in $targets) {
        if ($file -notlike "*$srcMarker*") { continue }
        $withoutExt = Join-Path (Split-Path $file -Parent) ([IO.Path]::GetFileNameWithoutExtension($file))
        $testFile = "$($withoutExt.Replace($srcMarker, $testsMarker)).Tests.ps1"
        if (-not (Test-Path $testFile)) { $testFile }
    }
    if ($missing) {
        $missing | ForEach-Object { Write-Host "Missing mirror test: $_" -ForegroundColor Red }
        Write-Error "PowerShell: $($missing.Count) file(s) missing a mirror Pester test."
        exit 1
    }
}

Write-Host "PowerShell quality gate passed ($($targets.Count) file(s))." -ForegroundColor Green
exit 0
