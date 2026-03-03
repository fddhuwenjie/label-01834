const api = require('../../utils/api');
const app = getApp();

Page({
  _tileIdSeq: 0,

  data: {
    gameId: '',
    playerHand: [],
    playerMelds: [],
    aiPlayers: [
      {name:'AI-东',tileCount:0,melds:[],discards:[]},
      {name:'AI-南',tileCount:0,melds:[],discards:[]},
      {name:'AI-西',tileCount:0,melds:[],discards:[]}
    ],
    aiHandArrays: [[], [], []],
    seatIndexes: [0, 1, 2, 3],
    allDiscards: [[], [], [], []],
    currentPlayer: 0,
    phase: '',
    availableActions: [],
    lastDiscard: null,
    lastDiscardPlayer: -1,
    lastDiscardPlayerName: '',
    wallRemaining: 0,
    selectedIndex: -1,
    flyingTile: { show: false, code: '' },
    showResult: false,
    resultTitle: '',
    resultDesc: '',
    statusText: '等待游戏开始...',
    canChi: false,
    canPong: false,
    canKong: false,
    canWin: false,
    canSelfKong: false,
    canSelfWin: false
  },

  _wrapTiles(arr) {
    return (arr || []).map(code => ({ _id: this._tileIdSeq++, code }));
  },

  _wrapMelds(melds) {
    return (melds || []).map(meld => ({
      _meldId: this._tileIdSeq++,
      tiles: this._wrapTiles(meld)
    }));
  },

  _getStatusText(phase, currentPlayer, actions) {
    if (phase === 'end') return '游戏结束';
    if (phase === 'discard' && currentPlayer === 0) {
      if (actions.includes('win')) return '你可以自摸胡牌！也可以选牌打出';
      if (actions.includes('selfkong')) return '你可以暗杠！也可以选牌打出';
      return '轮到你出牌，请点选一张手牌后点击"打出"';
    }
    if (phase === 'action' && currentPlayer === 0) {
      const names = [];
      if (actions.includes('win')) names.push('胡');
      if (actions.includes('kong')) names.push('杠');
      if (actions.includes('pong')) names.push('碰');
      if (actions.includes('chi')) names.push('吃');
      return '可以 ' + names.join(' / ') + '，或点击"过"跳过';
    }
    if (currentPlayer !== 0) {
      const aiNames = ['', 'AI-东', 'AI-南', 'AI-西'];
      return aiNames[currentPlayer] + ' 正在思考...';
    }
    return '游戏进行中...';
  },

  onLoad(options) {
    if (!app.checkLogin()) return;
    if (options.gameId) {
      this.setData({ gameId: options.gameId });
      this.loadState(options.gameId);
    }
  },

  async loadState(gameId) {
    try {
      const res = await api.getGameState(gameId);
      this.updateState(res.data);
    } catch (e) {
      wx.showToast({ title: '加载失败，请重试', icon: 'none' });
    }
  },

  updateState(state) {
    this._tileIdSeq = 0;
    const names = ['玩家', 'AI-东', 'AI-南', 'AI-西'];
    const actions = state.availableActions || [];
    const ais = state.aiPlayers || this.data.aiPlayers;
    const aiHandArrays = ais.map(ai => new Array(ai.tileCount || 0).fill(1));

    const playerHand = this._wrapTiles(state.playerHand);
    const playerMelds = this._wrapMelds(state.playerMelds);
    const allDiscards = [
      this._wrapTiles(state.playerDiscards),
      this._wrapTiles(ais[0] ? ais[0].discards : []),
      this._wrapTiles(ais[1] ? ais[1].discards : []),
      this._wrapTiles(ais[2] ? ais[2].discards : [])
    ];
    const aiPlayersWrapped = ais.map(ai => ({
      name: ai.name,
      seat: ai.seat,
      tileCount: ai.tileCount,
      melds: this._wrapMelds(ai.melds),
      discards: []
    }));

    const statusText = this._getStatusText(state.phase, state.currentPlayer, actions);

    this.setData({
      playerHand,
      playerMelds,
      aiPlayers: aiPlayersWrapped,
      aiHandArrays,
      allDiscards,
      currentPlayer: state.currentPlayer,
      phase: state.phase,
      availableActions: actions,
      lastDiscard: state.lastDiscard,
      lastDiscardPlayer: state.lastDiscardPlayer,
      lastDiscardPlayerName: state.lastDiscardPlayer >= 0 ? names[state.lastDiscardPlayer] : '',
      wallRemaining: state.wallRemaining,
      selectedIndex: -1,
      statusText,
      canChi: actions.includes('chi'),
      canPong: actions.includes('pong'),
      canKong: actions.includes('kong'),
      canWin: actions.includes('win'),
      canSelfKong: actions.includes('selfkong'),
      canSelfWin: state.phase === 'discard' && actions.includes('win')
    });

    if (state.phase === 'end') {
      let title, desc;
      if (state.winner === 0) {
        title = '恭喜胡牌！';
        desc = '你赢得了这局比赛，获得 10 分';
      } else if (state.winner === -1) {
        title = '流局';
        desc = '牌墙已空，本局平局';
      } else {
        title = '对局结束';
        desc = `${state.winnerName || names[state.winner]} 胡牌，你输了本局`;
      }
      this.setData({ showResult: true, resultTitle: title, resultDesc: desc });
    }
  },

  onTileSelect(e) {
    if (this.data.phase !== 'discard') return;
    const idx = e.currentTarget.dataset.index;
    if (this.data.selectedIndex === idx) {
      this.setData({ selectedIndex: -1 });
    } else {
      this.setData({ selectedIndex: idx });
    }
  },

  async onDiscard() {
    const { gameId, selectedIndex, phase, playerHand } = this.data;
    if (selectedIndex < 0 || phase !== 'discard') return;
    const tileCode = playerHand[selectedIndex].code;

    this.setData({ flyingTile: { show: true, code: tileCode }, selectedIndex: -1 });
    await new Promise(r => setTimeout(r, 850));
    this.setData({ flyingTile: { show: false, code: '' } });

    try {
      const res = await api.discard(gameId, tileCode);
      this.updateState(res.data);
    } catch (e) {
      wx.showToast({ title: '出牌失败，请重试', icon: 'none' });
    }
  },

  async doAction(action, tile, tiles) {
    wx.showLoading({ title: '操作中...', mask: true });
    try {
      const res = await api.gameAction(this.data.gameId, { action, tile, tiles });
      this.updateState(res.data);
    } catch (e) {
      wx.showToast({ title: '操作失败，请重试', icon: 'none' });
    } finally { wx.hideLoading(); }
  },

  onPong() { this.doAction('pong'); },
  onKong() { this.doAction('kong'); },
  onChi() { this.doAction('chi', null, null); },
  onWin() { this.doAction('win'); },
  onPass() { this.doAction('pass'); },
  onSelfKong() {
    const { selectedIndex, playerHand } = this.data;
    const tile = selectedIndex >= 0 ? playerHand[selectedIndex].code : null;
    this.doAction('kong', tile);
  },

  async newGame() {
    this.setData({ showResult: false });
    wx.showLoading({ title: '开始新局...', mask: true });
    try {
      const res = await api.startGame();
      this.setData({ gameId: res.data.gameId });
      this.updateState(res.data);
    } catch (e) {
      wx.showToast({ title: '创建游戏失败', icon: 'none' });
    } finally { wx.hideLoading(); }
  },

  backToHome() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
