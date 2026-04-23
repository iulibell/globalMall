<script setup>
import ProductCard from './ProductCard.vue'
import HeroCarousel from './HeroCarousel.vue'

const deals = [
  { title: '无线主动降噪耳机，续航约 30 小时，可折叠便携', price: '599.00', rating: 4.6, reviews: 12840, badge: '特价', imageTone: 'a' },
  { title: '不锈钢保温杯 950ml，防漏杯盖，长效保温', price: '168.00', rating: 4.4, reviews: 3201, badge: '省18%', imageTone: 'b' },
  { title: 'USB-C 七合一扩展坞，铝合金外壳，快充传输', price: '279.00', rating: 4.3, reviews: 892, badge: '特价', imageTone: 'c' },
  { title: '机械键盘 75% 配列，段落轴体，紧凑桌面方案', price: '799.00', rating: 4.8, reviews: 562, badge: '', imageTone: 'a' },
]

const picks = [
  { title: '智能运动手环，血氧与睡眠监测，长续航', price: '399.00', rating: 4.2, reviews: 9102, badge: '', imageTone: 'b' },
  { title: '陶瓷不粘锅具十件套，可进烤箱，易清洁', price: '999.00', rating: 4.5, reviews: 445, badge: '精选', imageTone: 'c' },
  { title: '移动固态硬盘 1TB，读取约 1050MB/s', price: '699.00', rating: 4.7, reviews: 2230, badge: '', imageTone: 'a' },
  { title: '护眼台灯，多档色温亮度可调', price: '229.00', rating: 4.1, reviews: 1203, badge: '', imageTone: 'b' },
  { title: '旅行双肩包 40L，独立电脑仓，防泼水面料', price: '459.00', rating: 4.5, reviews: 667, badge: '', imageTone: 'c' },
  { title: '空气净化器（适用中面积房间），HEPA H13 滤芯', price: '1299.00', rating: 4.4, reviews: 3341, badge: '', imageTone: 'a' },
]
</script>

<template>
  <main class="home">
    <HeroCarousel />

    <section class="block container">
      <div class="block-head">
        <h2>今日特惠</h2>
        <a class="see-all" href="#">查看全部特惠</a>
      </div>
      <div class="grid">
        <ProductCard v-for="(p, i) in deals" :key="'d-' + i" v-bind="p" />
      </div>
    </section>

    <section class="block container">
      <div class="block-head">
        <h2>猜你喜欢</h2>
        <a class="see-all" href="#">发现更多</a>
      </div>
      <div class="grid grid-wide">
        <ProductCard v-for="(p, i) in picks" :key="'p-' + i" v-bind="p" />
      </div>
    </section>

    <footer class="site-footer">
      <div class="container footer-inner">
        <p class="copyright">
          © {{ new Date().getFullYear() }} GlobalMall · 保留所有权利
        </p>
      </div>
    </footer>
  </main>
</template>

<style scoped>
.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.home {
  padding-bottom: 0;
}

.site-footer {
  margin-top: 8px;
  border-top: 1px solid var(--mall-border);
  background: var(--mall-black-soft);
}

.footer-inner {
  padding: 20px 16px 28px;
  text-align: center;
}

.copyright {
  margin: 0;
  font-size: 0.8rem;
  color: var(--mall-text-muted);
  line-height: 1.5;
}

.block {
  margin-top: 28px;
  margin-bottom: 40px;
}

.block-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.block-head h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
}

.see-all {
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--mall-orange);
}

.see-all:hover {
  text-decoration: underline;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.grid-wide {
  grid-template-columns: repeat(3, 1fr);
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .grid-wide {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .grid,
  .grid-wide {
    grid-template-columns: 1fr;
  }
}
</style>
