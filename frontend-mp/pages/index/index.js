const api = require('../../utils/api');
const { formatDateTime } = require('../../utils/util');
const app = getApp();

Page({
  data: {
    userInfo: null,
    stats: {},
    winRate: 0,
    recentGames: [],
    starting: false
  },

  onShow() {
    if (!app.checkLogin()) return;
    this.setData({ userInfo: app.globalData.userInfo });
    this.loadStats();
    this.loadRecent();
  },

  async loadStats() {
    try {
      const res = await api.getStats();
      const s = res.data;
      const total = Number(s.total) || 0;
      const wins = Number(s.wins) || 0;
      const rate = total > 0 ? Math.round((wins / total) * 100) : 0;
      this.setData({
        stats: { total, wins, losses: Number(s.losses) || 0, draws: Number(s.draws) || 0 },
        winRate: rate
      });
    } catch (e) { /* 已在 api 层提示 */ }
  },

  async loadRecent() {
    try {
      const res = await api.getHistory();
      const list = (res.data || []).slice(0, 5).map(item => ({
        ...item,
        timeStr: formatDateTime(item.createdAt)
      }));
      this.setData({ recentGames: list });
    } catch (e) { /* 已在 api 层提示 */ }
  },

  async startGame() {
    this.setData({ starting: true });
    try {
      const res = await api.startGame();
      wx.navigateTo({
        url: `/pages/game/game?gameId=${res.data.gameId}`
      });
    } catch (e) { /* 已在 api 层提示 */ }
    finally {
      this.setData({ starting: false });
    }
  }
});
