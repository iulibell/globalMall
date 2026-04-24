/**
 * 从 frontend/src/utils/pageDictionaryFallback.js 的 BY_TYPE.page_mall_profile 生成 MySQL INSERT。
 * 运行：node gen-profile-dict-sql.mjs（工作目录为 globalMall/sql）
 */
import { writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

import { BY_TYPE } from '../frontend/src/utils/pageDictionaryFallback.js'

const prof = BY_TYPE.page_mall_profile
const esc = (s) => String(s).replace(/\\/g, '\\\\').replace(/'/g, "''")

let sort = 0
const lines = []
for (const key of Object.keys(prof)) {
  sort += 1
  const row = prof[key]
  for (const lang of ['1', '2', '3']) {
    const name = row[lang] ?? row['1'] ?? ''
    lines.push(`('page_mall_profile','${esc(name)}','${key}',${sort},1,'${lang}')`)
  }
}

const out = `SET NAMES utf8mb4;

-- 个人中心等（由 sql/gen-profile-dict-sql.mjs 自 pageDictionaryFallback 生成）
DELETE FROM \`dictionary\` WHERE \`dict_type\` = 'page_mall_profile';

INSERT INTO \`dictionary\` (\`dict_type\`, \`dict_name\`, \`dict_value\`, \`sort\`, \`status\`, \`lang\`) VALUES
${lines.join(',\n')};
`

const dir = dirname(fileURLToPath(import.meta.url))
const target = join(dir, 'dictionary_mall_profile_inserts_mysql.sql')
writeFileSync(target, out)
console.log('Wrote', target, 'value rows', lines.length)
