<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { page } from '$app/state';
  import { api, getUser, clearSession } from '#lib/api.js';
  import Sidebar from '#lib/components/Sidebar.svelte';
  import StatusBadge from '#lib/components/StatusBadge.svelte';
  import Spinner from '#lib/components/Spinner.svelte';
  import EmptyState from '#lib/components/EmptyState.svelte';
  import Alert from '#lib/components/Alert.svelte';

  /** @type {string} */
  const accountNumber = page.params.accountNumber ?? '';
  const pageSize = 5;

  const modes = [
    { id: 'deposit', label: 'Deposit' },
    { id: 'withdraw', label: 'Withdraw' },
    { id: 'transfer', label: 'Transfer' }
  ];

  /** @type {any} */
  let account = $state(null);
  let loadError = $state('');
  let mode = $state('deposit');
  let amount = $state('');
  let toAccount = $state('');
  let submitting = $state(false);
  let formError = $state('');
  let message = $state('');

  /** @type {any[]} */
  let transactions = $state([]);
  let pageNumber = $state(0);
  let totalPages = $state(0);
  let totalElements = $state(0);
  let historyError = $state('');

  /** @type {{ email: string, role: string } | null} */
  let user = $state(null);

  /** @param {number | string} value */
  function money(value) {
    return Number(value).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
  }

  /** @param {unknown} e @param {string} fallback */
  function toMessage(e, fallback) {
    return e instanceof Error ? e.message : fallback;
  }

  /** @param {any} t */
  function isCredit(t) {
    return t.toAccountNumber === accountNumber;
  }

  /** @param {any} t */
  function counterpart(t) {
    if (t.type !== 'TRANSFER') return '';
    return isCredit(t) ? `From ${t.fromAccountNumber}` : `To ${t.toAccountNumber}`;
  }

  /** @param {string} value */
  function formatDate(value) {
    return new Date(value).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
  }

  async function loadAccount() {
    loadError = '';
    try {
      account = await api(`/api/accounts/${accountNumber}`);
    } catch (e) {
      loadError = toMessage(e, 'Could not load account');
      if (loadError === 'Please log in again') {
        clearSession();
        goto('/login');
      }
    }
  }

  async function loadHistory() {
    historyError = '';
    try {
      const data = await api(
        `/api/accounts/${accountNumber}/transactions?page=${pageNumber}&size=${pageSize}`
      );
      transactions = data.content;
      totalPages = data.totalPages;
      totalElements = data.totalElements;
    } catch (e) {
      historyError = toMessage(e, 'Could not load history');
    }
  }

  /** @param {number} n */
  function goToPage(n) {
    pageNumber = n;
    loadHistory();
  }

  /** @param {SubmitEvent} event */
  async function submit(event) {
    event.preventDefault();
    formError = '';
    message = '';
    submitting = true;
    try {
      const value = Number(amount);
      if (mode === 'transfer') {
        await api('/api/transfers', {
          method: 'POST',
          body: {
            fromAccountNumber: accountNumber,
            toAccountNumber: toAccount.trim(),
            amount: value
          }
        });
        message = 'Transfer successful';
      } else {
        await api(`/api/accounts/${accountNumber}/${mode}`, {
          method: 'POST',
          body: { amount: value }
        });
        message = mode === 'deposit' ? 'Deposit successful' : 'Withdrawal successful';
      }
      amount = '';
      toAccount = '';
      pageNumber = 0;
      await loadAccount();
      await loadHistory();
    } catch (e) {
      formError = toMessage(e, 'Something went wrong');
    } finally {
      submitting = false;
    }
  }

  /** @param {string} id */
  function chooseMode(id) {
    mode = id;
    formError = '';
    message = '';
  }

  onMount(() => {
    user = getUser();
    if (!user) {
      goto('/login');
      return;
    }
    loadAccount();
    loadHistory();
  });
</script>

