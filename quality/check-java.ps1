#!/usr/bin/env pwsh
<#
.SYNOPSIS
    L0 autofix (Spotless: imports/format) + L1 (Checkstyle) or full L1/L2 (+PMD/JaCoCo) validation for a Maven Java project.
.DESCRIPTION
    Self-contained: takes the target project as a parameter, so this script
    does not depend on any specific repo layout and can be copied anywhere.
    The actual Spotless/Checkstyle/PMD/JaCoCo rules live in the target project's own pom.xml.
.PARAMETER ProjectPath
    Folder containing the pom.xml (and, on Windows, the mvnw.cmd wrapper) to check.
.PARAMETER FastOnly
    L0 (Spotless autofix) + L1 (Checkstyle) only — skips PMD/CPD/JaCoCo (L2).
    Use this for a quick local check; use the default (full `clean verify`) for pre-push/CI.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectPath,
    [switch]$FastOnly
)

$ErrorActionPreference = 'Stop'
$pom = Join-Path $ProjectPath 'pom.xml'
if (-not (Test-Path $pom)) {
    Write-Error "No pom.xml found under '$ProjectPath'."
    exit 1
}

Push-Location $ProjectPath
try {
    $wrapper = (Test-Path './mvnw.cmd') ? './mvnw.cmd' : 'mvn'

    if ($FastOnly) {
        # L0: autofix, silent, never blocks
        & $wrapper -q spotless:apply
        # L1: fast validation (Checkstyle only)
        & $wrapper -q checkstyle:check
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Checkstyle violations found. See build output above."
            exit 1
        }
        Write-Host "Java fast quality gate passed (Spotless + Checkstyle)." -ForegroundColor Green
    } else {
        & $wrapper -q clean verify
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Java quality gate failed (Spotless/Checkstyle/PMD/JaCoCo). See build output above."
            exit 1
        }
        Write-Host "Java quality gate passed." -ForegroundColor Green
    }
} finally {
    Pop-Location
}
