App({
  globalData: {
    baseUrl: 'http://localhost:38942',
    token: '',
    userInfo: null
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
    }
  },

  setLogin(data) {
    this.globalData.token = data.token;
    this.globalData.userInfo = { userId: data.userId, nickname: data.nickname };
    wx.setStorageSync('token', data.token);
    wx.setStorageSync('userInfo', this.globalData.userInfo);
  },

  logout() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.reLaunch({ url: '/pages/login/login' });
  },

  checkLogin() {
    if (!this.globalData.token) {
      wx.reLaunch({ url: '/pages/login/login' });
      return false;
    }
    return true;
  }
});
