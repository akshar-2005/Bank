<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { api, getUser, clearSession } from '#lib/api.js';

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
    if (!getUser()) {
      goto('/login');
      return;
    }
    load();
  });
</script>

<div class="min-h-screen bg-slate-100">
  <header class="bg-white shadow-sm">
    <div class="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between">
      <h1 class="text-xl font-bold text-blue-700">My Bank</h1>
      <a href="/dashboard" class="text-sm text-blue-600 hover:underline">← Back to dashboard</a>
    </div>
  </header>

  <main class="max-w-4xl mx-auto px-6 py-8 space-y-8">
    {#if error}
      <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm">{error}</p>
    {/if}

    <section class="bg-white rounded-xl shadow-md p-6 max-w-md">
      <h2 class="text-lg font-semibold text-gray-800 mb-3">Apply for a loan</h2>

      {#if formError}
        <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm mb-3">{formError}</p>
      {/if}
      {#if message}
        <p class="rounded bg-green-100 text-green-700 px-3 py-2 text-sm mb-3">{message}</p>
      {/if}

      {#if loading}
        <p class="text-gray-500">Loading...</p>
      {:else if accounts.length === 0}
        <p class="text-gray-500">You need an account first. Open one on the dashboard.</p>
      {:else}
        <form onsubmit={apply} class="space-y-4">
          <div>
            <label class="block text-sm text-gray-700 mb-1" for="account">Receive money in</label>
            <select
              id="account"
              bind:value={accountNumber}
              class="w-full border border-gray-300 rounded-lg px-3 py-2"
            >
              {#each accounts as a (a.id)}
                <option value={a.accountNumber}>{a.accountNumber} ({a.type})</option>
              {/each}
            </select>
          </div>

          <div>
            <label class="block text-sm text-gray-700 mb-1" for="principal">Loan amount (min 1000)</label>
            <input
              id="principal"
              type="number"
              min="1000"
              step="0.01"
              bind:value={principal}
              required
              class="w-full border border-gray-300 rounded-lg px-3 py-2"
            />
          </div>

          <div>
            <label class="block text-sm text-gray-700 mb-1" for="rate">Yearly interest rate (%)</label>
            <input
              id="rate"
              type="number"
              min="0.01"
              max="50"
              step="0.01"
              bind:value={rate}
              required
              class="w-full border border-gray-300 rounded-lg px-3 py-2"
            />
          </div>

          <div>
            <label class="block text-sm text-gray-700 mb-1" for="months">Tenure (months, 1 to 360)</label>
            <input
              id="months"
              type="number"
              min="1"
              max="360"
              step="1"
              bind:value={months}
              required
              class="w-full border border-gray-300 rounded-lg px-3 py-2"
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            class="w-full py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Submitting...' : 'Apply'}
          </button>
        </form>
      {/if}
    </section>

    <section>
      <h2 class="text-lg font-semibold text-gray-800 mb-3">Your loans</h2>

      {#if loading}
        <p class="text-gray-500">Loading...</p>
      {:else if loans.length === 0}
        <p class="text-gray-500">You have no loans yet.</p>
      {:else}
        <div class="grid gap-4 sm:grid-cols-2">
          {#each loans as loan (loan.id)}
            <div class="bg-white rounded-xl shadow-md p-5">
              <div class="flex items-center justify-between">
                <p class="text-2xl font-bold text-blue-700">{money(loan.principal)}</p>
                <span class="px-2 py-0.5 rounded-full text-xs font-semibold {badge(loan.status)}">
                  {loan.status}
                </span>
              </div>
              <p class="mt-2 text-sm text-gray-600">
                {loan.annualInterestRate}% for {loan.tenureMonths} months
              </p>
              <p class="text-sm text-gray-800">EMI: <span class="font-semibold">{money(loan.emi)}</span> per month</p>
              <p class="mt-2 text-xs text-gray-500 font-mono">Account {loan.accountNumber}</p>
              <p class="text-xs text-gray-500">Applied {formatDate(loan.createdAt)}</p>
              {#if loan.decidedAt}
                <p class="text-xs text-gray-500">Decided {formatDate(loan.decidedAt)}</p>
              {/if}
            </div>
          {/each}
        </div>
      {/if}
    </section>
  </main>
</div>