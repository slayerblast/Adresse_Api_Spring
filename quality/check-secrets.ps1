#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Secret scanning (gitleaks), transverse to all languages.
.DESCRIPTION
    Self-contained: no repo-specific assumption, can be copied anywhere.
    Security findings are never auto-fixed (see docs/to_do/industrialisation-garde-fous-qualite-v3.md §1.4):
    this script only detects and blocks, it never rewrites files.
.PARAMETER Path
    Folder to scan. Defaults to the current directory.
.PARAMETER ScanGitHistory
    If set, scans the full git history of $Path instead of just the working tree
    (heavier, matches CI/Tier 3 usage rather than a quick local check).
#>
param(
    [string]$Path = '.',
    [switch]$ScanGitHistory
)

$ErrorActionPreference = 'Stop'

if (-not (Get-Command gitleaks -ErrorAction SilentlyContinue)) {
    Write-Error "gitleaks not installed or not on PATH. See quality/README.md#prérequis-dinstallation."
    exit 1
}

$gitleaksArgs = @('detect', '--source', $Path, '--redact')
if (-not $ScanGitHistory) { $gitleaksArgs += '--no-git' }

& gitleaks @gitleaksArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "gitleaks: secret(s) detected. See findings above."
    exit 1
}

Write-Host "Secret scan passed." -ForegroundColor Green
exit 0
