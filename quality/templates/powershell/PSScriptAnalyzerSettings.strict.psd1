@{
    # STRICT mode: fails on Error/Warning/Information, no rules excluded.
    # See quality/templates/powershell/README.md.
    IncludeDefaultRules = $true
    Severity            = @('Error', 'Warning', 'Information')
    ExcludeRules        = @()
}
