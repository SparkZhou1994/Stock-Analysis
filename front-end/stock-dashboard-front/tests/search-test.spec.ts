import { test, expect } from '@playwright/test';

test('搜索功能测试', async ({ page }) => {
  // 打开搜索页面
  await page.goto('http://localhost:3000/search');

  // 等待页面加载完成
  await page.waitForSelector('input[placeholder="输入股票代码或名称搜索"]');

  // 测试股票搜索
  await page.fill('input[placeholder="输入股票代码或名称搜索"]', '茅台');
  await page.click('button:has-text("搜索")');

  // 等待搜索结果加载
  await page.waitForSelector('.el-table', { timeout: 10000 });

  // 检查是否有搜索结果
  const stockResults = await page.locator('.el-table .el-table__row').count();
  expect(stockResults).toBeGreaterThan(0);

  // 截图保存搜索结果
  await page.screenshot({ path: 'C:/Users/Spark/doc/stock-dashboard/search-stock-result.png', fullPage: true });

  // 切换到基金搜索
  await page.click('text=基金');
  await page.fill('input[placeholder="输入股票代码或名称搜索"]', '华夏');
  await page.click('button:has-text("搜索")');

  // 等待搜索结果加载
  await page.waitForSelector('.el-table', { timeout: 10000 });

  // 检查是否有搜索结果
  const fundResults = await page.locator('.el-table .el-table__row').count();
  expect(fundResults).toBeGreaterThan(0);

  // 截图保存搜索结果
  await page.screenshot({ path: 'C:/Users/Spark/doc/stock-dashboard/search-fund-result.png', fullPage: true });

  // 测试添加到自选功能
  const firstResult = page.locator('.el-table .el-table__row').first();
  const addButton = firstResult.locator('button:has-text("加自选")');

  if (await addButton.isEnabled()) {
    await addButton.click();
    // 等待提示出现
    await page.waitForSelector('.el-message:has-text("添加到自选成功")', { timeout: 5000 });
    // 截图保存添加成功
    await page.screenshot({ path: 'C:/Users/Spark/doc/stock-dashboard/search-add-to-watchlist.png', fullPage: true });
  }

  console.log('搜索功能测试完成，截图已保存到 ~/doc/stock-dashboard/ 目录');
});
