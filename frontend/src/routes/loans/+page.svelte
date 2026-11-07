<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { api, getUser, clearSession } from '#lib/api.js';
  import Sidebar from '#lib/components/Sidebar.svelte';
  import StatusBadge from '#lib/components/StatusBadge.svelte';
  import Spinner from '#lib/components/Spinner.svelte';
  import EmptyState from '#lib/components/EmptyState.svelte';
  import Alert from '#lib/components/Alert.svelte';

  /** @type {any[]} */
  let accounts = $state([]);
  /** @type {any[]} */
  let loans = $state([]);
  let loading = $state(true);
  let error = $state('');

  let accountNumber = $state('');
  let principal = $state(100000);
  let rate = $state(12);
  let months = $state(12);
  let submitting = $state(false);
  let formError = $state('');
  let message = $state('');

  /** @type {{ email: string, role: string } | null} */
  let user = $state(null);

  /** @param {number | string} value */
  function money(value) {
    return Number(value).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
  }

  /** @param {string} value */
  function formatDate(value) {
    return new Date(value).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
  }

  /** @param {string} status */
  function badge(status) {
    if (status === 'APPROVED') return 'bg-green-100 text-green-700';
    if (status === 'REJECTED') return 'bg-red-100 text-red-700';
    return 'bg-yellow-100 text-yellow-700';
  }

  async function load() {
    error = '';
    try {
      accounts = await api('/api/accounts');
      loans = await api('/api/loans/mine');
      if (!accountNumber && accounts.length > 0) {
        accountNumber = accounts[0].accountNumber;
      }
    } catch (e) {
      error = e instanceof Error ? e.message : 'Could not load loans';
      if (error === 'Please log in again') {
        clearSession();
        goto('/login');
      }
    } finally {
      loading = false;
    }
  }

  /** @param {SubmitEvent} event */
  async function apply(event) {
    event.preventDefault();
    formError = '';
    message = '';
    submitting = true;
    try {
      await api('/api/loans', {
        method: 'POST',
        body: {
          accountNumber,
          principal: Number(principal),
          annualInterestRate: Number(rate),
          tenureMonths: Number(months)
        }
      });
      message = 'Loan application submitted. It is now pending approval.';
      await load();
    } catch (e) {
      formError = e instanceof Error ? e.message : 'Could not submit application';
    } finally {
      submitting = false;
    }
  }

  onMount(() => {
    user = getUser();
    if (!user) {
      goto('/login');
      return;
    }
    load();
  });
</script>

