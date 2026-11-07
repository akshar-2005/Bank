<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { getUser } from '#lib/api.js';

  onMount(() => {
    goto(getUser() ? '/dashboard' : '/login');
  });
</script>

<p class="p-8 text-gray-500">Loading...</p>