export function formatAffiliateRevenue(value: unknown): string {
  const num = Number(value);
  return Number.isFinite(num) ? num.toFixed(2) : '0.00';
}