<Sidebar {user} active="loans">
  <div class="space-y-8">
    <!-- Header Line -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200/80">
      <div>
        <h1 class="text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900">
          Personal Loans & Financing
        </h1>
        <p class="mt-1 text-sm text-slate-500">
          Apply for personal loans with competitive interest rates and transparent monthly EMI terms.
        </p>
      </div>

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
    </div>

    {#if error}
      <Alert type="error" message={error} />
    {/if}

    <!-- Apply for a Loan Form Card -->
    <section class="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-6 sm:p-8 max-w-xl">
      <div class="flex items-center gap-3 mb-5">
        <div class="w-10 h-10 rounded-xl bg-teal-50 border border-teal-200 text-teal-700 flex items-center justify-center shrink-0">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <h2 class="text-lg font-bold text-slate-900">Apply for a Loan</h2>
          <p class="text-xs text-slate-500">Funds will be credited directly to your account upon approval.</p>
        </div>
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

      {#if loading}
        <div class="h-64 rounded-xl bg-slate-200/70 animate-pulse"></div>
      {:else if accounts.length === 0}
        <div class="p-6 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-sm">
          <p class="font-semibold">Account required</p>
          <p class="mt-1 text-xs text-amber-700">You need an active bank account before applying for a loan.</p>
          <a
            href="/dashboard"
            class="inline-block mt-3 px-4 py-2 rounded-xl bg-amber-600 text-white text-xs font-semibold hover:bg-amber-700 transition-colors"
          >
            Open an account on the dashboard →
          </a>
        </div>
      {:else}
        <form onsubmit={apply} class="space-y-4">
          <div>
            <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="account">
              Disbursement Account
            </label>
            <select
              id="account"
              bind:value={accountNumber}
              class="w-full rounded-xl border border-slate-300 bg-slate-50/50 px-4 py-2.5 text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all cursor-pointer font-mono"
            >
              {#each accounts as a (a.id)}
                <option value={a.accountNumber}>{a.accountNumber} ({a.type})</option>
              {/each}
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="principal">
              Loan Amount (min ₹1,000)
            </label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400 text-sm font-semibold">₹</span>
              <input
                id="principal"
                type="number"
                min="1000"
                step="0.01"
                bind:value={principal}
                required
                class="w-full rounded-xl border border-slate-300 bg-slate-50/50 pl-8 pr-4 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all font-semibold"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="rate">
                Annual Interest (%)
              </label>
              <div class="relative">
                <input
                  id="rate"
                  type="number"
                  min="0.01"
                  max="50"
                  step="0.01"
                  bind:value={rate}
                  required
                  class="w-full rounded-xl border border-slate-300 bg-slate-50/50 px-4 py-2.5 text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
                />
                <span class="absolute inset-y-0 right-0 pr-3.5 flex items-center pointer-events-none text-slate-400 text-sm font-semibold">%</span>
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5" for="months">
                Tenure (Months)
              </label>
              <input
                id="months"
                type="number"
                min="1"
                max="360"
                step="1"
                bind:value={months}
                required
                class="w-full rounded-xl border border-slate-300 bg-slate-50/50 px-4 py-2.5 text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
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
              <span>Submitting...</span>
            {:else}
              <span>Apply</span>
            {/if}
          </button>
        </form>
      {/if}
    </section>

    <!-- Loans History Section -->
    <section class="space-y-4">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-bold text-slate-900">Your Loans</h2>
          <p class="text-xs text-slate-500">Track application status, monthly installments, and approval records.</p>
        </div>
        {#if !loading && loans.length > 0}
          <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
            {loans.length} {loans.length === 1 ? 'Application' : 'Applications'}
          </span>
        {/if}
      </div>

      {#if loading}
        <div class="grid gap-6 sm:grid-cols-2">
          <div class="h-48 rounded-2xl bg-slate-200/80 animate-pulse"></div>
          <div class="h-48 rounded-2xl bg-slate-200/80 animate-pulse"></div>
        </div>
      {:else if loans.length === 0}
        <EmptyState
          icon="loans"
          title="No loans yet"
          description="You haven't submitted any loan applications yet. Fill in the form above to apply."
        />
      {:else}
        <div class="grid gap-6 sm:grid-cols-2">
          {#each loans as loan (loan.id)}
            <div class="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-6 flex flex-col justify-between hover:shadow-md transition-shadow">
              <div>
                <!-- Top Row: Principal Amount & Status Badge -->
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <span class="text-[10px] uppercase font-bold tracking-widest text-slate-400 block">Principal Amount</span>
                    <p class="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-0.5">
                      {money(loan.principal)}
                    </p>
                  </div>
                  <StatusBadge status={loan.status} />
                </div>

                <!-- EMI Highlight Card -->
                <div class="mt-4 p-3.5 rounded-xl bg-teal-50/70 border border-teal-200/60 flex items-center justify-between">
                  <div>
                    <p class="text-[11px] uppercase font-bold tracking-wider text-teal-800">Monthly EMI</p>
                    <p class="text-xs text-teal-600">Calculated monthly installment</p>
                  </div>
                  <p class="text-lg font-black text-teal-900">
                    {money(loan.emi)} <span class="text-xs font-normal text-teal-700">/ mo</span>
                  </p>
                </div>
              </div>

              <!-- Thin Line of Details Underneath -->
              <div class="mt-5 pt-4 border-t border-slate-100 text-xs text-slate-500 space-y-1.5">
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">Terms:</span>
                  <span class="font-medium text-slate-700">{loan.annualInterestRate}% APR · {loan.tenureMonths} months</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">Target Account:</span>
                  <span class="font-mono text-slate-700 font-semibold">{loan.accountNumber}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">Applied:</span>
                  <span class="text-slate-600">{formatDate(loan.createdAt)}</span>
                </div>
                {#if loan.decidedAt}
                  <div class="flex items-center justify-between pt-1 border-t border-slate-100">
                    <span class="text-slate-400">Decided:</span>
                    <span class="font-medium {loan.status === 'APPROVED' ? 'text-emerald-700' : 'text-rose-700'}">
                      {formatDate(loan.decidedAt)}
                    </span>
                  </div>
                {/if}
              </div>
            </div>
          {/each}
        </div>
      {/if}
    </section>
  </div>
</Sidebar>
