# 设置颜色
$Yellow = 'Yellow'
$Green = 'Green'
$Red = 'Red'

Write-Host "开始运行Gitlet测试..." -ForegroundColor $Yellow

# 获取所有测试文件
$sampleTests = Get-ChildItem -Path "samples\*.in" -ErrorAction SilentlyContinue
$customTests = Get-ChildItem -Path "student_tests\*.in" -ErrorAction SilentlyContinue

$allTests = @($sampleTests) + @($customTests)
$totalTests = $allTests.Count
$passedTests = 0
$failedTests = @()

Write-Host "找到 $totalTests 个测试文件" -ForegroundColor $Yellow

# 运行每个测试并收集结果
foreach ($test in $allTests) {
    $testName = $test.Name
    Write-Host "运行测试: $testName" -NoNewline

    $output = python runner.py $test.FullName 2>&1

    # 修改这里的判断逻辑 - 查找"All passed"或判断退出代码
    if ($output -match "All passed" -or $output -match "Ran \d+ tests\. All passed\.") {
        Write-Host " - 通过" -ForegroundColor $Green
        $passedTests++
    } else {
        Write-Host " - 失败" -ForegroundColor $Red
        $failedTests += $testName
    }
}

# 显示测试结果摘要
Write-Host "`n测试结果摘要:" -ForegroundColor $Yellow
Write-Host "运行测试: $totalTests"
Write-Host "通过测试: $passedTests" -ForegroundColor $Green
$failedCount = $failedTests.Count
Write-Host "失败测试: $failedCount" -ForegroundColor $(if ($failedCount -gt 0) { $Red } else { $Green })

# 显示失败的测试
if ($failedCount -gt 0) {
    Write-Host "`n失败的测试:" -ForegroundColor $Red
    foreach ($test in $failedTests) {
        Write-Host "- $test" -ForegroundColor $Red
    }

    Write-Host "`n调试建议:" -ForegroundColor $Yellow
    Write-Host "要调试失败的测试，请使用以下命令:" -ForegroundColor $Yellow
    Write-Host "python runner.py --debug --keep 测试文件路径" -ForegroundColor $Green
}

Write-Host "`n测试完成!" -ForegroundColor $(if ($failedCount -eq 0) { $Green } else { $Yellow })