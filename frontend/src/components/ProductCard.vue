<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  title: { type: String, required: true },
  price: { type: String, required: true },
  badge: { type: String, default: '' },
  imageUrl: { type: String, default: '' },
  imageTone: { type: String, default: 'a' },
  /** 有值时点击卡片进入商品详情 */
  goodsId: { type: String, default: '' },
})

const router = useRouter()
const imageFailed = ref(false)
const displayImageUrl = computed(() => {
  const u = (props.imageUrl || '').trim()
  if (!u || imageFailed.value) return ''
  return u
})

function onCardClick() {
  const id = (props.goodsId || '').trim()
  if (!id) return
  router.push({ name: 'goods-detail', params: { goodsId: id } })
}

function onImageError() {
  imageFailed.value = true
}
</script>

<template>
  <article
    class="card"
    :class="{ clickable: goodsId }"
    :role="goodsId ? 'link' : undefined"
    :tabindex="goodsId ? 0 : undefined"
    @click="onCardClick"
    @keydown.enter.prevent="onCardClick"
    @keydown.space.prevent="onCardClick"
  >
    <div v-if="badge" class="badge">{{ badge }}</div>
    <div v-if="!displayImageUrl" class="img-wrap" :class="'tone-' + imageTone" role="img" :aria-label="title" />
    <img v-else class="img-wrap img-photo" :src="displayImageUrl" :alt="title" @error="onImageError" />
    <div class="body">
      <h3 class="title">{{ title }}</h3>
      <p class="price">
        <span class="currency">¥</span>{{ price }}
      </p>
      <button type="button" class="add" @click.stop>加入购物车</button>
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

.card.clickable {
  cursor: pointer;
}

.card.clickable:focus-visible {
  outline: 2px solid var(--mall-orange);
  outline-offset: 2px;
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

.img-photo {
  width: 100%;
  object-fit: cover;
  display: block;
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
