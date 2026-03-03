const api = require('../../utils/api');
const app = getApp();

Page({
  data: {
    isLogin: true,
    nickname: '',
    password: '',
    loading: false
  },

  switchToLogin() { this.setData({ isLogin: true }); },
  switchToRegister() { this.setData({ isLogin: false }); },
  onNicknameInput(e) { this.setData({ nickname: e.detail.value }); },
  onPasswordInput(e) { this.setData({ password: e.detail.value }); },

  async handleSubmit() {
    const { nickname, password, isLogin } = this.data;
    if (!nickname.trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }
    if (!password.trim() || password.length < 4) {
      wx.showToast({ title: '密码至少4位', icon: 'none' });
      return;
    }

    this.setData({ loading: true });
    try {
      const fn = isLogin ? api.login : api.register;
      const res = await fn({ nickname: nickname.trim(), password });
      app.setLogin(res.data);
      wx.showToast({ title: isLogin ? '登录成功' : '注册成功', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/index/index' });
      }, 800);
    } catch (e) {
      // api.js 中已做 toast 提示
    } finally {
      this.setData({ loading: false });
    }
  }
});
