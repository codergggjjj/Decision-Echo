<template>
  <main class="error-page">
    <section class="error-card" :class="`tone-${tone}`">
      <div class="error-illustration" aria-hidden="true">
        <span>{{ code }}</span>
        <i></i>
      </div>
      <div class="error-copy">
        <span>{{ kicker }}</span>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>
      <div class="error-actions">
        <button type="button" class="error-primary" @click="goHome">返回首页</button>
        <button v-if="showRetry" type="button" class="error-secondary" @click="retry">重新尝试</button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  code: {
    type: String,
    required: true
  },
  kicker: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  tone: {
    type: String,
    default: 'blue'
  },
  showRetry: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()

function goHome() {
  router.push('/dashboard')
}

function retry() {
  window.location.reload()
}
</script>

<style scoped>
.error-page {
  display: grid;
  min-height: 100vh;
  overflow-x: hidden;
  place-items: center;
  padding: 28px clamp(16px, 5vw, 56px);
  background:
    radial-gradient(circle at 16% 12%, rgba(0, 174, 236, 0.12), transparent 28%),
    radial-gradient(circle at 86% 18%, rgba(251, 114, 153, 0.12), transparent 24%),
    linear-gradient(135deg, #f7fbff 0%, #fff8fb 54%, #f2fff9 100%);
  color: #11161c;
}

.error-card {
  position: relative;
  display: grid;
  width: min(760px, 100%);
  gap: 22px;
  justify-items: center;
  overflow: hidden;
  padding: clamp(28px, 6vw, 56px);
  border: 1px solid rgba(0, 174, 236, 0.12);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 24px 56px rgba(23, 37, 63, 0.1);
  text-align: center;
}

.error-card::before {
  position: absolute;
  inset: 18px;
  border: 1px dashed rgba(0, 174, 236, 0.18);
  border-radius: 18px;
  content: "";
  pointer-events: none;
}

.error-illustration {
  position: relative;
  display: grid;
  width: min(220px, 58vw);
  aspect-ratio: 1;
  place-items: center;
  border-radius: 50%;
  background: #e8f8ff;
}

.tone-pink .error-illustration {
  background: #fff0f6;
}

.tone-yellow .error-illustration {
  background: #fff8df;
}

.error-illustration span {
  position: relative;
  z-index: 1;
  color: #008fc6;
  font-size: clamp(52px, 14vw, 92px);
  font-weight: 900;
  line-height: 1;
}

.tone-pink .error-illustration span {
  color: #d34a78;
}

.tone-yellow .error-illustration span {
  color: #a87300;
}

.error-illustration i {
  position: absolute;
  right: 22%;
  bottom: 20%;
  width: 42%;
  aspect-ratio: 1.35;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.62);
  transform: rotate(-12deg);
}

.error-copy {
  position: relative;
  z-index: 1;
}

.error-copy span {
  color: #fb7299;
  font-size: 13px;
  font-weight: 900;
}

.error-copy h1 {
  margin: 10px 0 0;
  font-size: clamp(30px, 6vw, 52px);
  line-height: 1.08;
  letter-spacing: 0;
}

.error-copy p {
  max-width: 520px;
  margin: 12px auto 0;
  color: #52606d;
  font-size: 16px;
  line-height: 1.7;
}

.error-actions {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.error-actions button {
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  cursor: pointer;
  font: inherit;
  font-weight: 900;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.error-primary {
  border: 1px solid rgba(0, 174, 236, 0.22);
  background: #dff5ff;
  color: #008fc6;
  box-shadow: 0 8px 18px rgba(0, 174, 236, 0.12);
}

.error-secondary {
  border: 1px solid rgba(251, 114, 153, 0.24);
  background: #fff0f6;
  color: #d34a78;
  box-shadow: 0 8px 18px rgba(251, 114, 153, 0.1);
}

.error-actions button:hover,
.error-actions button:focus-visible {
  outline: none;
  transform: translateY(-1px);
}

@media (max-width: 560px) {
  .error-page {
    padding: 18px 12px;
  }

  .error-card {
    gap: 18px;
    padding: 26px 18px;
  }

  .error-actions {
    width: 100%;
  }

  .error-actions button {
    width: 100%;
  }
}
</style>
