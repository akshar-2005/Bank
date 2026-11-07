<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { api, getUser, clearSession } from '#lib/api.js';
  import Sidebar from '#lib/components/Sidebar.svelte';
  import Spinner from '#lib/components/Spinner.svelte';
  import EmptyState from '#lib/components/EmptyState.svelte';
  import Alert from '#lib/components/Alert.svelte';

  /** @type {{ email: string, role: string } | null} */
  let user = $state(null);
  /** @type {any[]} */
  let accounts = $state([]);
  let loading = $state(true);
  let error = $state('');

  let type = $state('SAVINGS');
  let initialDeposit = $state(1000);
  let opening = $state(false);
  let formError = $state('');

  /** @param {number | string} value */
  function money(value) {
    return Number(value).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
  }

  async function loadAccounts() {
    error = '';
    try {
      accounts = await api('/api/accounts');
    } catch (e) {
      error = e instanceof Error ? e.message : 'Could not load accounts';
      if (error === 'Please log in again') {
        clearSession();
        goto('/login');
      }
    } finally {
      loading = false;
    }
  }

  /** @param {SubmitEvent} event */
  async function openAccount(event) {
    event.preventDefault();
    formError = '';
    opening = true;
    try {
      await api('/api/accounts', {
        method: 'POST',
        body: { type, initialDeposit: Number(initialDeposit) }
      });
      await loadAccounts();
    } catch (e) {
      formError = e instanceof Error ? e.message : 'Could not open account';
    } finally {
      opening = false;
    }
  }

  function logout() {
    clearSession();
    goto('/login');
  }

  onMount(() => {
    user = getUser();
    if (!user) {
      goto('/login');
      return;
    }
    if (user.role === 'ADMIN') {
      loading = false;
      return;
    }
    loadAccounts();
  });
</script>

