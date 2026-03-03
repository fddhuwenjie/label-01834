const { getTileInfo } = require('../../utils/util');

Component({
  properties: {
    code: { type: String, value: '' },
    selected: { type: Boolean, value: false },
    small: { type: Boolean, value: false },
    tiny: { type: Boolean, value: false },
    facedown: { type: Boolean, value: false }
  },

  observers: {
    'code': function(code) {
      this.setData({ info: getTileInfo(code) });
    }
  },

  data: {
    info: { text: '', sub: '', color: 'wan' }
  },

  lifetimes: {
    attached() {
      this.setData({ info: getTileInfo(this.properties.code) });
    }
  },

  methods: {
    onTap() {
      if (!this.properties.facedown) {
        this.triggerEvent('tap', { code: this.properties.code });
      }
    }
  }
});
