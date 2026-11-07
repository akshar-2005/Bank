<script>
  /**
   * @type {{
   *   type?: 'error' | 'success' | 'info',
   *   message: string,
   *   class?: string
   * }}
   */
  let { type = 'error', message, class: className = '' } = $props();
</script>

{#if message}
  <div
    class="flex items-start gap-3 p-4 rounded-xl text-sm transition-all {type === 'error'
      ? 'bg-rose-50 border border-rose-200 text-rose-800'
      : type === 'success'
        ? 'bg-emerald-50 border border-emerald-200 text-emerald-800'
        : 'bg-sky-50 border border-sky-200 text-sky-800'} {className}"
    role="alert"
  >
    {#if type === 'error'}
      <svg class="w-5 h-5 text-rose-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
    {:else if type === 'success'}
      <svg class="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    {:else}
      <svg class="w-5 h-5 text-sky-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    {/if}
    <div class="flex-1 font-medium leading-relaxed">{message}</div>
  </div>
{/if}

