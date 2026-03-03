/**
 * 牌面编码到显示信息的映射
 */
const TILE_MAP = {
  wan1: { text: '一', sub: '万', color: 'wan' },
  wan2: { text: '二', sub: '万', color: 'wan' },
  wan3: { text: '三', sub: '万', color: 'wan' },
  wan4: { text: '四', sub: '万', color: 'wan' },
  wan5: { text: '五', sub: '万', color: 'wan' },
  wan6: { text: '六', sub: '万', color: 'wan' },
  wan7: { text: '七', sub: '万', color: 'wan' },
  wan8: { text: '八', sub: '万', color: 'wan' },
  wan9: { text: '九', sub: '万', color: 'wan' },
  tiao1: { text: '一', sub: '条', color: 'tiao' },
  tiao2: { text: '二', sub: '条', color: 'tiao' },
  tiao3: { text: '三', sub: '条', color: 'tiao' },
  tiao4: { text: '四', sub: '条', color: 'tiao' },
  tiao5: { text: '五', sub: '条', color: 'tiao' },
  tiao6: { text: '六', sub: '条', color: 'tiao' },
  tiao7: { text: '七', sub: '条', color: 'tiao' },
  tiao8: { text: '八', sub: '条', color: 'tiao' },
  tiao9: { text: '九', sub: '条', color: 'tiao' },
  tong1: { text: '一', sub: '筒', color: 'tong' },
  tong2: { text: '二', sub: '筒', color: 'tong' },
  tong3: { text: '三', sub: '筒', color: 'tong' },
  tong4: { text: '四', sub: '筒', color: 'tong' },
  tong5: { text: '五', sub: '筒', color: 'tong' },
  tong6: { text: '六', sub: '筒', color: 'tong' },
  tong7: { text: '七', sub: '筒', color: 'tong' },
  tong8: { text: '八', sub: '筒', color: 'tong' },
  tong9: { text: '九', sub: '筒', color: 'tong' },
  feng1: { text: '東', sub: '', color: 'feng' },
  feng2: { text: '南', sub: '', color: 'feng' },
  feng3: { text: '西', sub: '', color: 'feng' },
  feng4: { text: '北', sub: '', color: 'feng' },
  jian1: { text: '中', sub: '', color: 'jian-zhong' },
  jian2: { text: '發', sub: '', color: 'jian-fa' },
  jian3: { text: '白', sub: '', color: 'jian-bai' }
};

function getTileInfo(code) {
  return TILE_MAP[code] || { text: '?', sub: '', color: 'wan' };
}

function formatDateTime(str) {
  if (!str) return '';
  const d = new Date(str);
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function formatDuration(seconds) {
  if (!seconds) return '0分0秒';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}分${s}秒`;
}

module.exports = { getTileInfo, formatDateTime, formatDuration, TILE_MAP };
