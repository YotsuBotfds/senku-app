param(
    [Parameter(Mandatory = $true)]
    [string]$MocksDir,
    [Parameter(Mandatory = $true)]
    [string]$MetadataPath,
    [string]$ReferenceDir = ""
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$ExpectedNames = @(
    "answer-phone-landscape.png",
    "answer-phone-portrait.png",
    "answer-tablet-landscape.png",
    "answer-tablet-portrait.png",
    "emergency-phone-portrait.png",
    "emergency-tablet-portrait.png",
    "guide-phone-landscape.png",
    "guide-phone-portrait.png",
    "guide-tablet-landscape.png",
    "guide-tablet-portrait.png",
    "home-phone-landscape.png",
    "home-phone-portrait.png",
    "home-tablet-landscape.png",
    "home-tablet-portrait.png",
    "search-phone-landscape.png",
    "search-phone-portrait.png",
    "search-tablet-landscape.png",
    "search-tablet-portrait.png",
    "thread-phone-landscape.png",
    "thread-phone-portrait.png",
    "thread-tablet-landscape.png",
    "thread-tablet-portrait.png"
)

$FallbackTargetDimensions = @{
    "answer-phone-landscape.png" = @(1331, 1098)
    "answer-phone-portrait.png" = @(592, 1801)
    "answer-tablet-landscape.png" = @(1435, 1770)
    "answer-tablet-portrait.png" = @(1381, 1802)
    "emergency-phone-portrait.png" = @(599, 1346)
    "emergency-tablet-portrait.png" = @(1423, 1773)
    "guide-phone-landscape.png" = @(1338, 931)
    "guide-phone-portrait.png" = @(599, 1492)
    "guide-tablet-landscape.png" = @(1419, 1618)
    "guide-tablet-portrait.png" = @(1314, 1780)
    "home-phone-landscape.png" = @(1345, 626)
    "home-phone-portrait.png" = @(594, 1307)
    "home-tablet-landscape.png" = @(1433, 1332)
    "home-tablet-portrait.png" = @(1360, 1805)
    "search-phone-landscape.png" = @(1363, 756)
    "search-phone-portrait.png" = @(612, 1313)
    "search-tablet-landscape.png" = @(1428, 1324)
    "search-tablet-portrait.png" = @(1340, 1732)
    "thread-phone-landscape.png" = @(1329, 627)
    "thread-phone-portrait.png" = @(579, 1322)
    "thread-tablet-landscape.png" = @(1441, 1326)
    "thread-tablet-portrait.png" = @(1337, 1791)
}

function Get-ImageSize {
    param([string]$Path)

    $image = [System.Drawing.Image]::FromFile($Path)
    try {
        return [pscustomobject]@{
            width = [int]$image.Width
            height = [int]$image.Height
        }
    } finally {
        $image.Dispose()
    }
}

function Get-TargetSize {
    param([string]$Name)

    if (-not [string]::IsNullOrWhiteSpace($ReferenceDir)) {
        $referencePath = Join-Path $ReferenceDir $Name
        if (Test-Path -LiteralPath $referencePath) {
            return Get-ImageSize -Path $referencePath
        }
    }

    $fallback = $FallbackTargetDimensions[$Name]
    if ($null -eq $fallback) {
        throw "No target dimensions known for $Name"
    }
    return [pscustomobject]@{
        width = [int]$fallback[0]
        height = [int]$fallback[1]
    }
}

function New-RoundedRectanglePath {
    param(
        [System.Drawing.RectangleF]$Rect,
        [float]$Radius
    )

    $diameter = [Math]::Max(1.0, $Radius * 2.0)
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddArc($Rect.X, $Rect.Y, $diameter, $diameter, 180, 90)
    $path.AddArc($Rect.Right - $diameter, $Rect.Y, $diameter, $diameter, 270, 90)
    $path.AddArc($Rect.Right - $diameter, $Rect.Bottom - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($Rect.X, $Rect.Bottom - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    return $path
}

function Get-ContentCrop {
    param([System.Drawing.Image]$Image, [string]$Name)

    $isLandscape = $Name.Contains("-landscape")
    $top = if ($isLandscape) { [Math]::Max(42, [int]($Image.Height * 0.045)) } else { [Math]::Max(72, [int]($Image.Height * 0.038)) }
    $bottom = if ($isLandscape) { [Math]::Max(34, [int]($Image.Height * 0.035)) } else { [Math]::Max(78, [int]($Image.Height * 0.040)) }
    $height = [Math]::Max(1, $Image.Height - $top - $bottom)
    return [System.Drawing.Rectangle]::new(0, $top, $Image.Width, $height)
}

function Draw-DeterministicStatusBar {
    param(
        [System.Drawing.Graphics]$Graphics,
        [int]$Width,
        [int]$StatusHeight,
        [bool]$Tablet
    )

    $fontSize = if ($Tablet) { 17.0 } elseif ($Width -lt 700) { 13.0 } else { 15.0 }
    $font = New-Object System.Drawing.Font([System.Drawing.FontFamily]::GenericMonospace, $fontSize, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(222, 228, 219))
    $mutedPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(150, 170, 158), 1.4)
    try {
        $baseline = [Math]::Max(8, [int](($StatusHeight - $font.Height) / 2))
        $Graphics.DrawString("4:21", $font, $brush, 22, $baseline)

        $batteryWidth = if ($Tablet) { 26 } else { 22 }
        $batteryHeight = if ($Tablet) { 13 } else { 11 }
        $capWidth = 3
        $rightPadding = 22
        $batteryX = $Width - $rightPadding - $batteryWidth - $capWidth
        $batteryY = [int](($StatusHeight - $batteryHeight) / 2)
        $offlineSize = $Graphics.MeasureString("OFFLINE", $font)
        $offlineX = $batteryX - [int]$offlineSize.Width - 14
        $Graphics.DrawString("OFFLINE", $font, $brush, $offlineX, $baseline)
        $Graphics.DrawRectangle($mutedPen, $batteryX, $batteryY, $batteryWidth, $batteryHeight)
        $Graphics.DrawLine($mutedPen, $batteryX + $batteryWidth + 1, $batteryY + 3, $batteryX + $batteryWidth + 1, $batteryY + $batteryHeight - 3)
    } finally {
        $font.Dispose()
        $brush.Dispose()
        $mutedPen.Dispose()
    }
}

function Export-FramedGoalMock {
    param(
        [string]$SourcePath,
        [string]$DestinationPath,
        [string]$Name,
        [object]$TargetSize
    )

    $sourceImage = [System.Drawing.Image]::FromFile($SourcePath)
    $bitmap = New-Object System.Drawing.Bitmap([int]$TargetSize.width, [int]$TargetSize.height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $sourceSize = [pscustomobject]@{ width = [int]$sourceImage.Width; height = [int]$sourceImage.Height }
    try {
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.Clear([System.Drawing.Color]::FromArgb(3, 13, 10))

        $tablet = $Name.Contains("-tablet-")
        $landscape = $Name.Contains("-landscape")
        $cornerRadius = if ($tablet) { 34.0 } else { 26.0 }
        $statusHeight = if ($tablet) { 52 } elseif ($landscape) { 38 } else { 42 }
        $outerInset = 2
        $innerInsetX = if ($tablet) { 18 } elseif ($landscape) { 14 } else { 12 }
        $bottomInset = if ($landscape) { 14 } else { 18 }

        $outerRect = [System.Drawing.RectangleF]::new($outerInset, $outerInset, $bitmap.Width - ($outerInset * 2), $bitmap.Height - ($outerInset * 2))
        $outerPath = New-RoundedRectanglePath -Rect $outerRect -Radius $cornerRadius
        $outerFill = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(6, 20, 15))
        $borderPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(55, 78, 65), 1.0)
        try {
            $graphics.FillPath($outerFill, $outerPath)
            $graphics.DrawPath($borderPen, $outerPath)
        } finally {
            $outerFill.Dispose()
            $borderPen.Dispose()
            $outerPath.Dispose()
        }

        Draw-DeterministicStatusBar -Graphics $graphics -Width $bitmap.Width -StatusHeight $statusHeight -Tablet:$tablet

        $contentCrop = Get-ContentCrop -Image $sourceImage -Name $Name
        $contentRect = [System.Drawing.Rectangle]::new(
            $innerInsetX,
            $statusHeight + 2,
            $bitmap.Width - ($innerInsetX * 2),
            $bitmap.Height - $statusHeight - $bottomInset - 2
        )

        $contentPath = New-RoundedRectanglePath -Rect ([System.Drawing.RectangleF]::new($contentRect.X, $contentRect.Y, $contentRect.Width, $contentRect.Height)) -Radius ([Math]::Max(12.0, $cornerRadius - 8.0))
        $oldClip = $graphics.Clip
        try {
            $graphics.SetClip($contentPath)
            $graphics.DrawImage($sourceImage, $contentRect, $contentCrop, [System.Drawing.GraphicsUnit]::Pixel)
            $graphics.Clip = $oldClip
        } finally {
            $oldClip.Dispose()
            $contentPath.Dispose()
        }

        $bitmap.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Png)
        return [pscustomobject]@{
            name = $Name
            source_path = $SourcePath
            output_path = $DestinationPath
            source_dimensions = $sourceSize
            output_dimensions = [pscustomobject]@{ width = [int]$TargetSize.width; height = [int]$TargetSize.height }
            source_crop = [pscustomobject]@{ x = [int]$contentCrop.X; y = [int]$contentCrop.Y; width = [int]$contentCrop.Width; height = [int]$contentCrop.Height }
            deterministic_status_time = "4:21"
            deterministic_status_right = "OFFLINE"
            battery_style = "outline"
            rounded_frame = $true
            live_os_chrome_cropped = $true
            app_content_fit_policy = "full-width chrome-cropped source stretched to target review frame"
        }
    } finally {
        $graphics.Dispose()
        $bitmap.Dispose()
        $sourceImage.Dispose()
    }
}

