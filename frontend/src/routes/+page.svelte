<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { getUser } from '#lib/api.js';

  onMount(() => {
    goto(getUser() ? '/dashboard' : '/login');
  });
</script>

<div class="min-h-screen bg-slate-50 flex items-center justify-center">
  <div class="flex flex-col items-center gap-3">
    <div class="w-8 h-8 rounded-full border-2 border-emerald-500 border-t-transparent animate-spin"></div>
    <p class="text-sm font-medium text-slate-400">Loading...</p>
  </div>
</div>
