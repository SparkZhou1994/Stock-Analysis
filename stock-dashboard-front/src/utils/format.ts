/**
 * 格式化数字，保留指定位数
 */
export function formatNumber(num: number | null | undefined, decimals = 2): string {
  if (num === null || num === undefined || isNaN(num)) {
    return '--';
  }
  return num.toFixed(decimals);
}

/**
 * 格式化涨跌幅
 */
export function formatChangePercent(percent: number | null | undefined): string {
  if (percent === null || percent === undefined || isNaN(percent)) {
    return '--';
  }
  const sign = percent >= 0 ? '+' : '';
  return `${sign}${percent.toFixed(2)}%`;
}

/**
 * 格式化涨跌额
 */
export function formatChangeAmount(amount: number | null | undefined): string {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return '--';
  }
  const sign = amount >= 0 ? '+' : '';
  return `${sign}${amount.toFixed(2)}`;
}

/**
 * 格式化大数字（万、亿）
 */
export function formatLargeNumber(num: number | null | undefined): string {
  if (num === null || num === undefined || isNaN(num)) {
    return '--';
  }

  if (num >= 100000000) {
    return (num / 100000000).toFixed(2) + '亿';
  } else if (num >= 10000) {
    return (num / 10000).toFixed(2) + '万';
  } else {
    return num.toFixed(2);
  }
}

/**
 * 格式化成交量
 */
export function formatVolume(volume: number | null | undefined): string {
  if (volume === null || volume === undefined || isNaN(volume)) {
    return '--';
  }

  if (volume >= 100000000) {
    return (volume / 100000000).toFixed(2) + '亿手';
  } else if (volume >= 10000) {
    return (volume / 10000).toFixed(2) + '万手';
  } else {
    return volume + '手';
  }
}

/**
 * 格式化成交额
 */
export function formatTurnover(turnover: number | null | undefined): string {
  if (turnover === null || turnover === undefined || isNaN(turnover)) {
    return '--';
  }

  if (turnover >= 100000000) {
    return (turnover / 100000000).toFixed(2) + '亿元';
  } else if (turnover >= 10000) {
    return (turnover / 10000).toFixed(2) + '万元';
  } else {
    return turnover + '元';
  }
}

/**
 * 格式化日期
 */
export function formatDate(date: string | Date | null | undefined, format = 'YYYY-MM-DD'): string {
  if (!date) return '--';

  const d = typeof date === 'string' ? new Date(date) : date;

  if (isNaN(d.getTime())) return '--';

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}

/**
 * 获取涨跌对应的颜色类
 */
export function getChangeColorClass(value: number | null | undefined): string {
  if (value === null || value === undefined || isNaN(value)) {
    return 'text-gray-500';
  }
  return value > 0 ? 'text-red-500' : value < 0 ? 'text-green-500' : 'text-gray-500';
}

/**
 * 获取涨跌对应的背景颜色类
 */
export function getChangeBgColorClass(value: number | null | undefined): string {
  if (value === null || value === undefined || isNaN(value)) {
    return 'bg-gray-100';
  }
  return value > 0 ? 'bg-red-50' : value < 0 ? 'bg-green-50' : 'bg-gray-100';
}