if (-not (Test-Path -LiteralPath $MocksDir)) {
    throw "MocksDir not found: $MocksDir"
}

$files = Get-ChildItem -LiteralPath $MocksDir -Filter "*.png" -File | Sort-Object Name
$actualNames = @($files | ForEach-Object { $_.Name })
$missing = @($ExpectedNames | Where-Object { $actualNames -notcontains $_ })
$extra = @($actualNames | Where-Object { $ExpectedNames -notcontains $_ })
if ($missing.Count -gt 0 -or $extra.Count -gt 0) {
    throw ("Cannot frame incomplete canonical mock set. Missing: {0}; Extra: {1}" -f ($missing -join ", "), ($extra -join ", "))
}

$processed = New-Object System.Collections.Generic.List[object]
$tempDir = Join-Path $MocksDir ".frame_tmp"
if (Test-Path -LiteralPath $tempDir) {
    Remove-Item -LiteralPath $tempDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

try {
    foreach ($file in $files) {
        $targetSize = Get-TargetSize -Name $file.Name
        $tempPath = Join-Path $tempDir $file.Name
        $result = Export-FramedGoalMock -SourcePath $file.FullName -DestinationPath $tempPath -Name $file.Name -TargetSize $targetSize
        Move-Item -LiteralPath $tempPath -Destination $file.FullName -Force
        $result | Add-Member -NotePropertyName output_sha256 -NotePropertyValue ((Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant())
        $processed.Add($result)
    }
} finally {
    if (Test-Path -LiteralPath $tempDir) {
        Remove-Item -LiteralPath $tempDir -Recurse -Force
    }
}

$metadata = [pscustomobject]@{
    schema_version = 1
    output_mode = "deterministic_mock_frame"
    generated_at_utc = (Get-Date).ToUniversalTime().ToString("o")
    mocks_dir = (Resolve-Path -LiteralPath $MocksDir).Path
    reference_dir = $(if (-not [string]::IsNullOrWhiteSpace($ReferenceDir) -and (Test-Path -LiteralPath $ReferenceDir)) { (Resolve-Path -LiteralPath $ReferenceDir).Path } else { $null })
    png_count = [int]$processed.Count
    deterministic_status_time = "4:21"
    deterministic_status_right = "OFFLINE"
    battery_style = "outline"
    live_os_chrome_policy = "crop emulator status/navigation bands before fitting app content"
    app_content_fit_policy = "preserve full cropped app width/height; do not center-crop to target aspect ratio"
    target_dimension_policy = "match canonical artifacts/mocks PNG dimensions"
    remaining_blockers = @(
        "Pixel-level app content parity still depends on Android UI states; this exporter only normalizes frame, live chrome, and output dimensions.",
        "Compact breadcrumb header parity is not synthesized by the exporter beyond removing live OS chrome."
    )
    files = @($processed.ToArray())
}

$metadataDir = Split-Path -Parent $MetadataPath
if (-not [string]::IsNullOrWhiteSpace($metadataDir)) {
    New-Item -ItemType Directory -Force -Path $metadataDir | Out-Null
}
$metadata | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $MetadataPath -Encoding UTF8
Write-Host ("android_goal_mock_frame_export: ok ({0} PNGs)" -f $processed.Count)
Write-Host ("metadata: {0}" -f $MetadataPath)
