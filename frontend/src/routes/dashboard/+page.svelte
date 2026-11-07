<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { api, getUser, clearSession } from '#lib/api.js';

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

<div class="min-h-screen bg-slate-100">
  <header class="bg-white shadow-sm">
    <div class="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between">
      <h1 class="text-xl font-bold text-blue-700">My Bank</h1>
      <div class="flex items-center gap-4">
        {#if user?.role === 'ADMIN'}
          <a href="/admin" class="text-sm text-blue-600 hover:underline">Admin</a>
        {:else}
          <a href="/loans" class="text-sm text-blue-600 hover:underline">Loans</a>
        {/if}
        {#if user}
          <span class="text-sm text-gray-600">{user.email} ({user.role})</span>
        {/if}
        <button
          onclick={logout}
          class="px-3 py-1.5 rounded-lg bg-gray-800 text-white text-sm hover:bg-gray-900"
        >
          Log out
        </button>
      </div>
    </div>
  </header>

  <main class="max-w-4xl mx-auto px-6 py-8 space-y-8">
    {#if user?.role === 'ADMIN'}
      <section class="bg-white rounded-xl shadow-md p-6 max-w-md">
        <h2 class="text-lg font-semibold text-gray-800">Admin dashboard</h2>
        <p class="mt-2 text-gray-600">Review and decide customer loan applications.</p>
        <a
          href="/admin"
          class="mt-4 inline-block px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700"
        >
          Pending loans
        </a>
      </section>
    {:else if user}
      <section>
        <h2 class="text-lg font-semibold text-gray-800 mb-3">Your accounts</h2>

        {#if error}
          <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm">{error}</p>
        {/if}

        {#if loading}
          <p class="text-gray-500">Loading...</p>
        {:else if accounts.length === 0}
          <p class="text-gray-500">You have no accounts yet. Open one below.</p>
        {:else}
          <div class="grid gap-4 sm:grid-cols-2">
            {#each accounts as account (account.id)}
              <a
                href={`/accounts/${account.accountNumber}`}
                class="block bg-white rounded-xl shadow-md p-5 hover:shadow-lg"
              >
                <p class="text-sm text-gray-500">{account.type}</p>
                <p class="mt-1 font-mono text-gray-800">{account.accountNumber}</p>
                <p class="mt-3 text-2xl font-bold text-blue-700">{money(account.balance)}</p>
                <p class="mt-2 text-sm text-blue-600">Open →</p>
              </a>
            {/each}
          </div>
        {/if}
      </section>

      <section class="bg-white rounded-xl shadow-md p-6 max-w-md">
        <h2 class="text-lg font-semibold text-gray-800 mb-3">Open a new account</h2>

        {#if formError}
          <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm mb-3">{formError}</p>
        {/if}

        <form onsubmit={openAccount} class="space-y-4">
          <div>
            <label class="block text-sm text-gray-700 mb-1" for="type">Account type</label>
            <select
              id="type"
              bind:value={type}
              class="w-full border border-gray-300 rounded-lg px-3 py-2"
            >
              <option value="SAVINGS">Savings</option>
              <option value="CURRENT">Current</option>
            </select>
          </div>

          <div>
            <label class="block text-sm text-gray-700 mb-1" for="deposit">Initial deposit</label>
            <input
              id="deposit"
              type="number"
              min="0"
              step="0.01"
              bind:value={initialDeposit}
              required
              class="w-full border border-gray-300 rounded-lg px-3 py-2"
            />
          </div>

          <button
            type="submit"
            disabled={opening}
            class="w-full py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {opening ? 'Opening...' : 'Open account'}
          </button>
        </form>
      </section>
    {/if}
  </main>
</div>