<script setup>
defineProps({
  title: { type: String, required: true },
  price: { type: String, required: true },
  rating: { type: Number, default: 4.5 },
  reviews: { type: Number, default: 0 },
  badge: { type: String, default: '' },
  imageTone: { type: String, default: 'a' },
})
</script>

<template>
  <article class="card">
    <div v-if="badge" class="badge">{{ badge }}</div>
    <div class="img-wrap" :class="'tone-' + imageTone" role="img" :aria-label="title" />
    <div class="body">
      <h3 class="title">{{ title }}</h3>
      <div class="rating" :aria-label="'评分 ' + rating + ' / 5 星'">
        <span class="stars" :style="{ '--fill': (rating / 5) * 100 + '%' }" />
        <span class="count">（{{ reviews }} 条评价）</span>
      </div>
      <p class="price">
        <span class="currency">¥</span>{{ price }}
      </p>
      <p class="prime">
        <span class="prime-tag">商城</span>
        满额享免费配送
      </p>
      <button type="button" class="add">加入购物车</button>
    </div>
  </article>
</template>

<style scoped>
.card {
  position: relative;
  background: var(--mall-surface-elevated);
  border: 1px solid var(--mall-border);
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.card:hover {
  border-color: var(--mall-orange);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.45);
}

.badge {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1;
  background: linear-gradient(90deg, var(--mall-orange-dim), var(--mall-orange));
  color: #0a0a0b;
  font-size: 0.65rem;
  font-weight: 800;
  letter-spacing: 0.04em;
  padding: 4px 8px;
  border-radius: 3px;
}

.img-wrap {
  aspect-ratio: 1;
  background: linear-gradient(145deg, #2a2a32 0%, #18181c 100%);
}

.tone-a {
  background: linear-gradient(160deg, #3d3530 0%, #1a1512 100%);
}

.tone-b {
  background: linear-gradient(160deg, #30353d 0%, #12151a 100%);
}

.tone-c {
  background: linear-gradient(160deg, #353d30 0%, #151a12 100%);
}

.body {
  padding: 12px 14px 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 6px;
}

.title {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--mall-text);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.6em;
}

.title:hover {
  color: var(--mall-orange-bright);
}

.rating {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.78rem;
  color: var(--mall-text-muted);
}

.stars {
  width: 72px;
  height: 14px;
  background: linear-gradient(
    90deg,
    var(--mall-orange) var(--fill),
    #3f3f46 var(--fill)
  );
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 20'%3E%3Cpath fill='black' d='M10 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L10 15l-5.4 3.2 1.4-6L2 7.7l6-.6zM30 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L30 15l-5.4 3.2 1.4-6L22 7.7l6-.6zM50 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L50 15l-5.4 3.2 1.4-6L42 7.7l6-.6zM70 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L70 15l-5.4 3.2 1.4-6L62 7.7l6-.6zM90 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L90 15l-5.4 3.2 1.4-6L82 7.7l6-.6z'/%3E%3C/svg%3E")
    center / contain no-repeat;
  -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 20'%3E%3Cpath fill='black' d='M10 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L10 15l-5.4 3.2 1.4-6L2 7.7l6-.6zM30 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L30 15l-5.4 3.2 1.4-6L22 7.7l6-.6zM50 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L50 15l-5.4 3.2 1.4-6L42 7.7l6-.6zM70 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L70 15l-5.4 3.2 1.4-6L62 7.7l6-.6zM90 1.6l2.5 5.5 6 .6-4.5 4 1.4 6L90 15l-5.4 3.2 1.4-6L82 7.7l6-.6z'/%3E%3C/svg%3E")
    center / contain no-repeat;
}

.price {
  margin: 4px 0 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--mall-orange-bright);
}

.currency {
  font-size: 0.85rem;
  vertical-align: top;
}

.prime {
  margin: 0;
  font-size: 0.72rem;
  color: var(--mall-text-muted);
}

.prime-tag {
  display: inline-block;
  background: var(--mall-orange);
  color: var(--mall-black);
  font-weight: 800;
  font-size: 0.62rem;
  padding: 1px 5px;
  border-radius: 2px;
  margin-right: 4px;
}

.add {
  margin-top: auto;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--mall-orange-bright), var(--mall-orange));
  color: var(--mall-black);
  font-weight: 700;
  font-size: 0.8rem;
}

.add:hover {
  filter: brightness(1.06);
}
</style>
