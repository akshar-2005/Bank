<script>
  import { goto } from '$app/navigation';
  import { api, saveSession } from '#lib/api.js';
  import Spinner from '#lib/components/Spinner.svelte';
  import Alert from '#lib/components/Alert.svelte';

  let email = $state('');
  let password = $state('');
  let error = $state('');
  let loading = $state(false);

  /** @param {SubmitEvent} event */
  async function handleSubmit(event) {
    event.preventDefault();
    error = '';
    loading = true;
    try {
      const data = await api('/api/auth/login', {
        method: 'POST',
        body: { email, password }
      });
      saveSession(data);
      goto('/dashboard');
    } catch (e) {
      error = e instanceof Error ? e.message : 'Login failed';
    } finally {
      loading = false;
    }
  }
</script>

<div class="min-h-screen flex bg-slate-50 font-sans antialiased text-slate-900">
  <!-- Left Side: Navy-to-Teal Brand Panel (hidden on mobile) -->
  <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-slate-950 via-slate-900 to-teal-950 text-white p-12 flex-col justify-between relative overflow-hidden">
    <!-- Ambient decorative glows -->
    <div class="absolute -top-24 -left-24 w-96 h-96 bg-teal-500/10 rounded-full blur-3xl pointer-events-none"></div>
    <div class="absolute -bottom-24 -right-24 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none"></div>

    <!-- Brand Header -->
    <div class="relative z-10 flex items-center gap-3">
      <div class="w-11 h-11 rounded-xl bg-gradient-to-tr from-teal-500 to-emerald-400 flex items-center justify-center text-slate-950 font-black shadow-lg shadow-emerald-500/20">
        <svg class="w-6 h-6 text-slate-950" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 21v-8m0 0l-5-5m5 5l5-5M3 10h18M5 10v10a1 1 0 001 1h12a1 1 0 001-1V10M12 3L2 9h20L12 3z" />
        </svg>
      </div>
      <div>
        <span class="text-2xl font-black tracking-tight block">My Bank</span>
        <span class="text-[11px] uppercase font-bold tracking-widest text-emerald-400 block -mt-0.5">Fintech Cloud</span>
      </div>
    </div>

    <!-- Center Hero Text & Features -->
    <div class="relative z-10 max-w-lg my-auto py-12">
      <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-teal-500/10 border border-teal-500/20 text-teal-300 text-xs font-semibold mb-6">
        <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
        <span>Secure Core Banking v2.0</span>
      </div>

      <h2 class="text-4xl font-extrabold tracking-tight text-white leading-tight">
        Next-generation digital banking made seamless.
      </h2>
      <p class="mt-4 text-base text-slate-300 leading-relaxed">
        Manage accounts, transfer funds with instant settlement, and apply for personal loans through one unified dashboard.
      </p>

      <!-- 3 Small Feature Points -->
      <div class="mt-10 space-y-5">
        <div class="flex items-start gap-4">
          <div class="w-10 h-10 rounded-xl bg-slate-800/80 border border-slate-700/60 flex items-center justify-center shrink-0 text-emerald-400">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <div>
            <h3 class="text-sm font-semibold text-white">Instant Money Transfers</h3>
            <p class="text-xs text-slate-400 mt-0.5 leading-relaxed">Atomic debit and credit transactions with automatic rollback protection.</p>
          </div>
        </div>

        <div class="flex items-start gap-4">
          <div class="w-10 h-10 rounded-xl bg-slate-800/80 border border-slate-700/60 flex items-center justify-center shrink-0 text-emerald-400">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <div>
            <h3 class="text-sm font-semibold text-white">Bank-Grade Protection</h3>
            <p class="text-xs text-slate-400 mt-0.5 leading-relaxed">Cryptographic token security, encrypted credentials, and role-based access.</p>
          </div>
        </div>

        <div class="flex items-start gap-4">
          <div class="w-10 h-10 rounded-xl bg-slate-800/80 border border-slate-700/60 flex items-center justify-center shrink-0 text-emerald-400">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <h3 class="text-sm font-semibold text-white">Smart Loan Approvals</h3>
            <p class="text-xs text-slate-400 mt-0.5 leading-relaxed">Transparent amortized EMI calculations and streamlined admin approvals.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Panel Footer -->
    <div class="relative z-10 text-xs text-slate-500">
      &copy; 2026 My Bank Inc. All rights reserved.
    </div>
  </div>

  <!-- Right Side: Clean Card Form (Always visible, full-width on mobile) -->
  <div class="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-10 lg:p-12">
    <div class="w-full max-w-md bg-white rounded-2xl shadow-xl shadow-slate-200/60 border border-slate-200/70 p-8 sm:p-10">
      <!-- Mobile brand header -->
      <div class="lg:hidden flex items-center gap-3 mb-8 pb-4 border-b border-slate-100">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-teal-500 to-emerald-400 flex items-center justify-center text-slate-950 font-black shadow-md shadow-emerald-500/20">
          <svg class="w-5 h-5 text-slate-950" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 21v-8m0 0l-5-5m5 5l5-5M3 10h18M5 10v10a1 1 0 001 1h12a1 1 0 001-1V10M12 3L2 9h20L12 3z" />
          </svg>
        </div>
        <div>
          <span class="text-xl font-black text-slate-900 tracking-tight block">My Bank</span>
          <span class="text-[10px] uppercase font-bold tracking-widest text-teal-600 block -mt-0.5">Fintech Cloud</span>
        </div>
      </div>

      <div class="mb-6">
        <h1 class="text-2xl font-bold tracking-tight text-slate-900">Log in to your account</h1>
        <p class="mt-1 text-sm text-slate-500">Welcome back! Please enter your details.</p>
      </div>

      {#if error}
        <div class="mb-5">
          <Alert type="error" message={error} />
        </div>
      {/if}

      <form onsubmit={handleSubmit} class="space-y-5">
        <div>
          <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="email">
            Email address
          </label>
          <input
            id="email"
            type="email"
            bind:value={email}
            required
            placeholder="name@example.com"
            class="w-full rounded-xl border border-slate-300 bg-slate-50/50 px-4 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="password">
            Password
          </label>
          <input
            id="password"
            type="password"
            bind:value={password}
            required
            placeholder="••••••••"
            class="w-full rounded-xl border border-slate-300 bg-slate-50/50 px-4 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          class="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-700 hover:to-emerald-700 text-white font-semibold shadow-md shadow-emerald-900/10 hover:shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer text-sm"
        >
          {#if loading}
            <Spinner size="sm" class="text-white" />
            <span>Logging in...</span>
          {:else}
            <span>Log in</span>
          {/if}
        </button>

        <p class="text-center text-sm text-slate-600 pt-2">
          No account yet?
          <a href="/register" class="font-semibold text-teal-600 hover:text-teal-700 hover:underline">
            Register
          </a>
        </p>
      </form>
    </div>
  </div>
</div>
