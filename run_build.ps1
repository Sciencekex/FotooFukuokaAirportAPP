$ErrorActionPreference = "Continue"
Set-Location "c:\Users\22004\Desktop\project-idea\LittleTools\FotooFukuokaAirportAPP"
$logFile = "gradle_full_output.txt"

# Run gradle and capture ALL output
$pinfo = New-Object System.Diagnostics.ProcessStartInfo
$pinfo.FileName = "cmd.exe"
$pinfo.Arguments = "/c gradlew.bat assembleDebug 2>&1"
$pinfo.RedirectStandardOutput = $true
$pinfo.RedirectStandardError = $true
$pinfo.UseShellExecute = $false
$pinfo.CreateNoWindow = $true
$pinfo.WorkingDirectory = (Get-Location).Path

$p = New-Object System.Diagnostics.Process
$p.StartInfo = $pinfo
$p.Start() | Out-Null

$stdout = $p.StandardOutput.ReadToEnd()
$stderr = $p.StandardError.ReadToEnd()
$p.WaitForExit(300000)

$allOutput = "=== STDOUT ===`r`n$stdout`r`n=== STDERR ===`r`n$stderr`r`n=== EXIT: $($p.ExitCode) ==="
$allOutput | Out-File -FilePath $logFile -Encoding UTF8
Write-Output "BUILD COMPLETE, exit=$($p.ExitCode)"
