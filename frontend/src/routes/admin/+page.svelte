<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { api, getUser, clearSession } from '#lib/api.js';

  /** @type {any[]} */
  let loans = $state([]);
  let loading = $state(true);
  let error = $state('');
  let message = $state('');
  /** @type {number | null} */
  let busyId = $state(null);

  /** @param {number | string} value */
  function money(value) {
    return Number(value).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
  }

  /** @param {string} value */
  function formatDate(value) {
    return new Date(value).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
  }

  async function load() {
    try {
      loans = await api('/api/admin/loans/pending');
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

  /**
   * @param {number} id
   * @param {'approve' | 'reject'} action
   */
  async function decide(id, action) {
    error = '';
    message = '';
    busyId = id;
    try {
      await api(`/api/admin/loans/${id}/${action}`, { method: 'POST' });
      message = action === 'approve' ? `Loan #${id} approved` : `Loan #${id} rejected`;
      await load();
    } catch (e) {
      error = e instanceof Error ? e.message : 'Action failed';
    } finally {
      busyId = null;
    }
  }

  function logout() {
    clearSession();
    goto('/login');
  }

  onMount(() => {
    const user = getUser();
    if (!user) {
      goto('/login');
      return;
    }
    if (user.role !== 'ADMIN') {
      goto('/dashboard');
      return;
    }
    load();
  });
</script>

<div class="min-h-screen bg-slate-100">
  <header class="bg-white shadow-sm">
    <div class="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between">
      <h1 class="text-xl font-bold text-blue-700">My Bank · Admin</h1>
      <div class="flex items-center gap-4">
        <a href="/dashboard" class="text-sm text-blue-600 hover:underline">Dashboard</a>
        <button
          onclick={logout}
          class="px-3 py-1.5 rounded-lg bg-gray-800 text-white text-sm hover:bg-gray-900"
        >
          Log out
        </button>
      </div>
    </div>
  </header>

  <main class="max-w-4xl mx-auto px-6 py-8 space-y-4">
    <h2 class="text-lg font-semibold text-gray-800">Pending loan applications</h2>

    {#if error}
      <p class="rounded bg-red-100 text-red-700 px-3 py-2 text-sm">{error}</p>
    {/if}
    {#if message}
      <p class="rounded bg-green-100 text-green-700 px-3 py-2 text-sm">{message}</p>
    {/if}

    {#if loading}
      <p class="text-gray-500">Loading...</p>
    {:else if loans.length === 0}
      <p class="text-gray-500">No pending applications.</p>
    {:else}
      <div class="space-y-4">
        {#each loans as loan (loan.id)}
          <div class="bg-white rounded-xl shadow-md p-5 flex flex-wrap items-center justify-between gap-4">
            <div>
              <p class="text-sm text-gray-500">Loan #{loan.id} · {loan.applicantEmail}</p>
              <p class="text-2xl font-bold text-blue-700">{money(loan.principal)}</p>
              <p class="text-sm text-gray-600">
                {loan.annualInterestRate}% for {loan.tenureMonths} months · EMI {money(loan.emi)}
              </p>
              <p class="text-xs text-gray-500 font-mono">Account {loan.accountNumber}</p>
              <p class="text-xs text-gray-500">Applied {formatDate(loan.createdAt)}</p>
            </div>
            <div class="flex gap-2">
              <button
                onclick={() => decide(loan.id, 'approve')}
                disabled={busyId === loan.id}
                class="px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700 disabled:opacity-50"
              >
                Approve
              </button>
              <button
                onclick={() => decide(loan.id, 'reject')}
                disabled={busyId === loan.id}
                class="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700 disabled:opacity-50"
              >
                Reject
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </main>
</div>