<template>
  <svg viewBox="0 0 80 80" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" role="img" :aria-label="condition.label">
    <g v-if="['clear', 'partly'].includes(condition.kind)" :class="condition.daylight ? 'sun' : 'moon'">
      <template v-if="condition.daylight">
        <circle cx="35" cy="32" r="13" fill="currentColor" stroke="none" />
        <path d="M35 9v-4m0 54v-4M12 32H8m54 0h-4M19 16l-3-3m38 38-3-3M19 48l-3 3m38-38-3 3" />
      </template>
      <path v-else d="M49 44A23 23 0 0 1 28 11a23 23 0 1 0 21 33Z" fill="currentColor" stroke="none" />
    </g>
    <path v-if="!['clear', 'unknown'].includes(condition.kind)" d="M22 52a12 12 0 1 1 1-24 17 17 0 0 1 33 5 10 10 0 1 1 3 19Z" fill="#c6d5e5" stroke="#c6d5e5" />
    <path v-if="condition.kind === 'rain'" d="m25 61-3 7m18-7-3 7m18-7-3 7" stroke="#83c9ff" />
    <path v-if="condition.kind === 'snow'" d="M25 61v8m-4-4h8m16-4v8m-4-4h8m12-4v8m-4-4h8" stroke="#bdeaff" />
    <path v-if="condition.kind === 'fog'" d="M16 61h48M24 69h32" stroke="#b1c0cf" />
    <path v-if="condition.kind === 'storm'" d="m41 53-9 12h10l-5 12 17-19H43l4-5" fill="#f8cc70" stroke="none" />
    <text v-if="condition.kind === 'unknown'" x="40" y="52" text-anchor="middle" fill="currentColor" stroke="none" font-size="38">—</text>
  </svg>
</template>

<script setup>
defineProps({ condition: { type: Object, required: true } })
</script>

<style scoped>
.sun { color: #f8cc70; }
.moon { color: #c9c8ff; }
</style>