<Sidebar {user} active="dashboard" onlogout={logout}>
  <div class="space-y-8">
    <!-- Welcome Header Line -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200/80">
      <div>
        <h1 class="text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900">
          Welcome back{#if user}, <span class="text-teal-700 font-bold">{user.email}</span>{/if}
        </h1>
        <p class="mt-1 text-sm text-slate-500">
          {user?.role === 'ADMIN'
            ? 'Administrative overview and loan approval queue.'
            : 'Here is your unified financial summary and account balances.'}
        </p>
      </div>

      {#if user?.role !== 'ADMIN'}
        <div>
          <a
            href="#open-account-section"
            class="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-semibold shadow-sm transition-all"
          >
            <svg class="w-4 h-4 text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            <span>Open New Account</span>
          </a>
        </div>
      {/if}
    </div>

    {#if user?.role === 'ADMIN'}
      <!-- Admin View: Welcome Card -->
      <section class="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-8 max-w-xl">
        <div class="w-12 h-12 rounded-xl bg-purple-50 text-purple-700 border border-purple-200 flex items-center justify-center mb-4">
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-slate-900">Admin Control Center</h2>
        <p class="mt-2 text-sm text-slate-600 leading-relaxed">
          Manage system operations and decide customer loan applications in real-time. Review incoming requests, calculated EMI, and disbursement approvals.
        </p>
        <div class="mt-6">
          <a
            href="/admin"
            class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-700 hover:to-emerald-700 text-white text-sm font-semibold shadow-md shadow-emerald-900/10 hover:shadow-lg transition-all"
          >
            <span>Review Pending Loans</span>
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M14 5l7 7m0 0l-7 7m7-7H3" />
            </svg>
          </a>
        </div>
      </section>
    {:else if user}
      <!-- Customer View -->
      {#if error}
        <Alert type="error" message={error} />
      {/if}

      <!-- Portfolio Summary Card -->
      <section class="bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 text-white rounded-2xl p-6 sm:p-8 shadow-xl shadow-slate-900/10 border border-slate-800 relative overflow-hidden">
        <div class="absolute -right-16 -top-16 w-64 h-64 bg-teal-500/10 rounded-full blur-2xl pointer-events-none"></div>

        <div class="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
              <p class="text-xs uppercase font-bold tracking-widest text-slate-300">Total Portfolio Balance</p>
            </div>
            <p class="mt-2 text-3xl sm:text-4xl font-extrabold tracking-tight text-white">
              {money(accounts.reduce((sum, a) => sum + Number(a.balance || 0), 0))}
            </p>
            <p class="mt-1 text-xs text-slate-400">
              Aggregated across {accounts.length} active {accounts.length === 1 ? 'account' : 'accounts'}
            </p>
          </div>

          <div class="flex items-center gap-3">
            <div class="px-4 py-2.5 rounded-xl bg-white/10 backdrop-blur-sm border border-white/10 text-xs">
              <span class="text-slate-400 block font-medium">Currency</span>
              <span class="text-white font-bold block">INR (₹)</span>
            </div>
            <div class="px-4 py-2.5 rounded-xl bg-white/10 backdrop-blur-sm border border-white/10 text-xs">
              <span class="text-slate-400 block font-medium">Status</span>
              <span class="text-emerald-400 font-bold block">Active & Verified</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Accounts Section -->
      <section class="space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-lg font-bold text-slate-900">Your Bank Accounts</h2>
            <p class="text-xs text-slate-500">Select any card to deposit, withdraw, or transfer funds.</p>
          </div>
          {#if !loading && accounts.length > 0}
            <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
              {accounts.length} {accounts.length === 1 ? 'Card' : 'Cards'}
            </span>
          {/if}
        </div>

        {#if loading}
          <!-- Skeleton Placeholders -->
          <div class="grid gap-6 sm:grid-cols-2">
            <div class="h-52 rounded-2xl bg-slate-200/80 animate-pulse"></div>
            <div class="h-52 rounded-2xl bg-slate-200/80 animate-pulse"></div>
          </div>
        {:else if accounts.length === 0}
          <EmptyState
            icon="accounts"
            title="No accounts yet"
            description="You don't have any open accounts yet. Use the form below to open your first Savings or Current account."
          />
        {:else}
          <!-- Debit Card Grid -->
          <div class="grid gap-6 sm:grid-cols-2">
            {#each accounts as account (account.id)}
              <a
                href={`/accounts/${account.accountNumber}`}
                class="group block relative rounded-2xl p-6 sm:p-7 text-white shadow-lg hover:shadow-2xl hover:-translate-y-1 transition-all duration-200 overflow-hidden cursor-pointer {account.type === 'SAVINGS'
                  ? 'bg-gradient-to-br from-slate-950 via-slate-900 to-teal-950 border border-slate-800'
                  : 'bg-gradient-to-br from-slate-950 via-indigo-950 to-slate-900 border border-slate-800'}"
              >
                <!-- Subtle card mesh glow -->
                <div class="absolute -right-8 -bottom-8 w-40 h-40 rounded-full blur-xl pointer-events-none {account.type === 'SAVINGS' ? 'bg-teal-500/15' : 'bg-indigo-500/15'}"></div>

                <!-- Top row: Chip, Contactless icon & Type Badge -->
                <div class="relative z-10 flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <!-- CSS Metallic Chip -->
                    <div class="w-11 h-8 rounded-md bg-gradient-to-tr from-amber-300 via-amber-200 to-amber-400 border border-amber-400/80 shadow-inner relative overflow-hidden flex items-center justify-center shrink-0">
                      <div class="w-full h-px bg-amber-700/30 absolute top-2.5"></div>
                      <div class="w-full h-px bg-amber-700/30 absolute bottom-2.5"></div>
                      <div class="h-full w-px bg-amber-700/30 absolute left-4"></div>
                      <div class="h-full w-px bg-amber-700/30 absolute right-4"></div>
                      <div class="w-3 h-3 rounded-full border border-amber-700/30"></div>
                    </div>
                    <!-- Contactless Icon -->
                    <svg class="w-5 h-5 text-white/40 rotate-90" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M8.111 16.404a5.5 5.5 0 017.778 0M12 20h.01m-7.08-7.071c3.904-3.905 10.236-3.905 14.14 0M1.393 9.393c5.857-5.857 15.355-5.857 21.213 0" />
                    </svg>
                  </div>

                  <span class="px-2.5 py-1 rounded-full text-[11px] font-bold tracking-wider uppercase bg-white/10 backdrop-blur-md border border-white/20 text-white">
                    {account.type}
                  </span>
                </div>

                <!-- Balance -->
                <div class="relative z-10 mt-6">
                  <p class="text-[11px] uppercase tracking-wider text-slate-400 font-semibold">Available Balance</p>
                  <p class="text-2xl sm:text-3xl font-extrabold tracking-tight text-white mt-0.5">
                    {money(account.balance)}
                  </p>
                </div>

                <!-- Bottom row: Masked Account Number & Action Arrow -->
                <div class="relative z-10 mt-6 pt-4 border-t border-white/10 flex items-end justify-between">
                  <div>
                    <p class="text-[10px] uppercase tracking-widest text-slate-400 font-medium">Account Number</p>
                    <div class="flex items-center gap-1.5 font-mono text-slate-300 mt-0.5">
                      <span class="tracking-widest text-sm text-slate-400">•••• ••••</span>
                      <span class="text-base font-bold text-white tracking-wider ml-1">{account.accountNumber.slice(-4)}</span>
                    </div>
                  </div>

                  <span class="inline-flex items-center gap-1 text-xs font-semibold text-emerald-400 group-hover:translate-x-1 transition-transform">
                    <span>Manage</span>
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                    </svg>
                  </span>
                </div>
              </a>
            {/each}
          </div>
        {/if}
      </section>

      <!-- Open New Account Section -->
      <section id="open-account-section" class="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-6 sm:p-8 max-w-xl">
        <div class="flex items-center gap-3 mb-5">
          <div class="w-10 h-10 rounded-xl bg-teal-50 border border-teal-200 text-teal-700 flex items-center justify-center shrink-0">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
            </svg>
          </div>
          <div>
            <h2 class="text-lg font-bold text-slate-900">Open a New Account</h2>
            <p class="text-xs text-slate-500">Create an additional Savings or Current account instantly.</p>
          </div>
        </div>

        {#if formError}
          <div class="mb-5">
            <Alert type="error" message={formError} />
          </div>
        {/if}

        <form onsubmit={openAccount} class="space-y-4">
          <div>
            <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="type">
              Account Type
            </label>
            <select
              id="type"
              bind:value={type}
              class="w-full rounded-xl border border-slate-300 bg-slate-50/50 px-4 py-2.5 text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all cursor-pointer"
            >
              <option value="SAVINGS">Savings Account</option>
              <option value="CURRENT">Current Account</option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="deposit">
              Initial Deposit (₹)
            </label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400 text-sm font-semibold">₹</span>
              <input
                id="deposit"
                type="number"
                min="0"
                step="0.01"
                bind:value={initialDeposit}
                required
                class="w-full rounded-xl border border-slate-300 bg-slate-50/50 pl-8 pr-4 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={opening}
            class="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-700 hover:to-emerald-700 text-white font-semibold text-sm shadow-md shadow-emerald-900/10 hover:shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer mt-2"
          >
            {#if opening}
              <Spinner size="sm" class="text-white" />
              <span>Opening...</span>
            {:else}
              <span>Open account</span>
            {/if}
          </button>
        </form>
      </section>
    {/if}
  </div>
</Sidebar>
