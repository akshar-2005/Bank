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
  let loans = $state([]);
  let loading = $state(true);
  let error = $state('');
  let message = $state('');
  /** @type {number | null} */
  let busyId = $state(null);

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
    user = getUser();
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

<Sidebar {user} active="admin" onlogout={logout}>
  <div class="space-y-8">
    <!-- Header Line -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200/80">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <span class="w-2.5 h-2.5 rounded-full bg-purple-600 animate-pulse"></span>
          <span class="text-xs uppercase font-bold tracking-widest text-purple-700">Administrative Portal</span>
        </div>
        <h1 class="text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900">
          Pending Loan Applications
        </h1>
        <p class="mt-1 text-sm text-slate-500">
          Review borrower verification, calculate risk, and execute loan approval or rejection decisions.
        </p>
      </div>

      {#if !loading && loans.length > 0}
        <span class="text-xs font-semibold px-3 py-1.5 rounded-full bg-amber-50 text-amber-800 border border-amber-200 self-start sm:self-auto">
          {loans.length} Pending Decision{loans.length === 1 ? '' : 's'}
        </span>
      {/if}
    </div>

    {#if error}
      <div class="mb-4">
        <Alert type="error" message={error} />
      </div>
    {/if}
    {#if message}
      <div class="mb-4">
        <Alert type="success" message={message} />
      </div>
    {/if}

    {#if loading}
      <div class="space-y-4">
        <div class="h-44 rounded-2xl bg-slate-200/80 animate-pulse"></div>
        <div class="h-44 rounded-2xl bg-slate-200/80 animate-pulse"></div>
      </div>
    {:else if loans.length === 0}
      <EmptyState
        icon="admin"
        title="No pending applications"
        description="All loan applications submitted by customers have been reviewed and processed."
      />
    {:else}
      <div class="space-y-4">
        {#each loans as loan (loan.id)}
          <div class="bg-white rounded-2xl shadow-sm border border-slate-200/80 p-6 sm:p-7 flex flex-col lg:flex-row lg:items-center justify-between gap-6 hover:shadow-md transition-shadow">
            <div class="space-y-3 min-w-0 flex-1">
              <!-- Header Meta: Loan ID, Email, Status -->
              <div class="flex flex-wrap items-center gap-2.5">
                <span class="font-mono text-xs font-bold px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 border border-slate-200">
                  #{loan.id}
                </span>
                <span class="text-xs font-semibold text-slate-700 truncate" title={loan.applicantEmail}>
                  {loan.applicantEmail}
                </span>
                <StatusBadge status="PENDING" />
              </div>

              <!-- Principal Amount -->
              <div>
                <span class="text-[10px] uppercase font-bold tracking-widest text-slate-400 block">Requested Amount</span>
                <p class="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight mt-0.5">
                  {money(loan.principal)}
                </p>
              </div>

              <!-- Term Details & EMI -->
              <div class="p-3 rounded-xl bg-slate-50 border border-slate-200/70 grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
                <div>
                  <span class="text-slate-400 block">Interest Rate & Tenure</span>
                  <span class="font-semibold text-slate-800">{loan.annualInterestRate}% APR · {loan.tenureMonths} Mo</span>
                </div>
                <div>
                  <span class="text-slate-400 block">Monthly Installment</span>
                  <span class="font-bold text-teal-800">{money(loan.emi)} / mo</span>
                </div>
                <div>
                  <span class="text-slate-400 block">Disbursement Account</span>
                  <span class="font-mono font-semibold text-slate-800">{loan.accountNumber}</span>
                </div>
              </div>

              <p class="text-[11px] text-slate-400">
                Submitted on {formatDate(loan.createdAt)}
              </p>
            </div>

            <!-- Action Buttons -->
            <div class="flex sm:flex-row lg:flex-col gap-2.5 shrink-0 pt-4 lg:pt-0 border-t lg:border-t-0 border-slate-100">
              <button
                onclick={() => decide(loan.id, 'approve')}
                disabled={busyId === loan.id}
                class="flex-1 lg:w-36 inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs shadow-sm hover:shadow transition-all disabled:opacity-50 cursor-pointer"
              >
                {#if busyId === loan.id}
                  <Spinner size="xs" class="text-white" />
                  <span>Processing...</span>
                {:else}
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                  <span>Approve</span>
                {/if}
              </button>

              <button
                onclick={() => decide(loan.id, 'reject')}
                disabled={busyId === loan.id}
                class="flex-1 lg:w-36 inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-semibold text-xs shadow-sm hover:shadow transition-all disabled:opacity-50 cursor-pointer"
              >
                {#if busyId === loan.id}
                  <Spinner size="xs" class="text-white" />
                  <span>Processing...</span>
                {:else}
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                  <span>Reject</span>
                {/if}
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</Sidebar>
