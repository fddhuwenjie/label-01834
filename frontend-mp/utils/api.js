const app = getApp();

function request(options) {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' };
    if (app.globalData.token) {
      header['Authorization'] = 'Bearer ' + app.globalData.token;
    }

    wx.request({
      url: app.globalData.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success(res) {
        if (res.statusCode === 401) {
          wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
          app.logout();
          reject(new Error('未授权'));
          return;
        }
        if (res.data && res.data.code === 200) {
          resolve(res.data);
        } else {
          const msg = (res.data && res.data.message) || '请求失败';
          wx.showToast({ title: msg, icon: 'none' });
          reject(new Error(msg));
        }
      },
      fail(err) {
        wx.showToast({ title: '网络连接失败', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = {
  login: (data) => request({ url: '/api/user/login', method: 'POST', data }),
  register: (data) => request({ url: '/api/user/register', method: 'POST', data }),
  getUserInfo: () => request({ url: '/api/user/info' }),
  startGame: () => request({ url: '/api/game/start', method: 'POST' }),
  discard: (gameId, tile) => request({ url: `/api/game/${gameId}/discard?tile=${tile}`, method: 'POST' }),
  gameAction: (gameId, data) => request({ url: `/api/game/${gameId}/action`, method: 'POST', data }),
  getGameState: (gameId) => request({ url: `/api/game/${gameId}/state` }),
  getHistory: () => request({ url: '/api/game/history' }),
  getStats: () => request({ url: '/api/game/stats' }),
  getSettings: () => request({ url: '/api/settings' }),
  updateSettings: (data) => request({ url: '/api/settings', method: 'PUT', data })
};
