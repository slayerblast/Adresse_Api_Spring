@{
    # COOL mode: fails only on Error/Warning, mutes rules that are noisy for
    # CLI/reporting scripts. See quality/templates/powershell/README.md.
    IncludeDefaultRules = $true
    Severity            = @('Error', 'Warning')
    ExcludeRules        = @(
        # Console output is the expected UX for CLI/reporting scripts, not a library.
        'PSAvoidUsingWriteHost',
        # Common in exploratory/ops scripts; not worth blocking the build over.
        'PSUseShouldProcessForStateChangingFunctions',
        'PSAvoidGlobalVars'
    )
}
