@{
    IncludeDefaultRules = $true
    Severity            = @('Error', 'Warning')
    ExcludeRules         = @(
        # Console output is the expected UX for these CLI/reporting scripts, not a library.
        'PSAvoidUsingWriteHost'
    )
}
