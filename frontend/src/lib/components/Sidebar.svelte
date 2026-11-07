<script>
  import { goto } from '$app/navigation';
  import { clearSession } from '#lib/api.js';
  import StatusBadge from '#lib/components/StatusBadge.svelte';

  /**
   * @type {{
   *   user?: { email: string, role: string } | null,
   *   active?: string,
   *   onlogout?: (() => void) | null,
   *   children?: import('svelte').Snippet
   * }}
   */
  let { user = null, active = '', onlogout = null, children } = $props();

  let mobileOpen = $state(false);

  function handleLogout() {
    if (onlogout) {
      onlogout();
    } else {
      clearSession();
      goto('/login');
    }
  }

  function closeMobile() {
    mobileOpen = false;
  }
</script>

<div class="min-h-screen bg-slate-50 flex flex-col md:flex-row font-sans text-slate-900 antialiased">
  <!-- Desktop Sidebar -->
  <aside class="hidden md:flex flex-col justify-between w-64 bg-slate-900 text-slate-200 border-r border-slate-800 shrink-0 h-screen sticky top-0 z-30">
    <div class="p-6">
      <!-- Logo -->
      <a href="/dashboard" class="flex items-center gap-3 group">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-teal-500 to-emerald-400 flex items-center justify-center text-slate-950 font-black shadow-lg shadow-emerald-500/20 group-hover:scale-105 transition-transform">
          <svg class="w-6 h-6 text-slate-950" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 21v-8m0 0l-5-5m5 5l5-5M3 10h18M5 10v10a1 1 0 001 1h12a1 1 0 001-1V10M12 3L2 9h20L12 3z" />
          </svg>
        </div>
        <div>
          <span class="text-xl font-extrabold tracking-tight text-white block">My Bank</span>
          <span class="text-[10px] uppercase font-bold tracking-widest text-emerald-400 block -mt-0.5">Fintech Cloud</span>
        </div>
      </a>

      <!-- Navigation -->
      <nav class="mt-8 space-y-1.5" aria-label="Sidebar Navigation">
        <a
          href="/dashboard"
          class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all {active === 'dashboard'
            ? 'bg-slate-800 text-emerald-400 shadow-sm ring-1 ring-white/10'
            : 'text-slate-400 hover:text-white hover:bg-slate-800/60'}"
        >
          <svg class="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
          </svg>
          <span>Dashboard</span>
        </a>

        {#if user?.role === 'ADMIN'}
          <a
            href="/admin"
            class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all {active === 'admin'
              ? 'bg-slate-800 text-emerald-400 shadow-sm ring-1 ring-white/10'
              : 'text-slate-400 hover:text-white hover:bg-slate-800/60'}"
          >
            <svg class="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            <span>Admin</span>
          </a>
        {:else}
          <a
            href="/loans"
            class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all {active === 'loans'
              ? 'bg-slate-800 text-emerald-400 shadow-sm ring-1 ring-white/10'
              : 'text-slate-400 hover:text-white hover:bg-slate-800/60'}"
          >
            <svg class="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>Loans</span>
          </a>
        {/if}
      </nav>
    </div>

    <!-- User Footer -->
    <div class="p-4 border-t border-slate-800/80 bg-slate-950/40">
      {#if user}
        <div class="p-3 rounded-xl bg-slate-800/50 border border-slate-700/50">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 font-bold flex items-center justify-center shrink-0 uppercase text-sm">
              {user.email ? user.email[0] : 'U'}
            </div>
            <div class="min-w-0 flex-1">
              <p class="text-xs font-medium text-white truncate" title={user.email}>{user.email}</p>
              <div class="mt-1">
                <StatusBadge status={user.role} />
              </div>
            </div>
          </div>
        </div>
      {/if}

      <button
        onclick={handleLogout}
        class="mt-3 w-full flex items-center justify-center gap-2 px-3 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white hover:bg-rose-500/20 hover:border-rose-500/30 border border-slate-800 transition-all cursor-pointer"
      >
        <svg class="w-4 h-4 text-slate-400 group-hover:text-rose-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
        <span>Log out</span>
      </button>
    </div>
  </aside>

  <!-- Mobile Top Bar -->
  <header class="md:hidden sticky top-0 z-40 bg-slate-900 border-b border-slate-800 px-4 py-3 flex items-center justify-between text-white">
    <a href="/dashboard" class="flex items-center gap-2.5">
      <div class="w-8 h-8 rounded-lg bg-gradient-to-tr from-teal-500 to-emerald-400 flex items-center justify-center text-slate-950 font-black">
        <svg class="w-5 h-5 text-slate-950" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 21v-8m0 0l-5-5m5 5l5-5M3 10h18M5 10v10a1 1 0 001 1h12a1 1 0 001-1V10M12 3L2 9h20L12 3z" />
        </svg>
      </div>
      <span class="text-lg font-bold tracking-tight">My Bank</span>
    </a>

    <button
      onclick={() => (mobileOpen = !mobileOpen)}
      class="p-2 rounded-lg bg-slate-800 text-slate-300 hover:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
      aria-label="Toggle navigation menu"
    >
      {#if mobileOpen}
        <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      {:else}
        <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      {/if}
    </button>
  </header>

  <!-- Mobile Drawer Overlay -->
  {#if mobileOpen}
    <div
      class="fixed inset-0 bg-slate-950/70 backdrop-blur-sm z-50 md:hidden transition-opacity"
      onclick={closeMobile}
      role="presentation"
    ></div>

    <div class="fixed inset-y-0 left-0 w-72 bg-slate-900 border-r border-slate-800 z-50 p-6 flex flex-col justify-between md:hidden shadow-2xl">
      <div>
        <div class="flex items-center justify-between pb-6 border-b border-slate-800">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-tr from-teal-500 to-emerald-400 flex items-center justify-center text-slate-950 font-black">
              <svg class="w-5 h-5 text-slate-950" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 21v-8m0 0l-5-5m5 5l5-5M3 10h18M5 10v10a1 1 0 001 1h12a1 1 0 001-1V10M12 3L2 9h20L12 3z" />
              </svg>
            </div>
            <span class="text-lg font-bold text-white tracking-tight">My Bank</span>
          </div>
          <button onclick={closeMobile} class="p-1 rounded-lg text-slate-400 hover:text-white" aria-label="Close menu">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <nav class="mt-6 space-y-2">
          <a
            href="/dashboard"
            onclick={closeMobile}
            class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium {active === 'dashboard'
              ? 'bg-slate-800 text-emerald-400'
              : 'text-slate-400 hover:text-white hover:bg-slate-800/60'}"
          >
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
            </svg>
            <span>Dashboard</span>
          </a>

          {#if user?.role === 'ADMIN'}
            <a
              href="/admin"
              onclick={closeMobile}
              class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium {active === 'admin'
                ? 'bg-slate-800 text-emerald-400'
                : 'text-slate-400 hover:text-white hover:bg-slate-800/60'}"
            >
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
              <span>Admin</span>
            </a>
          {:else}
            <a
              href="/loans"
              onclick={closeMobile}
              class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium {active === 'loans'
                ? 'bg-slate-800 text-emerald-400'
                : 'text-slate-400 hover:text-white hover:bg-slate-800/60'}"
            >
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>Loans</span>
            </a>
          {/if}
        </nav>
      </div>

      <div class="pt-6 border-t border-slate-800">
        {#if user}
          <div class="mb-4">
            <p class="text-xs text-slate-400">Signed in as</p>
            <p class="text-sm font-semibold text-white truncate">{user.email}</p>
            <div class="mt-1">
              <StatusBadge status={user.role} />
            </div>
          </div>
        {/if}
        <button
          onclick={() => { closeMobile(); handleLogout(); }}
          class="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl text-sm font-semibold text-white bg-slate-800 hover:bg-rose-600 transition-colors cursor-pointer"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          <span>Log out</span>
        </button>
      </div>
    </div>
  {/if}

  <!-- Main Viewport -->
  <main class="flex-1 min-w-0 overflow-y-auto">
    <div class="max-w-6xl mx-auto p-4 sm:p-6 lg:p-8">
      {@render children?.()}
    </div>
  </main>
</div>

