<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { page } from '$app/state';
  import { api, getUser, clearSession } from '#lib/api.js';

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
    if (!getUser()) {
      goto('/login');
      return;
    }
    loadAccount();
    loadHistory();
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
    {#if loadError}
      <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm">{loadError}</p>
    {:else if account}
      <section class="bg-white rounded-xl shadow-md p-6">
        <p class="text-sm text-gray-500">{account.type} account</p>
        <p class="mt-1 font-mono text-gray-800">{account.accountNumber}</p>
        <p class="mt-3 text-3xl font-bold text-blue-700">{money(account.balance)}</p>
      </section>

      <section class="bg-white rounded-xl shadow-md p-6 max-w-md">
        <div class="flex gap-2 mb-4">
          {#each modes as m (m.id)}
            <button
              type="button"
              onclick={() => chooseMode(m.id)}
              class="px-3 py-1.5 rounded-lg text-sm {mode === m.id
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}"
            >
              {m.label}
            </button>
          {/each}
        </div>

        {#if formError}
          <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm mb-3">{formError}</p>
        {/if}
        {#if message}
          <p class="rounded bg-green-100 text-green-700 px-3 py-2 text-sm mb-3">{message}</p>
        {/if}

        <form onsubmit={submit} class="space-y-4">
          {#if mode === 'transfer'}
            <div>
              <label class="block text-sm text-gray-700 mb-1" for="to">To account number</label>
              <input
                id="to"
                type="text"
                bind:value={toAccount}
                required
                class="w-full border border-gray-300 rounded-lg px-3 py-2"
              />
            </div>
          {/if}

          <div>
            <label class="block text-sm text-gray-700 mb-1" for="amount">Amount</label>
            <input
              id="amount"
              type="number"
              min="0.01"
              step="0.01"
              bind:value={amount}
              required
              class="w-full border border-gray-300 rounded-lg px-3 py-2"
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            class="w-full py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Please wait...' : 'Submit'}
          </button>
        </form>
      </section>

      <section class="bg-white rounded-xl shadow-md p-6">
        <h2 class="text-lg font-semibold text-gray-800 mb-3">Transaction history</h2>

        {#if historyError}
          <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm">{historyError}</p>
        {:else if transactions.length === 0}
          <p class="text-gray-500">No transactions yet.</p>
        {:else}
          <ul class="divide-y divide-gray-200">
            {#each transactions as t (t.id)}
              <li class="py-3 flex items-center justify-between gap-4">
                <div>
                  <p class="text-gray-800">{t.description}</p>
                  <p class="text-xs text-gray-500">
                    {formatDate(t.createdAt)}
                    {#if counterpart(t)}
                      · {counterpart(t)}
                    {/if}
                  </p>
                </div>
                <p class="font-semibold {isCredit(t) ? 'text-green-600' : 'text-red-600'}">
                  {isCredit(t) ? '+' : '-'}{money(t.amount)}
                </p>
              </li>
            {/each}
          </ul>

          <div class="mt-4 flex items-center justify-between">
            <button
              type="button"
              onclick={() => goToPage(pageNumber - 1)}
              disabled={pageNumber === 0}
              class="px-3 py-1.5 rounded-lg bg-gray-200 text-gray-700 text-sm hover:bg-gray-300 disabled:opacity-50"
            >
              Previous
            </button>
            <p class="text-sm text-gray-600">
              Page {pageNumber + 1} of {totalPages} ({totalElements} transactions)
            </p>
            <button
              type="button"
              onclick={() => goToPage(pageNumber + 1)}
              disabled={pageNumber + 1 >= totalPages}
              class="px-3 py-1.5 rounded-lg bg-gray-200 text-gray-700 text-sm hover:bg-gray-300 disabled:opacity-50"
            >
              Next
            </button>
          </div>
        {/if}
      </section>
    {:else}
      <p class="text-gray-500">Loading...</p>
    {/if}
  </main>
</div>