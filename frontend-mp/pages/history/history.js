const api = require('../../utils/api');
const { formatDateTime, formatDuration } = require('../../utils/util');
const app = getApp();

Page({
  data: {
    records: [],
    stats: {},
    winRate: 0,
    avgScore: 0,
    loading: true
  },

  onShow() {
    if (!app.checkLogin()) return;
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const [historyRes, statsRes] = await Promise.all([
        api.getHistory(),
        api.getStats()
      ]);

      const records = (historyRes.data || []).map(item => ({
        ...item,
        timeStr: formatDateTime(item.createdAt),
        durationStr: formatDuration(item.duration)
      }));

      const s = statsRes.data;
      const total = Number(s.total) || 0;
      const wins = Number(s.wins) || 0;

      this.setData({
        records,
        stats: { total, wins, losses: Number(s.losses) || 0, draws: Number(s.draws) || 0 },
        winRate: total > 0 ? Math.round((wins / total) * 100) : 0,
        avgScore: Number(s.avgScore) ? Number(s.avgScore).toFixed(1) : '0',
        loading: false
      });
    } catch (e) {
      this.setData({ loading: false });
    }
  }
});
