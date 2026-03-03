const api = require('../../utils/api');
const app = getApp();

Page({
  data: {
    speedOptions: ['慢速', '正常', '快速'],
    speedIndex: 1,
    soundEnabled: true,
    autoSort: true,
    nickname: '',
    totalGames: 0,
    winGames: 0,
    showLogoutModal: false
  },

  onShow() {
    if (!app.checkLogin()) return;
    this.loadSettings();
    this.loadUserInfo();
  },

  async loadSettings() {
    try {
      const res = await api.getSettings();
      const s = res.data;
      const speedMap = { slow: 0, normal: 1, fast: 2 };
      this.setData({
        speedIndex: speedMap[s.ai_speed] || 1,
        soundEnabled: s.sound_enabled !== 'false',
        autoSort: s.auto_sort !== 'false'
      });
    } catch (e) {
      console.warn('加载设置失败', e);
    }
  },

  async loadUserInfo() {
    try {
      const res = await api.getUserInfo();
      const u = res.data;
      this.setData({
        nickname: u.nickname,
        totalGames: u.totalGames || 0,
        winGames: u.winGames || 0
      });
    } catch (e) {
      console.warn('加载用户信息失败', e);
    }
  },

  async onSpeedChange(e) {
    const idx = Number(e.detail.value);
    const speedValues = ['slow', 'normal', 'fast'];
    this.setData({ speedIndex: idx });
    try {
      await api.updateSettings({ ai_speed: speedValues[idx] });
      wx.showToast({ title: '设置已保存', icon: 'success' });
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },

  async onSoundToggle(e) {
    const val = e.detail.value;
    this.setData({ soundEnabled: val });
    try {
      await api.updateSettings({ sound_enabled: String(val) });
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },

  async onAutoSortToggle(e) {
    const val = e.detail.value;
    this.setData({ autoSort: val });
    try {
      await api.updateSettings({ auto_sort: String(val) });
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },

  onLogout() {
    this.setData({ showLogoutModal: true });
  },

  onCancelLogout() {
    this.setData({ showLogoutModal: false });
  },

  onConfirmLogout() {
    this.setData({ showLogoutModal: false });
    app.logout();
  }
});
