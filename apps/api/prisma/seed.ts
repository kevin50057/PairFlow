/**
 * Local dev seed data.
 *
 * Creates: predefined review tags, 1 admin + 5 normal users, 16 restaurants
 * scattered around Taipei, and a spread of reviews with tags. Restaurant
 * aggregates (averageThunderScore / reviewCount) are recomputed at the end.
 *
 * Idempotent: transactional tables are wiped and rebuilt on every run.
 * Run with: pnpm --filter @thunder/api db:seed
 */
import { PrismaClient } from '@prisma/client';
import * as bcrypt from 'bcryptjs';
import { REVIEW_TAGS } from '../src/constants/tags';

const prisma = new PrismaClient();

// All seeded accounts share this password so you can log in immediately.
const SEED_PASSWORD = 'Password123!';

// Roughly Taipei 101. Restaurants are offset from here by small deltas.
const TAIPEI = { lat: 25.033, lng: 121.5654 };

// Small deterministic PRNG so seeds are stable across runs.
function mulberry32(seed: number) {
  let a = seed >>> 0;
  return () => {
    a |= 0;
    a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}
const rng = mulberry32(42);
const pick = <T>(arr: T[]): T => arr[Math.floor(rng() * arr.length)];
const randInt = (min: number, max: number) => Math.floor(rng() * (max - min + 1)) + min;
const shuffle = <T>(arr: T[]): T[] => [...arr].sort(() => rng() - 0.5);

interface RSeed {
  name: string;
  category: string;
  district: string;
  dLat: number;
  dLng: number;
}

const RESTAURANTS: RSeed[] = [
  { name: '阿明牛肉麵', category: '麵食', district: '信義區', dLat: 0.001, dLng: 0.002 },
  { name: '老地方熱炒', category: '熱炒', district: '信義區', dLat: -0.004, dLng: 0.006 },
  { name: '香味鹽酥雞', category: '鹽酥雞', district: '大安區', dLat: 0.008, dLng: -0.003 },
  { name: '真好味便當', category: '便當', district: '大安區', dLat: -0.006, dLng: -0.005 },
  { name: '一級棒拉麵', category: '拉麵', district: '信義區', dLat: 0.012, dLng: 0.004 },
  { name: '海派海鮮餐廳', category: '海鮮', district: '松山區', dLat: 0.003, dLng: 0.011 },
  { name: '巷口豆漿店', category: '早餐', district: '大安區', dLat: -0.009, dLng: 0.002 },
  { name: '川味紅油抄手', category: '川菜', district: '信義區', dLat: 0.006, dLng: -0.008 },
  { name: '阿姨自助餐', category: '自助餐', district: '大安區', dLat: -0.012, dLng: 0.007 },
  { name: '元氣火鍋', category: '火鍋', district: '信義區', dLat: 0.014, dLng: -0.002 },
  { name: '街角義大利麵', category: '義式', district: '松山區', dLat: -0.003, dLng: 0.014 },
  { name: '城市燒肉', category: '燒肉', district: '信義區', dLat: 0.002, dLng: 0.016 },
  { name: '幸福甜點屋', category: '甜點', district: '大安區', dLat: -0.015, dLng: -0.004 },
  { name: '速食漢堡王國', category: '速食', district: '松山區', dLat: 0.009, dLng: 0.009 },
  { name: '夜市大腸包小腸', category: '小吃', district: '信義區', dLat: -0.007, dLng: -0.011 },
  { name: '養生素食坊', category: '素食', district: '大安區', dLat: 0.016, dLng: 0.013 },
];

const CONTENTS = [
  '服務態度非常差，等了快一個小時菜才上桌。',
  '東西又貴又難吃，份量還少得可憐。',
  '環境髒亂，桌子油膩膩的完全不想坐下。',
  '照片跟實品差超多，整個被詐騙的感覺。',
  '食物送來就已經冷掉，吃起來完全沒味道。',
  '價格不合理，CP值超級低，真的不會再來。',
  '排隊排超久，結果根本不值得，大雷一顆。',
  '衛生狀況堪憂，看到廚房環境就沒胃口了。',
  '上菜超慢，服務生的態度也很不耐煩。',
  '整體體驗很差，踩到大雷，分享出來給大家避雷。',
  '老闆態度差到不行，點餐還被翻白眼。',
  '湯頭油到誇張，喝兩口就膩到受不了。',
];

const TITLES: (string | null)[] = ['大雷', '避雷', '地雷店', '踩雷紀錄', '不推', '雷到不行', null, null];

async function main() {
  // 1) Tags (upsert so ids stay stable across reseeds).
  for (const t of REVIEW_TAGS) {
    await prisma.reviewTag.upsert({
      where: { code: t.code },
      update: { label: t.label, sortOrder: t.sortOrder },
      create: t,
    });
  }

  // 2) Wipe transactional data for an idempotent reseed (FK-safe order).
  await prisma.reviewTagRel.deleteMany();
  await prisma.reviewImage.deleteMany();
  await prisma.report.deleteMany();
  await prisma.review.deleteMany();
  await prisma.restaurant.deleteMany();
  await prisma.user.deleteMany();

  // 3) Users. (all share SEED_PASSWORD so you can log in right away)
  const passwordHash = await bcrypt.hash(SEED_PASSWORD, 10);
  const admin = await prisma.user.create({
    data: { email: 'admin@thunder.test', passwordHash, nickname: '管理員', role: 'ADMIN' },
  });
  const nicknames = ['踩雷王', '美食偵探', '路過的人', '挑嘴小姐', '雷區探險家'];
  const users = [];
  for (let i = 0; i < nicknames.length; i++) {
    users.push(
      await prisma.user.create({
        data: { email: `user${i + 1}@thunder.test`, passwordHash, nickname: nicknames[i] },
      }),
    );
  }
  const reviewers = [admin, ...users];

  // 4) Restaurants.
  const restaurants = [];
  for (let i = 0; i < RESTAURANTS.length; i++) {
    const r = RESTAURANTS[i];
    restaurants.push(
      await prisma.restaurant.create({
        data: {
          name: r.name,
          category: r.category,
          city: '台北市',
          district: r.district,
          address: `台北市${r.district}測試路${randInt(1, 200)}號`,
          latitude: Number((TAIPEI.lat + r.dLat).toFixed(6)),
          longitude: Number((TAIPEI.lng + r.dLng).toFixed(6)),
          phone: `02-2${randInt(100, 999)}-${randInt(1000, 9999)}`,
          coverImageUrl: `https://picsum.photos/seed/thunder${i + 1}/640/400`,
        },
      }),
    );
  }

  // 5) Reviews (+ tags).
  const tagRows = await prisma.reviewTag.findMany();
  let reviewTotal = 0;
  for (const rest of restaurants) {
    const reviewerCount = randInt(1, Math.min(5, reviewers.length));
    for (const reviewer of shuffle(reviewers).slice(0, reviewerCount)) {
      const review = await prisma.review.create({
        data: {
          restaurantId: rest.id,
          userId: reviewer.id,
          thunderScore: pick([3, 3, 4, 4, 4, 5, 5, 2, 1]), // skewed toward 雷
          title: pick(TITLES),
          content: pick(CONTENTS),
          visitDate: new Date(Date.now() - randInt(1, 90) * 86_400_000),
        },
      });
      for (const tag of shuffle(tagRows).slice(0, randInt(1, 3))) {
        await prisma.reviewTagRel.create({ data: { reviewId: review.id, reviewTagId: tag.id } });
      }
      reviewTotal++;
    }
  }

  // 6) Recompute restaurant aggregates.
  for (const rest of restaurants) {
    const agg = await prisma.review.aggregate({
      where: { restaurantId: rest.id, status: 'PUBLISHED' },
      _avg: { thunderScore: true },
      _count: true,
    });
    await prisma.restaurant.update({
      where: { id: rest.id },
      data: {
        averageThunderScore: agg._avg.thunderScore ? Number(agg._avg.thunderScore.toFixed(1)) : 0,
        reviewCount: agg._count,
      },
    });
  }

  console.log(
    `✅ Seeded ${REVIEW_TAGS.length} tags, ${reviewers.length} users, ${restaurants.length} restaurants, ${reviewTotal} reviews`,
  );
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