<Sidebar {user} active="dashboard">
  <div class="space-y-8">
    <!-- Top Bar Navigation -->
    <div>
      <a
        href="/dashboard"
        class="inline-flex items-center gap-2 text-xs font-semibold text-slate-500 hover:text-teal-700 transition-colors"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span>Back to Dashboard</span>
      </a>
    </div>

    {#if loadError}
      <Alert type="error" message={loadError} />
    {:else if account}
      <!-- Large Balance Header Card -->
      <section class="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-6 sm:p-8 relative overflow-hidden">
        <!-- Top accent gradient ribbon -->
        <div class="h-1.5 bg-gradient-to-r from-teal-500 via-emerald-400 to-teal-600 absolute top-0 left-0 right-0"></div>

        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div class="flex items-center gap-2.5">
              <StatusBadge status={account.type} />
              <span class="text-xs font-semibold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                Active
              </span>
            </div>
            <div class="mt-2 flex items-center gap-2 font-mono text-xs sm:text-sm text-slate-500">
              <span>Account Number:</span>
              <span class="font-bold text-slate-800 tracking-wider text-base">{account.accountNumber}</span>
            </div>
          </div>

          <div class="sm:text-right">
            <p class="text-xs uppercase font-semibold tracking-wider text-slate-400">Total Available Balance</p>
            <p class="mt-1 text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight text-slate-900">
              {money(account.balance)}
            </p>
          </div>
        </div>
      </section>

      <!-- Operation Form (Segmented Tab Control) -->
      <section class="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-6 sm:p-8 max-w-xl">
        <h2 class="text-lg font-bold text-slate-900 mb-1">Account Actions</h2>
        <p class="text-xs text-slate-500 mb-5">Select an operation to deposit funds, withdraw, or transfer money.</p>

        <!-- Segmented Tab Control -->
        <div class="grid grid-cols-3 p-1 bg-slate-100 rounded-xl mb-6 gap-1" role="tablist">
          {#each modes as m (m.id)}
            <button
              type="button"
              role="tab"
              aria-selected={mode === m.id}
              onclick={() => chooseMode(m.id)}
              class="flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-xs font-semibold transition-all cursor-pointer {mode === m.id
                ? 'bg-white text-slate-900 shadow-sm ring-1 ring-slate-900/5'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50'}"
            >
              {#if m.id === 'deposit'}
                <svg class="w-4 h-4 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                </svg>
              {:else if m.id === 'withdraw'}
                <svg class="w-4 h-4 text-rose-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M5 10l7-7m0 0l7 7m-7-7v18" />
                </svg>
              {:else}
                <svg class="w-4 h-4 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                </svg>
              {/if}
              <span>{m.label}</span>
            </button>
          {/each}
        </div>

        {#if formError}
          <div class="mb-4">
            <Alert type="error" message={formError} />
          </div>
        {/if}
        {#if message}
          <div class="mb-4">
            <Alert type="success" message={message} />
          </div>
        {/if}

        <form onsubmit={submit} class="space-y-4">
          {#if mode === 'transfer'}
            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="to">
                Recipient Account Number
              </label>
              <input
                id="to"
                type="text"
                bind:value={toAccount}
                required
                placeholder="Enter 10-digit account number"
                class="w-full rounded-xl border border-slate-300 bg-slate-50/50 px-4 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all font-mono"
              />
            </div>
          {/if}

          <div>
            <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="amount">
              Amount (₹)
            </label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400 text-sm font-semibold">₹</span>
              <input
                id="amount"
                type="number"
                min="0.01"
                step="0.01"
                bind:value={amount}
                required
                placeholder="0.00"
                class="w-full rounded-xl border border-slate-300 bg-slate-50/50 pl-8 pr-4 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            class="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-700 hover:to-emerald-700 text-white font-semibold text-sm shadow-md shadow-emerald-900/10 hover:shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer mt-2"
          >
            {#if submitting}
              <Spinner size="sm" class="text-white" />
              <span>Please wait...</span>
            {:else}
              <span>Submit</span>
            {/if}
          </button>
        </form>
      </section>

      <!-- Transaction History Section -->
      <section class="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-6 sm:p-8">
        <div class="flex items-center justify-between mb-5 pb-3 border-b border-slate-100">
          <div>
            <h2 class="text-lg font-bold text-slate-900">Transaction History</h2>
            <p class="text-xs text-slate-500">Real-time ledger of debits, credits, and atomic transfers.</p>
          </div>
          {#if totalElements > 0}
            <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
              {totalElements} {totalElements === 1 ? 'Record' : 'Records'}
            </span>
          {/if}
        </div>

        {#if historyError}
          <div class="mb-4">
            <Alert type="error" message={historyError} />
          </div>
        {:else if transactions.length === 0}
          <EmptyState
            icon="transactions"
            title="No transactions yet"
            description="Your transaction records will appear here as soon as you deposit, withdraw, or transfer money."
          />
        {:else}
          <ul class="divide-y divide-slate-100">
            {#each transactions as t (t.id)}
              <li class="py-4 flex items-center justify-between gap-4 hover:bg-slate-50/60 px-3 rounded-xl transition-colors">
                <div class="flex items-center gap-3.5 min-w-0">
                  <!-- Round Icon -->
                  <div
                    class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 border {isCredit(t)
                      ? 'bg-emerald-50 text-emerald-600 border-emerald-200/80'
                      : 'bg-rose-50 text-rose-600 border-rose-200/80'}"
                  >
                    {#if isCredit(t)}
                      <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                      </svg>
                    {:else}
                      <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M5 10l7-7m0 0l7 7m-7-7v18" />
                      </svg>
                    {/if}
                  </div>

                  <div class="min-w-0">
                    <p class="text-sm font-semibold text-slate-900 truncate">{t.description}</p>
                    <p class="text-xs text-slate-500 mt-0.5">
                      {formatDate(t.createdAt)}
                      {#if counterpart(t)}
                        <span class="text-slate-300 mx-1">·</span>
                        <span class="font-mono text-slate-600 font-medium">{counterpart(t)}</span>
                      {/if}
                    </p>
                  </div>
                </div>

                <div class="text-right shrink-0">
                  <p class="text-sm sm:text-base font-bold {isCredit(t) ? 'text-emerald-600' : 'text-rose-600'}">
                    {isCredit(t) ? '+' : '-'}{money(t.amount)}
                  </p>
                  <p class="text-[10px] uppercase font-semibold text-slate-400 tracking-wider">
                    {t.type}
                  </p>
                </div>
              </li>
            {/each}
          </ul>

          <!-- Pagination Bar -->
          <div class="mt-6 pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-4">
            <button
              type="button"
              onclick={() => goToPage(pageNumber - 1)}
              disabled={pageNumber === 0}
              class="w-full sm:w-auto inline-flex items-center justify-center gap-1.5 px-4 py-2 rounded-xl border border-slate-200 bg-white text-slate-700 text-xs font-semibold hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed shadow-sm transition-all"
            >
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
              </svg>
              <span>Previous</span>
            </button>

            <p class="text-xs font-medium text-slate-600 text-center">
              Page <span class="font-bold text-slate-900">{pageNumber + 1}</span> of <span class="font-bold text-slate-900">{totalPages || 1}</span>
              <span class="text-slate-400 mx-1">·</span>
              ({totalElements} transactions)
            </p>

            <button
              type="button"
              onclick={() => goToPage(pageNumber + 1)}
              disabled={pageNumber + 1 >= totalPages}
              class="w-full sm:w-auto inline-flex items-center justify-center gap-1.5 px-4 py-2 rounded-xl border border-slate-200 bg-white text-slate-700 text-xs font-semibold hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed shadow-sm transition-all"
            >
              <span>Next</span>
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        {/if}
      </section>
    {:else}
      <!-- Loading Skeleton -->
      <div class="space-y-6">
        <div class="h-32 rounded-2xl bg-slate-200/80 animate-pulse"></div>
        <div class="h-64 rounded-2xl bg-slate-200/80 animate-pulse max-w-xl"></div>
        <div class="h-72 rounded-2xl bg-slate-200/80 animate-pulse"></div>
      </div>
    {/if}
  </div>
</Sidebar>
